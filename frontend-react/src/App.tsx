import { useCallback, useEffect, useMemo, useRef, useState } from 'react'

const API_BASE =
  import.meta.env.VITE_API_BASE?.replace(/\/$/, '') ?? 'http://localhost:8080'
const CURRENT_URL = `${API_BASE}/api/v1/samples/current`

type ProcessedSample = {
  timestampMillis: number
  serverReceivedAt: string | null
  temperatureC: number
  humidityPercent: number
  luminosityPercent: number
  deviceId: string
  valid: boolean
  validationNotes: string[]
}

type AlertRecord = {
  severity: string
  message: string
  triggeredAt: string
  metric: string
  observedValue: number
  limitMin: number | null
  limitMax: number | null
}

type CurrentReadingResponse = {
  sample: ProcessedSample | null
  alerts: AlertRecord[]
}

function alertIdentity(a: AlertRecord): string {
  return [
    a.triggeredAt,
    a.metric,
    a.message,
    String(a.observedValue),
    String(a.limitMin),
    String(a.limitMax),
  ].join('|')
}

function formatAlertTime(iso: string): string {
  try {
    return new Date(iso).toLocaleString('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'medium',
    })
  } catch {
    return iso
  }
}

/** Cores e rótulo da tag por tipo de métrica vindos da API. */
function alertMetricPresentation(metric: string) {
  switch (metric) {
    case 'TEMPERATURE':
      return {
        tagLabel: 'Temperatura',
        card: 'border-rose-200 bg-rose-50/95 text-rose-950 shadow-sm',
        tag: 'bg-rose-600 text-white shadow-sm ring-1 ring-rose-700/15',
        meta: 'text-rose-900/85',
        time: 'text-rose-800/90',
        dismiss: 'text-rose-800 hover:bg-rose-200/70',
      }
    case 'HUMIDITY':
      return {
        tagLabel: 'Umidade',
        card: 'border-sky-200 bg-sky-50/95 text-sky-950 shadow-sm',
        tag: 'bg-sky-600 text-white shadow-sm ring-1 ring-sky-700/15',
        meta: 'text-sky-900/85',
        time: 'text-sky-800/90',
        dismiss: 'text-sky-800 hover:bg-sky-200/70',
      }
    case 'LUMINOSITY':
      return {
        tagLabel: 'Luminosidade',
        card: 'border-violet-200 bg-violet-50/95 text-violet-950 shadow-sm',
        tag: 'bg-violet-600 text-white shadow-sm ring-1 ring-violet-700/15',
        meta: 'text-violet-900/85',
        time: 'text-violet-800/90',
        dismiss: 'text-violet-800 hover:bg-violet-200/70',
      }
    default:
      return {
        tagLabel: metric,
        card: 'border-slate-300 bg-slate-100/95 text-slate-900 shadow-sm',
        tag: 'bg-slate-600 text-white shadow-sm ring-1 ring-slate-700/15',
        meta: 'text-slate-800/90',
        time: 'text-slate-700/90',
        dismiss: 'text-slate-700 hover:bg-slate-200/80',
      }
  }
}

export default function App() {
  const [sample, setSample] = useState<ProcessedSample | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [storedAlerts, setStoredAlerts] = useState<
    { id: string; alert: AlertRecord }[]
  >([])
  const [panelOpen, setPanelOpen] = useState(false)
  const dismissedIds = useRef(new Set<string>())

  const fetchCurrent = useCallback(async () => {
    try {
      const res = await fetch(CURRENT_URL)
      if (!res.ok) {
        setError(`${res.status} ${res.statusText}`)
        return
      }
      setError(null)
      const data = (await res.json()) as CurrentReadingResponse
      setSample(data.sample)

      const incoming = Array.isArray(data.alerts) ? data.alerts : []
      setStoredAlerts((prev) => {
        const known = new Set(prev.map((x) => x.id))
        const next = [...prev]
        for (const a of incoming) {
          const id = alertIdentity(a)
          if (dismissedIds.current.has(id) || known.has(id)) continue
          known.add(id)
          next.push({ id, alert: a })
        }
        return next
      })
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Falha na requisição')
    }
  }, [])

  useEffect(() => {
    fetchCurrent()
    const t = window.setInterval(fetchCurrent, 1000)
    return () => window.clearInterval(t)
  }, [fetchCurrent])

  const dismiss = useCallback((id: string) => {
    dismissedIds.current.add(id)
    setStoredAlerts((prev) => prev.filter((x) => x.id !== id))
  }, [])

  const alertCount = storedAlerts.length

  const metricCards = useMemo(
    () =>
      sample
        ? [
            {
              label: 'Temperatura',
              value: `${sample.temperatureC.toFixed(1)} °C`,
            },
            {
              label: 'Umidade',
              value: `${sample.humidityPercent.toFixed(1)} %`,
            },
            {
              label: 'Luminosidade',
              value: `${sample.luminosityPercent.toFixed(1)} %`,
            },
          ]
        : null,
    [sample],
  )

  return (
    <div className="flex min-h-dvh flex-col bg-slate-50 text-slate-900 md:flex-row">
      <main className="flex min-h-0 flex-1 flex-col gap-4 p-4 md:p-6">
        <header className="flex flex-wrap items-center justify-between gap-3">
          <h1 className="text-xl font-semibold tracking-tight md:text-2xl">
            AmbientSense
          </h1>
          <button
            type="button"
            onClick={() => setPanelOpen((o) => !o)}
            className="relative rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm font-medium text-amber-900 shadow-sm md:hidden"
          >
            Alertas
            {alertCount > 0 && (
              <span className="absolute -right-1 -top-1 flex h-5 min-w-5 items-center justify-center rounded-full bg-amber-600 px-1 text-[11px] font-bold text-white">
                {alertCount > 99 ? '99+' : alertCount}
              </span>
            )}
          </button>
        </header>

        {error && (
          <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800">
            {error}
          </p>
        )}

        {!sample && !error && (
          <p className="text-sm text-slate-600">Aguardando leitura do sensor…</p>
        )}

        {sample && (
          <>
            <div className="flex flex-wrap items-center gap-2 text-xs text-slate-600">
              <span className="rounded-full bg-white px-2 py-0.5 font-mono shadow-sm ring-1 ring-slate-200/80">
                {sample.deviceId}
              </span>
              {sample.serverReceivedAt && (
                <span>
                  Servidor:{' '}
                  {formatAlertTime(sample.serverReceivedAt)}
                </span>
              )}
              {!sample.valid && (
                <span className="font-medium text-amber-800">
                  Amostra inválida
                </span>
              )}
            </div>
            {sample.validationNotes.length > 0 && (
              <ul className="list-inside list-disc text-sm text-amber-900">
                {sample.validationNotes.map((n) => (
                  <li key={n}>{n}</li>
                ))}
              </ul>
            )}
            <ul className="grid gap-3 sm:grid-cols-3">
              {metricCards?.map((m) => (
                <li
                  key={m.label}
                  className="rounded-xl border border-slate-200/80 bg-white p-4 shadow-sm"
                >
                  <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                    {m.label}
                  </p>
                  <p className="mt-1 text-2xl font-semibold tabular-nums text-slate-900">
                    {m.value}
                  </p>
                </li>
              ))}
            </ul>
          </>
        )}
      </main>

      {panelOpen && (
        <button
          type="button"
          aria-label="Fechar painel de alertas"
          className="fixed inset-0 z-40 bg-black/30 md:hidden"
          onClick={() => setPanelOpen(false)}
        />
      )}

      <aside
        className={[
          'z-50 flex max-h-[85dvh] w-full shrink-0 flex-col border-slate-200 bg-white shadow-[0_-4px_24px_rgba(0,0,0,0.06)] md:max-h-none md:h-dvh md:w-80 md:border-l md:shadow-none',
          'fixed bottom-0 right-0 transition-transform duration-200 md:static md:translate-x-0',
          panelOpen ? 'translate-x-0' : 'translate-x-full md:translate-x-0',
        ].join(' ')}
      >
        <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3">
          <h2 className="text-sm font-semibold text-slate-800">Alertas</h2>
          <button
            type="button"
            className="text-xs text-slate-500 md:hidden"
            onClick={() => setPanelOpen(false)}
          >
            Fechar
          </button>
        </div>
        <div className="min-h-0 flex-1 overflow-y-auto p-3">
          {storedAlerts.length === 0 ? (
            <p className="px-1 text-sm text-slate-500">
              Nenhum alerta. Novos alertas da API ficam aqui até você dispensar.
            </p>
          ) : (
            <ul className="flex flex-col gap-2">
              {storedAlerts.map(({ id, alert: a }) => {
                const v = alertMetricPresentation(a.metric)
                return (
                  <li
                    key={id}
                    className={[
                      'relative rounded-lg border p-3 pr-10 text-sm',
                      v.card,
                    ].join(' ')}
                  >
                    <button
                      type="button"
                      aria-label="Dispensar alerta"
                      className={[
                        'absolute right-2 top-2 flex h-7 w-7 items-center justify-center rounded-md text-lg leading-none',
                        v.dismiss,
                      ].join(' ')}
                      onClick={() => dismiss(id)}
                    >
                      ×
                    </button>
                    <span
                      className={[
                        'inline-flex rounded-md px-2 py-0.5 text-[10px] font-bold uppercase tracking-wide',
                        v.tag,
                      ].join(' ')}
                    >
                      {v.tagLabel}
                    </span>
                    <p className="mt-2 pr-1 font-medium">{a.message}</p>
                    <p className={`mt-1 text-xs ${v.meta}`}>
                      Valor observado: {a.observedValue}
                      {a.limitMin != null || a.limitMax != null ? (
                        <span>
                          {' '}
                          · limite{' '}
                          {a.limitMin != null ? `≥ ${a.limitMin}` : ''}
                          {a.limitMin != null && a.limitMax != null ? ' · ' : ''}
                          {a.limitMax != null ? `≤ ${a.limitMax}` : ''}
                        </span>
                      ) : null}
                    </p>
                    <p className={`mt-2 text-xs font-medium ${v.time}`}>
                      {formatAlertTime(a.triggeredAt)}
                    </p>
                  </li>
                )
              })}
            </ul>
          )}
        </div>
      </aside>
    </div>
  )
}
