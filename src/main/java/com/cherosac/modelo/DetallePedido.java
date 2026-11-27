package com.cherosac.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Linea de un pedido: producto, cantidad y precio unitario con IGV (tabla detalle_pedidos). */
public class DetallePedido {

    private int idDetalle;
    private int idPedido;
    private int idProducto;
    private int cantidad;
    private BigDecimal precioUnitario;

    /** Dato de consulta (JOIN con productos). */
    private String nombreProducto;

    public DetallePedido() {
    }

    /** Importe de la linea (precio unitario x cantidad), con IGV incluido. */
    public BigDecimal getSubtotalLinea() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
    }

    public int getIdDetalle() { return idDetalle; }
    public void setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    private String imagenUrl;
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

}
