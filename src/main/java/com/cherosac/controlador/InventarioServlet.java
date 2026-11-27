package com.cherosac.controlador;

import com.cherosac.config.ConexionBD;
import com.cherosac.dao.CategoriaDAO;
import com.cherosac.dao.HistorialAccionDAO;
import com.cherosac.dao.MovimientoInventarioDAO;
import com.cherosac.dao.ProductoDAO;
import com.cherosac.modelo.MovimientoInventario;
import com.cherosac.modelo.Producto;
import com.cherosac.modelo.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** A-04: inventario con stock disponible vs defectuoso, Kardex y gestion de productos. */
@jakarta.servlet.annotation.MultipartConfig(maxFileSize=5242880, maxRequestSize=6291456)
@WebServlet("/admin/inventario")
public class InventarioServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer idFiltro = entero(req.getParameter("producto"));
        try (Connection con = ConexionBD.obtener()) {
            List<Producto> productos = new ProductoDAO(con).listarTodos();
            int disponible = 0;
            int defectuoso = 0;
            for (Producto p : productos) {
                disponible += p.getStockDisponible();
                defectuoso += p.getStockDefectuoso();
            }
            req.setAttribute("productos", productos);
            req.setAttribute("totalDisponible", disponible);
            req.setAttribute("totalDefectuoso", defectuoso);
            req.setAttribute("categorias", new CategoriaDAO(con).listarActivas());
            req.setAttribute("kardex", new MovimientoInventarioDAO(con).listar(idFiltro, 150));
        } catch (SQLException e) {
            log("Error al cargar el inventario", e);
            req.setAttribute("error", "No se pudo cargar el inventario.");
        }
        req.setAttribute("filtroProducto", idFiltro);
        req.getRequestDispatcher("/inventario_productos.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Usuario admin = usuarioAdmin(req);
        String accion = texto(req.getParameter("accion"));

        java.nio.file.Path nuevaFoto = null;
        String ok = null;
        String error = null;
        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);
            try {
                if (("crearProducto".equals(accion) || "cambiarFoto".equals(accion))
                        && req.getContentType() != null && req.getContentType().startsWith("multipart/")) {
                    nuevaFoto = ImagenProductoServlet.guardar(req.getPart("imagenFile"));
                    if (nuevaFoto != null) req.setAttribute("rutaFoto", "/img/productos/subidas/" + nuevaFoto.getFileName());
                }
                ok = switch (accion == null ? "" : accion) {
                    case "ajustarStock" -> ajustarStock(con, admin, req);
                    case "actualizarPrecio" -> actualizarPrecio(con, admin, req);
                    case "crearProducto" -> crearProducto(con, admin, req);
                    case "reparar", "merma" -> resolverDefectuoso(con,admin,req,accion);
                    case "cambiarFoto" -> cambiarFoto(con,admin,req);
                    case "actualizarCosto" -> actualizarCosto(con,admin,req);
                    default -> throw new IllegalArgumentException("Acción no válida.");
                };
                con.commit();
                nuevaFoto = null;
            } catch (IllegalArgumentException e) {
                con.rollback();
                error = e.getMessage();
            } catch (IOException | IllegalStateException e) {
                con.rollback();
                error = "No se pudo guardar la imagen. Use una foto válida de hasta 5 MB.";
            } catch (SQLException e) {
                con.rollback();
                log("Error al guardar el cambio de inventario", e);
                error = "No se pudo guardar el cambio. Verifique los datos.";
            }
        } catch (SQLException e) {
            log("Sin conexion a la base de datos", e);
            error = "No hay conexión con la base de datos.";
        }
        if (nuevaFoto != null) java.nio.file.Files.deleteIfExists(nuevaFoto);
        redirigir(req, resp, "/admin/inventario", ok, error);
    }

    /** Ajuste manual del stock de venta (positivo o negativo), registrado como AJUSTE en el Kardex. */
    private String ajustarStock(Connection con, Usuario admin, HttpServletRequest req) throws SQLException {
        Integer idProducto = entero(req.getParameter("idProducto"));
        Integer delta = entero(req.getParameter("cantidad"));
        String motivo = texto(req.getParameter("motivo"));
        if (idProducto == null || delta == null || delta == 0 || Math.abs(delta) > 100000) {
            throw new IllegalArgumentException("Indique un ajuste válido (distinto de cero).");
        }
        ProductoDAO productoDAO = new ProductoDAO(con);
        Producto producto = productoDAO.buscarPorIdConBloqueo(idProducto);
        if (producto == null) {
            throw new IllegalArgumentException("El producto no existe.");
        }
        if (!productoDAO.ajustarStockDisponible(idProducto, delta)) {
            throw new IllegalArgumentException("El ajuste dejaría el stock de " + producto.getNombreProducto()
                    + " en negativo.");
        }

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setIdProducto(idProducto);
        movimiento.setTipoMovimiento("AJUSTE");
        movimiento.setCantidad(delta);
        movimiento.setReferenciaCodigo("AJUSTE-MANUAL");
        new MovimientoInventarioDAO(con).insertar(movimiento);

        new HistorialAccionDAO(con).registrar(admin.getIdUsuario(), "AJUSTE_STOCK",
                producto.getNombreProducto() + ": " + (delta > 0 ? "+" : "") + delta
                        + (motivo == null ? "" : " (" + motivo + ")"));
        return "Stock de " + producto.getNombreProducto() + " ajustado en " + delta + " unidad(es).";
    }

    private String actualizarPrecio(Connection con, Usuario admin, HttpServletRequest req) throws SQLException {
        Integer idProducto = entero(req.getParameter("idProducto"));
        BigDecimal precio = decimal(req.getParameter("precio"));
        if (idProducto == null || precio == null || precio.signum() <= 0 || precio.compareTo(new BigDecimal("99999.99")) > 0) {
            throw new IllegalArgumentException("Indique un precio válido mayor que cero.");
        }
        if (!new ProductoDAO(con).actualizarPrecio(idProducto, precio)) {
            throw new IllegalArgumentException("El producto no existe.");
        }
        new HistorialAccionDAO(con).registrar(admin.getIdUsuario(), "CAMBIO_PRECIO",
                "Producto #" + idProducto + " a S/ " + precio.toPlainString());
        return "Precio actualizado a S/ " + precio.toPlainString() + ".";
    }

    private String crearProducto(Connection con, Usuario admin, HttpServletRequest req) throws SQLException {
        String nombre = texto(req.getParameter("nombre"));
        String descripcion = texto(req.getParameter("descripcion"));
        String imagen = (String) req.getAttribute("rutaFoto");
        if (imagen == null) imagen = texto(req.getParameter("imagenUrl"));
        Integer idCategoria = entero(req.getParameter("idCategoria"));
        BigDecimal precio = decimal(req.getParameter("precio"));
        Integer stockDisponible = entero(req.getParameter("stockDisponible"));
        Integer stockDefectuoso = entero(req.getParameter("stockDefectuoso"));

        if (nombre == null || nombre.length() < 3 || nombre.length() > 150) {
            throw new IllegalArgumentException("El nombre del producto debe tener entre 3 y 150 caracteres.");
        }
        if (idCategoria == null) {
            throw new IllegalArgumentException("Seleccione una categoría.");
        }
        if (precio == null || precio.signum() <= 0) {
            throw new IllegalArgumentException("Indique un precio mayor que cero.");
        }
        if (stockDisponible == null || stockDisponible < 0 || stockDefectuoso == null || stockDefectuoso < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo.");
        }
        if (imagen != null && !imagen.startsWith("https://") && !imagen.startsWith("http://")
                && !imagen.startsWith("/img/")) {
            throw new IllegalArgumentException("La imagen debe ser una URL https:// o http://, o una ruta /img/.");
        }

        Producto p = new Producto();
        p.setIdCategoria(idCategoria);
        p.setNombreProducto(nombre);
        p.setDescripcion(descripcion);
        p.setImagenUrl(imagen);
        p.setCostoUnitario(costo(req));
        p.setPrecio(precio.setScale(2, java.math.RoundingMode.HALF_UP));
        p.setStockDisponible(stockDisponible);
        p.setStockDefectuoso(stockDefectuoso);
        int idProducto = new ProductoDAO(con).insertar(p);

        MovimientoInventarioDAO movimientoDAO = new MovimientoInventarioDAO(con);
        if (stockDisponible > 0) {
            MovimientoInventario m = new MovimientoInventario();
            m.setIdProducto(idProducto);
            m.setTipoMovimiento("AJUSTE");
            m.setCantidad(stockDisponible);
            m.setReferenciaCodigo("ALTA-PRODUCTO");
            movimientoDAO.insertar(m);
        }
        if (stockDefectuoso > 0) {
            MovimientoInventario m = new MovimientoInventario();
            m.setIdProducto(idProducto);
            m.setTipoMovimiento("INGRESO_DEFECTUOSO");
            m.setCantidad(stockDefectuoso);
            m.setReferenciaCodigo("ALTA-PRODUCTO");
            movimientoDAO.insertar(m);
        }
        new HistorialAccionDAO(con).registrar(admin.getIdUsuario(), "ALTA_PRODUCTO",
                "Producto #" + idProducto + ": " + nombre);
        return "Producto \"" + nombre + "\" registrado con ID " + idProducto + ".";
    }

    private BigDecimal costo(HttpServletRequest req) {
        BigDecimal costo = decimal(req.getParameter("costo"));
        if (costo == null || costo.signum()<0 || costo.compareTo(new BigDecimal("99999999.99"))>0)
            throw new IllegalArgumentException("Indique el costo unitario sin IGV.");
        return costo;
    }

    private String actualizarCosto(Connection con, Usuario admin, HttpServletRequest req) throws SQLException {
        Integer id = entero(req.getParameter("idProducto"));
        if (id == null) throw new IllegalArgumentException("Seleccione un producto.");
        BigDecimal importe = costo(req);
        try (var ps = con.prepareStatement("UPDATE productos SET costo_unitario=?, fecha_actualizacion=now() WHERE id_producto=?")) {
            ps.setBigDecimal(1,importe);
            ps.setInt(2,id);
            if (ps.executeUpdate()!=1) throw new IllegalArgumentException("Producto desconocido.");
        }
        new HistorialAccionDAO(con).registrar(admin.getIdUsuario(),"CAMBIO_COSTO","Producto #"+id+" · S/ "+importe);
        return "Costo actualizado para ventas futuras.";
    }

    private String cambiarFoto(Connection con, Usuario admin, HttpServletRequest req) throws SQLException {
        String ruta = (String)req.getAttribute("rutaFoto");
        Integer id = entero(req.getParameter("idProducto"));
        if (id == null || ruta == null) throw new IllegalArgumentException("Seleccione un producto y una imagen.");
        try (var ps = con.prepareStatement("UPDATE productos SET imagen_url=?, fecha_actualizacion=now() WHERE id_producto=?")) {
            ps.setString(1,ruta);
            ps.setInt(2,id);
            if (ps.executeUpdate()!=1) throw new IllegalArgumentException("Producto desconocido.");
        }
        new HistorialAccionDAO(con).registrar(admin.getIdUsuario(),"CAMBIO_FOTO","Producto #"+id);
        return "Foto actualizada.";
    }

    private String resolverDefectuoso(Connection con, Usuario admin, HttpServletRequest req, String accion) throws SQLException {
        Integer id = entero(req.getParameter("idProducto"));
        Integer cantidad = entero(req.getParameter("cantidad"));
        if (id == null || cantidad == null || cantidad<=0) throw new IllegalArgumentException("Indique una cantidad positiva.");
        boolean reparar = "reparar".equals(accion);
        try (var ps = con.prepareStatement("UPDATE productos SET stock_defectuoso=stock_defectuoso-?, " +
                "stock_disponible=stock_disponible+?,fecha_actualizacion=now() WHERE id_producto=? AND stock_defectuoso>=?")) {
            ps.setInt(1,cantidad);
            ps.setInt(2,reparar ? cantidad : 0);
            ps.setInt(3,id);
            ps.setInt(4,cantidad);
            if (ps.executeUpdate()!=1) throw new IllegalArgumentException("El stock defectuoso disponible es insuficiente.");
        }
        String tipo = reparar ? "REPARACION_OK" : "BAJA_MERMA";
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setIdProducto(id);
        movimiento.setReferenciaCodigo("INVENTARIO");
        new MovimientoInventarioDAO(con).insertar(movimiento);
        new HistorialAccionDAO(con).registrar(admin.getIdUsuario(),tipo,"Producto #"+id+" · "+cantidad+" unidad(es)");
        return reparar ? "Unidades reintegradas al stock de venta." : "Merma registrada.";
    }

}
