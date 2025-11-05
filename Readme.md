
- [Doc ia](docs/ANALISIS_Y_TESTS.md)

## Diagrama de clases
```mermaid
classDiagram
    direction LR
    class Port {
        +execute(): void
    }
    class InputPort
    class OutputPort
    class RequestPort

    Port <|-- InputPort
    Port <|-- OutputPort
    Port <|-- RequestPort

    class Slot {
        +xmlDocument: Document
    }

    InputPort "1" -- "1" Slot : entrada
    OutputPort "1" -- "1" Slot : salida
    RequestPort "1" -- "1" Slot : entrada
    RequestPort "1" -- "1" Slot : salida

    class PortFactory {
        +createInputPort(inputSlot: Slot): Port
        +createOutput(outputSlot: Slot): Port
        +createRequest(outputSlot: Slot, inputSlot: Slot): Port
    }
    PortFactory ..> Port : Creates
    PortFactory ..> Slot : Uses

    class TaskType {
        <<enumeration>>
        Modifier
        Transformer
        Router
    }

    class Task {
        +type: TaskType
        +execute(): void
    }

    Task "1" -- "1" TaskType
    Task o-- "1..*" Slot : inputSlots
    Task o-- "1..*" Slot : outputSlots

    class TaskFactory {
        +createCorrelator(inputSlots:List, outputSlots:List): Task
        +createMerger(inputSlots:List, outputSlots:Slot): Task
        +createFilter(inputSlots:Slot, outputSlots:Slot): Task
        +createDistributor(inputSlots:Slot, outputSlots:List): Task
        +createReplicator(inputSlots:Slot, outputSlots:List): Task
        +createThreader(inputSlots:Slot, outputSlots:Slot): Task
        +createSlimmer(inputSlots:Slot, outputSlots:Slot): Task
        +createContextSlimmer(inputSlots:Slot, outputSlots:Slot): Task
        +createContextEnricher(inputSlots:Slot, outputSlots:Slot): Task
        +createHeaderPromoter(inputSlots:Slot, outputSlots:Slot): Task
        +createHeaderDemoter(inputSlots:Slot, outputSlots:Slot): Task
        +createCorrelationIdSetter(inputSlots:Slot, outputSlots:Slot): Task
        +createReturnAddressSetter(inputSlots:Slot, outputSlots:Slot): Task
        +createTranslator(inputSlots:Slot, outputSlots:Slot): Task
        +createSplitter(inputSlots:Slot, outputSlots:Slot): Task
        +createAggregator(inputSlots:Slot, outputSlots:Slot): Task
        +createChopper(inputSlots:Slot, outputSlots:Slot): Task
        +createAssembler(inputSlots:Slot, outputSlots:Slot): Task
    }
    TaskFactory ..> Task : Creates
    TaskFactory ..> Slot : Uses

    class CorrelatorTask
    class MergerTask
    class FilterTask
    class DistributorTask
    class ReplicatorTask
    class ThreaderTask
    class SlimmerTask
    class ContextSlimmerTask
    class ContextEnricherTask
    class HeaderPromoterTask
    class HeaderDemoterTask
    class CorrelationIdSetterTask
    class ReturnAddressSetterTask
    class TranslatorTask
    class SplitterTask
    class AggregatorTask
    class ChopperTask
    class AssemblerTask

    Task <|-- CorrelatorTask
    Task <|-- MergerTask
    Task <|-- FilterTask
    Task <|-- DistributorTask
    Task <|-- ReplicatorTask
    Task <|-- ThreaderTask
    Task <|-- SlimmerTask
    Task <|-- ContextSlimmerTask
    Task <|-- ContextEnricherTask
    Task <|-- HeaderPromoterTask
    Task <|-- HeaderDemoterTask
    Task <|-- CorrelationIdSetterTask
    Task <|-- ReturnAddressSetterTask
    Task <|-- TranslatorTask
    Task <|-- SplitterTask
    Task <|-- AggregatorTask
    Task <|-- ChopperTask
    Task <|-- AssemblerTask
```