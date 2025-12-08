export async function fetchWithAuth(url: string,
                          init: RequestInit = {}): Promise<Response> {
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


// ---------------- helper: fetch with timeout ----------------
export async function fetchWithTimeout(
    input: RequestInfo | URL,
    init: RequestInit = {},
    timeoutMs = 15000,
    useFetchWithAuth = false
  ): Promise<Response> {
    const controller = new AbortController();
    const mergedInit: RequestInit = { ...init, signal: controller.signal };
    const timeoutId = setTimeout(() => controller.abort(), timeoutMs);
    try {
      if (useFetchWithAuth) {
        // fetchWithAuth acepta RequestInit y reenvía signal/headers porque hace: const opts = { ..., ...init, headers }
        return await fetchWithAuth(String(input), mergedInit);
      }
      return await fetch(input, mergedInit);
    } finally {
      clearTimeout(timeoutId);
      // No abort aquí; abort already triggered by timeout if needed or the fetch finished.
    }
}

