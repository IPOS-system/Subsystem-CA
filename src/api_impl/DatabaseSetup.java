package api_impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseSetup {

    private static final String URL = "jdbc:mysql://localhost:3306/?allowMultiQueries=true";
    private static final String USER = "root";
    private static final String PASSWORD = "Liv2game!!";
    //admin prev password

    public static void initialiseDatabase() {
        try (
                Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement stmt = conn.createStatement()
        )

        {
            System.out.println(
                    DatabaseSetup.class.getClassLoader().getResource("resources/schema.sql")
            );
            InputStream input = DatabaseSetup.class.getClassLoader().getResourceAsStream("resources/schema.sql");


            if (input == null) {
                throw new RuntimeException("schema.sql not found.. make sure resources folder is marked as resource.");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            StringBuilder sql = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }

            for (String command : sql.toString().split(";")) {
                command = command.trim();
                if (!command.isEmpty()) {
                    stmt.execute(command);
                }
            }

            System.out.println("Database setup complete.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}