package iia.dsl.framework;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import iia.dsl.framework.connectors.DataBaseConnector;
import iia.dsl.framework.connectors.FileConnector;
import iia.dsl.framework.connectors.HttpConnector;
import iia.dsl.framework.core.Flow;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.RequestPort;
import iia.dsl.framework.tasks.modifiers.ModifierFactory;
import iia.dsl.framework.tasks.routers.RouterFactory;

public class Main {

        private static final String SELECT_XSLT = """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                            <xsl:output method="xml" indent="yes"/>
                            <xsl:template match="/drink">
                                <sql>
                                    <xsl:text>SELECT * FROM drinks WHERE name = '</xsl:text>
                                    <xsl:value-of select="name"/>
                                    <xsl:text>'</xsl:text>
                                </sql>
                            </xsl:template>
                        </xsl:stylesheet>
                        """;

        private static final String DB_RESULT_XSLT = """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                            <xsl:output method="xml" indent="yes"/>
                            <xsl:template match="/resultset">
                                <context>
                                    <xpath>/drink</xpath>
                                    <body>
                                        <state><xsl:value-of select="row/STATE"/></state>
                                    </body>
                                </context>
                            </xsl:template>
                        </xsl:stylesheet>
                        """;

        private static final String HTTP_RESULT_XSLT = """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                            <xsl:output method="xml" indent="yes"/>
                            <xsl:template match="/">
                                <context>
                                    <xpath>/drink</xpath>
                                    <body>
                                        <state>ready</state>
                                    </body>
                                </context>
                            </xsl:template>
                        </xsl:stylesheet>
                        """;

        public static void main(String[] args) {
                cafeImplementation();
        }

        private static void cafeImplementation() {
                try {
                        // === CONFIGURACIÓN INICIAL ===
                        // Setup DB H2
                        try (java.sql.Connection conn = java.sql.DriverManager
                                        .getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")) {
                                conn.createStatement()
                                                .execute("CREATE TABLE drinks (name VARCHAR(255), state VARCHAR(255))");
                                conn.createStatement().execute("INSERT INTO drinks VALUES ('coca-cola', 'ready')");
                        }

                        // === PASO 1: Configurar Slots ===
                        var inputSlotSystem = new Slot();

                        var outputSlotSplitter = new Slot();

                        var outputSlotCorrelationIdSetter = new Slot();

                        var outputSlotDistributorToFrias = new Slot();
                        var outputSlotDistributorToCalientes = new Slot();

                        var outputSlot1Replicator1 = new Slot();
                        var outputSlot2Replicator1 = new Slot();
                        var outputSlot1Replicator2 = new Slot();
                        var outputSlot2Replicator2 = new Slot();

                        var inputSlotRequestPortFrias = new Slot();
                        var inputSlotRequestPortCalientes = new Slot();
                        var outputSlotRequestPortFrias = new Slot();
                        var outputSlotRequestPortCalientes = new Slot();

                        var outputSlot1Correlator1 = new Slot();
                        var outputSlot2Correlator1 = new Slot();
                        var outputSlot1Correlator2 = new Slot();
                        var outputSlot2Correlator2 = new Slot();

                        var outputSlotContextEnricher1 = new Slot();
                        var outputSlotContextEnricher2 = new Slot();

                        var outputSlotMerger = new Slot();

                        var outputSlotSystem = new Slot();

                        // === PASO 2: Configurar Ports y asociarlos a Connectors ===
                        var inputPort = new InputPort(inputSlotSystem);

                        // RequestPort para DB: Transforma drink -> SQL SELECT, y Resultado DB -> drink
                        // XML
                        var requestPortFrias = new RequestPort("requestPortFrias", inputSlotRequestPortFrias,
                                        outputSlotRequestPortFrias, DB_RESULT_XSLT);

                        // RequestPort para HTTP: Transforma respuesta HTTP -> drink XML
                        var requestPortCalientes = new RequestPort("requestPortCalientes",
                                        inputSlotRequestPortCalientes,
                                        outputSlotRequestPortCalientes, HTTP_RESULT_XSLT);

                        var outputPort = new OutputPort("outputPort", outputSlotSystem);

                        // === PASO 3: Configurar Connectors ===
                        // Input: FileConnector
                        var fileConnectorInput = new FileConnector(inputPort, "input.xml");

                        // Cold Drinks: DataBaseConnector
                        var dbConnectorFrias = new DataBaseConnector(requestPortFrias,
                                        "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", null,
                                        null);

                        // Hot Drinks: HttpConnector
                        var httpConnectorCalientes = new HttpConnector(requestPortCalientes);

                        // Output: FileConnector
                        var fileConnectorOutput = new FileConnector(outputPort, "output.xml");

                        // === PASO 4: Configurar Tasks Factories ===
                        var modifierFactory = new ModifierFactory();
                        var routerFactory = new RouterFactory();
                        var transformerFactory = new iia.dsl.framework.tasks.transformers.TransformerFactory();

                        // === PASO 5: Configurar Tasks ===
                        var splitter = transformerFactory.createSplitterTask("splitter", inputSlotSystem,
                                        outputSlotSplitter,
                                        "/cafe_order/drinks/drink");

                        var correlatorIdSetter = modifierFactory.createCorrelationIdSetterTask("correlationIdSetter",
                                        outputSlotSplitter, outputSlotCorrelationIdSetter);

                        var distributor = routerFactory.createDistributorTask("distributor",
                                        outputSlotCorrelationIdSetter,
                                        List.of(outputSlotDistributorToFrias, outputSlotDistributorToCalientes),
                                        List.of("/drink/type='cold'", "/drink/type='hot'"));

                        var replicatorFrias = routerFactory.createReplicatorTask("replicatorFrias",
                                        outputSlotDistributorToFrias,
                                        List.of(outputSlot1Replicator1, outputSlot2Replicator1));
                        var replicatorCalientes = routerFactory.createReplicatorTask("replicatorCalientes",
                                        outputSlotDistributorToCalientes,
                                        List.of(outputSlot1Replicator2, outputSlot2Replicator2));

                        // Translator Frias: Transforma drink -> SQL SELECT
                        var translatorFrias = transformerFactory.createTranslatorTask("translatorFrias",
                                        outputSlot2Replicator1,
                                        inputSlotRequestPortFrias, SELECT_XSLT);

                        String HTTP_REQUEST_XSLT = """
                                            <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                                                <xsl:output method="xml" indent="yes"/>
                                                <xsl:template match="/">
                                                    <http-request>
                                                        <url>https://httpbin.org/xml</url>
                                                        <method>GET</method>
                                                    </http-request>
                                                </xsl:template>
                                            </xsl:stylesheet>
                                        """;
                        var translatorCalientes = transformerFactory.createTranslatorTask("translatorCalientes",
                                        outputSlot2Replicator2, inputSlotRequestPortCalientes, HTTP_REQUEST_XSLT);

                        var correlatorFrias = routerFactory.createCorrelatorTask("correlatorFrias",
                                        List.of(outputSlot1Replicator1, outputSlotRequestPortFrias),
                                        List.of(outputSlot1Correlator1, outputSlot2Correlator1));
                        var correlatorCalientes = routerFactory.createCorrelatorTask("correlatorCalientes",
                                        List.of(outputSlot1Replicator2, outputSlotRequestPortCalientes),
                                        List.of(outputSlot1Correlator2, outputSlot2Correlator2));

                        var contextContentEnricherFrias = modifierFactory.createContextEnricherTask(
                                        "contextEnricherFrias",
                                        outputSlot1Correlator1, outputSlot2Correlator1, outputSlotContextEnricher1);
                        var contextContentEnricherCalientes = modifierFactory.createContextEnricherTask(
                                        "contextEnricherCalientes",
                                        outputSlot1Correlator2, outputSlot2Correlator2, outputSlotContextEnricher2);

                        var merger = routerFactory.createMergerTask("merger",
                                        List.of(outputSlotContextEnricher1, outputSlotContextEnricher2),
                                        outputSlotMerger);

                        var aggregator = transformerFactory.createAggregatorTask("aggregator", outputSlotMerger,
                                        outputSlotSystem,
                                        "/cafe_order/drinks");

                        // === PASO 7: Configurar Flow ===
                        Flow flow = new Flow();

                        flow.addElement(fileConnectorInput);
                        flow.addElement(splitter);
                        flow.addElement(correlatorIdSetter);
                        flow.addElement(distributor);
                        flow.addElement(replicatorFrias);
                        flow.addElement(replicatorCalientes);
                        flow.addElement(translatorFrias);
                        flow.addElement(translatorCalientes);
                        flow.addElement(dbConnectorFrias);
                        flow.addElement(httpConnectorCalientes);
                        flow.addElement(correlatorFrias);
                        flow.addElement(correlatorCalientes);
                        flow.addElement(contextContentEnricherFrias);
                        flow.addElement(contextContentEnricherCalientes);
                        flow.addElement(merger);
                        flow.addElement(aggregator);
                        flow.addElement(fileConnectorOutput);

                        // === EJECUCIÓN ===
                        flow.execute();
                } catch (Exception ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
}
