package com.cherosac.controlador;

import com.cherosac.config.ConexionBD;
import com.cherosac.dao.ClienteDAO;
import com.cherosac.dao.ComprobanteDAO;
import com.cherosac.dao.DetallePedidoDAO;
import com.cherosac.dao.MetodoPagoDAO;
import com.cherosac.dao.MovimientoInventarioDAO;
import com.cherosac.dao.PedidoDAO;
import com.cherosac.dao.ProductoDAO;
import com.cherosac.modelo.Cliente;
import com.cherosac.modelo.ComprobanteElectronico;
import com.cherosac.modelo.DetallePedido;
import com.cherosac.modelo.MetodoPago;
import com.cherosac.modelo.MovimientoInventario;
import com.cherosac.modelo.Pedido;
import com.cherosac.modelo.Producto;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * P-02: compra de cliente invitado. En una sola transaccion registra el cliente, el pedido,
 * el detalle, descuenta stock, registra la salida en el Kardex y emite el comprobante
 * (boleta si es DNI, factura si es RUC). Precios y stock se leen de la base de datos.
 */
@WebServlet("/checkout")
public class CheckoutServlet extends BaseServlet {

    private static final int MAX_LINEAS = 20;
    private static final int MAX_CANTIDAD = 99;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        mostrarFormulario(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String error;
        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);
            try {
                String codigo = procesarCompra(req, con);
                con.commit();
                if (req.getAttribute("claveVinculo") != null) {
                    req.getSession().setAttribute("claveVinculo", req.getAttribute("claveVinculo"));
                    req.getSession().setAttribute("documentoVinculo", ((Cliente)req.getAttribute("perfilCompra")).getNumeroDocumento());
                }
                if (clienteActual(req) != null)
                    req.getSession().setAttribute("cliente",req.getAttribute("perfilCompra"));
                String documento = ((Cliente) req.getAttribute("perfilCompra")).getNumeroDocumento();
                redirigir(req, resp, "/seguimiento?codigo=" + enc(codigo) + "&documento=" + enc(documento) + "&nuevo=1",
                        null, null);
                return;
            } catch (IllegalArgumentException e) {
                con.rollback();
                error = e.getMessage();
            } catch (SQLException e) {
                con.rollback();
                log("Error al registrar el pedido", e);
                error = "No se pudo registrar el pedido. Intente nuevamente.";
            }
        } catch (SQLException e) {
            log("Sin conexion a la base de datos", e);
            error = "No hay conexión con la base de datos. Verifique que PostgreSQL esté activo.";
        }
        req.setAttribute("error", error);
        mostrarFormulario(req, resp);
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try (Connection con = ConexionBD.obtener()) {
            List<MetodoPago> metodos = new MetodoPagoDAO(con).listarActivos();
            req.setAttribute("metodos", metodos);
        } catch (SQLException e) {
            log("No se pudieron cargar los metodos de pago", e);
            if (req.getAttribute("error") == null) {
                req.setAttribute("error", "No hay conexión con la base de datos.");
            }
        }
        req.getRequestDispatcher("/checkout.jsp").forward(req, resp);
    }

    /** Valida los datos, registra la venta completa y devuelve el codigo de seguimiento. */
    private String procesarCompra(HttpServletRequest req, Connection con) throws SQLException {
        String nombres = texto(req.getParameter("nombres"));
        String apellidos = texto(req.getParameter("apellidos"));
        String tipoDocumento = texto(req.getParameter("tipoDocumento"));
        String numeroDocumento = texto(req.getParameter("numeroDocumento"));
        String direccion = texto(req.getParameter("direccion"));
        String telefono = texto(req.getParameter("telefono"));
        String correo = texto(req.getParameter("correo"));
        Integer idMetodoPago = entero(req.getParameter("idMetodoPago"));
        Map<Integer, Integer> lineas = parsearCarrito(req.getParameter("carrito"));

        Cliente cuenta = clienteActual(req);
        if (cuenta != null) {
            tipoDocumento = cuenta.getTipoDocumento();
            numeroDocumento = cuenta.getNumeroDocumento();
            correo = cuenta.getCorreo();
        }
        boolean esRuc = "RUC".equals(tipoDocumento);
        if (nombres == null || nombres.length() < 2 || nombres.length() > 70) {
            throw new IllegalArgumentException("Ingrese los nombres o la razón social (2 a 70 caracteres).");
        }
        if (!esRuc && !"DNI".equals(tipoDocumento)) {
            throw new IllegalArgumentException("Seleccione un tipo de documento válido.");
        }
        if (!documentoValido(tipoDocumento, numeroDocumento)) {
            throw new IllegalArgumentException(esRuc ? "El RUC debe tener 11 dígitos y empezar por 10 o 20." : "El DNI debe tener 8 dígitos.");
        }
        if (!esRuc && (apellidos == null || apellidos.length() < 2 || apellidos.length() > 79)) {
            throw new IllegalArgumentException("Ingrese los apellidos del titular del DNI (2 a 79 caracteres).");
        }
        if (direccion == null || direccion.length() < 5 || direccion.length() > 200) {
            throw new IllegalArgumentException("Ingrese la dirección de entrega (5 a 200 caracteres).");
        }
        if (telefono != null && !telefono.matches("[0-9+ ]{6,15}")) {
            throw new IllegalArgumentException("El teléfono no tiene un formato válido.");
        }
        if (correo != null && !correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        }
        if (idMetodoPago == null) {
            throw new IllegalArgumentException("Seleccione un método de pago.");
        }
        MetodoPago metodo = new MetodoPagoDAO(con).buscarActivoPorId(idMetodoPago);
        if (metodo == null) {
            throw new IllegalArgumentException("El método de pago seleccionado no está disponible.");
        }

        // Cliente invitado: se registra, o se actualiza si el documento ya existe
        Cliente cliente = new Cliente();
        cliente.setTipoDocumento(tipoDocumento);
        cliente.setNumeroDocumento(numeroDocumento);
        cliente.setNombreRazonSocial(nombres);
        cliente.setApellido(esRuc ? null : apellidos);
        cliente.setTelefono(telefono);
        cliente.setCorreo(correo);
        cliente.setDireccion(direccion);
        String vinculo = cuenta == null ? java.util.UUID.randomUUID().toString() : null;
        boolean nuevoInvitado = new ClienteDAO(con).guardarOActualizar(cliente,
                cuenta == null ? null : cuenta.getIdCliente(),
                vinculo == null ? null : ComprobanteElectronico.hashSha256(vinculo));
        if (nuevoInvitado) req.setAttribute("claveVinculo", vinculo);
        req.setAttribute("perfilCompra", cliente);
        int idCliente = cliente.getIdCliente();

        // Precios y stock se leen de la base de datos con bloqueo de fila
        ProductoDAO productoDAO = new ProductoDAO(con);
        List<Producto> productos = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Integer, Integer> linea : lineas.entrySet()) {
            Producto p = productoDAO.buscarPorIdConBloqueo(linea.getKey());
            if (p == null || !p.isActivo()) {
                throw new IllegalArgumentException("Un producto del carrito ya no está disponible.");
            }
            if (p.getStockDisponible() < linea.getValue()) {
                throw new IllegalArgumentException("Stock insuficiente para " + p.getNombreProducto()
                        + ". Disponible: " + p.getStockDisponible() + " unidad(es).");
            }
            total = total.add(p.getPrecio().multiply(BigDecimal.valueOf(linea.getValue())));
            productos.add(p);
        }
        total = total.setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal = ComprobanteElectronico.subtotalDesde(total);
        BigDecimal igv = total.subtract(subtotal);

        // Cabecera del pedido con codigo CH-YYYY-XXXXXX
        PedidoDAO pedidoDAO = new PedidoDAO(con);
        String codigo = Pedido.generarCodigoSeguimiento(LocalDate.now().getYear(), pedidoDAO.siguienteNumeroCodigo());
        Pedido pedido = new Pedido();
        pedido.setCodigoSeguimiento(codigo);
        pedido.setIdCliente(idCliente);
        pedido.setIdMetodoPago(idMetodoPago);
        pedido.setIdUsuario(null);
        pedido.setEstado("PENDIENTE");
        pedido.setSubtotal(subtotal);
        pedido.setIgv(igv);
        pedido.setTotal(total);
        int idPedido = pedidoDAO.insertar(pedido);

        // Detalle, descuento de stock y Kardex (SALIDA_VENTA)
        DetallePedidoDAO detalleDAO = new DetallePedidoDAO(con);
        MovimientoInventarioDAO movimientoDAO = new MovimientoInventarioDAO(con);
        for (Producto p : productos) {
            int cantidad = lineas.get(p.getIdProducto());

            DetallePedido detalle = new DetallePedido();
            detalle.setIdPedido(idPedido);
            detalle.setIdProducto(p.getIdProducto());
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(p.getPrecio());
            detalleDAO.insertar(detalle);

            if (!productoDAO.descontarStockDisponible(p.getIdProducto(), cantidad)) {
                throw new IllegalArgumentException("Stock insuficiente para " + p.getNombreProducto() + ".");
            }

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setIdProducto(p.getIdProducto());
            movimiento.setTipoMovimiento("SALIDA_VENTA");
            movimiento.setCantidad(cantidad);
            movimiento.setReferenciaCodigo(codigo);
            movimientoDAO.insertar(movimiento);
        }

        // Comprobante automatico: DNI -> BOLETA (B001), RUC -> FACTURA (F001)
        String tipo = ComprobanteElectronico.tipoPorTipoDocumento(tipoDocumento);
        String serie = ComprobanteElectronico.serieBase(tipo);
        ComprobanteDAO comprobanteDAO = new ComprobanteDAO(con);

        ComprobanteElectronico comprobante = new ComprobanteElectronico();
        comprobante.setIdPedido(idPedido);
        comprobante.setTipoComprobante(tipo);
        comprobante.setSerie(serie);
        comprobante.setNumeroCorrelativo(comprobanteDAO.siguienteCorrelativo(serie));
        comprobante.setRucDniCliente(numeroDocumento);
        comprobante.setRazonSocialCliente(esRuc ? nombres : nombres + " " + apellidos);
        comprobante.setSubtotal(subtotal);
        comprobante.setIgv18(igv);
        comprobante.setTotal(total);
        comprobante.setHashSunat(ComprobanteElectronico.hashSha256(serie + "-" + comprobante.getNumeroCorrelativo()
                + "|" + numeroDocumento + "|" + total.toPlainString() + "|" + codigo));
        comprobante.setEstadoSunat("ACEPTADO");
        comprobanteDAO.insertar(comprobante);

        return codigo;
    }

    /** Formato del carrito enviado desde el navegador: "idProducto:cantidad;idProducto:cantidad". */
    private static Map<Integer, Integer> parsearCarrito(String carrito) {
        Map<Integer, Integer> cantidades = new TreeMap<>();
        if (carrito != null) {
            for (String parte : carrito.split(";")) {
                if (parte.isBlank()) {
                    continue;
                }
                String[] par = parte.split(":");
                Integer id = par.length == 2 ? entero(par[0]) : null;
                Integer cantidad = par.length == 2 ? entero(par[1]) : null;
                if (id == null || id < 1 || cantidad == null || cantidad < 1 || cantidad > MAX_CANTIDAD) {
                    throw new IllegalArgumentException("El carrito contiene datos no válidos. Vuelva al catálogo.");
                }
                int acumulada = cantidades.getOrDefault(id, 0) + cantidad;
                if (acumulada > MAX_CANTIDAD) throw new IllegalArgumentException("Cantidad máxima por producto: " + MAX_CANTIDAD);
                cantidades.put(id, acumulada);
            }
        }
        if (cantidades.isEmpty()) {
            throw new IllegalArgumentException("Su carrito está vacío.");
        }
        if (cantidades.size() > MAX_LINEAS) {
            throw new IllegalArgumentException("El carrito supera el máximo de " + MAX_LINEAS + " productos distintos.");
        }
        return cantidades;
    }
}
