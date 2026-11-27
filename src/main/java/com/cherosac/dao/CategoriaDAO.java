package com.cherosac.dao;

import com.cherosac.modelo.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla categorias. */
public class CategoriaDAO {

    private final Connection con;

    public CategoriaDAO(Connection con) {
        this.con = con;
    }

    public List<Categoria> listarActivas() throws SQLException {
        String sql = "SELECT id_categoria, nombre_categoria, descripcion, activo "
                + "FROM categorias WHERE activo = TRUE ORDER BY nombre_categoria";
        List<Categoria> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setIdCategoria(rs.getInt("id_categoria"));
                c.setNombreCategoria(rs.getString("nombre_categoria"));
                c.setDescripcion(rs.getString("descripcion"));
                c.setActivo(rs.getBoolean("activo"));
                lista.add(c);
            }
        }
        return lista;
    }
}
