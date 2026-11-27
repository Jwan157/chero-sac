package com.cherosac.dao;

import com.cherosac.modelo.MovimientoInventario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla movimientos_inventario (Kardex). */
public class MovimientoInventarioDAO {

    private final Connection con;

    public MovimientoInventarioDAO(Connection con) {
        this.con = con;
    }

    public void insertar(MovimientoInventario m) throws SQLException {
        String sql = "INSERT INTO movimientos_inventario (id_producto, tipo_movimiento, cantidad, "
                + "referencia_codigo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, m.getIdProducto());
            ps.setString(2, m.getTipoMovimiento());
            ps.setInt(3, m.getCantidad());
            ps.setString(4, m.getReferenciaCodigo());
            ps.executeUpdate();
        }
    }

    /** Ultimos movimientos, de un producto (si se indica) o de todos. */
    public List<MovimientoInventario> listar(Integer idProducto, int limite) throws SQLException {
        String sql = "SELECT m.id_movimiento, m.id_producto, m.tipo_movimiento, m.cantidad, "
                + "m.referencia_codigo, m.fecha_movimiento, pr.nombre_producto "
                + "FROM movimientos_inventario m JOIN productos pr ON pr.id_producto = m.id_producto "
                + "WHERE (? IS NULL OR m.id_producto = ?) "
                + "ORDER BY m.fecha_movimiento DESC, m.id_movimiento DESC LIMIT ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (idProducto == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, idProducto);
                ps.setInt(2, idProducto);
            }
            ps.setInt(3, limite);
            List<MovimientoInventario> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MovimientoInventario m = new MovimientoInventario();
                    m.setIdMovimiento(rs.getInt("id_movimiento"));
                    m.setIdProducto(rs.getInt("id_producto"));
                    m.setTipoMovimiento(rs.getString("tipo_movimiento"));
                    m.setCantidad(rs.getInt("cantidad"));
                    m.setReferenciaCodigo(rs.getString("referencia_codigo"));
                    m.setFechaMovimiento(rs.getObject("fecha_movimiento", java.time.LocalDateTime.class));
                    m.setNombreProducto(rs.getString("nombre_producto"));
                    lista.add(m);
                }
            }
            return lista;
        }
    }
}
