# Simulação Arduino — AmbientSense

Esta pasta contém o firmware para **Tinkercad Circuits** com leitura periódica de **temperatura**, **umidade (simulada)** e **luminosidade**, com saída em **JSON** (uma linha por amostra) pelo monitor serial, alinhada à seção 8 do [guia do projeto](../docs/guide.md).

## Circuito no Tinkercad (configuração atual)

| Componente | Função | Pino |
|------------|--------|------|
| **TMP36** | Temperatura (°C) | **A0** |
| **Fototransistor** | Intensidade luminosa (ADC 0–1023 → `luminosity` 0–100) | **A1** |
| **Potenciômetro** | Simula **umidade** em % (0–1023 → 0–100%) | **A2** |
| Arduino UNO | — | 5V / GND conforme o datasheet de cada parte |

Não é necessária biblioteca externa no Tinkercad: o sketch usa apenas `analogRead`.

### TMP36

Fórmula usada no código (tensão de referência 5 V, ADC 10 bits):

- `voltagem = leitura * (5.0 / 1024.0)`
- `temperatura °C = (voltagem - 0.5) * 100.0`

### Luminosidade (`luminosity`)

O valor bruto do fototransistor (ex.: 0–471 no ADC) é mapeado linearmente para **percentual 0–100** no JSON, conforme o contrato do guia (lux **não** é usado neste projeto).

Se no seu circuito “mais luz” diminuir o ADC, inverta fisicamente as conexões ou ajuste o `map` em [`AmbientSense.ino`](AmbientSense.ino).

### Umidade (`humidity`)

No Tinkercad, a umidade é **simulada** pelo potenciômetro: gira o knob para reproduzir cenários (ex. 0%, 30%, 82%). O **backend** (Etapa 2) aplica limites e alertas; o firmware só envia o valor numérico.

## Como usar o sketch

1. Copie [`AmbientSense.ino`](AmbientSense.ino) para o editor do circuito no Tinkercad.
2. Confirme pinos **A0 / A1 / A2** iguais à sua montagem.
3. Inicie a simulação e abra o **Serial Monitor** em **9600 baud**.
4. A cada `INTERVAL_MS` (padrão **1000 ms**) aparece **uma linha JSON** (sem banner — facilita automação e parsing).

## Cadência

- Padrão: **1 segundo** (`INTERVAL_MS = 1000`).
- Altere no topo do `.ino` se precisar outro ritmo.

## Carimbo temporal (`timestamp`)

Campo numérico = **`millis()`** desde o boot (ordenar leituras na sessão). Para ISO 8601, enriqueça no host ou na Etapa 3.

## Formato de saída (JSON Lines)

Exemplo:

```json
{"timestamp":1024,"temperature":24.71,"humidity":30.00,"luminosity":46,"deviceId":"ambient-sense-01"}
```

Campos: `timestamp`, `temperature`, `humidity`, `luminosity`, `deviceId`.

## CSV alternativo (documentação)

Cabeçalho sugerido: `timestamp,temperature,humidity,luminosity,deviceId` (UTF-8, vírgula).

## Arquivo de exemplo

Veja o exemplo de contrato JSON Lines em [`../backend-java/data/sample-output.jsonl`](../backend-java/data/sample-output.jsonl) (mantido junto ao backend no MVP).
