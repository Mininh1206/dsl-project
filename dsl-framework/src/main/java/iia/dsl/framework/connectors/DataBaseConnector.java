package iia.dsl.framework.connectors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

public class DataBaseConnector extends Connector {

    private final Optional<String> username;
    private final Optional<String> password;

    private final Connection connection;

    public DataBaseConnector(String connectionString, String username, String password) {
        this.username = Optional.ofNullable(username);
        this.password = Optional.ofNullable(password);

        try {
            if (this.username.isPresent() && this.password.isPresent()) {
                connection = DriverManager.getConnection(connectionString, this.username.get(), this.password.get());
            } else {
                connection = DriverManager.getConnection(connectionString);
            }


        } catch (SQLException e) {
            Logger.getLogger(DataBaseConnector.class.getName()).log(Level.SEVERE, "Error connecting to database", e);
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    @Override
    public Document call(Document input) throws Exception {
        // Make the conection with jdbc using connectionString
        // and if input is null throw an exception
        if (input == null) {
            throw new IllegalArgumentException("Input document cannot be null");
        }

        // Saca la consulta del input con el xpath /sql
        var xpath = "/sql";
        var xpathFactory = XPathFactory.newInstance();
        var xpathExpr = xpathFactory.newXPath().compile(xpath);
        var sqlQuery = (String) xpathExpr.evaluate(input, XPathConstants.STRING);

        // Ejecuta la consulta, insert, update, delete, select o lo que sea
        var statement = connection.createStatement();
        boolean hasResultSet = statement.execute(sqlQuery);

        if (hasResultSet) {
            // TODO: Si tiene resultado, lo transforma a XML usando XSLT
            
            return null;
        }

        return null;
    }
}
