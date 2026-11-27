package com.cherosac.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Solicitud postventa del cliente (devolucion, cambio o garantia).
 * Al aprobarla el administrador indica la condicion del producto:
 * BUEN_ESTADO o DEFECTUOSO.
 */
public class SolicitudDevolucion {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int idSolicitud;
    private int idPedido;
    private int idProducto;
    private int cantidadDevuelta;
    private String tipoSolicitud;
    private String motivo;
    private String estado;
    private String condicionProducto;
    private Integer idUsuario;
    private String respuestaAdmin;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaResolucion;

    /* Datos de consulta (JOIN con productos y pedidos). */
    private String nombreProducto;
    private String codigoPedido;

    public SolicitudDevolucion() {
    }

    public String getFechaSolicitudFormateada() {
        return fechaSolicitud == null ? "" : fechaSolicitud.format(FORMATO_FECHA);
    }

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public int getCantidadDevuelta() { return cantidadDevuelta; }
    public void setCantidadDevuelta(int cantidadDevuelta) { this.cantidadDevuelta = cantidadDevuelta; }

    public String getTipoSolicitud() { return tipoSolicitud; }
    public void setTipoSolicitud(String tipoSolicitud) { this.tipoSolicitud = tipoSolicitud; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCondicionProducto() { return condicionProducto; }
    public void setCondicionProducto(String condicionProducto) { this.condicionProducto = condicionProducto; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getRespuestaAdmin() { return respuestaAdmin; }
    public void setRespuestaAdmin(String respuestaAdmin) { this.respuestaAdmin = respuestaAdmin; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getCodigoPedido() { return codigoPedido; }
    public void setCodigoPedido(String codigoPedido) { this.codigoPedido = codigoPedido; }
}
