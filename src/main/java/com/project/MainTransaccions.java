package com.project;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

public class MainTransaccions {

    public static void main(String[] args) throws SQLException {
        String basePath = System.getProperty("user.dir") + "/data/";
        String filePath = basePath + "database.db";
        ResultSet rs = null;

        // Si no hi ha l'arxiu creat, el crea i li posa dades
        File fDatabase = new File(filePath);
        if (!fDatabase.exists()) { initDatabase(filePath); }

        // Connectar (crea la BBDD si no existeix)
        Connection conn = UtilsSQLite.connect(filePath);

        // Llistar les taules
        ArrayList<String> taules = UtilsSQLite.listTables(conn);
        System.out.println(taules);

        // Demanar informació de la taula
        rs = UtilsSQLite.querySelect(conn, "SELECT * FROM warehouses;");
        ResultSetMetaData rsmd = rs.getMetaData();
        System.out.println("Informació de la taula:");
        for (int cnt = 1; cnt < rsmd.getColumnCount(); cnt = cnt + 1) { 
            String label = rsmd.getColumnLabel(cnt);
            String name = rsmd.getColumnName(cnt);
            int type = rsmd.getColumnType(cnt);
            System.out.println("    " + label + ", " + name + ", " + type);
        }

        // SELECT a la base de dades
        rs = UtilsSQLite.querySelect(conn, "SELECT * FROM warehouses;");
        System.out.println("Contingut de la taula:");
        while (rs.next()) {
            System.out.println("    " + rs.getInt("id") + ", " + rs.getString("name"));
        }

        try {
            // Iniciar la transacció
            conn.setAutoCommit(false);

            // Actualitzar una fila
            UtilsSQLite.queryUpdate(conn, "UPDATE warehouses SET name=\"MediaMarkt\" WHERE id=2;");

            // Afegir transaccions addicionals
            UtilsSQLite.queryUpdate(conn, "INSERT INTO warehouses (name) VALUES (\"Decathlon\");");

            UtilsSQLite.queryUpdate(conn, "UPDATE warehouses SET name=\"Zara\" WHERE id=1;");

            // Esborrar una fila
            UtilsSQLite.queryUpdate(conn, "DELETE FROM warehouses WHERE id=3;");

            // Fer commit de la transacció
            conn.commit();
            System.out.println("Transacció completada amb èxit.");

        } catch (SQLException e) {
            // Fer rollback en cas d'error
            if (conn != null) {
                try {
                    System.out.println("Error durant la transacció, fent rollback.");
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            // Tornar a activar l'auto commit
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }

        // SELECT a la base de dades després de la transacció
        rs = UtilsSQLite.querySelect(conn, "SELECT * FROM warehouses;");
        System.out.println("Contingut de la taula modificada:");
        while (rs.next()) {
            System.out.println("    " + rs.getInt("id") + ", " + rs.getString("name"));
        }

        // Desconnectar
        UtilsSQLite.disconnect(conn);
    }

    static void initDatabase(String filePath) {
        Connection conn = UtilsSQLite.connect(filePath);

        // Esborrar la taula (per si existeix)
        UtilsSQLite.queryUpdate(conn, "DROP TABLE IF EXISTS warehouses;");

        // Crear una nova taula
        UtilsSQLite.queryUpdate(conn, "CREATE TABLE IF NOT EXISTS warehouses ("
                                    + " id integer PRIMARY KEY AUTOINCREMENT,"
                                    + " name text NOT NULL);");

        // Afegir elements a una taula
        UtilsSQLite.queryUpdate(conn, "INSERT INTO warehouses (name) VALUES (\"Amazon\");");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO warehouses (name) VALUES (\"El Corte Inglés\");");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO warehouses (name) VALUES (\"Mecalux\");");

        UtilsSQLite.disconnect(conn);
    }
}
