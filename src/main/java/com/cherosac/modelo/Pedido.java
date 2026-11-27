package com.cherosac.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Pedido de compra realizado por un cliente invitado (tabla pedidos). */
public class Pedido {

    public static final List<String> ESTADOS =
            List.of("PENDIENTE", "CONFIRMADO", "ENVIADO", "ENTREGADO", "CANCELADO");

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int idPedido;
    private String codigoSeguimiento;
    private int idCliente;
    private int idMetodoPago;
    private Integer idUsuario;
    private LocalDateTime fechaPedido;
    private String estado;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private String observaciones;
    private boolean demostracion;

    /* Datos de consulta (JOIN con clientes y metodos_pago); no son columnas de pedidos. */
    private String clienteNombre;
    private String clienteTipoDocumento;
    private String clienteNumeroDocumento;
    private String metodoPago;

    public Pedido() {
    }

    /**
     * Construye el codigo de seguimiento con el formato CH-YYYY-XXXXXX.
     */
    public static String generarCodigoSeguimiento(int anio, long correlativo) {
        return String.format("CH-%d-%06d", anio, correlativo);
    }

    public String getFechaFormateada() {
        return fechaPedido == null ? "" : fechaPedido.format(FORMATO_FECHA);
    }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public String getCodigoSeguimiento() { return codigoSeguimiento; }
    public void setCodigoSeguimiento(String codigoSeguimiento) { this.codigoSeguimiento = codigoSeguimiento; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public int getIdMetodoPago() { return idMetodoPago; }
    public void setIdMetodoPago(int idMetodoPago) { this.idMetodoPago = idMetodoPago; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public LocalDateTime getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDateTime fechaPedido) { this.fechaPedido = fechaPedido; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public boolean isDemostracion() { return demostracion; }
    public void setDemostracion(boolean demostracion) { this.demostracion = demostracion; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteTipoDocumento() { return clienteTipoDocumento; }
    public void setClienteTipoDocumento(String clienteTipoDocumento) { this.clienteTipoDocumento = clienteTipoDocumento; }

    public String getClienteNumeroDocumento() { return clienteNumeroDocumento; }
    public void setClienteNumeroDocumento(String clienteNumeroDocumento) { this.clienteNumeroDocumento = clienteNumeroDocumento; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    private boolean clienteConCuenta;
    public boolean isClienteConCuenta() { return clienteConCuenta; }
    public void setClienteConCuenta(boolean valor) { this.clienteConCuenta = valor; }

}
