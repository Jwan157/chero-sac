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
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * Genera en PDF la boleta, factura o nota de credito de un pedido.
 * Parametros: codigo (pedido), documento (cliente) e id (comprobante).
 */
@WebServlet("/comprobante-pdf")
public class ComprobantePdfServlet extends BaseServlet {

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
            Pedido pedido = new PedidoDAO(con).buscarPorCodigo(codigo.toUpperCase());
            ComprobanteElectronico comprobante = new ComprobanteDAO(con).buscarPorId(idComprobante);
            if (pedido == null || comprobante == null
                    || !puedeConsultar(req, pedido, documento)
                    || comprobante.getIdPedido() != pedido.getIdPedido()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            List<DetallePedido> detalles = new DetallePedidoDAO(con).listarPorPedido(pedido.getIdPedido());
            byte[] pdf = generarPdf(pedido, comprobante, detalles);

            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition",
                    "inline; filename=\"" + comprobante.getSerie() + "-" + comprobante.getNumeroCorrelativo() + ".pdf\"");
            resp.setContentLength(pdf.length);
            resp.getOutputStream().write(pdf);
        } catch (SQLException | DocumentException e) {
            log("Error al generar el comprobante PDF", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static byte[] generarPdf(Pedido pedido, ComprobanteElectronico c, List<DetallePedido> detalles)
            throws DocumentException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter.getInstance(doc, salida);
        doc.open();

        Font titulo = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font normal = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font negrita = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font pequena = new Font(Font.HELVETICA, 8, Font.NORMAL);

        doc.add(new Paragraph("CHERO.SAC", titulo));
        doc.add(new Paragraph("Catálogo de Hardware, Equipos y Receptores Originales", normal));
        doc.add(new Paragraph(" ", normal));
        doc.add(new Paragraph(etiquetaTipo(c.getTipoComprobante()) + "   N° " + c.getNumeroCompleto(), negrita));
        doc.add(new Paragraph(" ", normal));

        PdfPTable datos = new PdfPTable(2);
        datos.setWidthPercentage(100);
        fila(datos, "Fecha de emisión", c.getFechaFormateada(), negrita, normal);
        String etiquetaDocumento = c.getRucDniCliente().length() == 11 ? "RUC" : "DNI";
        fila(datos, etiquetaDocumento, c.getRucDniCliente(), negrita, normal);
        fila(datos, "Cliente", c.getRazonSocialCliente(), negrita, normal);
        fila(datos, "Pedido de referencia", pedido.getCodigoSeguimiento(), negrita, normal);
        fila(datos, "Método de pago", pedido.getMetodoPago(), negrita, normal);
        doc.add(datos);
        doc.add(new Paragraph(" ", normal));

        if ("NOTA_CREDITO".equals(c.getTipoComprobante())) {
            doc.add(new Paragraph("Nota de crédito por devolución aprobada sobre el pedido "
                    + pedido.getCodigoSeguimiento() + ".", normal));
        } else {
            PdfPTable items = new PdfPTable(4);
            items.setWidthPercentage(100);
            items.setWidths(new float[] {4f, 1f, 1.6f, 1.6f});
            celda(items, "Producto", negrita, true);
            celda(items, "Cant.", negrita, true);
            celda(items, "P. unit. (S/)", negrita, true);
            celda(items, "Importe (S/)", negrita, true);
            for (DetallePedido d : detalles) {
                celda(items, d.getNombreProducto(), normal, false);
                celda(items, String.valueOf(d.getCantidad()), normal, false);
                celda(items, dinero(d.getPrecioUnitario()), normal, false);
                celda(items, dinero(d.getSubtotalLinea()), normal, false);
            }
            doc.add(items);
        }
        doc.add(new Paragraph(" ", normal));

        PdfPTable totales = new PdfPTable(2);
        totales.setWidthPercentage(50);
        totales.setHorizontalAlignment(Element.ALIGN_RIGHT);
        fila(totales, "Subtotal (sin IGV)", "S/ " + dinero(c.getSubtotal()), normal, normal);
        fila(totales, "IGV (18%)", "S/ " + dinero(c.getIgv18()), normal, normal);
        fila(totales, "TOTAL", "S/ " + dinero(c.getTotal()), negrita, negrita);
        doc.add(totales);

        doc.add(new Paragraph(" ", normal));
        doc.add(new Paragraph("Hash de control (simulado): " + c.getHashSunat(), pequena));
        doc.add(new Paragraph("Documento de prueba sin validez tributaria. "
                + "La transmisión a SUNAT no está implementada.", pequena));
        doc.close();
        return salida.toByteArray();
    }

    private static String etiquetaTipo(String tipo) {
        if ("FACTURA".equals(tipo)) {
            return "FACTURA ELECTRÓNICA";
        }
        if ("NOTA_CREDITO".equals(tipo)) {
            return "NOTA DE CRÉDITO ELECTRÓNICA";
        }
        return "BOLETA DE VENTA ELECTRÓNICA";
    }

    private static String dinero(BigDecimal valor) {
        return String.format(Locale.US, "%,.2f", valor);
    }

    private static void fila(PdfPTable tabla, String etiqueta, String valor, Font fuenteEtiqueta, Font fuenteValor) {
        celda(tabla, etiqueta, fuenteEtiqueta, false);
        celda(tabla, valor, fuenteValor, false);
    }

    private static void celda(PdfPTable tabla, String contenido, Font fuente, boolean cabecera) {
        PdfPCell celda = new PdfPCell(new Phrase(contenido == null ? "" : contenido, fuente));
        celda.setPadding(5f);
        if (cabecera) {
            celda.setBackgroundColor(new java.awt.Color(235, 238, 243));
        }
        tabla.addCell(celda);
    }
}
