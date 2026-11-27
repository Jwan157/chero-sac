package com.cherosac.dao;

import com.cherosac.modelo.DetallePedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla detalle_pedidos (productos y cantidades de cada pedido). */
public class DetallePedidoDAO {

    private static final String SELECT_BASE =
            "SELECT d.id_detalle, d.id_pedido, d.id_producto, d.cantidad, d.precio_unitario, "
          + "pr.nombre_producto, pr.imagen_url "
          + "FROM detalle_pedidos d JOIN productos pr ON pr.id_producto = d.id_producto ";

    private final Connection con;

    public DetallePedidoDAO(Connection con) {
        this.con = con;
    }

    public void insertar(DetallePedido d) throws SQLException {
        String sql = "INSERT INTO detalle_pedidos (id_pedido, id_producto, cantidad, precio_unitario, costo_unitario) "
                + "SELECT ?, ?, ?, ?, costo_unitario FROM productos WHERE id_producto = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, d.getIdPedido());
            ps.setInt(2, d.getIdProducto());
            ps.setInt(3, d.getCantidad());
            ps.setBigDecimal(4, d.getPrecioUnitario());
            ps.setInt(5, d.getIdProducto());
            ps.executeUpdate();
        }
    }

    public List<DetallePedido> listarPorPedido(int idPedido) throws SQLException {
        String sql = SELECT_BASE + "WHERE d.id_pedido = ? ORDER BY d.id_detalle";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            return ejecutarListado(ps);
        }
    }

    /** Linea de un producto dentro de un pedido (la combinacion es unica), o null. */
    public DetallePedido buscarPorPedidoYProducto(int idPedido, int idProducto) throws SQLException {
        String sql = SELECT_BASE + "WHERE d.id_pedido = ? AND d.id_producto = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ps.setInt(2, idProducto);
            List<DetallePedido> lista = ejecutarListado(ps);
            return lista.isEmpty() ? null : lista.get(0);
        }
    }

    private List<DetallePedido> ejecutarListado(PreparedStatement ps) throws SQLException {
        List<DetallePedido> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DetallePedido d = new DetallePedido();
                d.setIdDetalle(rs.getInt("id_detalle"));
                d.setIdPedido(rs.getInt("id_pedido"));
                d.setIdProducto(rs.getInt("id_producto"));
                d.setCantidad(rs.getInt("cantidad"));
                d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                d.setNombreProducto(rs.getString("nombre_producto"));
                d.setImagenUrl(rs.getString("imagen_url"));
                lista.add(d);
            }
        }
        return lista;
    }
}
