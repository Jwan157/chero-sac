package com.cherosac.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Punto unico de acceso a PostgreSQL.
 *
 * Valores por defecto (desarrollo local):
 *   URL      jdbc:postgresql://localhost:5432/chero_sac
 *   Usuario  postgres
 *   Clave    postgres
 *
 * Se pueden sobrescribir con variables de entorno o propiedades de sistema:
 *   CHERO_DB_URL / cherosac.db.url
 *   CHERO_DB_USER / cherosac.db.user
 *   CHERO_DB_PASSWORD / cherosac.db.password
 */
public final class ConexionBD {

    private static final String URL = leer("cherosac.db.url", "CHERO_DB_URL",
            "jdbc:postgresql://localhost:5432/chero_sac");
    private static final String USUARIO = leer("cherosac.db.user", "CHERO_DB_USER", "postgres");
    private static final String CLAVE = leer("cherosac.db.password", "CHERO_DB_PASSWORD", "postgres");

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("No se encontro el driver de PostgreSQL: " + e.getMessage());
        }
    }

    private ConexionBD() {
    }

    /**
     * Abre una nueva conexion. Quien la solicita debe cerrarla (try-with-resources).
     */
    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    private static String leer(String propiedad, String variable, String valorPorDefecto) {
        String valor = System.getProperty(propiedad);
        if (valor == null || valor.isBlank()) {
            valor = System.getenv(variable);
        }
        return (valor == null || valor.isBlank()) ? valorPorDefecto : valor.trim();
    }
}
