package com.cherosac.modelo;

import java.time.LocalDateTime;

/** Cliente que compra sin registro previo; se identifica por tipo y numero de documento. */
public class Cliente {

    private int idCliente;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombreRazonSocial;
    private String apellido;
    private String telefono;
    private String correo;
    private String direccion;
    private LocalDateTime fechaRegistro;

    public Cliente() {
    }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getNombreRazonSocial() { return nombreRazonSocial; }
    public void setNombreRazonSocial(String nombreRazonSocial) { this.nombreRazonSocial = nombreRazonSocial; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
