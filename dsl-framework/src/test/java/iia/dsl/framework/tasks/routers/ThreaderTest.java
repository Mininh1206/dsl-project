package iia.dsl.framework.tasks.routers;

import iia.dsl.framework.Slot;
import iia.dsl.framework.Task;
import iia.dsl.framework.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ThreaderTest {
    
    private Slot inputSlot;
    private Slot outputSlot;
    private DocumentBuilderFactory factory;
    
    @BeforeEach
    void setUp() {
        inputSlot = new Slot("input-slot");
        outputSlot = new Slot("output-slot");
        factory = DocumentBuilderFactory.newInstance();
    }
    
    private Document createXmlDocument(String xmlContent) throws Exception {
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
    }
    
    // Mock Task para pruebas
    private static class MockTask extends Task {
        private final CountDownLatch latch;
        private final AtomicBoolean executed;
        private final long sleepTime;
        
        public MockTask(String id, Slot inputSlot, CountDownLatch latch, AtomicBoolean executed) {
            this(id, inputSlot, latch, executed, 0);
        }
        
        public MockTask(String id, Slot inputSlot, CountDownLatch latch, AtomicBoolean executed, long sleepTime) {
            super(id, TaskType.TRANSFORMER);
            addInputSlot(inputSlot);
            this.latch = latch;
            this.executed = executed;
            this.sleepTime = sleepTime;
        }
        
        @Override
        public void execute() throws Exception {
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
            executed.set(true);
            latch.countDown();
        }
    }
    
    @Test
    void testThreaderCreation() {
        Slot taskInputSlot = new Slot("task-input");
        MockTask mockTask = new MockTask("mock-1", taskInputSlot, new CountDownLatch(1), new AtomicBoolean(false));
        
        Threader threader = new Threader("threader-1", inputSlot, outputSlot, mockTask);
        
        assertNotNull(threader);
        assertEquals("threader-1", threader.getId());
        assertEquals(1, threader.getInputSlots().size());
        assertEquals(1, threader.getOutputSlots().size());
    }
    
    @Test
    void testAsynchronousExecution() throws Exception {
        String xml = "<task><data>Test Data</data></task>";
        Document doc = createXmlDocument(xml);
        inputSlot.setDocument(doc);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean taskExecuted = new AtomicBoolean(false);
        
        Slot taskInputSlot = new Slot("task-input");
        MockTask mockTask = new MockTask("mock-1", taskInputSlot, latch, taskExecuted, 100);
        
        Threader threader = new Threader("threader-1", inputSlot, outputSlot, mockTask);
        
        threader.execute();
        
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        
        assertTrue(completed, "La tarea asíncrona debería completarse");
        assertTrue(taskExecuted.get(), "La tarea debería haberse ejecutado");
    }
    
    @Test
    void testMainFlowReturnsImmediately() throws Exception {
        String xml = "<task><data>Test Data</data></task>";
        Document doc = createXmlDocument(xml);
        inputSlot.setDocument(doc);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean taskExecuted = new AtomicBoolean(false);
        
        Slot taskInputSlot = new Slot("task-input");
        MockTask mockTask = new MockTask("mock-1", taskInputSlot, latch, taskExecuted, 500);
        
        Threader threader = new Threader("threader-1", inputSlot, outputSlot, mockTask);
        
        long startTime = System.currentTimeMillis();
        threader.execute();
        long executionTime = System.currentTimeMillis() - startTime;
        
        assertTrue(executionTime < 200, "El método execute debería retornar inmediatamente");
        assertFalse(taskExecuted.get(), "La tarea no debería haberse completado aún");
    }
    
    @Test
    void testThrowsExceptionWhenNoDocument() throws Exception {
        inputSlot.setDocument(null);
        
        Slot taskInputSlot = new Slot("task-input");
        MockTask mockTask = new MockTask("mock-1", taskInputSlot, new CountDownLatch(1), new AtomicBoolean(false));
        
        Threader threader = new Threader("threader-1", inputSlot, outputSlot, mockTask);
        
        Exception exception = assertThrows(Exception.class, () -> {
            threader.execute();
        });
        
        assertTrue(exception.getMessage().contains("no tiene documento para procesar"));
    }
    
    @Test
    void testDocumentIsCopiedToAsyncTask() throws Exception {
        String xml = "<data><value>Original Value</value></data>";
        Document doc = createXmlDocument(xml);
        inputSlot.setDocument(doc);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean taskExecuted = new AtomicBoolean(false);
        
        Slot taskInputSlot = new Slot("task-input");
        MockTask mockTask = new MockTask("mock-1", taskInputSlot, latch, taskExecuted);
        
        Threader threader = new Threader("threader-1", inputSlot, outputSlot, mockTask);
        
        threader.execute();
        
        latch.await(2, TimeUnit.SECONDS);
        
        Document asyncDoc = taskInputSlot.getDocument();
        assertNotNull(asyncDoc);
        assertNotSame(doc, asyncDoc, "El documento debería ser una copia");
    }
    
    @Test
    void testMultipleThreadersExecuteConcurrently() throws Exception {
        String xml = "<task><data>Concurrent Test</data></task>";
        Document doc = createXmlDocument(xml);
        
        int numberOfThreaders = 3;
        CountDownLatch latch = new CountDownLatch(numberOfThreaders);
        AtomicBoolean[] executed = new AtomicBoolean[numberOfThreaders];
        
        for (int i = 0; i < numberOfThreaders; i++) {
            Slot input = new Slot("input-" + i);
            Slot output = new Slot("output-" + i);
            Slot taskInput = new Slot("task-input-" + i);
            
            input.setDocument((Document) doc.cloneNode(true));
            executed[i] = new AtomicBoolean(false);
            
            MockTask mockTask = new MockTask("mock-" + i, taskInput, latch, executed[i], 100);
            Threader threader = new Threader("threader-" + i, input, output, mockTask);
            
            threader.execute();
        }
        
        boolean allCompleted = latch.await(3, TimeUnit.SECONDS);
        
        assertTrue(allCompleted, "Todas las tareas deberían completarse");
        for (int i = 0; i < numberOfThreaders; i++) {
            assertTrue(executed[i].get(), "Tarea " + i + " debería haberse ejecutado");
        }
    }
    
    @Test
    void testAsyncTaskExceptionIsHandled() throws Exception {
        String xml = "<task><data>Exception Test</data></task>";
        Document doc = createXmlDocument(xml);
        inputSlot.setDocument(doc);
        
        Slot taskInputSlot = new Slot("task-input");
        Task faultyTask = new Task("faulty-1", TaskType.TRANSFORMER) {
            {
                addInputSlot(taskInputSlot);
            }
            
            @Override
            public void execute() throws Exception {
                throw new RuntimeException("Simulated async error");
            }
        };
        
        Threader threader = new Threader("threader-1", inputSlot, outputSlot, faultyTask);
        
        assertDoesNotThrow(() -> threader.execute());
        
        Thread.sleep(200);
    }
}