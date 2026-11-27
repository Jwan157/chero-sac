package com.cherosac.dao;

import com.cherosac.modelo.Pedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Acceso a la tabla pedidos y a sus consultas de resumen. */
public class PedidoDAO {

    private static final String SELECT_BASE =
            "SELECT p.id_pedido, p.codigo_seguimiento, p.id_cliente, p.id_metodo_pago, p.id_usuario, "
          + "p.fecha_pedido, p.estado, p.subtotal, p.igv, p.total, p.observaciones, p.es_demo, "
          + "c.tiene_cuenta, c.tipo_documento AS cliente_tipo_documento, "
          + "c.numero_documento AS cliente_numero_documento, "
          + "c.nombre_razon_social || COALESCE(' ' || c.apellido, '') AS cliente_nombre, "
          + "m.tipo_pago AS metodo_pago "
          + "FROM pedidos p "
          + "JOIN clientes c ON c.id_cliente = p.id_cliente "
          + "JOIN metodos_pago m ON m.id_metodo_pago = p.id_metodo_pago ";

    private final Connection con;

    public PedidoDAO(Connection con) {
        this.con = con;
    }

    /** Siguiente valor de la secuencia usada para el codigo CH-YYYY-XXXXXX. */
    public long siguienteNumeroCodigo() throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT nextval('seq_codigo_pedido')");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    /** Inserta la cabecera del pedido y devuelve su id. */
    public int insertar(Pedido p) throws SQLException {
        String sql = "INSERT INTO pedidos (codigo_seguimiento, id_cliente, id_metodo_pago, id_usuario, "
                + "estado, subtotal, igv, total, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_pedido";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getCodigoSeguimiento());
            ps.setInt(2, p.getIdCliente());
            ps.setInt(3, p.getIdMetodoPago());
            if (p.getIdUsuario() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, p.getIdUsuario());
            }
            ps.setString(5, p.getEstado());
            ps.setBigDecimal(6, p.getSubtotal());
            ps.setBigDecimal(7, p.getIgv());
            ps.setBigDecimal(8, p.getTotal());
            ps.setString(9, p.getObservaciones());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Pedido buscarPorCodigo(String codigo) throws SQLException {
        return buscarUno("WHERE p.codigo_seguimiento = ?", codigo);
    }

    public Pedido buscarPorId(int idPedido) throws SQLException {
        return buscarUno("WHERE p.id_pedido = ?", idPedido);
    }

    /** Pedidos para el dashboard, filtrados por estado y por texto (codigo, documento o nombre). */
    public List<Pedido> listarFiltrado(String estado, String busqueda) throws SQLException {
        String like = (busqueda == null || busqueda.isBlank()) ? null : "%" + busqueda.trim() + "%";
        String sql = SELECT_BASE
                + "WHERE (? IS NULL OR p.estado = ?) "
                + "AND (? IS NULL OR p.codigo_seguimiento ILIKE ? OR c.numero_documento ILIKE ? "
                + "OR (c.nombre_razon_social || ' ' || COALESCE(c.apellido, '')) ILIKE ?) "
                + "ORDER BY p.fecha_pedido DESC LIMIT 200";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String estadoFiltro = (estado == null || estado.isBlank()) ? null : estado;
            ps.setString(1, estadoFiltro);
            ps.setString(2, estadoFiltro);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setString(6, like);
            return ejecutarListado(ps);
        }
    }

    /** Cantidad de pedidos nuevos que esperan atención del dueño o del equipo de ventas. */
    public int contarPendientes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pedidos WHERE estado = 'PENDIENTE'";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }


    /** Conteo general por estado para los indicadores del panel del propietario. */
    public Map<String, Integer> contarPorEstado() throws SQLException {
        Map<String, Integer> conteos = new LinkedHashMap<>();
        for (String estado : Pedido.ESTADOS) {
            conteos.put(estado, 0);
        }
        String sql = "SELECT estado, COUNT(*) AS cantidad FROM pedidos GROUP BY estado";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                conteos.put(rs.getString("estado"), rs.getInt("cantidad"));
            }
        }
        return conteos;
    }

    /** Cantidad total de pedidos de muestra (no representan ventas reales). */
    public int contarDemostraciones() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pedidos WHERE es_demo = TRUE";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Actualiza estado y observaciones, y registra al administrador que atendio el pedido. */
    public boolean actualizarEstado(int idPedido, String estado, String observaciones, int idUsuario)
            throws SQLException {
        String sql = "UPDATE pedidos SET estado = ?, observaciones = ?, id_usuario = ? WHERE id_pedido = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setString(2, observaciones);
            ps.setInt(3, idUsuario);
            ps.setInt(4, idPedido);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Resumen de los ultimos 7 dias (sin pedidos cancelados).
     * Claves: "ventas" (BigDecimal), "igv" (BigDecimal) y "pedidos" (Integer).
     */
    public Map<String, Object> resumenUltimos7Dias() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) AS ventas, COALESCE(SUM(igv), 0) AS igv, "
                + "COUNT(*) AS pedidos FROM pedidos "
                + "WHERE fecha_pedido >= CURRENT_TIMESTAMP - INTERVAL '7 days' AND estado <> 'CANCELADO'";
        Map<String, Object> resumen = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            resumen.put("ventas", rs.getBigDecimal("ventas"));
            resumen.put("igv", rs.getBigDecimal("igv"));
            resumen.put("pedidos", rs.getInt("pedidos"));
        }
        return resumen;
    }

    private Pedido buscarUno(String where, Object valor) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT_BASE + where)) {
            if (valor instanceof Integer) {
                ps.setInt(1, (Integer) valor);
            } else {
                ps.setString(1, String.valueOf(valor));
            }
            List<Pedido> lista = ejecutarListado(ps);
            return lista.isEmpty() ? null : lista.get(0);
        }
    }

    private List<Pedido> ejecutarListado(PreparedStatement ps) throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setIdPedido(rs.getInt("id_pedido"));
        p.setClienteConCuenta(rs.getBoolean("tiene_cuenta"));
        p.setCodigoSeguimiento(rs.getString("codigo_seguimiento"));
        p.setIdCliente(rs.getInt("id_cliente"));
        p.setIdMetodoPago(rs.getInt("id_metodo_pago"));
        p.setIdUsuario(rs.getObject("id_usuario", Integer.class));
        p.setFechaPedido(rs.getObject("fecha_pedido", java.time.LocalDateTime.class));
        p.setEstado(rs.getString("estado"));
        p.setSubtotal(rs.getBigDecimal("subtotal"));
        p.setIgv(rs.getBigDecimal("igv"));
        p.setTotal(rs.getBigDecimal("total"));
        p.setObservaciones(rs.getString("observaciones"));
        p.setDemostracion(rs.getBoolean("es_demo"));
        p.setClienteTipoDocumento(rs.getString("cliente_tipo_documento"));
        p.setClienteNumeroDocumento(rs.getString("cliente_numero_documento"));
        p.setClienteNombre(rs.getString("cliente_nombre"));
        p.setMetodoPago(rs.getString("metodo_pago"));
        return p;
    }

    public Pedido bloquear(int idPedido) throws SQLException {
        return buscarUno("WHERE p.id_pedido = ? FOR UPDATE OF p", idPedido);
    }

    public List<Pedido> listarPorCliente(int idCliente) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT_BASE +
                "WHERE p.id_cliente = ? ORDER BY p.fecha_pedido DESC, p.id_pedido DESC")) {
            ps.setInt(1, idCliente);
            return ejecutarListado(ps);
        }
    }

}
