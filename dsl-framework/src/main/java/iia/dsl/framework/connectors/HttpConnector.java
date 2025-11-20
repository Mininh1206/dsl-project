package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.RequestPort;

public class HttpConnector extends Connector {
    private final String url;
    
    public HttpConnector(String id, String url) {
        super(id);
        this.url = url;
    }
    
    @Override
    protected Document call(Document input) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            // Intentar parsear la respuesta como XML
            try {
                return iia.dsl.framework.util.DocumentUtil.createXMLDocument(responseBody);
            } catch (Exception e) {
                // Si falla (ej. es JSON), lo envolvemos en un XML genérico
                String wrappedXml = "<response><![CDATA[" + responseBody + "]]></response>";
                return iia.dsl.framework.util.DocumentUtil.createXMLDocument(wrappedXml);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error en HttpConnector llamando a " + url, e);
        }
    }
    
    @Override
    public void execute() throws Exception {
        if (port == null) {
            throw new IllegalStateException("Port no asignado al HttpConnector");
        }
        
        if (port instanceof InputPort) {
            InputPort inputPort = (InputPort) port;
            Document doc = call(null);
            inputPort.handleDocument(doc);
        } else if (port instanceof OutputPort) {
            OutputPort outputPort = (OutputPort) port;
            Document doc = outputPort.getDocument();
            if (doc != null) {
                call(doc);
            }
        } else if (port instanceof RequestPort) {
            RequestPort requestPort = (RequestPort) port;
            Document request = requestPort.getRequestDocument();
            if (request != null) {
                Document response = call(request);
                requestPort.handleResponse(response);
            }
        }
    }
}
