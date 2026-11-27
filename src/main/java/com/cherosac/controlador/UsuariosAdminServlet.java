package com.cherosac.controlador;

import com.cherosac.config.ConexionBD;
import com.cherosac.dao.HistorialAccionDAO;
import com.cherosac.dao.UsuarioDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.sql.*;

@WebServlet("/admin/usuarios")
public class UsuariosAdminServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (Connection con = ConexionBD.obtener()) {
            req.setAttribute("trabajadores", new UsuarioDAO(con).listar());
            Integer antes = entero(req.getParameter("antes"));
            var pagina = new HistorialAccionDAO(con).listarPagina(101,antes == null ? Integer.MAX_VALUE : antes);
            if (pagina.size() > 100) {
                req.setAttribute("siguiente", pagina.get(99).getIdHistorial());
                pagina = pagina.subList(0,100);
            }
            req.setAttribute("historial",pagina);
        } catch (SQLException e) {
            throw new ServletException("Error al cargar los trabajadores", e);
        }
        req.getRequestDispatcher("/usuarios_admin.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String error = null;
        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);
            try {
                String accion = req.getParameter("accion");
                String detalle;
                if ("crear".equals(accion)) {
                    String nombre = texto(req.getParameter("nombre"));
                    String login = texto(req.getParameter("usuario"));
                    String correo = texto(req.getParameter("correo"));
                    String clave = req.getParameter("clave");
                    if (nombre == null || nombre.length() > 120 || login == null || !login.matches("[a-zA-Z0-9_.-]{3,50}")
                            || !correoValido(correo) || !claveValida(clave))
                        throw new IllegalArgumentException("Revise nombre, usuario, correo y contraseña (8 a 72 bytes).");
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO usuarios " +
                            "(nombre_completo,nombre_usuario,correo,password_hash,rol) VALUES (?,?,?,?,'ADMIN')")) {
                        ps.setString(1,nombre);
                        ps.setString(2,login);
                        ps.setString(3,correo);
                        ps.setString(4,BCrypt.hashpw(clave, BCrypt.gensalt(11)));
                        ps.executeUpdate();
                    }
                    detalle = "Alta de " + login;
                } else if ("desactivar".equals(accion) || "activar".equals(accion)) {
                    Integer id = entero(req.getParameter("idUsuario"));
                    if (id == null) throw new IllegalArgumentException("Seleccione un trabajador.");
                    try (PreparedStatement ps = con.prepareStatement("UPDATE usuarios SET activo=? WHERE id_usuario=? AND rol='ADMIN' AND activo<>?")) {
                        boolean activar = "activar".equals(accion);
                        ps.setBoolean(1,activar);
                        ps.setInt(2,id);
                        ps.setBoolean(3,activar);
                        if (ps.executeUpdate() != 1) throw new IllegalArgumentException("El trabajador no admite ese cambio.");
                    }
                    detalle = accion + " trabajador #" + id;
                } else throw new IllegalArgumentException("Acción desconocida.");
                new HistorialAccionDAO(con).registrar(usuarioAdmin(req).getIdUsuario(), "GESTION_USUARIO", detalle);
                con.commit();
            } catch (IllegalArgumentException e) {
                con.rollback();
                error = e.getMessage();
            } catch (SQLException e) {
                con.rollback();
                if (!"23505".equals(e.getSQLState())) throw e;
                error = "El nombre de usuario o el correo ya está registrado.";
            }
        } catch (SQLException e) {
            throw new ServletException("Error al guardar el trabajador", e);
        }
        redirigir(req,resp,"/admin/usuarios","Trabajador actualizado.",error);
    }
}

