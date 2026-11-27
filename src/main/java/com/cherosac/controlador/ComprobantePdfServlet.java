package com.cherosac.controlador;

import com.cherosac.config.ConexionBD;
import com.cherosac.dao.ComprobanteDAO;
import com.cherosac.dao.DetallePedidoDAO;
import com.cherosac.dao.PedidoDAO;
import com.cherosac.modelo.ComprobanteElectronico;
import com.cherosac.modelo.DetallePedido;
import com.cherosac.modelo.Pedido;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * Genera un PDF de presentación formal para boletas, facturas y notas de crédito internas.
 * Los archivos son documentos demostrativos: no se transmiten ni son autorizados por SUNAT.
 */
@WebServlet("/comprobante-pdf")
public class ComprobantePdfServlet extends BaseServlet {

    private static final Color CARBON = new Color(13, 15, 18);
    private static final Color ROJO = new Color(217, 27, 56);
    private static final Color TEXTO = new Color(38, 43, 52);
    private static final Color GRIS = new Color(102, 110, 122);
    private static final Color LINEA = new Color(222, 226, 232);
    private static final Color FONDO = new Color(246, 247, 249);
    private static final Color ROJO_CLARO = new Color(255, 241, 244);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String codigo = texto(req.getParameter("codigo"));
        String documento = texto(req.getParameter("documento"));
        Integer idComprobante = entero(req.getParameter("id"));
        if (codigo == null || (documento == null && clienteActual(req) == null) || idComprobante == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try (Connection con = ConexionBD.obtener()) {
            Pedido pedido = new PedidoDAO(con).buscarPorCodigo(codigo.toUpperCase(Locale.ROOT));
            ComprobanteElectronico comprobante = new ComprobanteDAO(con).buscarPorId(idComprobante);
            if (pedido == null || comprobante == null
                    || !puedeConsultar(req, pedido, documento)
                    || comprobante.getIdPedido() != pedido.getIdPedido()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            List<DetallePedido> detalles = new DetallePedidoDAO(con).listarPorPedido(pedido.getIdPedido());
            byte[] pdf = generarPdf(pedido, comprobante, detalles);

            resp.resetBuffer();
            resp.setContentType("application/pdf");
            resp.setHeader("X-Content-Type-Options", "nosniff");
            resp.setHeader("Content-Disposition", "inline; filename=\"CHERO-"
                    + comprobante.getNumeroCompleto() + ".pdf\"");
            resp.setContentLength(pdf.length);
            resp.getOutputStream().write(pdf);
        } catch (SQLException | DocumentException e) {
            log("Error al generar el PDF del documento interno", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "No se pudo generar el documento. Revise el registro del pedido.");
        }
    }

    private static byte[] generarPdf(Pedido pedido, ComprobanteElectronico comprobante,
                                     List<DetallePedido> detalles) throws DocumentException, IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 38, 38, 38, 42);
        PdfWriter.getInstance(documento, salida);
        documento.addTitle(etiquetaTipo(comprobante.getTipoComprobante()) + " " + comprobante.getNumeroCompleto());
        documento.addSubject("Registro interno de venta CHERO.SAC");
        documento.addAuthor("CHERO.SAC");
        documento.open();

        // WinAnsi permite representar correctamente los caracteres españoles en las fuentes PDF base.
        BaseFont base = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        Font marca = new Font(base, 20, Font.BOLD, Color.WHITE);
        Font lema = new Font(base, 8, Font.NORMAL, new Color(220, 224, 230));
        Font tituloDocumento = new Font(base, 11, Font.BOLD, Color.WHITE);
        Font numeroDocumento = new Font(base, 14, Font.BOLD, Color.WHITE);
        Font seccion = new Font(base, 9, Font.BOLD, ROJO);
        Font normal = new Font(base, 9, Font.NORMAL, TEXTO);
        Font normalNegrita = new Font(base, 9, Font.BOLD, TEXTO);
        Font pequena = new Font(base, 7.5f, Font.NORMAL, GRIS);
        Font tablaCabecera = new Font(base, 8, Font.BOLD, Color.WHITE);
        Font totalFont = new Font(base, 12, Font.BOLD, ROJO);

        // Cabecera de marca y tipo de documento.
        PdfPTable cabecera = new PdfPTable(new float[]{58f, 42f});
        cabecera.setWidthPercentage(100);
        cabecera.setSpacingAfter(12f);

        PdfPCell bloqueMarca = new PdfPCell();
        bloqueMarca.setBackgroundColor(CARBON);
        bloqueMarca.setBorder(Rectangle.NO_BORDER);
        bloqueMarca.setPadding(15f);
        bloqueMarca.addElement(new Paragraph("CHERO.SAC", marca));
        bloqueMarca.addElement(new Paragraph("Catálogo de Hardware, Equipos y Receptores Originales", lema));
        bloqueMarca.addElement(new Paragraph("TIENDA DE TECNOLOGÍA Y HARDWARE", lema));
        cabecera.addCell(bloqueMarca);

        PdfPCell bloqueDocumento = new PdfPCell();
        bloqueDocumento.setBackgroundColor(ROJO);
        bloqueDocumento.setBorder(Rectangle.NO_BORDER);
        bloqueDocumento.setPadding(12f);
        bloqueDocumento.setHorizontalAlignment(Element.ALIGN_CENTER);
        bloqueDocumento.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph tipo = new Paragraph(etiquetaTipo(comprobante.getTipoComprobante()), tituloDocumento);
        tipo.setAlignment(Element.ALIGN_CENTER);
        bloqueDocumento.addElement(tipo);
        Paragraph numero = new Paragraph(comprobante.getNumeroCompleto(), numeroDocumento);
        numero.setAlignment(Element.ALIGN_CENTER);
        numero.setSpacingBefore(5f);
        bloqueDocumento.addElement(numero);
        Paragraph interno = new Paragraph("DOCUMENTO INTERNO · DEMOSTRACIÓN", new Font(base, 7, Font.BOLD, Color.WHITE));
        interno.setAlignment(Element.ALIGN_CENTER);
        interno.setSpacingBefore(5f);
        bloqueDocumento.addElement(interno);
        cabecera.addCell(bloqueDocumento);
        documento.add(cabecera);

        // Aviso de estado visible desde el inicio del documento.
        PdfPTable aviso = new PdfPTable(1);
        aviso.setWidthPercentage(100);
        aviso.setSpacingAfter(10f);
        String estadoRegistro = "ANULADO".equals(comprobante.getEstadoSunat())
                ? "REGISTRO DE DEMOSTRACIÓN ANULADO"
                : "REGISTRO INTERNO DE VENTA · NO TRANSMITIDO A SUNAT";
        PdfPCell avisoCelda = new PdfPCell(new Phrase(estadoRegistro,
                new Font(base, 8, Font.BOLD, "ANULADO".equals(comprobante.getEstadoSunat()) ? ROJO : new Color(104, 82, 31))));
        avisoCelda.setBackgroundColor("ANULADO".equals(comprobante.getEstadoSunat()) ? ROJO_CLARO : new Color(255, 248, 226));
        avisoCelda.setBorder(Rectangle.NO_BORDER);
        avisoCelda.setPadding(8f);
        aviso.addCell(avisoCelda);
        documento.add(aviso);

        // Información del emisor y de la operación. No se inventan datos tributarios.
        PdfPTable datosPrincipales = new PdfPTable(new float[]{50f, 50f});
        datosPrincipales.setWidthPercentage(100);
        datosPrincipales.setSpacingAfter(10f);
        datosPrincipales.addCell(celdaBloque("DATOS DEL EMISOR", new String[]{
                "CHERO.SAC",
                "Catálogo de Hardware, Equipos y Receptores Originales",
                "Identificación tributaria: pendiente de configuración"
        }, seccion, normal, normalNegrita));
        datosPrincipales.addCell(celdaBloque("DATOS DE LA OPERACIÓN", new String[]{
                "Pedido: " + pedido.getCodigoSeguimiento(),
                "Fecha de emisión: " + comprobante.getFechaFormateada(),
                "Medio de pago: " + textoSeguro(pedido.getMetodoPago())
        }, seccion, normal, normalNegrita));
        documento.add(datosPrincipales);

        PdfPTable cliente = new PdfPTable(new float[]{70f, 30f});
        cliente.setWidthPercentage(100);
        cliente.setSpacingAfter(12f);
        PdfPCell tituloCliente = new PdfPCell(new Phrase("DATOS DEL CLIENTE", seccion));
        tituloCliente.setColspan(2);
        tituloCliente.setBackgroundColor(FONDO);
        tituloCliente.setBorderColor(LINEA);
        tituloCliente.setPadding(7f);
        cliente.addCell(tituloCliente);
        cliente.addCell(celdaDato("Nombre / razón social", comprobante.getRazonSocialCliente(), normalNegrita, normal));
        String tipoDocumento = comprobante.getRucDniCliente() != null && comprobante.getRucDniCliente().length() == 11
                ? "RUC" : "DNI";
        cliente.addCell(celdaDato(tipoDocumento, comprobante.getRucDniCliente(), normalNegrita, normal));
        documento.add(cliente);

        Paragraph tituloDetalle = new Paragraph("DETALLE DE LOS PRODUCTOS", seccion);
        tituloDetalle.setSpacingAfter(5f);
        documento.add(tituloDetalle);

        PdfPTable items = new PdfPTable(new float[]{7f, 48f, 10f, 17f, 18f});
        items.setWidthPercentage(100);
        items.setWidths(new float[]{7f, 48f, 10f, 17f, 18f});
        items.setHeaderRows(1);
        items.setSpacingAfter(12f);
        String[] encabezados = {"N.º", "Descripción", "Cant.", "P. unitario", "Importe"};
        for (String encabezado : encabezados) {
            PdfPCell cell = new PdfPCell(new Phrase(encabezado, tablaCabecera));
            cell.setBackgroundColor(CARBON);
            cell.setBorderColor(CARBON);
            cell.setPadding(7f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            items.addCell(cell);
        }

        if ("NOTA_CREDITO".equals(comprobante.getTipoComprobante())) {
            agregarFila(items, "1", "Ajuste por devolución aprobada vinculada al pedido "
                    + pedido.getCodigoSeguimiento(), "1", dinero(comprobante.getTotal()), dinero(comprobante.getTotal()), normal);
        } else {
            int correlativo = 1;
            for (DetallePedido detalle : detalles) {
                agregarFila(items, String.valueOf(correlativo++), detalle.getNombreProducto(),
                        String.valueOf(detalle.getCantidad()), dinero(detalle.getPrecioUnitario()),
                        dinero(detalle.getSubtotalLinea()), normal);
            }
        }
        documento.add(items);

        // Desglose de importes.
        PdfPTable totales = new PdfPTable(new float[]{62f, 38f});
        totales.setWidthPercentage(48);
        totales.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totales.setSpacingBefore(2f);
        totales.setSpacingAfter(15f);
        agregarTotal(totales, "Base imponible", "S/ " + dinero(comprobante.getSubtotal()), normal, false);
        agregarTotal(totales, "IGV (18 %)", "S/ " + dinero(comprobante.getIgv18()), normal, false);
        PdfPCell etiquetaTotal = new PdfPCell(new Phrase("TOTAL", new Font(base, 10, Font.BOLD, Color.WHITE)));
        etiquetaTotal.setBackgroundColor(CARBON);
        etiquetaTotal.setBorderColor(CARBON);
        etiquetaTotal.setPadding(9f);
        PdfPCell valorTotal = new PdfPCell(new Phrase("S/ " + dinero(comprobante.getTotal()), totalFont));
        valorTotal.setBackgroundColor(ROJO_CLARO);
        valorTotal.setBorderColor(LINEA);
        valorTotal.setPadding(9f);
        valorTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totales.addCell(etiquetaTotal);
        totales.addCell(valorTotal);
        documento.add(totales);

        PdfPTable referencia = new PdfPTable(1);
        referencia.setWidthPercentage(100);
        referencia.setSpacingBefore(4f);
        String huella = comprobante.getHashSunat();
        String referenciaCorta = huella == null || huella.isBlank()
                ? "No disponible" : huella.substring(0, Math.min(24, huella.length())).toUpperCase(Locale.ROOT);
        PdfPCell referenciaCelda = new PdfPCell();
        referenciaCelda.setBackgroundColor(FONDO);
        referenciaCelda.setBorderColor(LINEA);
        referenciaCelda.setPadding(8f);
        referenciaCelda.addElement(new Paragraph("REFERENCIA DE CONTROL INTERNO", seccion));
        referenciaCelda.addElement(new Paragraph("Código: " + referenciaCorta, pequena));
        referenciaCelda.addElement(new Paragraph("Este documento registra una operación dentro del sistema CHERO.SAC.", pequena));
        referencia.addCell(referenciaCelda);
        documento.add(referencia);

        Paragraph avisoLegal = new Paragraph(
                "IMPORTANTE: Documento de demostración académica. No constituye una boleta ni factura electrónica "
                + "autorizada por SUNAT, no acredita efectos tributarios y no ha sido enviado a los sistemas de SUNAT. "
                + "Los datos tributarios del emisor deben configurarse antes de cualquier uso comercial.",
                new Font(base, 7.5f, Font.BOLD, GRIS));
        avisoLegal.setAlignment(Element.ALIGN_JUSTIFIED);
        avisoLegal.setSpacingBefore(10f);
        documento.add(avisoLegal);

        Paragraph pie = new Paragraph("CHERO.SAC · Sistema de gestión y ventas · Gracias por su preferencia.",
                new Font(base, 8, Font.BOLD, CARBON));
        pie.setAlignment(Element.ALIGN_CENTER);
        pie.setSpacingBefore(14f);
        documento.add(pie);

        documento.close();
        return salida.toByteArray();
    }

    private static PdfPCell celdaBloque(String titulo, String[] lineas, Font fuenteTitulo,
                                        Font fuenteNormal, Font fuenteNegrita) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(FONDO);
        cell.setBorderColor(LINEA);
        cell.setPadding(9f);
        cell.addElement(new Paragraph(titulo, fuenteTitulo));
        for (int i = 0; i < lineas.length; i++) {
            cell.addElement(new Paragraph(lineas[i], i == 0 ? fuenteNegrita : fuenteNormal));
        }
        return cell;
    }

    private static PdfPCell celdaDato(String etiqueta, String valor, Font fuenteEtiqueta, Font fuenteValor) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(LINEA);
        cell.setPadding(8f);
        cell.addElement(new Paragraph(etiqueta, new Font(fuenteEtiqueta.getBaseFont(), 7.5f, Font.NORMAL, GRIS)));
        cell.addElement(new Paragraph(textoSeguro(valor), fuenteValor));
        return cell;
    }

    private static void agregarFila(PdfPTable tabla, String numero, String descripcion, String cantidad,
                                    String unitario, String importe, Font fuente) {
        String[] valores = {numero, textoSeguro(descripcion), cantidad, "S/ " + unitario, "S/ " + importe};
        for (int i = 0; i < valores.length; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(valores[i], fuente));
            cell.setBorderColor(LINEA);
            cell.setPadding(7f);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            if (i == 0 || i == 2) cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            if (i >= 3) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tabla.addCell(cell);
        }
    }

    private static void agregarTotal(PdfPTable tabla, String etiqueta, String valor, Font fuente, boolean resaltar) {
        PdfPCell izquierda = new PdfPCell(new Phrase(etiqueta, fuente));
        izquierda.setBorderColor(LINEA);
        izquierda.setPadding(6f);
        PdfPCell derecha = new PdfPCell(new Phrase(valor, fuente));
        derecha.setBorderColor(LINEA);
        derecha.setPadding(6f);
        derecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (resaltar) {
            izquierda.setBackgroundColor(ROJO_CLARO);
            derecha.setBackgroundColor(ROJO_CLARO);
        }
        tabla.addCell(izquierda);
        tabla.addCell(derecha);
    }

    private static String etiquetaTipo(String tipo) {
        if ("FACTURA".equals(tipo)) return "FACTURA INTERNA DE VENTA";
        if ("NOTA_CREDITO".equals(tipo)) return "NOTA DE CRÉDITO INTERNA";
        return "BOLETA INTERNA DE VENTA";
    }

    private static String textoSeguro(String valor) {
        return valor == null || valor.isBlank() ? "No consignado" : valor.trim();
    }

    private static String dinero(BigDecimal valor) {
        return String.format(Locale.US, "%,.2f", valor == null ? BigDecimal.ZERO : valor);
    }
}
