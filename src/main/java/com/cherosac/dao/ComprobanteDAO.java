package com.cherosac.dao;

import com.cherosac.modelo.ComprobanteElectronico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla comprobantes_electronicos (boletas, facturas y notas de credito). */
public class ComprobanteDAO {

    private static final String SELECT_BASE =
            "SELECT id_comprobante, id_pedido, tipo_comprobante, serie, numero_correlativo, "
          + "ruc_dni_cliente, razon_social_cliente, subtotal, igv_18, total, hash_sunat, "
          + "estado_sunat, fecha_emision FROM comprobantes_electronicos ";

    private final Connection con;

    public ComprobanteDAO(Connection con) {
        this.con = con;
    }

    /**
     * Siguiente correlativo de una serie. Usa un bloqueo consultivo de la transaccion
     * para evitar numeros repetidos cuando hay ventas simultaneas.
     */
    public int siguienteCorrelativo(String serie) throws SQLException {
        try (PreparedStatement lock = con.prepareStatement("SELECT pg_advisory_xact_lock(hashtext(?))")) {
            lock.setString(1, "comprobante-" + serie);
            try (ResultSet rs = lock.executeQuery()) {
                rs.next();
            }
        }
        String sql = "SELECT COALESCE(MAX(numero_correlativo), 0) + 1 "
                + "FROM comprobantes_electronicos WHERE serie = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, serie);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void insertar(ComprobanteElectronico c) throws SQLException {
        String sql = "INSERT INTO comprobantes_electronicos (id_pedido, tipo_comprobante, serie, "
                + "numero_correlativo, ruc_dni_cliente, razon_social_cliente, subtotal, igv_18, total, "
                + "hash_sunat, estado_sunat) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.getIdPedido());
            ps.setString(2, c.getTipoComprobante());
            ps.setString(3, c.getSerie());
            ps.setInt(4, c.getNumeroCorrelativo());
            ps.setString(5, c.getRucDniCliente());
            ps.setString(6, c.getRazonSocialCliente());
            ps.setBigDecimal(7, c.getSubtotal());
            ps.setBigDecimal(8, c.getIgv18());
            ps.setBigDecimal(9, c.getTotal());
            ps.setString(10, c.getHashSunat());
            ps.setString(11, c.getEstadoSunat());
            ps.executeUpdate();
        }
    }

    public List<ComprobanteElectronico> listarPorPedido(int idPedido) throws SQLException {
        String sql = SELECT_BASE + "WHERE id_pedido = ? ORDER BY fecha_emision, id_comprobante";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            return ejecutarListado(ps);
        }
    }

    public ComprobanteElectronico buscarPorId(int idComprobante) throws SQLException {
        String sql = SELECT_BASE + "WHERE id_comprobante = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idComprobante);
            List<ComprobanteElectronico> lista = ejecutarListado(ps);
            return lista.isEmpty() ? null : lista.get(0);
        }
    }

    /** Marca como ANULADO las boletas y facturas de un pedido cancelado (las notas se conservan). */
    public void anularPorPedido(int idPedido) throws SQLException {
        String sql = "UPDATE comprobantes_electronicos SET estado_sunat = 'ANULADO' "
                + "WHERE id_pedido = ? AND tipo_comprobante <> 'NOTA_CREDITO'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ps.executeUpdate();
        }
    }

    private List<ComprobanteElectronico> ejecutarListado(PreparedStatement ps) throws SQLException {
        List<ComprobanteElectronico> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ComprobanteElectronico c = new ComprobanteElectronico();
                c.setIdComprobante(rs.getInt("id_comprobante"));
                c.setIdPedido(rs.getInt("id_pedido"));
                c.setTipoComprobante(rs.getString("tipo_comprobante"));
                c.setSerie(rs.getString("serie"));
                c.setNumeroCorrelativo(rs.getInt("numero_correlativo"));
                c.setRucDniCliente(rs.getString("ruc_dni_cliente"));
                c.setRazonSocialCliente(rs.getString("razon_social_cliente"));
                c.setSubtotal(rs.getBigDecimal("subtotal"));
                c.setIgv18(rs.getBigDecimal("igv_18"));
                c.setTotal(rs.getBigDecimal("total"));
                c.setHashSunat(rs.getString("hash_sunat"));
                c.setEstadoSunat(rs.getString("estado_sunat"));
                c.setFechaEmision(rs.getObject("fecha_emision", java.time.LocalDateTime.class));
                lista.add(c);
            }
        }
        return lista;
    }

    public List<ComprobanteElectronico> listarPorCliente(int idCliente) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT_BASE +
                "WHERE id_pedido IN (SELECT id_pedido FROM pedidos WHERE id_cliente=?) ORDER BY fecha_emision DESC")) {
            ps.setInt(1,idCliente);
            return ejecutarListado(ps);
        }
    }

}
