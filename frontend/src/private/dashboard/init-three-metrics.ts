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
 * Inicializa los 3 charts horarios y hace fetch separado por métrica (attendance, rounds, visits).
 */
export async function initThreeMetrics(
  container: HTMLElement,
  date?: string,
  opts?: { refreshMs?: number }
) {
  const refreshMs = opts?.refreshMs ?? 0;

  // Inicializa los 3 charts y obtén sus "refresher"
  const attendanceRef = await initHourlyMetricChart(container, 'attendance', 'Asistencias', '#chart-hourly-attendance', undefined);
  const roundsRef     = await initHourlyMetricChart(container, 'rounds',     'Rondas',     '#chart-hourly-rounds',     undefined);
  const visitsRef     = await initHourlyMetricChart(container, 'visits',     'Visitas',    '#chart-hourly-visits',     undefined);

  const refreshAll = (raw: HourlyPointRaw[]) => {
    if (attendanceRef) attendanceRef.refresh(raw);
    if (roundsRef)     roundsRef.refresh(raw);
    if (visitsRef)     visitsRef.refresh(raw);
  };

  // Detectar zona horaria del cliente (fallback a 'UTC')
  let tz = 'UTC';
  try { tz = Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC'; } catch {}

  // Helpers
  const hours24 = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0'));

  function extractHour(item: any): string {
    const raw = item?.hour ?? item?.x ?? item?.label ?? item?.h ?? item?.time ?? '';
    const hh = String(raw ?? '').padStart(2, '0').slice(-2);
    return /^[0-2]\d$/.test(hh) ? hh : '00';
  }

  function extractNumber(item: any, keys: string[]): number {
    for (const k of keys) {
      const v = item?.[k];
      if (v !== undefined && v !== null && v !== '') {
        const n = Number(v);
        if (!Number.isNaN(n)) return n;
      }
    }
    return 0;
  }

  // Normalizadores por métrica: devuelven Map<hour, {count, forecast?}>
  async function fetchAttendanceMap(dateArg?: string): Promise<Map<string, { count: number; forecast?: number }>> {
    const q = dateArg ? `?date=${encodeURIComponent(dateArg)}&tz=${encodeURIComponent(tz)}` : `?tz=${encodeURIComponent(tz)}`;
    const url = `/api/attendance/hourly${q}`;
    try {
      const res = await fetchWithAuth(url);
      const arr = res.ok ? await res.json().catch(() => []) : [];
      const m = new Map<string, { count: number; forecast?: number }>();
      (arr || []).forEach((it: any) => {
        const hh = extractHour(it);
        const count = extractNumber(it, ['count','cnt','y','value','attendance']);
        const forecast = extractNumber(it, ['forecast','f','attendanceForecast']);
        m.set(hh, { count, forecast: forecast || undefined });
      });
      return m;
    } catch (e) {
      console.warn('[initThreeMetrics] fetchAttendanceMap error', e);
      return new Map();
    }
  }

  async function fetchRoundsMap(dateArg?: string): Promise<Map<string, { count: number; forecast?: number }>> {
    const q = dateArg ? `?date=${encodeURIComponent(dateArg)}&tz=${encodeURIComponent(tz)}` : `?tz=${encodeURIComponent(tz)}`;
    const url = `/api/patrols-runs/hourly${q}`;
    try {
      const res = await fetchWithAuth(url);
      const arr = res.ok ? await res.json().catch(() => []) : [];
      const m = new Map<string, { count: number; forecast?: number }>();
      (arr || []).forEach((it: any) => {
        const hh = extractHour(it);
        const count = extractNumber(it, ['count','cnt','y','value','rounds']);
        const forecast = extractNumber(it, ['forecast','f','roundsForecast']);
        m.set(hh, { count, forecast: forecast || undefined });
      });
      return m;
    } catch (e) {
      console.warn('[initThreeMetrics] fetchRoundsMap error', e);
      return new Map();
    }
  }

  async function fetchVisitsMap(dateArg?: string): Promise<Map<string, { count: number; forecast?: number }>> {
    const q = dateArg ? `?date=${encodeURIComponent(dateArg)}&tz=${encodeURIComponent(tz)}` : `?tz=${encodeURIComponent(tz)}`;
    const url = `/api/site-supervision-visits/hourly-aggregated${q}`; // aggregated endpoint
    try {
      const res = await fetchWithAuth(url);
      const arr = res.ok ? await res.json().catch(() => []) : [];
      const m = new Map<string, { count: number; forecast?: number }>();
      (arr || []).forEach((it: any) => {
        const hh = extractHour(it);
        // accept shapes like {x,y} or {hour,count} or {hour,count,forecast}
        const count = extractNumber(it, ['count','cnt','y','value','visits']);
        const forecast = extractNumber(it, ['forecast','f','visitsForecast']);
        m.set(hh, { count, forecast: forecast || undefined });
      });
      return m;
    } catch (e) {
      console.warn('[initThreeMetrics] fetchVisitsMap error', e);
      return new Map();
    }
  }

  // Combinar las 3 métricas en la estructura HourlyPointRaw[]
  async function fetchAndCombine(dateArg?: string): Promise<HourlyPointRaw[]> {
    const [ma, mr, mv] = await Promise.all([
      fetchAttendanceMap(dateArg),
      fetchRoundsMap(dateArg),
      fetchVisitsMap(dateArg)
    ]);

    return hours24.map(h => ({
      hour: h,
      attendance: ma.get(h)?.count ?? 0,
      attendanceForecast: ma.get(h)?.forecast ?? 0,
      rounds: mr.get(h)?.count ?? 0,
      roundsForecast: mr.get(h)?.forecast ?? 0,
      visits: mv.get(h)?.count ?? 0,
      visitsForecast: mv.get(h)?.forecast ?? 0
    }));
  }

  // Carga inicial
  const initialRaw = await fetchAndCombine(date);
  if (initialRaw && initialRaw.length) refreshAll(initialRaw);

  // Refresco periódico
  let intervalId: number | undefined;
  if (refreshMs && refreshMs > 0) {
    intervalId = window.setInterval(async () => {
      const raw = await fetchAndCombine(date);
      if (raw && raw.length) refreshAll(raw);
    }, refreshMs);
  }

  // Cleanup
  const onUnload = () => {
    document.removeEventListener('fragment:will-unload', onUnload as EventListener);
    try { if (intervalId) window.clearInterval(intervalId); } catch {}
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

  return {
    stop: () => {
      try { if (intervalId) window.clearInterval(intervalId); } catch {}
      onUnload();
    }
  };
}