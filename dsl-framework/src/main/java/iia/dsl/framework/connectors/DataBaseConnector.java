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

import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.Port;
import iia.dsl.framework.ports.RequestPort;

/**
 * Connector para la interacción con bases de datos SQL mediante JDBC.
 * Permite ejecutar consultas SQL definidas dinámicamente en el mensaje de
 * entrada.
 * 
 * Los documentos de entrada deben tener la siguiente estructura:
 * <sql>SELECT * FROM users WHERE id = 1</sql>
 * 
 * La salida será un documento XML con la estructura:
 * <resultset>
 * <row>
 * <column_name>value</column_name>
 * ...
 * </row>
 * ...
 * </resultset>
 */
public class DataBaseConnector extends Connector {

    private final Optional<String> username;
    private final Optional<String> password;

    private final Connection connection;

    public DataBaseConnector(Port port, String connectionString, String username, String password) {
        super(port);

        if (port instanceof InputPort) {
            throw new IllegalArgumentException("DataBaseConnector no soporta InputPort");
        }

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

    protected Document sqlQuery(Document input) throws Exception {
        // Saca la consulta del input con el xpath /sql
        var xpath = "/sql";
        var xpathFactory = XPathFactory.newInstance();
        var xpathExpr = xpathFactory.newXPath().compile(xpath);
        var sqlQuery = (String) xpathExpr.evaluate(input, XPathConstants.STRING);

        var statement = connection.createStatement();

        // Split and execute statements sequentially
        String[] queries = sqlQuery.split(";");

        for (String query : queries) {
            String trimmedQuery = query.trim();
            if (trimmedQuery.isEmpty()) {
                continue;
            }

            boolean hasResultSet = statement.execute(trimmedQuery);

            if (hasResultSet) {
                // Found a ResultSet
                var resultSet = statement.getResultSet();
                var resultSetMetaData = resultSet.getMetaData();
                var columnCount = resultSetMetaData.getColumnCount();

                // Crear el documento XML
                var docBuilder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
                var resultDoc = docBuilder.newDocument();
                var rootElement = resultDoc.createElement("resultset");
                resultDoc.appendChild(rootElement);

                // Iterar sobre las filas del resultado
                while (resultSet.next()) {
                    var rowElement = resultDoc.createElement("row");
                    rootElement.appendChild(rowElement);

                    // Iterar sobre las columnas
                    for (int i = 1; i <= columnCount; i++) {
                        var columnName = resultSetMetaData.getColumnName(i);
                        var columnValue = resultSet.getString(i);

                        var columnElement = resultDoc.createElement(columnName);
                        if (columnValue != null) {
                            columnElement.setTextContent(columnValue);
                        }
                        rowElement.appendChild(columnElement);
                    }
                }

                resultSet.close();
                return resultDoc;
            }
        }

        return null;
    }

    @Override
    public void execute() throws Exception {
        if (port instanceof OutputPort) {
            OutputPort outputPort = (OutputPort) port;
            Document doc = outputPort.getDocument();

            if (doc == null) {
                // If concurrent execution, just wait for the event.
                return;
            }

            sqlQuery(doc);
        } else if (port instanceof RequestPort) {
            RequestPort requestPort = (RequestPort) port;
            Document request = requestPort.getRequestDocument();

            if (request == null) {
                // If concurrent execution, just wait for the event.
                return;
            }

            Document response = sqlQuery(request);
            requestPort.handleResponse(response);
        }
    }
}
