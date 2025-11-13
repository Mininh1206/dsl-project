package iia.dsl.framework;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import iia.dsl.framework.connectors.Connector;
import iia.dsl.framework.connectors.ConsoleConnector;
import iia.dsl.framework.connectors.FileConnector;
import iia.dsl.framework.connectors.MockConnector;
import iia.dsl.framework.core.Flow;
import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.RequestPort;
import iia.dsl.framework.tasks.modifiers.ContextEnricher;
import iia.dsl.framework.tasks.modifiers.ModifierFactory;
import iia.dsl.framework.tasks.modifiers.Slimmer;
import iia.dsl.framework.tasks.routers.Correlator;
import iia.dsl.framework.tasks.routers.Distributor;
import iia.dsl.framework.tasks.routers.Filter;
import iia.dsl.framework.tasks.routers.Merger;
import iia.dsl.framework.tasks.routers.Replicator;
import iia.dsl.framework.tasks.routers.RouterFactory;
import iia.dsl.framework.tasks.transformers.Aggregator;
import iia.dsl.framework.tasks.transformers.Splitter;
import iia.dsl.framework.tasks.transformers.Translator;
import iia.dsl.framework.util.DocumentUtil;

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

    private static final String ORDER_1 = """
                                   <cafe_order>
            <order_id>1</order_id>
            <drinks>
            <drink>
            <name>cafe</name>
            <type>hot</type>
            </drink>
            <drink>
            <name>coca-cola</name>
            <type>cold</type>
            </drink>
            </drinks>
            </cafe_order>
                                    """;
    private static final String ORDER_COLD = """
            <drinks>
            <drink>
            <name>coca-cola</name>
            <state>cready</state>
            </drink>
            </drinks>
                                    """;
    private static final String ORDER_HOT = """
            <drinks>
            <drink>
            <name>cafe</name>
            <state>ready</state>
            </drink>
            </drinks>
                                                """;
    private static final String ORDER_XSLT = """
            <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                <xsl:output method="xml" indent="yes"/>

                <xsl:template match="/cafe_order">
                    <sql>
                        <xsl:text>INSERT INTO cafe_orders (order_id) VALUES (</xsl:text>
                        <xsl:value-of select="order_id"/>
                        <xsl:text>);&#10;</xsl:text>

                        <xsl:for-each select="drinks/drink">
                            <xsl:text>INSERT INTO order_drinks (order_id, drink_name, drink_type) VALUES (</xsl:text>
                            <xsl:value-of select="../../order_id"/>
                            <xsl:text>, '</xsl:text>
                            <xsl:value-of select="name"/>
                            <xsl:text>', '</xsl:text>
                            <xsl:value-of select="type"/>
                            <xsl:text>');</xsl:text>
                            <xsl:if test="position() != last()">
                                <xsl:text>&#10;</xsl:text>
                            </xsl:if>
                        </xsl:for-each>
                    </sql>
                </xsl:template>

            </xsl:stylesheet>
                                    """;

    public static void main(String[] args) {
        demoFlow();
    }

    @SuppressWarnings("unused")
    private static void demoManual() {
        System.out.println("=== DEMO del Framework DSL ===\n");
        try {
            System.out.println("?XML de entrada (Pedido original):");

            // Filter: Solo pedidos con 2 o más items
            // System.out.println("FILTER: Verificando que el pedido tenga al menos 2
            // items...");
            // Filter filter = new Filter(
            // "orderFilter",
            // inputSlot,
            // afterFilterSlot,
            // "count(/order/items/item) >= 2"
            // );
            // filter.execute();
            // if (afterFilterSlot.getDocument() == null) {
            // System.out.println("El pedido fue rechazado por el filtro");
            // return;
            // }
            // System.out.println("Pedido aceptado (tiene "
            // + orderDoc.getElementsByTagName("item").getLength() + " items)");
            // System.out.println();
            // // Slimmer: Eliminar información sensible (email)
            // System.out.println("SLIMMER: Eliminando información sensible (email)...");
            // Slimmer slimmer = new Slimmer(
            // "emailRemover",
            // afterFilterSlot,
            // afterSlimmerSlot,
            // "/order/header/email"
            // );
            // slimmer.execute();
            // System.out.println("Email eliminado del documento");
            // System.out.println();
            // // Translator: Convertir a formato de factura
            // System.out.println("TRANSLATOR: Transformando pedido a formato de
            // factura...");
            // Translator translator = new Translator(
            // "orderToInvoice",
            // afterSlimmerSlot,
            // outputSlot,
            // INVOICE_XSLT
            // );
            // translator.execute();
            // System.out.println("Transformación completada");
            // System.out.println();
            // === PASO 3: Mostrar resultado final ===
            // Document result = outputSlot.getDocument();
            // System.out.println("XML de salida (Factura generada):");
            // System.out.println(documentToString(result));
            // System.out.println("\n=== Pipeline completado exitosamente ===");
            // // Verificaciones
            // System.out.println("\nVerificaciones:");
            // System.out.println("- Nodo raíz: " +
            // result.getDocumentElement().getNodeName());
            // System.out.println("- Número de factura: "
            // + result.getElementsByTagName("invoiceNumber").item(0).getTextContent());
            // System.out.println("- Cliente: "
            // + result.getElementsByTagName("customerName").item(0).getTextContent());
            // System.out.println("- Total de items: "
            // + result.getElementsByTagName("totalItems").item(0).getTextContent());
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @SuppressWarnings("unused")
    private static void demoFlow() {
        try {
            // === CONFIGURACIÓN ===

            System.out.println(ORDER_1);
            System.out.println();
            // Crear documento inicial
            Document orderDoc = createDocument(ORDER_1);
            // 1 Splitter
            // 2 Distributor
            // 3 Replicator
            // 4 Translator
            // 5 Correlator
            // 6 Context Content Enricher
            // 7 Replicator
            // 8 Translator
            // 9 Correlator
            // 10 Context Content Enricher
            // 11 Merger
            // 12 Aggregator
            // === PASO 1: Configurar Slots ===
            Slot inputSlotSystem = new Slot();
            Slot inputSlotCorrelator = new Slot();
            Slot inputSlotCorrelator2 = new Slot();
            List<Slot> inputSlotsCorrelator = new ArrayList<>();
            List<Slot> inputSlotsCorrelator2 = new ArrayList<>();
            List<Slot> inputSlotsMerger = new ArrayList<>();
            Slot outputSlotSplitter = new Slot();
            List<Slot> outputSlotsDistributor = new ArrayList<>();
            List<Slot> outputSlotsReplicator = new ArrayList<>();
            Slot outputSlotTranslator = new Slot();
            List<Slot> outputSlotsCorrelator = new ArrayList<>();
            Slot outputSlotContextContentEnricher = new Slot();
            List<Slot> outputSlotsReplicator2 = new ArrayList<>();
            Slot outputSlotTranslator2 = new Slot();
            List<Slot> outputSlotsCorrelator2 = new ArrayList<>();
            Slot outputSlotContextContentEnricher2 = new Slot();
            Slot outputSlotMerger = new Slot();
            Slot outputSlotAggregator = new Slot();

            InputPort inputPort = new InputPort("inputPort", new MockConnector(DocumentUtil.createXMLDocument(ORDER_1)),
                    inputSlotSystem);
            OutputPort outputPort = new OutputPort("outputPort", new FileConnector("output.xml"), outputSlotMerger);

            RequestPort requestPortFrias = new RequestPort("requestPortFrias",
                    new MockConnector(DocumentUtil.createXMLDocument(ORDER_COLD)),
                    outputSlotTranslator, inputSlotCorrelator);
            RequestPort requestPortCalientes = new RequestPort("requestPortCalientes",
                    new MockConnector(DocumentUtil.createXMLDocument(ORDER_HOT)),
                    outputSlotTranslator2, inputSlotCorrelator2);

            inputSlotSystem.setMessage(new Message(inputSlotSystem.getMessageId(), orderDoc));
            // === PASO 2: Configurar Tasks ===

            var tf = new iia.dsl.framework.tasks.transformers.TransformerFactory();
            RouterFactory rf = new RouterFactory();
            ModifierFactory mf = new ModifierFactory();

            Splitter splitter = tf.createSplitterTask("splitter", inputSlotSystem, outputSlotSplitter,
                    "/cafe_order/drinks/drink");
            List<String> xPathDistributor = new ArrayList<String>();
            xPathDistributor.add("/cafe_order/drinks/drink/type = 'frio'");
            xPathDistributor.add("/cafe_order/drinks/drink/type = 'caliente'");

            outputSlotsDistributor.add(new Slot("outputSlotDistributor0"));
            outputSlotsDistributor.add(new Slot("outputSlotDistributor1"));

            Distributor distributor = rf.createDistributorTask("distributor", outputSlotSplitter,
                    outputSlotsDistributor, xPathDistributor);

            for (int i = 0; i < 2; i++) {
                outputSlotsReplicator.add(new Slot("outputSlotReplicator" + i));
            }
            Replicator replicator = rf.createReplicatorTask("replicator", outputSlotsDistributor.get(0),
                    outputSlotsReplicator);
            Translator translator = tf.createTranslatorTask("translator", outputSlotsReplicator.get(0),
                    outputSlotTranslator, ORDER_XSLT);

            inputSlotsCorrelator.add(outputSlotsReplicator.get(1));
            inputSlotsCorrelator.add(inputSlotCorrelator);
            outputSlotsCorrelator.add(new Slot("outputSlotCorrelator"));
            outputSlotsCorrelator.add(new Slot("outputSlotCorrelator2"));

            Correlator correlator = rf.createCorrelatorTask("correlator", inputSlotsCorrelator, outputSlotsCorrelator);

            ContextEnricher contextContentEnricher = mf.createContextEnricherTask("contextContentEnricher",
                    outputSlotsCorrelator.get(0), outputSlotsCorrelator.get(1), outputSlotContextContentEnricher);

            for (int i = 0; i < 2; i++) {
                outputSlotsReplicator2.add(new Slot("outputSlotReplicator2" + i));
            }
            Replicator replicator2 = rf.createReplicatorTask("replicator", outputSlotsDistributor.get(1),
                    outputSlotsReplicator2);
            Translator translator2 = tf.createTranslatorTask("translator", outputSlotsReplicator2.get(0),
                    outputSlotTranslator2, ORDER_XSLT);

            inputSlotsCorrelator2.add(outputSlotsReplicator2.get(1));
            inputSlotsCorrelator2.add(inputSlotCorrelator2);
            outputSlotsCorrelator2.add(new Slot("outputSlotCorrelator"));
            outputSlotsCorrelator2.add(new Slot("outputSlotCorrelator2"));

            Correlator correlator2 = rf.createCorrelatorTask("correlator", inputSlotsCorrelator2,
                    outputSlotsCorrelator2);

            ContextEnricher contextContentEnricher2 = mf.createContextEnricherTask("contextContentEnricher",
                    outputSlotsCorrelator2.get(0), outputSlotsCorrelator2.get(1), outputSlotContextContentEnricher2);

            inputSlotsMerger.add(outputSlotContextContentEnricher);
            inputSlotsMerger.add(outputSlotContextContentEnricher2);
            Merger merger = rf.createMergerTask("merger", inputSlotsMerger, outputSlotMerger);

            Aggregator aggregator = tf.createAggregatorTask("aggregator", outputSlotMerger, outputSlotAggregator,
                    "/cafe_order");

            // // Slots
            // Slot inputSlot = new Slot("input");
            // Slot processedSlot = new Slot("processed");
            // Slot outputSlot = new Slot("output");

            // // Connectors
            // Connector fileInput = new MockConnector("fileInput",
            // createDocument(ORDER_XML));
            // Connector consoleOutput = new ConsoleConnector("console");

            // // Ports
            // InputPort input = new InputPort("orderInput", fileInput, inputSlot);
            // OutputPort output = new OutputPort("consoleOutput", consoleOutput,
            // outputSlot);

            // // Tasks
            // Filter filter = new Filter("filter", inputSlot, processedSlot,
            // "count(/order/items/item) >= 2");
            // Translator translator = new Translator("translator", processedSlot,
            // outputSlot, INVOICE_XSLT);

            // Flow
            Flow flow = new Flow("orderProcessing");
            // 1 Splitter
            // 2 Distributor
            // 3 Replicator
            // 4 Translator
            // 5 Correlator
            // 6 Context Content Enricher
            // 7 Replicator
            // 8 Translator
            // 9 Correlator
            // 10 Context Content Enricher
            // 11 Merger
            // 12 Aggregator
            flow.addElement(inputPort);
            flow.addElement(splitter);
            flow.addElement(distributor);
            flow.addElement(replicator);
            flow.addElement(translator);
            flow.addElement(correlator);
            flow.addElement(contextContentEnricher);
            flow.addElement(replicator2);
            flow.addElement(translator2);
            flow.addElement(correlator2);
            flow.addElement(contextContentEnricher2);
            flow.addElement(merger);
            flow.addElement(aggregator);
            flow.addElement(requestPortCalientes);
            flow.addElement(requestPortFrias);
            flow.addElement(outputPort);

            // === EJECUCIÓN ===
            flow.execute();

            // === PASO 3: Mostrar resultado final ===
            Document result = outputSlotAggregator.getDocument();
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

        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // === UTILIDADES ===
    private static Document createDocument(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(
                xml.getBytes(StandardCharsets.UTF_8));
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
