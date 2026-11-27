package com.cherosac.dao;

import com.cherosac.modelo.HistorialAccion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla historial_acciones (auditoria administrativa). */
public class HistorialAccionDAO {

    private final Connection con;

    public HistorialAccionDAO(Connection con) {
        this.con = con;
    }

    public void registrar(int idUsuario, String accion, String detalle) throws SQLException {
        String sql = "INSERT INTO historial_acciones (id_usuario, accion, detalle) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, accion);
            ps.setString(3, detalle);
            ps.executeUpdate();
        }
    }

    public List<HistorialAccion> listarPagina(int limite, int antes) throws SQLException {
        String sql = "SELECT h.id_historial, h.id_usuario, h.accion, h.detalle, h.fecha_accion, "
                + "u.nombre_usuario FROM historial_acciones h "
                + "JOIN usuarios u ON u.id_usuario = h.id_usuario "
                + "WHERE h.id_historial < ? ORDER BY h.id_historial DESC LIMIT ?";
        List<HistorialAccion> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, antes);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialAccion h = new HistorialAccion();
                    h.setIdHistorial(rs.getInt("id_historial"));
                    h.setIdUsuario(rs.getInt("id_usuario"));
                    h.setAccion(rs.getString("accion"));
                    h.setDetalle(rs.getString("detalle"));
                    h.setFechaAccion(rs.getObject("fecha_accion", java.time.LocalDateTime.class));
                    h.setNombreUsuario(rs.getString("nombre_usuario"));
                    lista.add(h);
                }
            }
        }
        return lista;
    }
}
