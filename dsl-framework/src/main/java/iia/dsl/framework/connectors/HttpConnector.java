package iia.dsl.framework.connectors;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.RequestPort;
import iia.dsl.framework.util.DocumentUtil;

public class HttpConnector extends Connector {
    private final String url;
    
    public HttpConnector(String id, String url) {
        super(id);
        this.url = url;
    }
    
    @Override
    protected Document call(Document input) {
        try {
            // Lectura (GET)
            if (input == null) {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/xml, text/xml, */*");

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                if (is == null)
                    return null;
                byte[] bytes = is.readAllBytes();
                String resp = new String(bytes, StandardCharsets.UTF_8).trim();
                if (resp.isEmpty())
                    return null;
                return DocumentUtil.createXMLDocument(resp);
            } else {
                // Escritura / Request (POST)
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/xml; charset=UTF-8");

                // Serializar Document a String
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                StringWriter sw = new StringWriter();
                transformer.transform(new DOMSource(input), new StreamResult(sw));
                String payload = sw.toString();

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                if (is == null)
                    return null;
                byte[] bytes = is.readAllBytes();
                String resp = new String(bytes, StandardCharsets.UTF_8).trim();
                if (resp.isEmpty())
                    return null;
                return DocumentUtil.createXMLDocument(resp);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error realizando llamada HTTP a: " + url, e);
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
