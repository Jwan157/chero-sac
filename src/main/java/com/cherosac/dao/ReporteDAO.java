package com.cherosac.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Consultas agregadas para los reportes del panel. Las ventas excluyen los pedidos cancelados.
 * Todas las fechas se reciben como rango [desde, hasta).
 */
public class ReporteDAO {

    private final Connection con;

    public ReporteDAO(Connection con) {
        this.con = con;
    }

    /** Totales del periodo: pedidos, cancelados, ventas, subtotal, IGV, clientes y ticket promedio. */
    public Map<String, Object> resumenVentas(LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = "SELECT COUNT(*) FILTER (WHERE estado <> 'CANCELADO') AS pedidos, "
                + "COUNT(*) FILTER (WHERE estado = 'CANCELADO') AS cancelados, "
                + "COALESCE(SUM(total) FILTER (WHERE estado <> 'CANCELADO'), 0) AS ventas, "
                + "COALESCE(SUM(subtotal) FILTER (WHERE estado <> 'CANCELADO'), 0) AS subtotal, "
                + "COALESCE(SUM(igv) FILTER (WHERE estado <> 'CANCELADO'), 0) AS igv, "
                + "COUNT(DISTINCT id_cliente) FILTER (WHERE estado <> 'CANCELADO') AS clientes "
                + "FROM pedidos WHERE fecha_pedido >= ? AND fecha_pedido < ?";
        Map<String, Object> resumen = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int pedidos = rs.getInt("pedidos");
                BigDecimal ventas = rs.getBigDecimal("ventas");
                resumen.put("pedidos", pedidos);
                resumen.put("cancelados", rs.getInt("cancelados"));
                resumen.put("ventas", ventas);
                resumen.put("subtotal", rs.getBigDecimal("subtotal"));
                resumen.put("igv", rs.getBigDecimal("igv"));
                resumen.put("clientes", rs.getInt("clientes"));
                BigDecimal ticket = pedidos > 0
                        ? ventas.divide(BigDecimal.valueOf(pedidos), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                resumen.put("ticket", ticket);
            }
        }
        return resumen;
    }

    /** Ventas agrupadas por día (unidad "day") o por mes (unidad "month"). */
    public List<Map<String, Object>> serie(LocalDateTime desde, LocalDateTime hasta, String unidad)
            throws SQLException {
        String sql = "SELECT date_trunc(CAST(? AS text), fecha_pedido) AS periodo, "
                + "COUNT(*) AS pedidos, SUM(total) AS ventas "
                + "FROM pedidos WHERE fecha_pedido >= ? AND fecha_pedido < ? AND estado <> 'CANCELADO' "
                + "GROUP BY 1 ORDER BY 1";
        List<Map<String, Object>> filas = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, unidad);
            ps.setTimestamp(2, Timestamp.valueOf(desde));
            ps.setTimestamp(3, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("fecha", rs.getTimestamp("periodo").toLocalDateTime());
                    fila.put("pedidos", rs.getInt("pedidos"));
                    fila.put("ventas", rs.getBigDecimal("ventas"));
                    filas.add(fila);
                }
            }
        }
        return filas;
    }

    /** Productos ordenados por importe vendido en el periodo. */
    public List<Map<String, Object>> topProductos(LocalDateTime desde, LocalDateTime hasta, int limite)
            throws SQLException {
        String sql = "SELECT pr.nombre_producto AS producto, c.nombre_categoria AS categoria, "
                + "SUM(d.cantidad) AS unidades, SUM(d.cantidad * d.precio_unitario) AS importe "
                + "FROM detalle_pedidos d "
                + "JOIN pedidos p ON p.id_pedido = d.id_pedido "
                + "JOIN productos pr ON pr.id_producto = d.id_producto "
                + "JOIN categorias c ON c.id_categoria = pr.id_categoria "
                + "WHERE p.fecha_pedido >= ? AND p.fecha_pedido < ? AND p.estado <> 'CANCELADO' "
                + "GROUP BY pr.id_producto, pr.nombre_producto, c.nombre_categoria "
                + "ORDER BY importe DESC, unidades DESC LIMIT ?";
        return ventasDetalle(sql, desde, hasta, limite);
    }

    /** Unidades e importe vendidos por categoría en el periodo. */
    public List<Map<String, Object>> ventasPorCategoria(LocalDateTime desde, LocalDateTime hasta)
            throws SQLException {
        String sql = "SELECT c.nombre_categoria AS categoria, "
                + "SUM(d.cantidad) AS unidades, SUM(d.cantidad * d.precio_unitario) AS importe "
                + "FROM detalle_pedidos d "
                + "JOIN pedidos p ON p.id_pedido = d.id_pedido "
                + "JOIN productos pr ON pr.id_producto = d.id_producto "
                + "JOIN categorias c ON c.id_categoria = pr.id_categoria "
                + "WHERE p.fecha_pedido >= ? AND p.fecha_pedido < ? AND p.estado <> 'CANCELADO' "
                + "GROUP BY c.id_categoria, c.nombre_categoria "
                + "ORDER BY importe DESC";
        return ventasDetalle(sql, desde, hasta, null);
    }

    /** Pedidos e importe por método de pago en el periodo. */
    public List<Map<String, Object>> ventasPorMetodoPago(LocalDateTime desde, LocalDateTime hasta)
            throws SQLException {
        String sql = "SELECT m.tipo_pago AS metodo, COUNT(*) AS pedidos, SUM(p.total) AS importe "
                + "FROM pedidos p JOIN metodos_pago m ON m.id_metodo_pago = p.id_metodo_pago "
                + "WHERE p.fecha_pedido >= ? AND p.fecha_pedido < ? AND p.estado <> 'CANCELADO' "
                + "GROUP BY m.id_metodo_pago, m.tipo_pago ORDER BY importe DESC";
        List<Map<String, Object>> filas = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("metodo", rs.getString("metodo"));
                    fila.put("pedidos", rs.getInt("pedidos"));
                    fila.put("importe", rs.getBigDecimal("importe"));
                    filas.add(fila);
                }
            }
        }
        return filas;
    }

    /** Cantidad de pedidos por estado (incluye cancelados) en el periodo. */
    public Map<String, Integer> pedidosPorEstado(LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = "SELECT estado, COUNT(*) AS total FROM pedidos "
                + "WHERE fecha_pedido >= ? AND fecha_pedido < ? GROUP BY estado";
        Map<String, Integer> conteo = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    conteo.put(rs.getString("estado"), rs.getInt("total"));
                }
            }
        }
        return conteo;
    }

    /** Solicitudes postventa registradas en el periodo, por tipo y resultado. */
    public Map<String, Object> resumenPostventa(LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = "SELECT COUNT(*) AS total, "
                + "COUNT(*) FILTER (WHERE tipo_solicitud = 'DEVOLUCION') AS devoluciones, "
                + "COUNT(*) FILTER (WHERE estado = 'APROBADO') AS aprobadas, "
                + "COUNT(*) FILTER (WHERE estado = 'RECHAZADO') AS rechazadas, "
                + "COUNT(*) FILTER (WHERE estado = 'PENDIENTE') AS pendientes "
                + "FROM solicitudes_devolucion WHERE fecha_solicitud >= ? AND fecha_solicitud < ?";
        Map<String, Object> resumen = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                resumen.put("total", rs.getInt("total"));
                resumen.put("devoluciones", rs.getInt("devoluciones"));
                resumen.put("aprobadas", rs.getInt("aprobadas"));
                resumen.put("rechazadas", rs.getInt("rechazadas"));
                resumen.put("pendientes", rs.getInt("pendientes"));
            }
        }
        return resumen;
    }

    /** Productos activos con stock disponible igual o menor al umbral (no depende del periodo). */
    public List<Map<String, Object>> stockBajo(int umbral) throws SQLException {
        String sql = "SELECT p.nombre_producto AS producto, c.nombre_categoria AS categoria, "
                + "p.stock_disponible AS stock "
                + "FROM productos p JOIN categorias c ON c.id_categoria = p.id_categoria "
                + "WHERE p.activo = TRUE AND p.stock_disponible <= ? "
                + "ORDER BY p.stock_disponible, p.nombre_producto";
        List<Map<String, Object>> filas = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, umbral);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("producto", rs.getString("producto"));
                    fila.put("categoria", rs.getString("categoria"));
                    fila.put("stock", rs.getInt("stock"));
                    filas.add(fila);
                }
            }
        }
        return filas;
    }

    /** Ejecuta una consulta de detalle con rango de fechas y, opcionalmente, un límite de filas. */
    private List<Map<String, Object>> ventasDetalle(String sql, LocalDateTime desde, LocalDateTime hasta,
                                                    Integer limite) throws SQLException {
        List<Map<String, Object>> filas = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            if (limite != null) {
                ps.setInt(3, limite);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    for (String columna : columnasDe(rs)) {
                        fila.put(columna, rs.getObject(columna));
                    }
                    filas.add(fila);
                }
            }
        }
        return filas;
    }

    private static List<String> columnasDe(ResultSet rs) throws SQLException {
        int total = rs.getMetaData().getColumnCount();
        List<String> nombres = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            nombres.add(rs.getMetaData().getColumnLabel(i));
        }
        return nombres;
    }

    /** Agrupa los créditos por fecha de emisión y los costos históricos por fecha del movimiento. */
    public void agregarResultadosNetos(Map<String,Object> resumen, LocalDateTime desde, LocalDateTime hasta)
            throws SQLException {
        String sql = """
            WITH periodo AS (SELECT CAST(? AS timestamp) inicio, CAST(? AS timestamp) fin),
            creditos AS (
                SELECT COALESCE(SUM(c.total),0) importe, COALESCE(SUM(c.igv_18),0) igv,
                       COALESCE(SUM(c.subtotal),0) base
                FROM comprobantes_electronicos c JOIN pedidos p USING(id_pedido), periodo
                WHERE c.tipo_comprobante='NOTA_CREDITO' AND p.estado<>'CANCELADO'
                  AND c.estado_sunat<>'ANULADO' AND c.fecha_emision>=inicio AND c.fecha_emision<fin
            ), unidades AS (
                SELECT d.cantidad unidades, d.costo_unitario costo
                FROM detalle_pedidos d JOIN pedidos p USING(id_pedido), periodo
                WHERE p.estado<>'CANCELADO' AND p.fecha_pedido>=inicio AND p.fecha_pedido<fin
                UNION ALL
                SELECT -s.cantidad_devuelta, d.costo_unitario
                FROM solicitudes_devolucion s JOIN detalle_pedidos d USING(id_pedido,id_producto)
                JOIN pedidos p ON p.id_pedido=s.id_pedido, periodo
                WHERE s.estado='APROBADO' AND p.estado<>'CANCELADO'
                  AND s.fecha_resolucion>=inicio AND s.fecha_resolucion<fin
            )
            SELECT creditos.*, (SELECT COALESCE(SUM(unidades*costo),0) FROM unidades) costo,
                   EXISTS(SELECT 1 FROM unidades WHERE costo IS NULL) incompleto FROM creditos
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1,Timestamp.valueOf(desde));
            ps.setTimestamp(2,Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                BigDecimal devoluciones = rs.getBigDecimal("importe");
                resumen.put("creditos",devoluciones);
                resumen.put("ventasNetas",((BigDecimal)resumen.get("ventas")).subtract(devoluciones));
                resumen.put("igvNeto",((BigDecimal)resumen.get("igv")).subtract(rs.getBigDecimal("igv")));
                BigDecimal baseNeta = ((BigDecimal)resumen.get("subtotal")).subtract(rs.getBigDecimal("base"));
                resumen.put("ganancia",rs.getBoolean("incompleto") ? null : baseNeta.subtract(rs.getBigDecimal("costo")));
            }
        }
    }

}
