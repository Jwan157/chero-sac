package com.cherosac.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Periodo calendario de un reporte: semanal (lunes a domingo), quincenal (1-15 y 16-fin de mes),
 * mensual o anual. El inicio es inclusive y el fin es exclusivo.
 */
public final class Periodo {

    public static final List<String> TIPOS = List.of("SEMANAL", "QUINCENAL", "MENSUAL", "ANUAL");

    private static final String[] MESES = {
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    };
    private static final String[] DIAS = {"lun", "mar", "mié", "jue", "vie", "sáb", "dom"};
    private static final String[] MESES_CORTOS = {
        "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"
    };

    private final String tipo;
    private final LocalDate desde;
    private final LocalDate hasta;

    private Periodo(String tipo, LocalDate desde, LocalDate hasta) {
        this.tipo = tipo;
        this.desde = desde;
        this.hasta = hasta;
    }

    /** Periodo del tipo indicado que contiene la fecha de referencia. Tipo desconocido: SEMANAL. */
    public static Periodo de(String tipo, LocalDate referencia) {
        if ("QUINCENAL".equals(tipo)) {
            if (referencia.getDayOfMonth() <= 15) {
                return new Periodo(tipo, referencia.withDayOfMonth(1), referencia.withDayOfMonth(16));
            }
            LocalDate inicioMes = referencia.withDayOfMonth(1);
            return new Periodo(tipo, referencia.withDayOfMonth(16), inicioMes.plusMonths(1));
        }
        if ("MENSUAL".equals(tipo)) {
            LocalDate inicio = referencia.withDayOfMonth(1);
            return new Periodo(tipo, inicio, inicio.plusMonths(1));
        }
        if ("ANUAL".equals(tipo)) {
            LocalDate inicio = LocalDate.of(referencia.getYear(), 1, 1);
            return new Periodo(tipo, inicio, inicio.plusYears(1));
        }
        LocalDate inicio = referencia.minusDays(referencia.getDayOfWeek().getValue() - 1);
        return new Periodo("SEMANAL", inicio, inicio.plusDays(7));
    }

    /** Periodo inmediatamente anterior, para comparar ventas. */
    public Periodo anterior() {
        return de(tipo, desde.minusDays(1));
    }

    /** Periodo inmediatamente posterior. */
    public Periodo siguiente() {
        return de(tipo, hasta);
    }

    /** Unidad de agrupación de la serie: por día, o por mes en el reporte anual. */
    public String unidadSerie() {
        return "ANUAL".equals(tipo) ? "month" : "day";
    }

    /** Etiqueta de una fila de la serie según la unidad de agrupación. */
    public static String etiquetaFila(String unidad, LocalDateTime fecha) {
        if ("month".equals(unidad)) {
            return capitalizar(MESES_CORTOS[fecha.getMonthValue() - 1]) + " " + fecha.getYear();
        }
        String dia = DIAS[fecha.getDayOfWeek().getValue() - 1];
        return capitalizar(dia) + " " + String.format("%02d/%02d", fecha.getDayOfMonth(), fecha.getMonthValue());
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public LocalDateTime getDesdeTs() {
        return desde.atStartOfDay();
    }

    public LocalDateTime getHastaTs() {
        return hasta.atStartOfDay();
    }

    /** Texto legible del periodo, por ejemplo "2.ª quincena de noviembre de 2026". */
    public String getEtiqueta() {
        int anio = desde.getYear();
        String mes = MESES[desde.getMonthValue() - 1];
        switch (tipo) {
            case "QUINCENAL":
                String orden = desde.getDayOfMonth() == 1 ? "1.ª" : "2.ª";
                return orden + " quincena de " + mes + " de " + anio;
            case "MENSUAL":
                return capitalizar(mes) + " de " + anio;
            case "ANUAL":
                return "Año " + anio;
            default:
                return "Semana del " + fecha(desde) + " al " + fecha(hasta.minusDays(1));
        }
    }

    private static String fecha(LocalDate f) {
        return String.format("%02d/%02d/%d", f.getDayOfMonth(), f.getMonthValue(), f.getYear());
    }

    private static String capitalizar(String texto) {
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }
}
