package com.cherosac.controlador;

import com.cherosac.config.ConexionBD;
import com.cherosac.dao.DetallePedidoDAO;
import com.cherosac.dao.PedidoDAO;
import com.cherosac.dao.SolicitudDevolucionDAO;
import com.cherosac.modelo.DetallePedido;
import com.cherosac.modelo.Pedido;
import com.cherosac.modelo.SolicitudDevolucion;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** P-03: el cliente registra una solicitud de devolucion, cambio o garantia (queda PENDIENTE). */
@WebServlet("/devolucion")
public class DevolucionServlet extends BaseServlet {

    private static final List<String> TIPOS = List.of("DEVOLUCION", "CAMBIO", "GARANTIA");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String codigo = texto(req.getParameter("codigo"));
        String documento = texto(req.getParameter("documento"));

        String error;
        try (Connection con = ConexionBD.obtener()) {
            error = registrarSolicitud(req, con);
        } catch (SQLException e) {
            log("Error al registrar la solicitud de devolucion", e);
            error = "No se pudo registrar la solicitud. Intente nuevamente.";
        }

        String destino = "/seguimiento?codigo=" + enc(codigo) + "&documento=" + enc(documento);
        redirigir(req, resp, destino, "Su solicitud fue registrada. Nuestro equipo la revisará pronto.", error);
    }

    /** Valida la solicitud y la guarda. Devuelve null si todo esta bien, o el mensaje de error. */
    private String registrarSolicitud(HttpServletRequest req, Connection con) throws SQLException {
        String codigo = texto(req.getParameter("codigo"));
        String documento = texto(req.getParameter("documento"));
        String tipo = texto(req.getParameter("tipoSolicitud"));
        String motivo = texto(req.getParameter("motivo"));
        Integer idProducto = entero(req.getParameter("idProducto"));
        Integer cantidad = entero(req.getParameter("cantidad"));

        if (codigo == null || documento == null) {
            return "Datos del pedido incompletos.";
        }
        Pedido pedido = new PedidoDAO(con).buscarPorCodigo(codigo.toUpperCase());
        if (!puedeConsultar(req, pedido, documento)) {
            return "Pedido o documento no válidos.";
        }
        if (!"ENTREGADO".equals(pedido.getEstado())) {
            return "Solo puede solicitar cambios o devoluciones de pedidos entregados.";
        }
        if (tipo == null || !TIPOS.contains(tipo)) {
            return "Seleccione el tipo de solicitud.";
        }
        if (motivo == null || motivo.length() < 10 || motivo.length() > 500) {
            return "Describa el motivo (entre 10 y 500 caracteres).";
        }
        if (idProducto == null || cantidad == null) {
            return "Seleccione el producto y la cantidad.";
        }

        DetallePedido detalle = new DetallePedidoDAO(con).buscarPorPedidoYProducto(pedido.getIdPedido(), idProducto);
        if (detalle == null) {
            return "El producto seleccionado no pertenece a este pedido.";
        }
        SolicitudDevolucionDAO solicitudDAO = new SolicitudDevolucionDAO(con);
        int disponible = detalle.getCantidad() - solicitudDAO.cantidadAprobada(pedido.getIdPedido(), idProducto);
        if (disponible <= 0) {
            return "Este producto ya fue devuelto en su totalidad.";
        }
        if (cantidad < 1 || cantidad > disponible) {
            return "La cantidad debe estar entre 1 y " + disponible + ".";
        }

        SolicitudDevolucion solicitud = new SolicitudDevolucion();
        solicitud.setIdPedido(pedido.getIdPedido());
        solicitud.setIdProducto(idProducto);
        solicitud.setCantidadDevuelta(cantidad);
        solicitud.setTipoSolicitud(tipo);
        solicitud.setMotivo(motivo);
        solicitudDAO.insertar(solicitud);
        return null;
    }
}
