package com.cherosac.controlador;

import com.cherosac.config.ConexionBD;
import com.cherosac.dao.ReporteDAO;
import com.cherosac.modelo.Pedido;
import com.cherosac.modelo.Periodo;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reportes del panel: resumen por periodo (semanal, quincenal, mensual o anual), comparativo con
 * el periodo anterior, rankings, alertas de stock bajo y exportación en CSV.
 */
@WebServlet("/admin/reportes")
public class ReportesAdminServlet extends BaseServlet {

    private static final ZoneId ZONA = ZoneId.of("America/Lima");
    private static final int LIMITE_TOP = 10;
    private static final int UMBRAL_STOCK_BAJO = 5;
    private static final Map<String, String> NOMBRE_TIPO = Map.of(
            "SEMANAL", "Semanal", "QUINCENAL", "Quincenal", "MENSUAL", "Mensual", "ANUAL", "Anual");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tipo = tipoDe(req.getParameter("tipo"));
        LocalDate referencia = fechaDe(req.getParameter("fecha"));
        boolean exportar = "csv".equalsIgnoreCase(texto(req.getParameter("formato")));

        Datos datos = new Datos(Periodo.de(tipo, referencia));
        try (Connection con = ConexionBD.obtener()) {
            cargar(new ReporteDAO(con), datos);
        } catch (SQLException e) {
            log("Error al generar el reporte " + tipo, e);
            if (exportar) {
                redirigir(req, resp, "/admin/reportes", null, "No se pudo generar el reporte.");
                return;
            }
            req.setAttribute("error", "No se pudo generar el reporte. Verifique la conexión a PostgreSQL.");
        }

        if (exportar) {
            exportarCsv(resp, datos);
            return;
        }
        publicar(req, datos, tipo, referencia);
        req.getRequestDispatcher("/reportes.jsp").forward(req, resp);
    }

    /** Consulta todos los datos del periodo y del periodo anterior. */
    private static void cargar(ReporteDAO dao, Datos d) throws SQLException {
        Periodo periodo = d.periodo;
        LocalDateTime desde = periodo.getDesdeTs();
        LocalDateTime hasta = periodo.getHastaTs();
        Periodo anterior = periodo.anterior();

        d.actual = dao.resumenVentas(desde, hasta);
        dao.agregarResultadosNetos(d.actual,desde,hasta);
        d.previo = dao.resumenVentas(anterior.getDesdeTs(), anterior.getHastaTs());
        d.actual.put("cambioVentas", cambio((BigDecimal) d.actual.get("ventas"),
                (BigDecimal) d.previo.get("ventas")));
        d.actual.put("cambioPedidos", cambio(
                BigDecimal.valueOf(((Integer) d.actual.get("pedidos")).longValue()),
                BigDecimal.valueOf(((Integer) d.previo.get("pedidos")).longValue())));

        String unidad = periodo.unidadSerie();
        d.serie = dao.serie(desde, hasta, unidad);
        BigDecimal maxVentas = maximo(d.serie, "ventas");
        for (Map<String, Object> fila : d.serie) {
            fila.put("etiqueta", Periodo.etiquetaFila(unidad, (LocalDateTime) fila.get("fecha")));
            fila.put("porcentaje", porcentaje((BigDecimal) fila.get("ventas"), maxVentas));
        }

        d.top = dao.topProductos(desde, hasta, LIMITE_TOP);
        conPorcentaje(d.top, "importe");
        d.categorias = dao.ventasPorCategoria(desde, hasta);
        conPorcentaje(d.categorias, "importe");
        d.metodos = dao.ventasPorMetodoPago(desde, hasta);
        conPorcentaje(d.metodos, "importe");

        d.totalUnidades = 0;
        for (Map<String, Object> fila : d.categorias) {
            d.totalUnidades += ((Number) fila.get("unidades")).longValue();
        }

        d.conteoEstados = new LinkedHashMap<>();
        for (String estado : Pedido.ESTADOS) {
            d.conteoEstados.put(estado, 0);
        }
        d.conteoEstados.putAll(dao.pedidosPorEstado(desde, hasta));

        d.postventa = dao.resumenPostventa(desde, hasta);
        d.stockBajo = dao.stockBajo(UMBRAL_STOCK_BAJO);
    }

    /** Pasa los datos del reporte a la página JSP. */
    private static void publicar(HttpServletRequest req, Datos d, String tipo, LocalDate referencia) {
        req.setAttribute("periodo", d.periodo);
        req.setAttribute("anteriorPeriodo", d.periodo.anterior());
        req.setAttribute("siguientePeriodo", d.periodo.siguiente());
        req.setAttribute("actual", d.actual);
        req.setAttribute("previo", d.previo);
        req.setAttribute("serie", d.serie);
        req.setAttribute("top", d.top);
        req.setAttribute("categorias", d.categorias);
        req.setAttribute("metodos", d.metodos);
        req.setAttribute("conteoEstados", d.conteoEstados);
        req.setAttribute("postventa", d.postventa);
        req.setAttribute("stockBajo", d.stockBajo);
        req.setAttribute("totalUnidades", d.totalUnidades);
        req.setAttribute("umbralStock", UMBRAL_STOCK_BAJO);
        req.setAttribute("tipo", tipo);
        req.setAttribute("tipos", Periodo.TIPOS);
        req.setAttribute("nombresTipo", NOMBRE_TIPO);
        req.setAttribute("fecha", referencia.toString());
    }

    /** Descarga el reporte del periodo como CSV (con BOM para que Excel reconozca los acentos). */
    private static void exportarCsv(HttpServletResponse resp, Datos d) throws IOException {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Reporte,").append(celda(d.periodo.getEtiqueta())).append('\n');
        csv.append('\n').append("Indicador,Valor\n");
        filaCsv(csv, "Ventas netas (S/)", d.actual.get("ventasNetas"));
        filaCsv(csv, "IGV neto (S/)", d.actual.get("igvNeto"));
        filaCsv(csv, "Notas de crédito (S/)", d.actual.get("creditos"));
        filaCsv(csv, "Ganancia bruta (S/)", d.actual.get("ganancia") == null ? "Costo pendiente" : d.actual.get("ganancia"));
        filaCsv(csv, "Ventas brutas (S/)", d.actual.get("ventas"));
        filaCsv(csv, "Pedidos", d.actual.get("pedidos"));
        filaCsv(csv, "Pedidos cancelados", d.actual.get("cancelados"));
        filaCsv(csv, "Subtotal (S/)", d.actual.get("subtotal"));
        filaCsv(csv, "IGV (S/)", d.actual.get("igv"));
        filaCsv(csv, "Ticket promedio (S/)", d.actual.get("ticket"));
        filaCsv(csv, "Clientes distintos", d.actual.get("clientes"));
        filaCsv(csv, "Ventas periodo anterior (S/)", d.previo.get("ventas"));

        csv.append('\n').append("Periodo,Pedidos,Ventas (S/)\n");
        for (Map<String, Object> fila : d.serie) {
            csv.append(celda(fila.get("etiqueta"))).append(',')
                    .append(celda(fila.get("pedidos"))).append(',')
                    .append(celda(fila.get("ventas"))).append('\n');
        }

        csv.append('\n').append("Producto,Categoria,Unidades,Importe (S/)\n");
        for (Map<String, Object> fila : d.top) {
            csv.append(celda(fila.get("producto"))).append(',')
                    .append(celda(fila.get("categoria"))).append(',')
                    .append(celda(fila.get("unidades"))).append(',')
                    .append(celda(fila.get("importe"))).append('\n');
        }

        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"reporte-"
                + d.periodo.getTipo().toLowerCase(Locale.ROOT) + "-" + d.periodo.getDesde() + ".csv\"");
        resp.getWriter().write(csv.toString());
    }

    private static void filaCsv(StringBuilder csv, String indicador, Object valor) {
        csv.append(celda(indicador)).append(',').append(celda(valor)).append('\n');
    }

    /** Escapa un valor para CSV: lo encierra entre comillas si contiene separadores o comillas. */
    private static String celda(Object valor) {
        String texto;
        if (valor == null) {
            texto = "";
        } else if (valor instanceof BigDecimal) {
            texto = ((BigDecimal) valor).toPlainString();
        } else {
            texto = String.valueOf(valor);
        }
        if (texto.contains(",") || texto.contains("\"") || texto.contains("\n") || texto.contains(";")) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }

    /** Variación porcentual de un valor frente al periodo anterior, lista para mostrarse. */
    private static Map<String, Object> cambio(BigDecimal actual, BigDecimal previo) {
        Map<String, Object> cambio = new LinkedHashMap<>();
        cambio.put("texto", null);
        cambio.put("sube", null);
        if (actual == null || previo == null || previo.signum() == 0) {
            return cambio;
        }
        BigDecimal porcentaje = actual.subtract(previo)
                .multiply(BigDecimal.valueOf(100))
                .divide(previo, 1, RoundingMode.HALF_UP);
        boolean sube = porcentaje.signum() >= 0;
        cambio.put("sube", sube);
        cambio.put("texto", (sube ? "▲ +" : "▼ ") + porcentaje.toPlainString() + "%");
        return cambio;
    }

    private static void conPorcentaje(List<Map<String, Object>> filas, String campo) {
        BigDecimal maximo = maximo(filas, campo);
        for (Map<String, Object> fila : filas) {
            fila.put("porcentaje", porcentaje((BigDecimal) fila.get(campo), maximo));
        }
    }

    private static BigDecimal maximo(List<Map<String, Object>> filas, String campo) {
        BigDecimal maximo = BigDecimal.ZERO;
        for (Map<String, Object> fila : filas) {
            BigDecimal valor = (BigDecimal) fila.get(campo);
            if (valor != null && valor.compareTo(maximo) > 0) {
                maximo = valor;
            }
        }
        return maximo;
    }

    /** Porcentaje entero de un valor respecto al máximo, para dibujar barras. */
    private static int porcentaje(BigDecimal valor, BigDecimal maximo) {
        if (valor == null || maximo == null || maximo.signum() <= 0) {
            return 0;
        }
        return valor.multiply(BigDecimal.valueOf(100)).divide(maximo, 0, RoundingMode.HALF_UP).intValue();
    }

    private static String tipoDe(String valor) {
        String limpio = texto(valor);
        String tipo = limpio == null ? "SEMANAL" : limpio.toUpperCase(Locale.ROOT);
        return Periodo.TIPOS.contains(tipo) ? tipo : "SEMANAL";
    }

    private static LocalDate fechaDe(String valor) {
        String limpio = texto(valor);
        if (limpio != null) {
            try {
                return LocalDate.parse(limpio);
            } catch (DateTimeParseException e) {
                // Fecha no válida: se usa la fecha actual de Lima.
            }
        }
        return LocalDate.now(ZONA);
    }

    /** Datos calculados para un periodo. */
    private static final class Datos {
        final Periodo periodo;
        Map<String, Object> actual;
        Map<String, Object> previo;
        List<Map<String, Object>> serie;
        List<Map<String, Object>> top;
        List<Map<String, Object>> categorias;
        List<Map<String, Object>> metodos;
        Map<String, Integer> conteoEstados;
        Map<String, Object> postventa;
        List<Map<String, Object>> stockBajo;
        long totalUnidades;

        Datos(Periodo periodo) {
            this.periodo = periodo;
        }
    }
}
