package com.cherosac.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Auditoria: accion realizada por un administrador (tabla historial_acciones). */
public class HistorialAccion {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int idHistorial;
    private int idUsuario;
    private String accion;
    private String detalle;
    private LocalDateTime fechaAccion;

    /** Dato de consulta (JOIN con usuarios). */
    private String nombreUsuario;

    public HistorialAccion() {
    }

    public String getFechaAccionFormateada() {
        return fechaAccion == null ? "" : fechaAccion.format(FORMATO_FECHA);
    }

    public int getIdHistorial() { return idHistorial; }
    public void setIdHistorial(int idHistorial) { this.idHistorial = idHistorial; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public LocalDateTime getFechaAccion() { return fechaAccion; }
    public void setFechaAccion(LocalDateTime fechaAccion) { this.fechaAccion = fechaAccion; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
}
