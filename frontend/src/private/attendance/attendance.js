// src/private/attendance/attendance-basic.js
import { fetchWithAuth } from '../../auth.js';

(function () {
  const $ = (id) => document.getElementById(id);
  const root = document.getElementById('att-widget');
  if (!root) return;

  const statusEl = $('att-status');
  const btnIn    = $('att-in');
  const btnOut   = $('att-out');

  const endpoint = root.dataset.punchEndpoint || '/api/attendance/punch';

  function setStatus(text, isError = false) {
    if (!statusEl) return;
    statusEl.textContent = text;
    statusEl.style.color = isError ? '#b00020' : '#444';
  }

  function disable(val) {
    btnIn && (btnIn.disabled = val);
    btnOut && (btnOut.disabled = val);
  }

  function getPosition() {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) return reject(new Error('Geolocalización no soportada'));
      navigator.geolocation.getCurrentPosition(
        (p) => resolve(p),
        (e) => reject(new Error(e.message || 'No se pudo obtener ubicación')),
        { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 }
      );
    });
  }

  async function punch(action) {
    try {
      disable(true);
      setStatus('Obteniendo ubicación...');
      const pos = await getPosition();

      const payload = {
        action,                              // 'IN' | 'OUT'
        lat: pos.coords.latitude,
        lon: pos.coords.longitude,
        accuracy: pos.coords.accuracy
      };

      setStatus('Registrando asistencia...');
      const res = await fetchWithAuth(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(payload)
      });

      if (!res.ok) {
        const txt = await res.text().catch(()=>'');
        throw new Error(txt || `Error ${res.status}`);
      }

      const out = await res.json().catch(()=>({}));
      const hora = out.ts ? new Date(out.ts).toLocaleTimeString('es-CL', { hour12: false }) : '';
      setStatus(`Marcación ${action === 'IN' ? 'de entrada' : 'de salida'} ${hora ? 'a las ' + hora : ''} ✅`);
    } catch (e) {
      setStatus('Error en marcación: ' + e.message, true);
    } finally {
      disable(false);
    }
  }

  btnIn?.addEventListener('click', () => punch('IN'));
  btnOut?.addEventListener('click', () => punch('OUT'));
})();
