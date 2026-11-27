package com.cherosac.controlador;

import com.cherosac.config.ConexionBD;
import com.cherosac.dao.ComprobanteDAO;
import com.cherosac.dao.DetallePedidoDAO;
import com.cherosac.dao.HistorialAccionDAO;
import com.cherosac.dao.MovimientoInventarioDAO;
import com.cherosac.dao.PedidoDAO;
import com.cherosac.dao.ProductoDAO;
import com.cherosac.dao.SolicitudDevolucionDAO;
import com.cherosac.modelo.DetallePedido;
import com.cherosac.modelo.MovimientoInventario;
import com.cherosac.modelo.Pedido;
import com.cherosac.modelo.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A-02: dashboard de pedidos con resumen semanal, cambio de estado y observaciones. */
@WebServlet("/admin/pedidos")
public class PedidosAdminServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String estado = texto(req.getParameter("estado"));
        String busqueda = texto(req.getParameter("q"));

        try (Connection con = ConexionBD.obtener()) {
            PedidoDAO pedidoDAO = new PedidoDAO(con);
            List<Pedido> pedidos = pedidoDAO.listarFiltrado(estado, busqueda);
            Map<Integer, List<DetallePedido>> detallesPedidos = new LinkedHashMap<>();
            DetallePedidoDAO detalleDAO = new DetallePedidoDAO(con);
            for (Pedido pedido : pedidos) {
                detallesPedidos.put(pedido.getIdPedido(), detalleDAO.listarPorPedido(pedido.getIdPedido()));
            }
            req.setAttribute("pedidos", pedidos);
            req.setAttribute("detallesPedidos", detallesPedidos);
            req.setAttribute("pedidosPendientes", pedidoDAO.contarPendientes());
            req.setAttribute("conteoEstados", pedidoDAO.contarPorEstado());
            req.setAttribute("pedidosDemo", pedidoDAO.contarDemostraciones());
            req.setAttribute("resumen", pedidoDAO.resumenUltimos7Dias());
            req.setAttribute("devolucionesSemana", new SolicitudDevolucionDAO(con).contarUltimos7Dias());
        } catch (SQLException e) {
            log("Error al cargar el dashboard de pedidos", e);
            req.setAttribute("error", "No se pudo cargar la información de pedidos.");
        }

        req.setAttribute("estados", Pedido.ESTADOS);
        req.setAttribute("filtroEstado", estado);
        req.setAttribute("filtroQ", busqueda);
        req.getRequestDispatcher("/dashboard_pedidos.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Usuario admin = usuarioAdmin(req);
        String accion = texto(req.getParameter("accion"));
        Integer idPedido = entero(req.getParameter("idPedido"));
        String nuevoEstado = texto(req.getParameter("estado"));
        String observaciones = texto(req.getParameter("observaciones"));

        String ok = null;
        String error = null;
        if (!"actualizarEstado".equals(accion) || idPedido == null
                || nuevoEstado == null || !Pedido.ESTADOS.contains(nuevoEstado)) {
            error = "Datos de actualización no válidos.";
        } else if (observaciones != null && observaciones.length() > 500) {
            error = "Las observaciones no pueden superar 500 caracteres.";
        } else {
            try (Connection con = ConexionBD.obtener()) {
                con.setAutoCommit(false);
                try {
                    ok = cambiarEstado(con, admin, idPedido, nuevoEstado, observaciones);
                    con.commit();
                } catch (IllegalArgumentException e) {
                    con.rollback();
                    error = e.getMessage();
                } catch (SQLException e) {
                    con.rollback();
                    log("Error al actualizar el pedido", e);
                    error = "No se pudo actualizar el pedido.";
                }
            } catch (SQLException e) {
                log("Sin conexion a la base de datos", e);
                error = "No hay conexión con la base de datos.";
            }
        }
        redirigir(req, resp, "/admin/pedidos", ok, error);
    }

    /**
     * Cambia el estado del pedido. Si se cancela, devuelve al stock de venta lo que no fue
     * devuelto antes, registra el Kardex y anula boletas y facturas.
     */
    private String cambiarEstado(Connection con, Usuario admin, int idPedido, String nuevoEstado,
                                 String observaciones) throws SQLException {
        PedidoDAO pedidoDAO = new PedidoDAO(con);
        Pedido pedido = pedidoDAO.bloquear(idPedido);
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido no existe.");
        }
        if ("CANCELADO".equals(pedido.getEstado())) {
            throw new IllegalArgumentException("Un pedido cancelado no admite cambios de estado.");
        }

        if ("CANCELADO".equals(nuevoEstado) && !pedido.isDemostracion()) {
            // Las compras demo no descontaron stock; por eso no deben reponerlo al cancelarse.
            ProductoDAO productoDAO = new ProductoDAO(con);
            MovimientoInventarioDAO movimientoDAO = new MovimientoInventarioDAO(con);
            SolicitudDevolucionDAO solicitudDAO = new SolicitudDevolucionDAO(con);
            for (DetallePedido d : new DetallePedidoDAO(con).listarPorPedido(idPedido)) {
                int aReponer = d.getCantidad() - solicitudDAO.cantidadAprobada(idPedido, d.getIdProducto());
                if (aReponer > 0) {
                    productoDAO.sumarStockDisponible(d.getIdProducto(), aReponer);
                    MovimientoInventario m = new MovimientoInventario();
                    m.setIdProducto(d.getIdProducto());
                    m.setTipoMovimiento("INGRESO_DEVOLUCION_OK");
                    m.setCantidad(aReponer);
                    m.setReferenciaCodigo(pedido.getCodigoSeguimiento());
                    movimientoDAO.insertar(m);
                }
            }
            new ComprobanteDAO(con).anularPorPedido(idPedido);
        }

        pedidoDAO.actualizarEstado(idPedido, nuevoEstado, observaciones, admin.getIdUsuario());
        new HistorialAccionDAO(con).registrar(admin.getIdUsuario(), "CAMBIO_ESTADO_PEDIDO",
                pedido.getCodigoSeguimiento() + ": " + pedido.getEstado() + " -> " + nuevoEstado);
        return "Pedido " + pedido.getCodigoSeguimiento() + " actualizado a " + nuevoEstado + ".";
    }
}
