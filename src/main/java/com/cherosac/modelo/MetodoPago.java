package com.cherosac.modelo;

/** Metodo de pago aceptado: Efectivo, Tarjeta, Transferencia o Yape/Plin. */
public class MetodoPago {

    private int idMetodoPago;
    private String tipoPago;
    private boolean activo;

    public MetodoPago() {
    }

    public int getIdMetodoPago() { return idMetodoPago; }
    public void setIdMetodoPago(int idMetodoPago) { this.idMetodoPago = idMetodoPago; }

    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
