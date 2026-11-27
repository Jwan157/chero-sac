package com.cherosac.controlador;

import com.cherosac.config.ConexionBD;
import com.cherosac.dao.CategoriaDAO;
import com.cherosac.dao.ProductoDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/** P-01: catalogo de productos con busqueda y filtro por categoria. */
@WebServlet("/catalogo")
public class CatalogoServlet extends BaseServlet {

    private static final int PRODUCTOS_POR_PAGINA = 16;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busqueda = texto(req.getParameter("q"));
        Integer idCategoria = entero(req.getParameter("cat"));
        Integer paginaSolicitada = entero(req.getParameter("pagina"));
        int pagina = (paginaSolicitada == null || paginaSolicitada < 1) ? 1 : paginaSolicitada;
        int totalProductos = 0;
        int totalPaginas = 1;

        try (Connection con = ConexionBD.obtener()) {
            req.setAttribute("categorias", new CategoriaDAO(con).listarActivas());
            ProductoDAO productoDAO = new ProductoDAO(con);
            totalProductos = productoDAO.contarCatalogo(busqueda, idCategoria);
            totalPaginas = Math.max(1, (int) Math.ceil(totalProductos / (double) PRODUCTOS_POR_PAGINA));
            pagina = Math.min(pagina, totalPaginas);
            int offset = (pagina - 1) * PRODUCTOS_POR_PAGINA;
            req.setAttribute("productos", productoDAO.listarCatalogo(busqueda, idCategoria,
                    PRODUCTOS_POR_PAGINA, offset));
        } catch (SQLException e) {
            log("Error al cargar el catalogo", e);
            req.setAttribute("error", "No se pudo cargar el catálogo. Verifique la conexión a PostgreSQL.");
        }

        req.setAttribute("q", busqueda);
        req.setAttribute("cat", idCategoria);
        req.setAttribute("paginaActual", pagina);
        req.setAttribute("totalPaginas", totalPaginas);
        req.setAttribute("totalProductos", totalProductos);
        req.setAttribute("inicioResultados", totalProductos == 0 ? 0 : ((pagina - 1) * PRODUCTOS_POR_PAGINA) + 1);
        req.setAttribute("finResultados", Math.min(pagina * PRODUCTOS_POR_PAGINA, totalProductos));
        req.getRequestDispatcher("/catalogo.jsp").forward(req, resp);
    }
}
