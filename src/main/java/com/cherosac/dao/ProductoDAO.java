package com.cherosac.dao;

import com.cherosac.modelo.Producto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla productos, incluido el control de stock de venta y defectuoso. */
public class ProductoDAO {

    private static final String SELECT_BASE =
            "SELECT p.id_producto, p.id_categoria, p.nombre_producto, p.descripcion, p.imagen_url, "
          + "p.precio, p.costo_unitario, p.stock_disponible, p.stock_defectuoso, p.activo, p.fecha_registro, "
          + "p.fecha_actualizacion, c.nombre_categoria "
          + "FROM productos p JOIN categorias c ON c.id_categoria = p.id_categoria ";

    private final Connection con;

    public ProductoDAO(Connection con) {
        this.con = con;
    }

    /** Cuenta resultados del catalogo aplicando los mismos filtros que la pagina actual. */
    public int contarCatalogo(String busqueda, Integer idCategoria) throws SQLException {
        String like = (busqueda == null || busqueda.isBlank()) ? null : "%" + busqueda.trim() + "%";
        String sql = "SELECT COUNT(*) FROM productos p WHERE p.activo = TRUE "
                + "AND (? IS NULL OR p.nombre_producto ILIKE ? OR p.descripcion ILIKE ?) "
                + "AND (? IS NULL OR p.id_categoria = ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            if (idCategoria == null) {
                ps.setNull(4, Types.INTEGER);
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(4, idCategoria);
                ps.setInt(5, idCategoria);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Productos activos para el catalogo, filtrados y paginados desde PostgreSQL. */
    public List<Producto> listarCatalogo(String busqueda, Integer idCategoria, int limite, int offset)
            throws SQLException {
        String like = (busqueda == null || busqueda.isBlank()) ? null : "%" + busqueda.trim() + "%";
        String sql = SELECT_BASE
                + "WHERE p.activo = TRUE "
                + "AND (? IS NULL OR p.nombre_producto ILIKE ? OR p.descripcion ILIKE ?) "
                + "AND (? IS NULL OR p.id_categoria = ?) "
                + "ORDER BY c.nombre_categoria, p.nombre_producto LIMIT ? OFFSET ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            if (idCategoria == null) {
                ps.setNull(4, Types.INTEGER);
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(4, idCategoria);
                ps.setInt(5, idCategoria);
            }
            ps.setInt(6, Math.max(1, limite));
            ps.setInt(7, Math.max(0, offset));
            return ejecutarListado(ps);
        }
    }

    /** Todos los productos (incluidos inactivos) para la pantalla de inventario. */
    public List<Producto> listarTodos() throws SQLException {
        String sql = SELECT_BASE + "ORDER BY c.nombre_categoria, p.nombre_producto";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            return ejecutarListado(ps);
        }
    }

    public Producto buscarPorId(int idProducto) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.id_producto = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            List<Producto> lista = ejecutarListado(ps);
            return lista.isEmpty() ? null : lista.get(0);
        }
    }

    /** Busca el producto bloqueando su fila (SELECT ... FOR UPDATE) dentro de una transaccion. */
    public Producto buscarPorIdConBloqueo(int idProducto) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.id_producto = ? FOR UPDATE OF p";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            List<Producto> lista = ejecutarListado(ps);
            return lista.isEmpty() ? null : lista.get(0);
        }
    }

    public int insertar(Producto p) throws SQLException {
        String sql = "INSERT INTO productos (id_categoria, nombre_producto, descripcion, imagen_url, "
                + "precio, stock_disponible, stock_defectuoso, costo_unitario) VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "RETURNING id_producto";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getIdCategoria());
            ps.setString(2, p.getNombreProducto());
            ps.setString(3, p.getDescripcion());
            ps.setString(4, p.getImagenUrl());
            ps.setBigDecimal(5, p.getPrecio());
            ps.setInt(6, p.getStockDisponible());
            ps.setInt(7, p.getStockDefectuoso());
            ps.setBigDecimal(8,p.getCostoUnitario());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public boolean actualizarPrecio(int idProducto, BigDecimal precio) throws SQLException {
        String sql = "UPDATE productos SET precio = ?, fecha_actualizacion = CURRENT_TIMESTAMP "
                + "WHERE id_producto = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, precio);
            ps.setInt(2, idProducto);
            return ps.executeUpdate() == 1;
        }
    }

    /** Descuenta stock de venta solo si alcanza; devuelve false si no hay stock suficiente. */
    public boolean descontarStockDisponible(int idProducto, int cantidad) throws SQLException {
        String sql = "UPDATE productos SET stock_disponible = stock_disponible - ?, "
                + "fecha_actualizacion = CURRENT_TIMESTAMP "
                + "WHERE id_producto = ? AND stock_disponible >= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad);
            return ps.executeUpdate() == 1;
        }
    }

    /** Ajuste positivo o negativo del stock de venta; no permite quedar en negativo. */
    public boolean ajustarStockDisponible(int idProducto, int delta) throws SQLException {
        String sql = "UPDATE productos SET stock_disponible = stock_disponible + ?, "
                + "fecha_actualizacion = CURRENT_TIMESTAMP "
                + "WHERE id_producto = ? AND stock_disponible + ? >= 0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, idProducto);
            ps.setInt(3, delta);
            return ps.executeUpdate() == 1;
        }
    }

    public void sumarStockDisponible(int idProducto, int cantidad) throws SQLException {
        actualizarStock("stock_disponible", idProducto, cantidad);
    }

    public void sumarStockDefectuoso(int idProducto, int cantidad) throws SQLException {
        actualizarStock("stock_defectuoso", idProducto, cantidad);
    }

    /** Solo se llama con el nombre de columna fijo de esta clase (nunca con datos del usuario). */
    private void actualizarStock(String columna, int idProducto, int cantidad) throws SQLException {
        String sql = "UPDATE productos SET " + columna + " = " + columna + " + ?, "
                + "fecha_actualizacion = CURRENT_TIMESTAMP WHERE id_producto = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    private List<Producto> ejecutarListado(PreparedStatement ps) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setIdCategoria(rs.getInt("id_categoria"));
        p.setNombreProducto(rs.getString("nombre_producto"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setImagenUrl(rs.getString("imagen_url"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setCostoUnitario(rs.getBigDecimal("costo_unitario"));
        p.setStockDisponible(rs.getInt("stock_disponible"));
        p.setStockDefectuoso(rs.getInt("stock_defectuoso"));
        p.setActivo(rs.getBoolean("activo"));
        p.setFechaRegistro(rs.getObject("fecha_registro", java.time.LocalDateTime.class));
        p.setFechaActualizacion(rs.getObject("fecha_actualizacion", java.time.LocalDateTime.class));
        p.setNombreCategoria(rs.getString("nombre_categoria"));
        return p;
    }
}
