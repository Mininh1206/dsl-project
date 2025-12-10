package iia.dsl.framework;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import iia.dsl.framework.connectors.DataBaseConnector;
import iia.dsl.framework.connectors.FileConnector;
import iia.dsl.framework.connectors.HttpConnector;
import iia.dsl.framework.core.Flow;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.core.policy.FifoPolicy;
import iia.dsl.framework.core.policy.MostWorkPolicy;
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
                                    <xsl:text>INSERT INTO drinks VALUES ('</xsl:text>
                                    <xsl:value-of select="name"/>
                                    <xsl:text>', 'processing'); </xsl:text>
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
                                        <xsl:choose>
                                            <xsl:when test="error">
                                                <state>error_from_http</state>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <state>ready</state>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </body>
                                </context>
                            </xsl:template>
                        </xsl:stylesheet>
                        """;

        private static final String CLEANUP_XSLT = """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                            <xsl:output method="xml" indent="yes"/>
                            <xsl:template match="@*|node()">
                                <xsl:copy>
                                    <xsl:apply-templates select="@*|node()"/>
                                </xsl:copy>
                            </xsl:template>
                            <xsl:template match="drink[state='error_from_http']"/>
                        </xsl:stylesheet>
                        """;

        public static void main(String[] args) {
                // Run Concurrent Version (with different policies)
                System.out.println("=== Running Concurrent Multi-Flow Implementation ===");
                runCafeImplementationConcurrent();

                // Run Sequential Version
                System.out.println("\n=== Running Sequential Multi-Flow Implementation ===");
                // runCafeImplementationSequential();
        }

        private static void runCafeImplementationSequential() {
                try {
                        // === CONFIGURACIÓN INICIAL ===
                        // Setup DB H2
                        try (java.sql.Connection conn = java.sql.DriverManager
                                        .getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")) {
                                conn.createStatement()
                                                .execute("CREATE TABLE IF NOT EXISTS drinks (name VARCHAR(255), state VARCHAR(255))");
                                // Clear table for fresh run
                                conn.createStatement().execute("DELETE FROM drinks");
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

                        // === PASO 2: Configurar Ports y Connectors ===
                        var inputPort = new InputPort(inputSlotSystem);
                        var requestPortFrias = new RequestPort("requestPortFrias", inputSlotRequestPortFrias,
                                        outputSlotRequestPortFrias, DB_RESULT_XSLT);
                        var requestPortCalientes = new RequestPort("requestPortCalientes",
                                        inputSlotRequestPortCalientes,
                                        outputSlotRequestPortCalientes, HTTP_RESULT_XSLT);
                        var outputPort = new OutputPort("outputPort", outputSlotSystem, CLEANUP_XSLT);

                        var fileConnectorInput = new FileConnector(inputPort, "input.xml");
                        var dbConnectorFrias = new DataBaseConnector(requestPortFrias,
                                        "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", null,
                                        null);
                        var httpConnectorCalientes = new HttpConnector(requestPortCalientes);
                        var fileConnectorOutput = new FileConnector(outputPort, "output.xml");

                        // === PASO 3: Configurar Tasks Factories ===
                        var modifierFactory = new ModifierFactory();
                        var routerFactory = new RouterFactory();
                        var transformerFactory = new iia.dsl.framework.tasks.transformers.TransformerFactory();

                        // === PASO 4: Definir Flows ===
                        // Main Flow: FifoPolicy (General coordination)
                        Flow mainFlow = Flow.builder()
                                        .id("MainFlow")
                                        .build();

                        // Cold Flow: FifoPolicy (Standard priority)
                        Flow coldFlow = Flow.builder()
                                        .id("ColdFlow")
                                        .build();

                        // Hot Flow: MostWorkPolicy (High Priority for complex tasks)
                        Flow hotFlow = Flow.builder()
                                        .id("HotFlow")
                                        .build();

                        // === PASO 5: Crear Tareas ===

                        var splitter = transformerFactory.createSplitterTask("splitter", inputSlotSystem,
                                        outputSlotSplitter,
                                        "/cafe_order/drinks/drink");
                        var correlatorIdSetter = modifierFactory.createCorrelationIdSetterTask("correlationIdSetter",
                                        outputSlotSplitter, outputSlotCorrelationIdSetter);
                        var distributor = routerFactory.createDistributorTask("distributor",
                                        outputSlotCorrelationIdSetter,
                                        List.of(outputSlotDistributorToFrias, outputSlotDistributorToCalientes),
                                        List.of("/drink/type='cold'", "/drink/type='hot'"));

                        // Cold Tasks
                        var replicatorFrias = routerFactory.createReplicatorTask("replicatorFrias",
                                        outputSlotDistributorToFrias,
                                        List.of(outputSlot1Replicator1, outputSlot2Replicator1));
                        var translatorFrias = transformerFactory.createTranslatorTask("translatorFrias",
                                        outputSlot2Replicator1,
                                        inputSlotRequestPortFrias, SELECT_XSLT);
                        var correlatorFrias = routerFactory.createCorrelatorTask("correlatorFrias",
                                        List.of(outputSlot1Replicator1, outputSlotRequestPortFrias),
                                        List.of(outputSlot1Correlator1, outputSlot2Correlator1));
                        var contextContentEnricherFrias = modifierFactory.createContextEnricherTask(
                                        "contextEnricherFrias",
                                        outputSlot1Correlator1, outputSlot2Correlator1, outputSlotContextEnricher1);

                        // Hot Tasks
                        var replicatorCalientes = routerFactory.createReplicatorTask("replicatorCalientes",
                                        outputSlotDistributorToCalientes,
                                        List.of(outputSlot1Replicator2, outputSlot2Replicator2));
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
                        var correlatorCalientes = routerFactory.createCorrelatorTask("correlatorCalientes",
                                        List.of(outputSlot1Replicator2, outputSlotRequestPortCalientes),
                                        List.of(outputSlot1Correlator2, outputSlot2Correlator2));
                        var contextContentEnricherCalientes = modifierFactory.createContextEnricherTask(
                                        "contextEnricherCalientes",
                                        outputSlot1Correlator2, outputSlot2Correlator2, outputSlotContextEnricher2);

                        var merger = routerFactory.createMergerTask("merger",
                                        List.of(outputSlotContextEnricher1, outputSlotContextEnricher2),
                                        outputSlotMerger);
                        var aggregator = transformerFactory.createAggregatorTask("aggregator", outputSlotMerger,
                                        outputSlotSystem,
                                        "/cafe_order/drinks");

                        // === PASO 6: Asignar Tareas a Flows ===

                        // --- Cold Flow ---
                        coldFlow.addElement(replicatorFrias);
                        coldFlow.addElement(translatorFrias);
                        coldFlow.addElement(dbConnectorFrias);
                        coldFlow.addElement(correlatorFrias);
                        coldFlow.addElement(contextContentEnricherFrias);

                        // --- Hot Flow ---
                        hotFlow.addElement(replicatorCalientes);
                        hotFlow.addElement(translatorCalientes);
                        hotFlow.addElement(httpConnectorCalientes);
                        hotFlow.addElement(correlatorCalientes);
                        hotFlow.addElement(contextContentEnricherCalientes);

                        // --- Main Flow ---
                        // Adding elements in logical processing order
                        mainFlow.addElement(fileConnectorInput);
                        mainFlow.addElement(splitter);
                        mainFlow.addElement(correlatorIdSetter);
                        mainFlow.addElement(distributor);

                        // Nested Flows added where their branches begin logically
                        mainFlow.addElement(coldFlow);
                        mainFlow.addElement(hotFlow);

                        // Convergence
                        mainFlow.addElement(merger);
                        mainFlow.addElement(aggregator);
                        mainFlow.addElement(fileConnectorOutput);

                        // === EJECUCIÓN ===
                        System.out.println("Starting Main Flow...");
                        mainFlow.execute();

                } catch (Exception ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                        com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
                }
        }

        private static void runCafeImplementationConcurrent() {
                try {
                        // === CONFIGURACIÓN INICIAL ===
                        // Setup DB H2
                        try (java.sql.Connection conn = java.sql.DriverManager
                                        .getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")) {
                                conn.createStatement()
                                                .execute("CREATE TABLE IF NOT EXISTS drinks (name VARCHAR(255), state VARCHAR(255))");
                                // Clear table for fresh run
                                conn.createStatement().execute("DELETE FROM drinks");
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

                        // === PASO 2: Configurar Ports y Connectors ===
                        var inputPort = new InputPort(inputSlotSystem);
                        var requestPortFrias = new RequestPort("requestPortFrias", inputSlotRequestPortFrias,
                                        outputSlotRequestPortFrias, DB_RESULT_XSLT);
                        var requestPortCalientes = new RequestPort("requestPortCalientes",
                                        inputSlotRequestPortCalientes,
                                        outputSlotRequestPortCalientes, HTTP_RESULT_XSLT);
                        var outputPort = new OutputPort("outputPort", outputSlotSystem, CLEANUP_XSLT);

                        var fileConnectorInput = new FileConnector(inputPort, "input.xml");
                        var dbConnectorFrias = new DataBaseConnector(requestPortFrias,
                                        "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", null,
                                        null);
                        var httpConnectorCalientes = new HttpConnector(requestPortCalientes);
                        var fileConnectorOutput = new FileConnector(outputPort, "output.xml");

                        // === PASO 3: Configurar Tasks Factories ===
                        var modifierFactory = new ModifierFactory();
                        var routerFactory = new RouterFactory();
                        var transformerFactory = new iia.dsl.framework.tasks.transformers.TransformerFactory();

                        // === PASO 4: Definir Flows ===
                        // Main Flow: FifoPolicy (General coordination)
                        Flow mainFlow = Flow.builder()
                                        .id("MainFlow")
                                        .concurrent(new FifoPolicy())
                                        .build();

                        // Cold Flow: FifoPolicy (Standard priority)
                        Flow coldFlow = Flow.builder()
                                        .id("ColdFlow")
                                        .concurrent(new FifoPolicy())
                                        .build();

                        // Hot Flow: MostWorkPolicy (High Priority for complex tasks)
                        Flow hotFlow = Flow.builder()
                                        .id("HotFlow")
                                        .concurrent(new MostWorkPolicy())
                                        .build();

                        // === PASO 5: Crear Tareas ===

                        var splitter = transformerFactory.createSplitterTask("splitter", inputSlotSystem,
                                        outputSlotSplitter,
                                        "/cafe_order/drinks/drink");
                        var correlatorIdSetter = modifierFactory.createCorrelationIdSetterTask("correlationIdSetter",
                                        outputSlotSplitter, outputSlotCorrelationIdSetter);
                        var distributor = routerFactory.createDistributorTask("distributor",
                                        outputSlotCorrelationIdSetter,
                                        List.of(outputSlotDistributorToFrias, outputSlotDistributorToCalientes),
                                        List.of("/drink/type='cold'", "/drink/type='hot'"));

                        // Cold Tasks
                        var replicatorFrias = routerFactory.createReplicatorTask("replicatorFrias",
                                        outputSlotDistributorToFrias,
                                        List.of(outputSlot1Replicator1, outputSlot2Replicator1));
                        var translatorFrias = transformerFactory.createTranslatorTask("translatorFrias",
                                        outputSlot2Replicator1,
                                        inputSlotRequestPortFrias, SELECT_XSLT);
                        var correlatorFrias = routerFactory.createCorrelatorTask("correlatorFrias",
                                        List.of(outputSlot1Replicator1, outputSlotRequestPortFrias),
                                        List.of(outputSlot1Correlator1, outputSlot2Correlator1));
                        var contextContentEnricherFrias = modifierFactory.createContextEnricherTask(
                                        "contextEnricherFrias",
                                        outputSlot1Correlator1, outputSlot2Correlator1, outputSlotContextEnricher1);

                        // Hot Tasks
                        var replicatorCalientes = routerFactory.createReplicatorTask("replicatorCalientes",
                                        outputSlotDistributorToCalientes,
                                        List.of(outputSlot1Replicator2, outputSlot2Replicator2));
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
                        var correlatorCalientes = routerFactory.createCorrelatorTask("correlatorCalientes",
                                        List.of(outputSlot1Replicator2, outputSlotRequestPortCalientes),
                                        List.of(outputSlot1Correlator2, outputSlot2Correlator2));
                        var contextContentEnricherCalientes = modifierFactory.createContextEnricherTask(
                                        "contextEnricherCalientes",
                                        outputSlot1Correlator2, outputSlot2Correlator2, outputSlotContextEnricher2);

                        var merger = routerFactory.createMergerTask("merger",
                                        List.of(outputSlotContextEnricher1, outputSlotContextEnricher2),
                                        outputSlotMerger);
                        var aggregator = transformerFactory.createAggregatorTask("aggregator", outputSlotMerger,
                                        outputSlotSystem,
                                        "/cafe_order/drinks");

                        // === PASO 6: Asignar Tareas a Flows ===

                        // --- Cold Flow ---
                        coldFlow.addElement(replicatorFrias);
                        coldFlow.addElement(translatorFrias);
                        coldFlow.addElement(dbConnectorFrias);
                        coldFlow.addElement(correlatorFrias);
                        coldFlow.addElement(contextContentEnricherFrias);

                        // --- Hot Flow ---
                        hotFlow.addElement(replicatorCalientes);
                        hotFlow.addElement(translatorCalientes);
                        hotFlow.addElement(httpConnectorCalientes);
                        hotFlow.addElement(correlatorCalientes);
                        hotFlow.addElement(contextContentEnricherCalientes);

                        // --- Main Flow ---
                        // Adding elements in logical processing order
                        mainFlow.addElement(fileConnectorInput);
                        mainFlow.addElement(splitter);
                        mainFlow.addElement(correlatorIdSetter);
                        mainFlow.addElement(distributor);

                        // Nested Flows added where their branches begin logically
                        mainFlow.addElement(coldFlow);
                        mainFlow.addElement(hotFlow);

                        // Convergence
                        mainFlow.addElement(merger);
                        mainFlow.addElement(aggregator);
                        mainFlow.addElement(fileConnectorOutput);

                        // === EJECUCIÓN ===
                        System.out.println("Starting Main Flow...");
                        mainFlow.execute();

                        // Wait for concurrent execution to finish
                        System.out.println("Waiting for flow quiescence...");
                        iia.dsl.framework.core.ExecutionEnvironment.getInstance().waitForQuiescence(30000);
                        System.out.println("Flow quiescence reached.");
                        iia.dsl.framework.core.ExecutionEnvironment.getInstance().shutdown();
                } catch (Exception ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                        com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
                }
        }
}
