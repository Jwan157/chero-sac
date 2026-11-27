package com.cherosac.controlador;

import com.cherosac.config.ConexionBD;
import com.cherosac.dao.UsuarioDAO;
import com.cherosac.modelo.Usuario;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@WebFilter("/*")
public class AuthFilter implements Filter {
    private static final Map<String,String> VISTAS = Map.of(
        "/dashboard_pedidos.jsp", "/admin/pedidos", "/postventa_devoluciones.jsp", "/admin/devoluciones",
        "/inventario_productos.jsp", "/admin/inventario", "/reportes.jsp", "/admin/reportes",
        "/usuarios_admin.jsp", "/admin/usuarios", "/cliente_auth.jsp", "/cliente", "/seguimiento.jsp", "/seguimiento",
        "/checkout.jsp", "/checkout", "/catalogo.jsp", "/catalogo", "/login.jsp", "/login");
    private static final Set<String> EMPLEADO = Set.of("/admin/pedidos", "/admin/devoluciones", "/admin/inventario");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String ruta = req.getServletPath();
        if (ruta.startsWith("/img/") || ruta.startsWith("/css/") || ruta.startsWith("/js/")) {
            chain.doFilter(req, resp);
            return;
        }
        resp.setHeader("Cache-Control", "no-store");
        HttpSession sesion = req.getSession();
        synchronized (sesion) {
            if (sesion.getAttribute("csrf") == null) sesion.setAttribute("csrf", UUID.randomUUID().toString());
        }
        String destino = VISTAS.getOrDefault(ruta, ruta);
        if (destino.startsWith("/admin/")) {
            Usuario usuario = (Usuario) sesion.getAttribute("usuarioAdmin");
            if (usuario != null) {
                try (Connection con = ConexionBD.obtener()) {
                    usuario = new UsuarioDAO(con).buscarActivoPorNombreUsuario(usuario.getNombreUsuario());
                } catch (SQLException e) {
                    throw new ServletException("Error al comprobar el acceso del trabajador", e);
                }
            }
            if (usuario == null) {
                sesion.removeAttribute("usuarioAdmin");
                resp.sendRedirect(req.getContextPath() + "/login?motivo=sesion");
                return;
            }
            usuario.setPasswordHash(null);
            sesion.setAttribute("usuarioAdmin", usuario);
            if (!"DUENO".equals(usuario.getRol()) && !EMPLEADO.contains(destino)) {
                resp.sendRedirect(req.getContextPath() + "/admin/pedidos?error=Acceso+denegado");
                return;
            }
        }
        if (!destino.equals(ruta)) {
            resp.sendRedirect(req.getContextPath() + destino);
            return;
        }
        if ("POST".equals(req.getMethod())) {
            String token;
            try {
                token = req.getParameter("csrf");
            } catch (IllegalStateException e) {
                resp.sendError(413,"La carga supera el límite admitido. Use una foto de hasta 5 MB.");
                return;
            }
            if (!sesion.getAttribute("csrf").equals(token)) {
                resp.sendError(403, "Actualice la página y vuelva a enviar el formulario.");
                return;
            }
        }
        chain.doFilter(req, resp);
    }
}

