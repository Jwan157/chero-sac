package com.cherosac.controlador;

import com.cherosac.config.ConexionBD;
import com.cherosac.dao.ClienteDAO;
import com.cherosac.modelo.Cliente;
import com.cherosac.modelo.ComprobanteElectronico;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.sql.*;

@WebServlet("/cliente")
public class ClienteAuthServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (clienteActual(req) != null) resp.sendRedirect(req.getContextPath()+"/seguimiento");
        else req.getRequestDispatcher("/cliente_auth.jsp").forward(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accion = req.getParameter("accion");
        if ("salir".equals(accion)) {
            req.getSession().invalidate();
            resp.sendRedirect(req.getContextPath()+"/catalogo");
            return;
        }
        String documento = texto(req.getParameter("documento"));
        String clave = req.getParameter("clave");
        try (Connection con = ConexionBD.obtener()) {
            ClienteDAO dao = new ClienteDAO(con);
            Cliente c;
            if ("registrar".equals(accion)) {
                String tipo = texto(req.getParameter("tipoDocumento"));
                String correo = texto(req.getParameter("correo"));
                String nombre = texto(req.getParameter("nombre"));
                String apellido = texto(req.getParameter("apellido"));
                String direccion = texto(req.getParameter("direccion"));
                if (!documentoValido(tipo,documento) || !correoValido(correo) || !claveValida(clave)
                        || nombre == null || nombre.length() < 2 || nombre.length() > 70
                        || ("DNI".equals(tipo) && (apellido == null || apellido.length()<2 || apellido.length()>79))
                        || direccion == null || direccion.length()<5 || direccion.length()>200)
                    throw new IllegalArgumentException("Revise su documento, nombre, dirección, correo y contraseña.");
                c = new Cliente();
                c.setTipoDocumento(tipo);
                c.setNumeroDocumento(documento);
                c.setCorreo(correo);
                c.setNombreRazonSocial(nombre);
                c.setApellido("DNI".equals(tipo) ? apellido : null);
                c.setDireccion(direccion);
                String vinculo = texto(req.getParameter("vinculo"));
                if (!dao.registrar(c, BCrypt.hashpw(clave,BCrypt.gensalt(11)),
                        vinculo == null ? null : ComprobanteElectronico.hashSha256(vinculo)))
                    throw new IllegalArgumentException("Si ya tiene cuenta, inicie sesión. Para vincular compras previas ingrese la clave de su primera compra o contacte a la tienda.");
            } else if ("entrar".equals(accion)) {
                c = documento != null && claveValida(clave) ? dao.autenticar(documento,clave) : null;
                if (c == null) throw new IllegalArgumentException("Documento o contraseña incorrectos.");
            } else throw new IllegalArgumentException("Acción desconocida.");
            req.changeSessionId();
            req.getSession().removeAttribute("usuarioAdmin");
            req.getSession().removeAttribute("claveVinculo");
            req.getSession().setAttribute("cliente",c);
            resp.sendRedirect(req.getContextPath()+"/seguimiento");
            return;
        } catch (IllegalArgumentException e) {
            req.setAttribute("error",e.getMessage());
        } catch (SQLException e) {
            throw new ServletException("Error al acceder a la cuenta",e);
        }
        req.getRequestDispatcher("/cliente_auth.jsp").forward(req,resp);
    }
}

