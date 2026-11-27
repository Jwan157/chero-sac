package com.cherosac.controlador;

import com.cherosac.config.ConexionBD;
import com.cherosac.dao.HistorialAccionDAO;
import com.cherosac.dao.UsuarioDAO;
import com.cherosac.modelo.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/** A-01: acceso de administradores con contrasena BCrypt. Tambien cierra la sesion en /logout. */
@WebServlet({"/login", "/logout"})
public class LoginServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/logout".equals(req.getServletPath())) {
            resp.sendRedirect(req.getContextPath() + "/admin/pedidos");
            return;
        }
        if (usuarioAdmin(req) != null) {
            resp.sendRedirect(req.getContextPath() + "/admin/pedidos");
            return;
        }
        if ("sesion".equals(req.getParameter("motivo"))) {
            req.setAttribute("aviso", "Su sesión expiró o aún no ha iniciado sesión.");
        }
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/logout".equals(req.getServletPath())) {
            Usuario actual = usuarioAdmin(req);
            if (actual != null) {
                try (Connection con = ConexionBD.obtener()) {
                    new HistorialAccionDAO(con).registrar(actual.getIdUsuario(),"LOGOUT","Cierre de sesión");
                } catch (SQLException e) { log("Error al registrar el cierre de sesión",e); }
            }
            req.getSession().invalidate();
            resp.sendRedirect(req.getContextPath()+"/catalogo");
            return;
        }
        String nombreUsuario = texto(req.getParameter("usuario"));
        String clave = req.getParameter("clave");

        if (nombreUsuario == null || clave == null || clave.isEmpty()) {
            req.setAttribute("error", "Ingrese usuario y contraseña.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        try (Connection con = ConexionBD.obtener()) {
            UsuarioDAO usuarioDAO = new UsuarioDAO(con);
            Usuario usuario = usuarioDAO.buscarActivoPorNombreUsuario(nombreUsuario);
            if (usuario == null || !BCrypt.checkpw(clave, usuario.getPasswordHash())) {
                req.setAttribute("error", "Usuario o contraseña incorrectos.");
            } else {
                usuarioDAO.actualizarUltimoAcceso(usuario.getIdUsuario());
                new HistorialAccionDAO(con).registrar(usuario.getIdUsuario(), "LOGIN",
                        "Inicio de sesión desde " + req.getRemoteAddr());

                // No se guarda el hash en la sesion; se renueva el id de sesion contra fijacion
                usuario.setPasswordHash(null);
                req.getSession(true);
                req.changeSessionId();
                req.getSession().removeAttribute("cliente");
                req.getSession().setAttribute("usuarioAdmin", usuario);
                resp.sendRedirect(req.getContextPath() + "/admin/pedidos");
                return;
            }
        } catch (SQLException e) {
            log("Error al validar el acceso", e);
            req.setAttribute("error", "No se pudo validar el acceso. Intente nuevamente.");
        }
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }
}
