package com.cherosac.controlador;

import com.cherosac.config.ConexionBD;
import com.cherosac.dao.ComprobanteDAO;
import com.cherosac.dao.DetallePedidoDAO;
import com.cherosac.dao.HistorialAccionDAO;
import com.cherosac.dao.MovimientoInventarioDAO;
import com.cherosac.dao.PedidoDAO;
import com.cherosac.dao.ProductoDAO;
import com.cherosac.dao.SolicitudDevolucionDAO;
import com.cherosac.modelo.ComprobanteElectronico;
import com.cherosac.modelo.DetallePedido;
import com.cherosac.modelo.MovimientoInventario;
import com.cherosac.modelo.Pedido;
import com.cherosac.modelo.SolicitudDevolucion;
import com.cherosac.modelo.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A-03: postventa. El administrador aprueba o rechaza cada solicitud. Al aprobar indica la
 * condicion del producto: BUEN_ESTADO suma al stock de venta; DEFECTUOSO suma al stock
 * defectuoso. En ambos casos se genera la nota de credito del pedido.
 */
@WebServlet("/admin/devoluciones")
public class DevolucionAdminServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String estado = texto(req.getParameter("estado"));
        try (Connection con = ConexionBD.obtener()) {
            req.setAttribute("solicitudes", new SolicitudDevolucionDAO(con).listarFiltrado(estado));
        } catch (SQLException e) {
            log("Error al cargar las solicitudes", e);
            req.setAttribute("error", "No se pudieron cargar las solicitudes de postventa.");
        }
        req.setAttribute("filtroEstado", estado);
        req.getRequestDispatcher("/postventa_devoluciones.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Usuario admin = usuarioAdmin(req);
        Integer idSolicitud = entero(req.getParameter("idSolicitud"));
        String decision = texto(req.getParameter("decision"));
        String condicion = texto(req.getParameter("condicion"));
        String respuesta = texto(req.getParameter("respuesta"));

        String ok = null;
        String error = null;
        if (idSolicitud == null || !("APROBAR".equals(decision) || "RECHAZAR".equals(decision))) {
            error = "Datos de la resolución no válidos.";
        } else if (respuesta != null && respuesta.length() > 500) {
            error = "La respuesta no puede superar 500 caracteres.";
        } else {
            try (Connection con = ConexionBD.obtener()) {
                con.setAutoCommit(false);
                try {
                    ok = resolver(con, admin, idSolicitud, decision, condicion, respuesta);
                    con.commit();
                } catch (IllegalArgumentException e) {
                    con.rollback();
                    error = e.getMessage();
                } catch (SQLException e) {
                    con.rollback();
                    log("Error al resolver la solicitud", e);
                    error = "No se pudo registrar la resolución.";
                }
            } catch (SQLException e) {
                log("Sin conexion a la base de datos", e);
                error = "No hay conexión con la base de datos.";
            }
        }
        redirigir(req, resp, "/admin/devoluciones", ok, error);
    }

    private String resolver(Connection con, Usuario admin, int idSolicitud, String decision,
                            String condicion, String respuesta) throws SQLException {
        SolicitudDevolucionDAO solicitudDAO = new SolicitudDevolucionDAO(con);
        SolicitudDevolucion solicitud = solicitudDAO.buscarPorId(idSolicitud);
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud no existe.");
        }
        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new IllegalArgumentException("La solicitud ya fue resuelta.");
        }
        Pedido pedido = new PedidoDAO(con).bloquear(solicitud.getIdPedido());
        if (pedido == null || "CANCELADO".equals(pedido.getEstado())) {
            throw new IllegalArgumentException("El pedido asociado no admite devoluciones.");
        }

        if ("RECHAZAR".equals(decision)) {
            if (!solicitudDAO.resolver(idSolicitud, "RECHAZADO", null, respuesta, admin.getIdUsuario())) {
                throw new IllegalArgumentException("La solicitud ya fue resuelta.");
            }
            new HistorialAccionDAO(con).registrar(admin.getIdUsuario(), "DEVOLUCION_RECHAZADA",
                    "Solicitud #" + idSolicitud + " del pedido " + pedido.getCodigoSeguimiento());
            return "Solicitud #" + idSolicitud + " rechazada.";
        }

        // Aprobacion
        if (!"BUEN_ESTADO".equals(condicion) && !"DEFECTUOSO".equals(condicion)) {
            throw new IllegalArgumentException("Indique si el producto está en buen estado o es defectuoso.");
        }
        DetallePedido detalle = new DetallePedidoDAO(con)
                .buscarPorPedidoYProducto(pedido.getIdPedido(), solicitud.getIdProducto());
        if (detalle == null) {
            throw new IllegalArgumentException("El producto ya no figura en el pedido.");
        }
        int yaDevuelto = solicitudDAO.cantidadAprobada(pedido.getIdPedido(), solicitud.getIdProducto());
        if (yaDevuelto + solicitud.getCantidadDevuelta() > detalle.getCantidad()) {
            throw new IllegalArgumentException("Las devoluciones superarían la cantidad comprada ("
                    + detalle.getCantidad() + ").");
        }
        if (!solicitudDAO.resolver(idSolicitud, "APROBADO", condicion, respuesta, admin.getIdUsuario())) {
            throw new IllegalArgumentException("La solicitud ya fue resuelta.");
        }

        // Ingreso al inventario segun la condicion del producto
        ProductoDAO productoDAO = new ProductoDAO(con);
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setIdProducto(solicitud.getIdProducto());
        movimiento.setCantidad(solicitud.getCantidadDevuelta());
        movimiento.setReferenciaCodigo(pedido.getCodigoSeguimiento());
        if ("BUEN_ESTADO".equals(condicion)) {
            productoDAO.sumarStockDisponible(solicitud.getIdProducto(), solicitud.getCantidadDevuelta());
            movimiento.setTipoMovimiento("INGRESO_DEVOLUCION_OK");
        } else {
            productoDAO.sumarStockDefectuoso(solicitud.getIdProducto(), solicitud.getCantidadDevuelta());
            movimiento.setTipoMovimiento("INGRESO_DEFECTUOSO");
        }
        new MovimientoInventarioDAO(con).insertar(movimiento);

        // Nota de credito automatica, serie BC01 (si el original es boleta) o FC01 (factura)
        ComprobanteDAO comprobanteDAO = new ComprobanteDAO(con);
        ComprobanteElectronico original = null;
        BigDecimal totalAcreditado = BigDecimal.ZERO;
        BigDecimal baseAcreditada = BigDecimal.ZERO;
        for (ComprobanteElectronico c : comprobanteDAO.listarPorPedido(pedido.getIdPedido())) {
            if ("NOTA_CREDITO".equals(c.getTipoComprobante())) {
                totalAcreditado = totalAcreditado.add(c.getTotal());
                baseAcreditada = baseAcreditada.add(c.getSubtotal());
            } else {
                original = c;
            }
        }
        if (original == null) throw new IllegalArgumentException("El pedido no tiene comprobante original.");
        BigDecimal monto = detalle.getPrecioUnitario()
                .multiply(BigDecimal.valueOf(solicitud.getCantidadDevuelta()))
                .setScale(2, RoundingMode.HALF_UP);
        // Repartir los centavos sobre el saldo mantiene el IGV conciliado al devolver por partes.
        BigDecimal saldo = original.getTotal().subtract(totalAcreditado).subtract(monto);
        BigDecimal subtotal = original.getSubtotal().subtract(baseAcreditada)
                .subtract(ComprobanteElectronico.subtotalDesde(saldo));
        String serie = ComprobanteElectronico.serieNotaCredito(original.getTipoComprobante());

        ComprobanteElectronico notaCredito = new ComprobanteElectronico();
        notaCredito.setIdPedido(pedido.getIdPedido());
        notaCredito.setTipoComprobante("NOTA_CREDITO");
        notaCredito.setSerie(serie);
        notaCredito.setNumeroCorrelativo(comprobanteDAO.siguienteCorrelativo(serie));
        notaCredito.setRucDniCliente(pedido.getClienteNumeroDocumento());
        notaCredito.setRazonSocialCliente(original.getRazonSocialCliente());
        notaCredito.setSubtotal(subtotal);
        notaCredito.setIgv18(monto.subtract(subtotal));
        notaCredito.setTotal(monto);
        notaCredito.setHashSunat(ComprobanteElectronico.hashSha256(serie + "-" + notaCredito.getNumeroCorrelativo()
                + "|" + pedido.getClienteNumeroDocumento() + "|" + monto.toPlainString()
                + "|" + pedido.getCodigoSeguimiento()));
        notaCredito.setEstadoSunat("ACEPTADO");
        comprobanteDAO.insertar(notaCredito);

        new HistorialAccionDAO(con).registrar(admin.getIdUsuario(), "DEVOLUCION_APROBADA",
                "Solicitud #" + idSolicitud + " (" + condicion + ") pedido " + pedido.getCodigoSeguimiento()
                        + ", nota de crédito " + notaCredito.getNumeroCompleto());
        return "Solicitud #" + idSolicitud + " aprobada. Nota de crédito " + notaCredito.getNumeroCompleto()
                + " generada.";
    }
}
