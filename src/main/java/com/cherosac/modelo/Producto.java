package com.cherosac.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Producto del catalogo. stockDisponible es el stock de venta;
 * stockDefectuoso se mantiene separado y no se vende.
 */
public class Producto {

    private int idProducto;
    private int idCategoria;
    private String nombreProducto;
    private String descripcion;
    private String imagenUrl;
    private BigDecimal precio;
    private int stockDisponible;
    private int stockDefectuoso;
    private boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;

    /** Dato de consulta (JOIN con categorias); no es columna de la tabla. */
    private String nombreCategoria;

    public Producto() {
    }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public int getStockDisponible() { return stockDisponible; }
    public void setStockDisponible(int stockDisponible) { this.stockDisponible = stockDisponible; }

    public int getStockDefectuoso() { return stockDefectuoso; }
    public void setStockDefectuoso(int stockDefectuoso) { this.stockDefectuoso = stockDefectuoso; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }

    private BigDecimal costoUnitario;
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal valor) { costoUnitario = valor; }

}
