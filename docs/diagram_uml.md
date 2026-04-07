# Diagrama de classes UML — backend AmbientSense (MVP)

Visão dos principais componentes do backend Java, fluxo de ingestão mock (JSONL) e exposição REST. Relações de **dependência** (uso) e **composição** (histórico); DTOs de API espelham os registros de domínio na camada `web`.

Para visualizar: qualquer visualizador Markdown com suporte a Mermaid, ou copie o bloco abaixo para o [Mermaid Live Editor](https://mermaid.live) e exporte PNG/SVG.

```mermaid
classDiagram
  direction TB

  class AmbientSenseApplication {
    <<SpringBootApplication>>
    +main(String[] args)$
  }

  class AmbientsenseConfig {
    <<Configuration>>
  }

  class OpenApiConfig {
    <<Configuration>>
  }

  class WebConfig {
    <<Configuration>>
  }

  class AmbientsenseProperties {
    <<ConfigurationProperties>>
    Mock mock
    Alerts alerts
    History history
  }

  class SensorRestController {
    <<RestController>>
    +current() CurrentReadingResponse
    +recent(int) RecentReadingsResponse
    +integrationState() IngestionState
  }

  class ApiDtos {
    <<utility>>
    ProcessedSampleDto
    AlertRecordDto
    CurrentReadingResponse
    HistoryEntryDto
    RecentReadingsResponse
  }

  class MockJsonlIngestionService {
    <<Service>>
    +reloadSourceFile()
    +tick()
    +getState() IngestionState
  }

  class JsonlLineParser {
    <<Component>>
    +parseLine(String) RawSensorReading
  }

  class SampleProcessor {
    <<Component>>
    +process(RawSensorReading, Instant) ProcessedSample
  }

  class AlertEvaluator {
    <<Component>>
    +evaluate(ProcessedSample) List~AlertRecord~
  }

  class SampleHistoryStore {
    <<Component>>
    +append(ProcessedSample, List)
    +getLatestSample() ProcessedSample
    +getLatestAlerts() List
    +recent(int) List
  }

  class HistoryEntry {
    <<nested record>>
    ProcessedSample sample
    List alerts
  }

  class RawSensorReading {
    <<record>>
  }

  class ProcessedSample {
    <<record>>
  }

  class AlertRecord {
    <<record>>
  }

  class MetricKind {
    <<enumeration>>
    TEMPERATURE
    HUMIDITY
    LUMINOSITY
  }

  class IngestionState {
    <<record>>
  }

  AmbientsenseConfig ..> AmbientsenseProperties : habilita
  AmbientSenseApplication ..> SensorRestController : container IoC
  AmbientSenseApplication ..> MockJsonlIngestionService : agendamento

  SensorRestController --> SampleHistoryStore
  SensorRestController --> MockJsonlIngestionService
  SensorRestController ..> ApiDtos : respostas
  ApiDtos ..> ProcessedSample : from()
  ApiDtos ..> AlertRecord : from()
  ApiDtos ..> HistoryEntry : from()

  MockJsonlIngestionService --> AmbientsenseProperties
  MockJsonlIngestionService --> JsonlLineParser
  MockJsonlIngestionService --> SampleProcessor
  MockJsonlIngestionService --> AlertEvaluator
  MockJsonlIngestionService --> SampleHistoryStore
  MockJsonlIngestionService ..> IngestionState : cria

  JsonlLineParser ..> RawSensorReading : desserializa
  SampleProcessor ..> RawSensorReading : entrada
  SampleProcessor ..> ProcessedSample : saída
  AlertEvaluator --> AmbientsenseProperties : limites
  AlertEvaluator ..> ProcessedSample : entrada
  AlertEvaluator ..> AlertRecord : cria
  AlertEvaluator ..> MetricKind

  SampleHistoryStore *-- HistoryEntry : buffer
  HistoryEntry --> ProcessedSample
  HistoryEntry --> AlertRecord : 0..*
  AlertRecord --> MetricKind
```
