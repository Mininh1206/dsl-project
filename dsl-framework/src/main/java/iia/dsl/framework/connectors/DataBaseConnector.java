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
 * Conector JDBC para interacción con bases de datos relacionales.
 * Permite ejecutar sentencias SQL dinámicas contenidas en los mensajes XML
 * entrantes.
 * 
 * <p>
 * Estructura esperada del mensaje de entrada (Request):
 * 
 * <pre>
 * {@code
 * <sql>SELECT * FROM users WHERE id = 1</sql>
 * }
 * </pre>
 * 
 * <p>
 * Estructura del mensaje de salida (Response):
 * 
 * <pre>
 * {@code
 * <resultset>
 *   <row>
 *     <column_name>valor</column_name>
 *     ...
 *   </row>
 *   ...
 * </resultset>
 * }
 * </pre>
 */
public class DataBaseConnector extends Connector {

    private final Optional<String> username;
    private final Optional<String> password;

    private final Connection connection;

    /**
     * Constructor para DataBaseConnector.
     * 
     * @param port             El puerto asociado (usualmente RequestPort).
     * @param connectionString URL de conexión JDBC.
     * @param username         Usuario de la base de datos (opcional).
     * @param password         Contraseña de la base de datos (opcional).
     */
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

    /**
     * Ejecuta la consulta SQL extraída del documento XML.
     * 
     * @param input El documento XML conteniendo la consulta en el nodo /sql.
     * @return Un nuevo documento XML con el ResultSet formateado, o null si no hubo
     *         resultados.
     * @throws Exception Si ocurre error en XPath o SQL.
     */
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
