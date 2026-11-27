package com.cherosac.controlador;

import com.cherosac.modelo.Usuario;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Utilidades comunes de los servlets: lectura de parametros, redirecciones y sesion de administrador. */
public abstract class BaseServlet extends HttpServlet {

    /** Devuelve el texto sin espacios laterales, o null si esta vacio. */
    protected static String texto(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    /** Convierte a entero; devuelve null si no es un numero valido. */
    protected static Integer entero(String valor) {
        String limpio = texto(valor);
        if (limpio == null) {
            return null;
        }
        try {
            return Integer.valueOf(limpio);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Convierte a monto con dos decimales; devuelve null si no es valido. */
    protected static BigDecimal decimal(String valor) {
        String limpio = texto(valor);
        if (limpio == null) {
            return null;
        }
        try {
            return new BigDecimal(limpio.replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Codifica un valor para usarlo en una URL. */
    protected static String enc(String valor) {
        return valor == null ? "" : URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    /** Administrador autenticado en la sesion, o null. */
    protected static Usuario usuarioAdmin(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        return sesion == null ? null : (Usuario) sesion.getAttribute("usuarioAdmin");
    }

    /**
     * Redirige a una ruta de la aplicacion. Si hay mensajeError se envia como ?error=,
     * de lo contrario, si hay mensajeOk, como ?ok=.
     */
    protected static void redirigir(HttpServletRequest req, HttpServletResponse resp, String destino,
                                    String mensajeOk, String mensajeError) throws IOException {
        String separador = destino.contains("?") ? "&" : "?";
        String url = req.getContextPath() + destino;
        if (mensajeError != null) {
            url += separador + "error=" + enc(mensajeError);
        } else if (mensajeOk != null) {
            url += separador + "ok=" + enc(mensajeOk);
        }
        resp.sendRedirect(url);
    }

    protected static boolean correoValido(String valor) {
        return valor != null && valor.length() <= 120 && valor.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    }

    protected static boolean claveValida(String clave) {
        return clave != null && clave.length() >= 8 && clave.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    protected static boolean documentoValido(String tipo, String numero) {
        return numero != null && (("DNI".equals(tipo) && numero.matches("[0-9]{8}"))
                || ("RUC".equals(tipo) && numero.matches("(10|20)[0-9]{9}")));
    }

    protected static com.cherosac.modelo.Cliente clienteActual(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (com.cherosac.modelo.Cliente) session.getAttribute("cliente");
    }


    protected static boolean puedeConsultar(HttpServletRequest req, com.cherosac.modelo.Pedido pedido, String documento) {
        if (pedido == null) return false;
        var cuenta = clienteActual(req);
        if (cuenta != null) return cuenta.getIdCliente() == pedido.getIdCliente();
        return !pedido.isClienteConCuenta() && pedido.getClienteNumeroDocumento().equals(documento);
    }

}
