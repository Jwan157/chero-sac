package com.cherosac.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Registro del Kardex de inventario (tabla movimientos_inventario). */
public class MovimientoInventario {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int idMovimiento;
    private int idProducto;
    private String tipoMovimiento;
    private int cantidad;
    private String referenciaCodigo;
    private LocalDateTime fechaMovimiento;

    /** Dato de consulta (JOIN con productos). */
    private String nombreProducto;

    public MovimientoInventario() {
    }

    public String getFechaMovimientoFormateada() {
        return fechaMovimiento == null ? "" : fechaMovimiento.format(FORMATO_FECHA);
    }

    public int getIdMovimiento() { return idMovimiento; }
    public void setIdMovimiento(int idMovimiento) { this.idMovimiento = idMovimiento; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getReferenciaCodigo() { return referenciaCodigo; }
    public void setReferenciaCodigo(String referenciaCodigo) { this.referenciaCodigo = referenciaCodigo; }

    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
}
