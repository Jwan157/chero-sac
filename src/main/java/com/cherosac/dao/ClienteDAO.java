package com.cherosac.dao;

import com.cherosac.modelo.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Acceso a la tabla clientes. Un cliente se identifica por su tipo y numero de documento. */
public class ClienteDAO {

    private final Connection con;

    public ClienteDAO(Connection con) {
        this.con = con;
    }

    public boolean guardarOActualizar(Cliente c, Integer cuentaId, String hashActivacion) throws SQLException {
        String sql = """
                INSERT INTO clientes (tipo_documento,numero_documento,nombre_razon_social,apellido,
                                      telefono,correo,direccion,hash_activacion)
                VALUES (?,?,?,?,?,?,?,?)
                ON CONFLICT (tipo_documento,numero_documento) DO UPDATE SET
                    nombre_razon_social=excluded.nombre_razon_social, apellido=excluded.apellido,
                    telefono=excluded.telefono, correo=excluded.correo, direccion=excluded.direccion
                WHERE NOT clientes.tiene_cuenta OR clientes.id_cliente=?
                RETURNING id_cliente,hash_activacion
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1,c.getTipoDocumento());
            ps.setString(2,c.getNumeroDocumento());
            ps.setString(3,c.getNombreRazonSocial());
            ps.setString(4,c.getApellido());
            ps.setString(5,c.getTelefono());
            ps.setString(6,c.getCorreo());
            ps.setString(7,c.getDireccion());
            ps.setString(8,hashActivacion);
            ps.setObject(9,cuentaId,java.sql.Types.INTEGER);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Inicie sesión para comprar con una cuenta registrada.");
                c.setIdCliente(rs.getInt(1));
                return hashActivacion != null && hashActivacion.equals(rs.getString(2));
            }
        }
    }

    public boolean registrar(Cliente c, String claveHash, String prueba) throws SQLException {
        String sql = """
                INSERT INTO clientes (tipo_documento,numero_documento,nombre_razon_social,apellido,
                                      correo,direccion,password_hash,tiene_cuenta)
                VALUES (?,?,?,?,?,?,?,true)
                ON CONFLICT (tipo_documento,numero_documento) DO UPDATE SET
                    nombre_razon_social=excluded.nombre_razon_social, apellido=excluded.apellido,
                    correo=excluded.correo, direccion=excluded.direccion,
                    tiene_cuenta=true, password_hash=excluded.password_hash, hash_activacion=null
                WHERE NOT clientes.tiene_cuenta AND clientes.hash_activacion=?
                RETURNING id_cliente,telefono
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1,c.getTipoDocumento());
            ps.setString(2,c.getNumeroDocumento());
            ps.setString(3,c.getNombreRazonSocial());
            ps.setString(4,c.getApellido());
            ps.setString(5,c.getCorreo());
            ps.setString(6,c.getDireccion());
            ps.setString(7,claveHash);
            ps.setString(8,prueba);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                c.setIdCliente(rs.getInt(1));
                c.setTelefono(rs.getString(2));
                return true;
            }
        }
    }

    public Cliente autenticar(String documento, String clave) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM clientes WHERE tiene_cuenta AND numero_documento=?")) {
            ps.setString(1,documento);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !org.mindrot.jbcrypt.BCrypt.checkpw(clave,rs.getString("password_hash"))) return null;
                Cliente c = new Cliente();
                c.setIdCliente(rs.getInt("id_cliente"));
                c.setTipoDocumento(rs.getString("tipo_documento"));
                c.setNumeroDocumento(rs.getString("numero_documento"));
                c.setNombreRazonSocial(rs.getString("nombre_razon_social"));
                c.setApellido(rs.getString("apellido"));
                c.setTelefono(rs.getString("telefono"));
                c.setCorreo(rs.getString("correo"));
                c.setDireccion(rs.getString("direccion"));
                return c;
            }
        }
    }
}
