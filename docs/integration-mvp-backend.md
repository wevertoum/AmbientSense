# Integração MVP — simulador (JSONL) → backend Java

Este documento descreve o canal **Etapa 3** entre o artefato `backend-java/data/sample-output.jsonl` e o processamento no servidor, para reprodutibilidade em aula e apresentação.

## Arquivo e codificação

| Aspecto | Valor |
|--------|--------|
| Arquivo | `backend-java/data/sample-output.jsonl` (relativo à pasta do módulo Java) |
| Encoding | **UTF-8** |
| Formato | JSON Lines — **uma linha = um objeto JSON** (ver seção 8 do `guide.md`) |
| Linhas vazias | Ignoradas (após trim) |

## Caminho configurável

O backend resolve o caminho definido em `ambientsense.mock.jsonl-path` (veja `backend-java/src/main/resources/application.yml`). O padrão assume execução com diretório de trabalho em `backend-java/`:

- Padrão: `data/sample-output.jsonl`

Para apontar outro arquivo:

```bash
export AMBENTSENSE_JSONL=/caminho/absoluto/outro.jsonl
mvn -f backend-java/pom.xml spring-boot:run
```

Ou: `mvn spring-boot:run -Dspring-boot.run.arguments="--ambientsense.mock.jsonl-path=/caminho/arquivo.jsonl"` (a partir de `backend-java/`).

## Cadência (avanço de linha)

| Parâmetro | Descrição | Padrão |
|-----------|-----------|--------|
| `ambientsense.mock.tick-ms` | Intervalo entre leituras de **uma nova linha** do arquivo (simula cadência do serial / firmware) | `1000` ms |

Implementação: `@Scheduled(fixedDelay = tick-ms)` — após cada ciclo, aguarda o intervalo antes do próximo.

## Fim do arquivo (`on-eof`)

| Valor | Comportamento |
|--------|----------------|
| `RESTART` (padrão) | Ao atingir o fim, o índice volta ao início e o mock **recomeça** (loop para demo contínua). |
| `STOP` | Para de avançar linhas; histórico e última leitura permanecem disponíveis via REST. |

## Falhas leves (comportamento previsível)

| Situação | Comportamento |
|----------|----------------|
| Linha vazia | Ignorada no carregamento inicial (não entra na lista de linhas). |
| JSON malformado na linha | Parser registra **WARN** no log; essa linha não gera `RawSensorReading`; o agendador segue para a próxima linha no próximo tick. |
| Arquivo ausente ou ilegível no startup | **ERROR** no log; lista de linhas fica vazia; ticks não processam dados até novo carregamento (reinício da aplicação ou evolução futura com reload explícito). |

## Enriquecimento de tempo (host)

Cada amostra processada inclui `serverReceivedAt` (instante ISO-8601 no servidor), além de `timestampMillis` vindo do Arduino (`millis()` na Etapa 1), para ordenação e correlação com alertas.

## API REST (contrato estável para o dashboard)

Base: `http://localhost:8080/api/v1` (porta configurável via `server.port`).

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/samples/current` | Última amostra processada + alertas avaliados para essa amostra. |
| GET | `/samples/recent?limit=64` | Histórico recente (amostra + alertas por entrada); `limit` entre 1 e 500. |
| GET | `/integration/state` | Estado do mock: caminho absoluto resolvido, total de linhas, cursor, se parou em EOF (`STOP`). |

**CORS:** `GET` e `OPTIONS` em `/api/**` liberados para qualquer origem (adequado ao MVP local; endureça em produção).

## Execução local

Na pasta `backend-java/`:

```bash
mvn spring-boot:run
```

Aguarde pelo menos um `tick-ms` para a primeira linha aparecer em `/samples/current`.
