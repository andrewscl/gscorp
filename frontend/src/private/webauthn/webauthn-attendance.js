// attendance.js (ES module)
(function () {
  const byId = (id) => document.getElementById(id);

  function setStatus(text, isError = false) {
    const el = byId('att-status');
    if (!el) return;
    el.textContent = text;
    el.style.color = isError ? '#b00020' : '#444';
  }

  function b64uToBuf(s) {
    s = s.replace(/-/g, '+').replace(/_/g, '/');
    const pad = s.length % 4;
    if (pad) s += '='.repeat(4 - pad);
    const bin = atob(s);
    const buf = new Uint8Array(bin.length);
    for (let i = 0; i < bin.length; i++) buf[i] = bin.charCodeAt(i);
    return buf.buffer;
  }

  async function getPosition() {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) return reject(new Error('Geolocalización no soportada'));
      navigator.geolocation.getCurrentPosition(
        (p) => resolve(p),
        (e) => reject(new Error(e.message || 'No se pudo obtener ubicación')),
        { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 }
      );
    });
  }

  function authzHeaders() {
    const jwt = localStorage.getItem('jwt');
    return jwt ? { Authorization: 'Bearer ' + jwt } : {};
  }

  async function registerPasskey(endpoints) {
    try {
      setStatus('Generando challenge de registro...');
      const optRes = await fetch(endpoints.registerOptions, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authzHeaders() },
        credentials: 'same-origin',
      });
      if (!optRes.ok) {
        setStatus('No se pudo obtener opciones de registro', true);
        return;
      }
      const options = await optRes.json();

      let cred;
      if (window.PublicKeyCredential?.parseCreationOptionsFromJSON) {
        cred = await navigator.credentials.create({
          publicKey: PublicKeyCredential.parseCreationOptionsFromJSON(options),
        });
      } else {
        options.challenge = b64uToBuf(options.challenge);
        if (options.user && options.user.id) options.user.id = b64uToBuf(options.user.id);
        cred = await navigator.credentials.create({ publicKey: options });
      }

      setStatus('Verificando passkey...');
      const verRes = await fetch(endpoints.registerVerify, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authzHeaders() },
        credentials: 'same-origin',
        body: JSON.stringify(cred.toJSON()),
      });
      if (!verRes.ok) {
        setStatus('Registro de passkey rechazado', true);
        return;
      }
      setStatus('Passkey registrada correctamente ✅');
    } catch (e) {
      setStatus('Error registrando passkey: ' + e.message, true);
    }
  }

  async function punch(endpoints, btn) {
    try {
      btn && (btn.disabled = true);
      setStatus('Obteniendo ubicación...');
      const pos = await getPosition();
      const lat = pos.coords.latitude,
        lon = pos.coords.longitude,
        acc = pos.coords.accuracy;

      setStatus('Solicitando verificación WebAuthn...');
      const optRes = await fetch(endpoints.punchOptions, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authzHeaders() },
        credentials: 'same-origin',
      });

      if (optRes.status === 412) {
        setStatus('No tienes passkey registrada. Usa "Registrar passkey" y vuelve a intentar.', true);
        btn && (btn.disabled = false);
        return;
      }
      if (!optRes.ok) {
        setStatus('No se pudo iniciar la autenticación', true);
        btn && (btn.disabled = false);
        return;
      }
      const options = await optRes.json();

      let assertion;
      if (window.PublicKeyCredential?.parseRequestOptionsFromJSON) {
        assertion = await navigator.credentials.get({
          publicKey: PublicKeyCredential.parseRequestOptionsFromJSON(options),
        });
      } else {
        options.challenge = b64uToBuf(options.challenge);
        if (Array.isArray(options.allowCredentials)) {
          options.allowCredentials = options.allowCredentials.map((c) => ({
            ...c,
            id: b64uToBuf(c.id),
          }));
        }
        assertion = await navigator.credentials.get({ publicKey: options });
      }

      setStatus('Verificando y registrando asistencia...');
      const payload = {
        assertionJSON: JSON.stringify(assertion.toJSON()),
        lat,
        lon,
        accuracy: acc,
      };

      const verRes = await fetch(endpoints.punchVerify, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authzHeaders() },
        credentials: 'same-origin',
        body: JSON.stringify(payload),
      });

      if (!verRes.ok) {
        const txt = await verRes.text();
        setStatus('Error en marcación: ' + txt, true);
        btn && (btn.disabled = false);
        return;
      }

      const out = await verRes.json();
      const hora = new Date(out.ts).toLocaleTimeString('es-CL', { hour12: false });
      setStatus(`Marcación ${out.action} a las ${hora} ✅`);
      setTimeout(() => btn && (btn.disabled = false), 1200);
    } catch (e) {
      setStatus('Error: ' + e.message, true);
      btn && (btn.disabled = false);
    }
  }

  function initAttendanceWidget() {
    const root = document.getElementById('att-widget');
    const btnPunch = document.getElementById('att-punch');
    const btnReg = document.getElementById('att-register');
    if (!root || !btnPunch || !btnReg) return;

    const endpoints = {
      registerOptions: root.dataset.registerOptions || '/passkeys/register/options',
      registerVerify: root.dataset.registerVerify || '/passkeys/register/verify',
      punchOptions: root.dataset.punchOptions || '/api/attendance/punch/options',
      punchVerify: root.dataset.punchVerify || '/api/attendance/punch/verify',
    };

    btnPunch.addEventListener('click', () => punch(endpoints, btnPunch));
    btnReg.addEventListener('click', () => registerPasskey(endpoints));
  }

  // Auto-init cuando el fragmento esté presente en la página
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initAttendanceWidget);
  } else {
    initAttendanceWidget();
  }
})();
