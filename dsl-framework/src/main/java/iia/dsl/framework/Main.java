package iia.dsl.framework;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import iia.dsl.framework.tasks.modifiers.Slimmer;
import iia.dsl.framework.tasks.routers.Filter;
import iia.dsl.framework.tasks.transformers.Translator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    // XML de entrada: pedido de productos
    private static final String ORDER_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <order>
                <header>
                    <orderId>12345</orderId>
                    <customer>John Doe</customer>
                    <email>john@example.com</email>
                    <date>2025-11-05</date>
                </header>
                <items>
                    <item>
                        <productId>P001</productId>
                        <name>Laptop</name>
                        <price>999.99</price>
                        <quantity>1</quantity>
                    </item>
                    <item>
                        <productId>P002</productId>
                        <name>Mouse</name>
                        <price>29.99</price>
                        <quantity>2</quantity>
                    </item>
                    <item>
                        <productId>P003</productId>
                        <name>Keyboard</name>
                        <price>79.99</price>
                        <quantity>1</quantity>
                    </item>
                </items>
            </order>
            """;

    // XSLT para transformar el pedido en un formato de factura simplificado
    private static final String INVOICE_XSLT = """
            <?xml version="1.0" encoding="UTF-8"?>
            <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                <xsl:output method="xml" indent="yes"/>
                
                <xsl:template match="/order">
                    <invoice>
                        <invoiceNumber><xsl:value-of select="header/orderId"/></invoiceNumber>
                        <customerName><xsl:value-of select="header/customer"/></customerName>
                        <date><xsl:value-of select="header/date"/></date>
                        <lineItems>
                            <xsl:apply-templates select="items/item"/>
                        </lineItems>
                        <totalItems><xsl:value-of select="count(items/item)"/></totalItems>
                    </invoice>
                </xsl:template>
                
                <xsl:template match="item">
                    <line>
                        <product><xsl:value-of select="name"/></product>
                        <qty><xsl:value-of select="quantity"/></qty>
                        <price><xsl:value-of select="price"/></price>
                    </line>
                </xsl:template>
            </xsl:stylesheet>
            """;

    public static void main(String[] args) {
        demoFlow();
    }

    private static void demoManual() {
        System.out.println("=== DEMO del Framework DSL ===\n");
        try {
            System.out.println("?XML de entrada (Pedido original):");
            System.out.println(ORDER_XML);
            System.out.println();
            // Crear documento inicial
            Document orderDoc = createDocument(ORDER_XML);
            // === PASO 1: Configurar Slots ===
            Slot inputSlot = new Slot("input");
            Slot afterFilterSlot = new Slot("afterFilter");
            Slot afterSlimmerSlot = new Slot("afterSlimmer");
            Slot outputSlot = new Slot("output");
            inputSlot.setDocument(orderDoc);
            // === PASO 2: Configurar Tasks ===

            // Filter: Solo pedidos con 2 o más items
            System.out.println("FILTER: Verificando que el pedido tenga al menos 2 items...");
            Filter filter = new Filter(
                    "orderFilter",
                    inputSlot,
                    afterFilterSlot,
                    "count(/order/items/item) >= 2"
            );
            filter.execute();
            if (afterFilterSlot.getDocument() == null) {
                System.out.println("El pedido fue rechazado por el filtro");
                return;
            }
            System.out.println("Pedido aceptado (tiene "
                    + orderDoc.getElementsByTagName("item").getLength() + " items)");
            System.out.println();
            // Slimmer: Eliminar información sensible (email)
            System.out.println("SLIMMER: Eliminando información sensible (email)...");
            Slimmer slimmer = new Slimmer(
                    "emailRemover",
                    afterFilterSlot,
                    afterSlimmerSlot,
                    "/order/header/email"
            );
            slimmer.execute();
            System.out.println("Email eliminado del documento");
            System.out.println();
            // Translator: Convertir a formato de factura
            System.out.println("TRANSLATOR: Transformando pedido a formato de factura...");
            Translator translator = new Translator(
                    "orderToInvoice",
                    afterSlimmerSlot,
                    outputSlot,
                    INVOICE_XSLT
            );
            translator.execute();
            System.out.println("Transformación completada");
            System.out.println();
            // === PASO 3: Mostrar resultado final ===
            Document result = outputSlot.getDocument();
            System.out.println("XML de salida (Factura generada):");
            System.out.println(documentToString(result));
            System.out.println("\n=== Pipeline completado exitosamente ===");
            // Verificaciones
            System.out.println("\nVerificaciones:");
            System.out.println("- Nodo raíz: " + result.getDocumentElement().getNodeName());
            System.out.println("- Número de factura: "
                    + result.getElementsByTagName("invoiceNumber").item(0).getTextContent());
            System.out.println("- Cliente: "
                    + result.getElementsByTagName("customerName").item(0).getTextContent());
            System.out.println("- Total de items: "
                    + result.getElementsByTagName("totalItems").item(0).getTextContent());
        } catch (Exception e) {
            System.err.println("Error en el pipeline: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void demoFlow() {
        try {
            // === CONFIGURACIÓN ===
            
            // Slots
            Slot inputSlot = new Slot("input");
            Slot processedSlot = new Slot("processed");
            Slot outputSlot = new Slot("output");
            
            // Connectors
            Connector fileInput = new MockConnector("fileInput", createDocument(ORDER_XML));
            Connector consoleOutput = new ConsoleConnector("console");
            
            // Ports
            InputPort input = new InputPort("orderInput", fileInput, inputSlot);
            OutputPort output = new OutputPort("consoleOutput", consoleOutput, outputSlot);
            
            // Tasks
            Filter filter = new Filter("filter", inputSlot, processedSlot, "count(/order/items/item) >= 2");
            Translator translator = new Translator("translator", processedSlot, outputSlot, INVOICE_XSLT);
            
            // Flow
            Flow flow = new Flow("orderProcessing");
            flow.addPort(input);
            flow.addPort(output);
            flow.addTask(filter);
            flow.addTask(translator);
            
            // === EJECUCIÓN ===
            flow.execute();
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // === UTILIDADES ===
    private static Document createDocument(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(
                xml.getBytes(StandardCharsets.UTF_8)
        );
        return builder.parse(input);
    }

    private static String documentToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
}
