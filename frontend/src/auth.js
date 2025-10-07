// /js/auth.js
export async function fetchWithAuth(url, init = {}) {
  const token = localStorage.getItem('jwt');

  // Fusiona headers sin pisar los existentes
  const headers = new Headers(init.headers || {});
  if (!headers.has('Accept')) headers.set('Accept', 'application/json');
  if (token) headers.set('Authorization', `Bearer ${token}`);

  // Incluye cookies/sesión si las usas (no molesta si no)
  const opts = {
    credentials: init.credentials ?? 'same-origin',
    ...init,
    headers
  };

  const res = await fetch(url, opts);

  // Si el backend devuelve 401, limpia y manda a login
  if (res.status === 401) {
    localStorage.removeItem('jwt');
    // opcional: guarda ruta actual para volver luego
    try { sessionStorage.setItem('postLoginTarget', location.pathname + location.search + location.hash); } catch {}
    window.location.href = '/auth/signin';
    return new Response(null, { status: 401 });
  }

  return res;
}
