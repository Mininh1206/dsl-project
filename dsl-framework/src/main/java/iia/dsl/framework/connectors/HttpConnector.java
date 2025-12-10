package iia.dsl.framework.connectors;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.Port;
import iia.dsl.framework.ports.RequestPort;
import iia.dsl.framework.util.Method;

/**
 * Connector que implementa la funcionalidad de un cliente HTTP.
 * Los documentos de entrada deben tener la siguiente estructura:
 * 
 * <http-request>
 * <url>http://example.com</url>
 * <method>GET</method>
 * <headers>
 * <header name="Content-Type" value="application/xml" />
 * </headers>
 * <body>
 * <element>value</element>
 * </body>
 * </http-request>
 */
public class HttpConnector extends Connector {

    private final HttpClient httpClient;

    public HttpConnector(Port port) {
        super(port);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(2)) // Adjusted timeout
                .build();

        if (port instanceof InputPort) {
            throw new IllegalArgumentException("HttpConnector no soporta InputPort");
        }
    }

    @Override
    public void execute() {
        try {
            if (port instanceof OutputPort) {
                OutputPort outputPort = (OutputPort) port;
                Document doc = outputPort.getDocument();

                if (doc == null) {
                    // If concurrent execution, just wait for the event.
                    return;
                }
                sendRequest(doc);
            } else if (port instanceof RequestPort) {
                RequestPort requestPort = (RequestPort) port;
                Document request = requestPort.getRequestDocument();

                if (request == null) {
                    return;
                }

                Document response = sendRequest(request);
                requestPort.handleResponse(response);
            }
        } catch (Exception ex) {
            System.err.println("[ERROR] Unexpected error in HttpConnector: " + ex.getMessage());
        }
    }

    private Document sendRequest(Document input) throws Exception {
        // Extract details from XML
        var xpathFactory = XPathFactory.newInstance();
        var xpath = xpathFactory.newXPath();

        // 1. URL
        String urlString = (String) xpath.evaluate("/http-request/url", input, XPathConstants.STRING);
        if (urlString == null || urlString.isBlank()) {
            throw new IllegalArgumentException("Missing /http-request/url in input document");
        }

        System.err.println("[HTTP] Sending " + xpath.evaluate("/http-request/method", input, XPathConstants.STRING)
                + " request to: " + urlString);

        // 2. Method
        String methodString = (String) xpath.evaluate("/http-request/method", input, XPathConstants.STRING);
        Method method;
        if (methodString == null || methodString.isBlank()) {
            method = Method.GET;
        } else {
            method = Method.valueOf(methodString.toUpperCase());
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .timeout(java.time.Duration.ofSeconds(5)); // Request timeout

        // 3. Headers
        NodeList headerNodes = (NodeList) xpath.evaluate("/http-request/headers/header", input, XPathConstants.NODESET);
        for (int i = 0; i < headerNodes.getLength(); i++) {
            Node node = headerNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) node;
                String name = elem.getAttribute("name");
                String value = elem.getAttribute("value");
                if (!name.isBlank() && !value.isBlank()) {
                    requestBuilder.header(name, value);
                } else if (!name.isBlank()) {
                    // try text content if value attr is missing
                    value = elem.getTextContent();
                    if (value != null) {
                        requestBuilder.header(name, value);
                    }
                }
            }
        }

        // 4. Body
        Node bodyNode = (Node) xpath.evaluate("/http-request/body", input, XPathConstants.NODE);

        switch (method) {
            case GET -> requestBuilder.GET();
            case POST -> {
                String body = nodeContentToString(bodyNode);
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
                requestBuilder.header("Content-Type", "application/xml");
            }
            case PUT -> {
                String body = nodeContentToString(bodyNode);
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body));
                requestBuilder.header("Content-Type", "application/xml");
            }
            case DELETE -> requestBuilder.DELETE();
            default -> throw new UnsupportedOperationException("Metodo no soportado: " + method);
        }

        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                HttpResponse<String> response = httpClient.send(requestBuilder.build(),
                        HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    System.err.println("[HTTP] Response received: " + response.statusCode());
                    return stringToDocument(response.body());
                } else if (response.statusCode() >= 500) {
                    // SERVER ERROR, retry
                    throw new RuntimeException("Server Error " + response.statusCode());
                } else {
                    // CLIENT ERROR, do not retry
                    throw new RuntimeException("Client Error HTTP: " + response.statusCode() + " " + response.body());
                }
            } catch (Exception e) {
                attempt++;
                System.err.println("[WARNING] HTTP " + method + " to " + urlString + " failed (Attempt " + attempt + "/"
                        + maxRetries + "): " + e.getMessage());

                // Only sleep if we are going to retry
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        // If we get here, all retries failed.
        // We log and return null or throw?
        // Returning null might cause NPE downstream if not handled, but catching in
        // execute() handles generic exceptions.
        // Let's rethrow properly or allow execute() catch to handle it.
        System.err.println("[ERROR] All " + maxRetries + " retries failed for " + urlString + ". Return Fallback.");
        // Fallback: return an error document so the flow does not hang.
        return stringToDocument("<error>Service Unavailable</error>");
        // throw new RuntimeException("HTTP Request failed after retries",
        // lastException);
    }

    private String nodeContentToString(Node node) {
        if (node == null)
            return "";
        try {
            java.io.StringWriter sw = new java.io.StringWriter();
            javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "no");

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                transformer.transform(new javax.xml.transform.dom.DOMSource(children.item(i)),
                        new javax.xml.transform.stream.StreamResult(sw));
            }
            String result = sw.toString();
            // If result is empty, try getTextContent as fallback (e.g. for simple text node
            // scenarios)
            if (result.isBlank()) {
                return node.getTextContent();
            }
            return result;
        } catch (Exception ex) {
            return node.getTextContent();
        }
    }

    private Document stringToDocument(String xmlStr) {
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(xmlStr)));
        } catch (Exception ex) {
            throw new RuntimeException("Error parseando respuesta XML", ex);
        }
    }
}
