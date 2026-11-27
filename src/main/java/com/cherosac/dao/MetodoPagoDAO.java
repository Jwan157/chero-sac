package com.cherosac.dao;

import com.cherosac.modelo.MetodoPago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla metodos_pago. */
public class MetodoPagoDAO {

    private final Connection con;

    public MetodoPagoDAO(Connection con) {
        this.con = con;
    }

    public List<MetodoPago> listarActivos() throws SQLException {
        String sql = "SELECT id_metodo_pago, tipo_pago, activo FROM metodos_pago "
                + "WHERE activo = TRUE ORDER BY id_metodo_pago";
        List<MetodoPago> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MetodoPago m = new MetodoPago();
                m.setIdMetodoPago(rs.getInt("id_metodo_pago"));
                m.setTipoPago(rs.getString("tipo_pago"));
                m.setActivo(rs.getBoolean("activo"));
                lista.add(m);
            }
        }
        return lista;
    }

    /** Devuelve el metodo de pago activo con ese id, o null si no existe. */
    public MetodoPago buscarActivoPorId(int idMetodoPago) throws SQLException {
        String sql = "SELECT id_metodo_pago, tipo_pago, activo FROM metodos_pago "
                + "WHERE id_metodo_pago = ? AND activo = TRUE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMetodoPago);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                MetodoPago m = new MetodoPago();
                m.setIdMetodoPago(rs.getInt("id_metodo_pago"));
                m.setTipoPago(rs.getString("tipo_pago"));
                m.setActivo(rs.getBoolean("activo"));
                return m;
            }
        }
    }
}
