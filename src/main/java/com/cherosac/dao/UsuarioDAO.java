package com.cherosac.dao;

import com.cherosac.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Acceso a la tabla usuarios (administradores). */
public class UsuarioDAO {

    private final Connection con;

    public UsuarioDAO(Connection con) {
        this.con = con;
    }

    /** Devuelve el usuario activo con ese nombre de usuario, o null si no existe. */
    public Usuario buscarActivoPorNombreUsuario(String nombreUsuario) throws SQLException {
        String sql = "SELECT id_usuario, nombre_completo, nombre_usuario, correo, password_hash, "
                + "rol, activo, fecha_creacion, ultimo_acceso "
                + "FROM usuarios WHERE nombre_usuario = ? AND activo = TRUE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public void actualizarUltimoAcceso(int idUsuario) throws SQLException {
        String sql = "UPDATE usuarios SET ultimo_acceso = CURRENT_TIMESTAMP WHERE id_usuario = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setNombreUsuario(rs.getString("nombre_usuario"));
        u.setCorreo(rs.getString("correo"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRol(rs.getString("rol"));
        u.setActivo(rs.getBoolean("activo"));
        u.setFechaCreacion(rs.getObject("fecha_creacion", java.time.LocalDateTime.class));
        u.setUltimoAcceso(rs.getObject("ultimo_acceso", java.time.LocalDateTime.class));
        return u;
    }

    public java.util.List<Usuario> listar() throws SQLException {
        java.util.List<Usuario> lista = new java.util.ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM usuarios ORDER BY nombre_usuario");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario usuario = mapear(rs);
                usuario.setPasswordHash(null);
                lista.add(usuario);
            }
        }
        return lista;
    }

}
