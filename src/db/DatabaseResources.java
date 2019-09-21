package db;

import java.sql.*;

public class DatabaseResources 
{        
    public static Boolean changed = false;
    public static Connection getConnection() throws SQLException
    {
        final String DATABASE_URL = "jdbc:sqlserver://localhost;databaseName=AUDIODIARY;integratedSecurity=true";
        return DriverManager.getConnection(DATABASE_URL);
    }
}
