package com.cherosac.dao;

import com.cherosac.modelo.SolicitudDevolucion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla solicitudes_devolucion (postventa). */
public class SolicitudDevolucionDAO {

    private static final String SELECT_BASE =
            "SELECT s.id_solicitud, s.id_pedido, s.id_producto, s.cantidad_devuelta, s.tipo_solicitud, "
          + "s.motivo, s.estado, s.condicion_producto, s.id_usuario, s.respuesta_admin, "
          + "s.fecha_solicitud, s.fecha_resolucion, pr.nombre_producto, pe.codigo_seguimiento AS codigo_pedido "
          + "FROM solicitudes_devolucion s "
          + "JOIN productos pr ON pr.id_producto = s.id_producto "
          + "JOIN pedidos pe ON pe.id_pedido = s.id_pedido ";

    private final Connection con;

    public SolicitudDevolucionDAO(Connection con) {
        this.con = con;
    }

    public int insertar(SolicitudDevolucion s) throws SQLException {
        String sql = "INSERT INTO solicitudes_devolucion (id_pedido, id_producto, cantidad_devuelta, "
                + "tipo_solicitud, motivo, estado) VALUES (?, ?, ?, ?, ?, 'PENDIENTE') RETURNING id_solicitud";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, s.getIdPedido());
            ps.setInt(2, s.getIdProducto());
            ps.setInt(3, s.getCantidadDevuelta());
            ps.setString(4, s.getTipoSolicitud());
            ps.setString(5, s.getMotivo());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public SolicitudDevolucion buscarPorId(int idSolicitud) throws SQLException {
        String sql = SELECT_BASE + "WHERE s.id_solicitud = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            List<SolicitudDevolucion> lista = ejecutarListado(ps);
            return lista.isEmpty() ? null : lista.get(0);
        }
    }

    public List<SolicitudDevolucion> listarPorPedido(int idPedido) throws SQLException {
        String sql = SELECT_BASE + "WHERE s.id_pedido = ? ORDER BY s.fecha_solicitud DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            return ejecutarListado(ps);
        }
    }

    /** Solicitudes para el administrador; estado opcional (null = todas). */
    public List<SolicitudDevolucion> listarFiltrado(String estado) throws SQLException {
        String sql = SELECT_BASE
                + "WHERE (? IS NULL OR s.estado = ?) "
                + "ORDER BY CASE s.estado WHEN 'PENDIENTE' THEN 0 ELSE 1 END, s.fecha_solicitud DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String filtro = (estado == null || estado.isBlank()) ? null : estado;
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            return ejecutarListado(ps);
        }
    }

    /** Cantidad ya devuelta y aprobada de un producto en un pedido. */
    public int cantidadAprobada(int idPedido, int idProducto) throws SQLException {
        String sql = "SELECT COALESCE(SUM(cantidad_devuelta), 0) FROM solicitudes_devolucion "
                + "WHERE id_pedido = ? AND id_producto = ? AND estado = 'APROBADO'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ps.setInt(2, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * Resuelve una solicitud pendiente. Devuelve false si ya no estaba pendiente,
     * lo que evita resolverla dos veces.
     */
    public boolean resolver(int idSolicitud, String estado, String condicion, String respuesta, int idUsuario)
            throws SQLException {
        String sql = "UPDATE solicitudes_devolucion SET estado = ?, condicion_producto = ?, "
                + "respuesta_admin = ?, id_usuario = ?, fecha_resolucion = CURRENT_TIMESTAMP "
                + "WHERE id_solicitud = ? AND estado = 'PENDIENTE'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setString(2, condicion);
            ps.setString(3, respuesta);
            ps.setInt(4, idUsuario);
            ps.setInt(5, idSolicitud);
            return ps.executeUpdate() == 1;
        }
    }

    public int contarUltimos7Dias() throws SQLException {
        String sql = "SELECT COUNT(*) FROM solicitudes_devolucion "
                + "WHERE fecha_solicitud >= CURRENT_TIMESTAMP - INTERVAL '7 days'";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private List<SolicitudDevolucion> ejecutarListado(PreparedStatement ps) throws SQLException {
        List<SolicitudDevolucion> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SolicitudDevolucion s = new SolicitudDevolucion();
                s.setIdSolicitud(rs.getInt("id_solicitud"));
                s.setIdPedido(rs.getInt("id_pedido"));
                s.setIdProducto(rs.getInt("id_producto"));
                s.setCantidadDevuelta(rs.getInt("cantidad_devuelta"));
                s.setTipoSolicitud(rs.getString("tipo_solicitud"));
                s.setMotivo(rs.getString("motivo"));
                s.setEstado(rs.getString("estado"));
                s.setCondicionProducto(rs.getString("condicion_producto"));
                s.setIdUsuario(rs.getObject("id_usuario", Integer.class));
                s.setRespuestaAdmin(rs.getString("respuesta_admin"));
                s.setFechaSolicitud(rs.getObject("fecha_solicitud", java.time.LocalDateTime.class));
                s.setFechaResolucion(rs.getObject("fecha_resolucion", java.time.LocalDateTime.class));
                s.setNombreProducto(rs.getString("nombre_producto"));
                s.setCodigoPedido(rs.getString("codigo_pedido"));
                lista.add(s);
            }
        }
        return lista;
    }

    public List<SolicitudDevolucion> listarPorCliente(int idCliente) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT_BASE +
                "WHERE pe.id_cliente=? ORDER BY s.fecha_solicitud DESC")) {
            ps.setInt(1,idCliente);
            return ejecutarListado(ps);
        }
    }

}
