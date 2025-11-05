package iia.dsl.framework;

import org.w3c.dom.Document;

public class RequestPort extends Port {
    private Slot requestSlot;
    private Slot responseSlot;
    
    public RequestPort(String id, Connector connector, Slot requestSlot, Slot responseSlot) {
        super(id, connector, requestSlot);
        this.requestSlot = requestSlot;
        this.responseSlot = responseSlot;
    }
    
    @Override
    public void execute() {
        // 1. Obtiene el documento de request del slot
        Document request = requestSlot.getDocument();
        
        // 2. Envía el request y obtiene response del connector
        Document response = connector.call(request);
        
        // 3. Coloca la respuesta en el slot de response
        responseSlot.setDocument(response);
        
        System.out.println("✓ RequestPort '" + id + "' completó request/response");
    }
}