export async function fetchWithAuth(url: string, init: RequestInit = {}): Promise<Response> {
  const token = localStorage.getItem('jwt');
  const headers = new Headers(init.headers || {});
  if (!headers.has('Accept')) headers.set('Accept', 'application/json');
  if (token) headers.set('Authorization', `Bearer ${token}`);

  const opts: RequestInit = {
    credentials: init.credentials ?? 'same-origin',
    ...init,
    headers
  };

  const res = await fetch(url, opts);

  if (res.status === 401) {
    localStorage.removeItem('jwt');
    try { sessionStorage.setItem('postLoginTarget', location.pathname + location.search + location.hash); } catch {}
    window.location.href = '/auth/signin';
    return new Response(null, { status: 401 });
  }

  return res;
}