// Utilidades para series temporales y formateo de fechas (cliente)
export type Point = { x: string; y: number };

/**
 * Construye Intl.DateTimeFormat para short month labels en el locale del navegador o uno pasado.
 */
export function getMonthFormatter(locale?: string) {
  const loc = locale ?? (typeof navigator !== 'undefined' ? navigator.language : 'es');
  return new Intl.DateTimeFormat(loc, { day: '2-digit', month: 'short' });
}

/**
 * Convierte 'YYYY-MM-DD' o ISO completo a Date.
 * Si viene solo YYYY-MM-DD construye en zona local.
 */
export function toDateFromIsoDay(isoDay: string | null | undefined): Date | null {
  if (!isoDay) return null;
  const raw = isoDay.includes('T') ? isoDay : `${isoDay}T00:00:00`;
  const d = new Date(raw);
  return Number.isNaN(d.getTime()) ? null : d;
}

/**
 * Etiqueta corta '12-ene' (sin punto). Usa el formatter pasado o crea uno por defecto.
 */
export function shortLabelFromIso(isoDay: string, formatter?: Intl.DateTimeFormat): string {
  const fmt = formatter ?? getMonthFormatter();
  const d = toDateFromIsoDay(isoDay);
  if (!d) return isoDay ?? '';
  const txt = fmt.format(d).replace('.', '');
  return txt.replace(/\s+/, '-');
}

/**
 * Build last N local dates 'YYYY-MM-DD' (browser local timezone).
 * zoneTz argumento se mantiene para compatibilidad si decide usarse (no cambia comportamiento client-side).
 */
export function buildLastNDatesIso(days: number, _zoneTz?: string): string[] {
  const out: string[] = [];
  const now = new Date();
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date(now);
    d.setDate(now.getDate() - i);
    out.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`);
  }
  return out;
}

/**
 * Normaliza diferentes formatos de payload a { x: 'YYYY-MM-DD', y: number }.
 * Acepta arrays [date,value], objetos {x:, y:}, {date:, value:}, {day:, value:}, etc.
 */
export function normalizeToPoints(arr: any[] | null | undefined): Point[] {
  if (!Array.isArray(arr)) return [];
  return arr.map(d => {
    let rawX: any = '';
    if (Array.isArray(d)) rawX = d[0];
    else rawX = d?.x ?? d?.date ?? d?.day ?? '';

    let rawY: any = 0;
    if (Array.isArray(d) && d.length > 1) rawY = d[1];
    else rawY = d?.y ?? d?.value ?? 0;

    const x = isoToLocalIsoDateString(rawX);
    const yNum = (typeof rawY === 'number') ? rawY : Number(String(rawY ?? 0));
    return { x, y: Number.isFinite(yNum) ? yNum : 0 };
  }).filter(p => !!p.x && typeof p.x === 'string' && p.x.length >= 4);
}

/**
 * Normaliza un ISO/fecha a 'YYYY-MM-DD' en zona local del navegador.
 * Maneja input 'YYYY-MM-DD', ISO con zona ó timestamps.
 */
export function isoToLocalIsoDateString(iso: unknown): string {
  if (iso === null || iso === undefined) return '';
  const s = String(iso).trim();
  if (!s) return '';

  // YYYY-MM-DD -> crear Date en zona local
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
    const [yStr, mStr, dStr] = s.split('-');
    const y = Number(yStr), m = Number(mStr), d = Number(dStr);
    if (Number.isFinite(y) && Number.isFinite(m) && Number.isFinite(d)) {
      const dt = new Date(y, m - 1, d);
      return `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`;
    }
    return '';
  }

  const parsed = new Date(s);
  if (Number.isNaN(parsed.getTime())) {
    const part = s.split('T')[0];
    return part || '';
  }
  return `${parsed.getFullYear()}-${String(parsed.getMonth() + 1).padStart(2, '0')}-${String(parsed.getDate()).padStart(2, '0')}`;
}

/**
 * Agrega puntos por fecha (suma si hay múltiples registros en la misma fecha).
 * Devuelve Map<date, sum>.
 */
export function aggregateByDate(points: Point[]): Map<string, number> {
  const m = new Map<string, number>();
  points.forEach(p => {
    if (!p || !p.x) return;
    const prev = m.get(p.x) ?? 0;
    m.set(p.x, prev + (Number.isFinite(Number(p.y)) ? Number(p.y) : 0));
  });
  return m;
}

/**
 * Construye array de valores alineados con labels. Si falta fecha -> 0.
 */
export function valuesForLabels(labels: string[], map: Map<string, number>): number[] {
  return labels.map(l => map.has(l) ? map.get(l)! : 0);
}




