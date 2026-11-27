package com.cherosac.controlador;

import com.cherosac.config.ConexionBD;
import com.cherosac.dao.ComprobanteDAO;
import com.cherosac.dao.DetallePedidoDAO;
import com.cherosac.dao.PedidoDAO;
import com.cherosac.dao.SolicitudDevolucionDAO;
import com.cherosac.modelo.Pedido;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** P-03: seguimiento del pedido con codigo y documento del cliente. */
@WebServlet("/seguimiento")
public class SeguimientoServlet extends BaseServlet {

    /** Pasos de la linea de tiempo, en orden. */
    public static final List<String> PASOS = List.of("PENDIENTE", "CONFIRMADO", "ENVIADO", "ENTREGADO");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String codigo = texto(req.getParameter("codigo"));
        String documento = texto(req.getParameter("documento"));
        req.setAttribute("pasos", PASOS);

        var cuenta = clienteActual(req);
        try (Connection con = ConexionBD.obtener()) {
            PedidoDAO pedidos = new PedidoDAO(con);
            if (cuenta != null) {
                List<Pedido> historial = pedidos.listarPorCliente(cuenta.getIdCliente());
                req.setAttribute("misPedidos", historial);
                req.setAttribute("documentosPorPedido",new ComprobanteDAO(con).listarPorCliente(cuenta.getIdCliente())
                        .stream().collect(java.util.stream.Collectors.groupingBy(com.cherosac.modelo.ComprobanteElectronico::getIdPedido)));
                req.setAttribute("postventaPorPedido",new SolicitudDevolucionDAO(con).listarPorCliente(cuenta.getIdCliente())
                        .stream().collect(java.util.stream.Collectors.groupingBy(com.cherosac.modelo.SolicitudDevolucion::getIdPedido)));
                if (codigo == null && !historial.isEmpty()) codigo = historial.get(0).getCodigoSeguimiento();
            }
            if (codigo != null) {
                Pedido pedido = pedidos.buscarPorCodigo(codigo.toUpperCase(java.util.Locale.ROOT));
                if (!puedeConsultar(req, pedido, documento)) {
                    req.setAttribute("error", "Revise el código y el documento. Si tiene cuenta, inicie sesión.");
                } else {
                    req.setAttribute("pedido", pedido);
                    req.setAttribute("indicePaso", PASOS.indexOf(pedido.getEstado()));
                    req.setAttribute("detalles", new DetallePedidoDAO(con).listarPorPedido(pedido.getIdPedido()));
                    req.setAttribute("comprobantes", new ComprobanteDAO(con).listarPorPedido(pedido.getIdPedido()));
                    req.setAttribute("solicitudes", new SolicitudDevolucionDAO(con).listarPorPedido(pedido.getIdPedido()));
                }
            }
        } catch (SQLException e) {
            log("Error al consultar el seguimiento", e);
            req.setAttribute("error", "No se pudo consultar el historial.");
        }
        req.getRequestDispatcher("/seguimiento.jsp").forward(req, resp);
    }
}
