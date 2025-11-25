import { fetchWithAuth } from './api/api';
import { initHourlyMetricChart } from './hourly-metric';

type HourlyPointRaw = {
  hour: string;
  attendance?: number;
  attendanceForecast?: number;
  rounds?: number;
  roundsForecast?: number;
  visits?: number;
  visitsForecast?: number;
};

/**
 * Inicializa los 3 charts horarios y hace fetch combinado de las 3 métricas.
 * - container: elemento raíz donde buscar los divs
 * - date: YYYY-MM-DD (opcional)
 * - opts.refreshMs: intervalo en ms para refrescar; 0 = no refrescar
 */
export async function initThreeMetrics(
  container: HTMLElement,
  date?: string,
  opts?: { refreshMs?: number }
) {
  const refreshMs = opts?.refreshMs ?? 0;

  // Inicializa los 3 charts y obtén sus "refresher" (pueden ser null si el div no existe)
  const attendanceRef = await initHourlyMetricChart(container, 'attendance', 'Asistencias', '#chart-hourly-attendance', /*date*/ undefined);
  const roundsRef     = await initHourlyMetricChart(container, 'rounds',     'Rondas',     '#chart-hourly-rounds',     /*date*/ undefined);
  const visitsRef     = await initHourlyMetricChart(container, 'visits',     'Visitas',    '#chart-hourly-visits',     /*date*/ undefined);

  // Helper: aplica raw[] a cada refresher si existe
  const refreshAll = (raw: HourlyPointRaw[]) => {
    if (attendanceRef) attendanceRef.refresh(raw);
    if (roundsRef)     roundsRef.refresh(raw);
    if (visitsRef)     visitsRef.refresh(raw);
  };

  // Detectar zona horaria del cliente (fallback a 'UTC' si falla)
  let tz = 'UTC';
  try { tz = Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC'; } catch {}

  // Función que obtiene y normaliza los datos desde 3 endpoints horarios
  async function fetchHourlyCombined(dateArg?: string): Promise<HourlyPointRaw[]> {
    const q = dateArg ? `?date=${encodeURIComponent(dateArg)}&tz=${encodeURIComponent(tz)}` : `?tz=${encodeURIComponent(tz)}`;

    // Ajusta paths si tus endpoints son distintos
    const attendanceUrl = `/api/attendance/hourly${q}`;
    const patrolsUrl    = `/api/patrols-runs/hourly${q}`; // si tu endpoint difiere, cámbialo
    const visitsUrl     = `/api/site-supervision-visits/hourly-aggregated${q}`;

    try {
      const [ra, rr, rv] = await Promise.all([
        fetchWithAuth(attendanceUrl),
        fetchWithAuth(patrolsUrl),
        fetchWithAuth(visitsUrl)
      ]);

      const a = ra.ok ? await ra.json().catch(() => []) : [];
      const r = rr.ok ? await rr.json().catch(() => []) : [];
      const v = rv.ok ? await rv.json().catch(() => []) : [];

      // convertir a maps por hour (asegurar clave "00".."23")
      const ma = new Map((a as any[]).map((x: any) => [String(x.hour).padStart(2, '0'), x]));
      const mr = new Map((r as any[]).map((x: any) => [String(x.hour).padStart(2, '0'), x]));
      const mv = new Map((v as any[]).map((x: any) => [String(x.hour).padStart(2, '0'), x]));

      const hours = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0'));
      return hours.map(h => ({
        hour: h,
        attendance: Number(ma.get(h)?.count ?? ma.get(h)?.cnt ?? 0),
        attendanceForecast: Number(ma.get(h)?.forecast ?? 0),
        rounds: Number(mr.get(h)?.count ?? mr.get(h)?.cnt ?? 0),
        roundsForecast: Number(mr.get(h)?.forecast ?? 0),
        visits: Number(mv.get(h)?.count ?? mv.get(h)?.cnt ?? 0),
        visitsForecast: Number(mv.get(h)?.forecast ?? 0)
      }));
    } catch (e) {
      console.warn('[initThreeMetrics] fetchHourlyCombined error', e);
      // devolver array vacío con 24 horas para evitar excepciones en refresher
      return Array.from({ length: 24 }, (_, i) => ({ hour: String(i).padStart(2, '0') }));
    }
  }

  // Carga inicial: si date fue provisto, úsalo; si no, puedes llamar sin date (backend decide)
  const initialRaw = await fetchHourlyCombined(date);
  if (initialRaw && initialRaw.length) refreshAll(initialRaw);

  // Refresco periódico usando la función fetchHourlyCombined (actualiza in-place)
  let intervalId: number | undefined;
  if (refreshMs && refreshMs > 0) {
    intervalId = window.setInterval(async () => {
      const raw = await fetchHourlyCombined(date);
      if (raw && raw.length) refreshAll(raw);
    }, refreshMs);
  }

  // Cleanup: cuando se salga del fragmento SPA, detener interval y destruir charts
  const onUnload = () => {
    document.removeEventListener('fragment:will-unload', onUnload as EventListener);
    try { if (intervalId) window.clearInterval(intervalId); } catch {}
    // Cada refresher adosa cleanup al chart bajo __hourly_metric_cleanup o dispondrá el chart
    [attendanceRef, roundsRef, visitsRef].forEach(r => {
      if (!r) return;
      const c = r.chart as any;
      if (c && c.__hourly_metric_cleanup) {
        try { c.__hourly_metric_cleanup(); } catch {}
      } else {
        try { r.chart.dispose(); } catch {}
      }
    });
  };
  document.addEventListener('fragment:will-unload', onUnload, { once: true });

  // Devolver un control para detener manualmente si el llamador lo necesita
  return {
    stop: () => {
      try { if (intervalId) window.clearInterval(intervalId); } catch {}
      onUnload();
    }
  };
}