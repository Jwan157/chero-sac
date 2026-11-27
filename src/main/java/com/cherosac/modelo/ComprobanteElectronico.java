package com.cherosac.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Comprobante electronico: BOLETA (DNI), FACTURA (RUC) o NOTA_CREDITO.
 * Los precios del sistema incluyen IGV (18%); el subtotal se obtiene por desglose.
 * El hash es una huella SHA-256 interna; no se transmite a SUNAT.
 */
public class ComprobanteElectronico {

    public static final BigDecimal FACTOR_IGV = new BigDecimal("1.18");

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int idComprobante;
    private int idPedido;
    private String tipoComprobante;
    private String serie;
    private int numeroCorrelativo;
    private String rucDniCliente;
    private String razonSocialCliente;
    private BigDecimal subtotal;
    private BigDecimal igv18;
    private BigDecimal total;
    private String hashSunat;
    private String estadoSunat;
    private LocalDateTime fechaEmision;

    public ComprobanteElectronico() {
    }

    /** Subtotal sin IGV a partir de un total que ya incluye IGV. */
    public static BigDecimal subtotalDesde(BigDecimal totalConIgv) {
        return totalConIgv.divide(FACTOR_IGV, 2, RoundingMode.HALF_UP);
    }

    /** Tipo de comprobante que corresponde al tipo de documento del cliente. */
    public static String tipoPorTipoDocumento(String tipoDocumento) {
        return "RUC".equals(tipoDocumento) ? "FACTURA" : "BOLETA";
    }

    /** Serie de boleta o factura segun el tipo. */
    public static String serieBase(String tipo) {
        return "FACTURA".equals(tipo) ? "F001" : "B001";
    }

    /** Serie de la nota de credito segun el comprobante original (BC01 o FC01). */
    public static String serieNotaCredito(String tipoOriginal) {
        return "FACTURA".equals(tipoOriginal) ? "FC01" : "BC01";
    }

    /** Huella SHA-256 en hexadecimal. */
    public static String hashSha256(String texto) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 no disponible", e);
        }
    }

    public String getFechaFormateada() {
        return fechaEmision == null ? "" : fechaEmision.format(FORMATO_FECHA);
    }

    /** Numero completo para mostrar, por ejemplo B001-00000012. */
    public String getNumeroCompleto() {
        return serie + "-" + String.format("%08d", numeroCorrelativo);
    }

    public int getIdComprobante() { return idComprobante; }
    public void setIdComprobante(int idComprobante) { this.idComprobante = idComprobante; }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public int getNumeroCorrelativo() { return numeroCorrelativo; }
    public void setNumeroCorrelativo(int numeroCorrelativo) { this.numeroCorrelativo = numeroCorrelativo; }

    public String getRucDniCliente() { return rucDniCliente; }
    public void setRucDniCliente(String rucDniCliente) { this.rucDniCliente = rucDniCliente; }

    public String getRazonSocialCliente() { return razonSocialCliente; }
    public void setRazonSocialCliente(String razonSocialCliente) { this.razonSocialCliente = razonSocialCliente; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getIgv18() { return igv18; }
    public void setIgv18(BigDecimal igv18) { this.igv18 = igv18; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getHashSunat() { return hashSunat; }
    public void setHashSunat(String hashSunat) { this.hashSunat = hashSunat; }

    public String getEstadoSunat() { return estadoSunat; }
    public void setEstadoSunat(String estadoSunat) { this.estadoSunat = estadoSunat; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }
}
