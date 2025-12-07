package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.Port;
import iia.dsl.framework.ports.RequestPort;
import iia.dsl.framework.util.Method;

public class HttpConnector extends Connector {
    private final String url;
    private final Method method;

    public HttpConnector(Port port, String url, Method method) {
        super(port);
        this.url = url;
        this.method = method;
    }

    @Override
    protected Document call(Document input) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
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
        // TODO implement
    }
}
