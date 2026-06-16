import { fetchWithAuth } from './auth.js';

    let currentRoute = null;

    export async function navigateTo(path, force = false) {

    if (path === currentRoute && !force) {
      console.warn(`[navigateTo] Ruta ya cargada: ${path}. Evitando carga duplicada.`);
      return;
    }

    console.log(`[navigateTo] Actualizando currentRoute a: ${path}`);
    currentRoute = path;

    const targetLayout = detectLayout(path);
    const currentLayout = document.body.getAttribute('data-layout');

    const isPublicHome = path === "/public/home";
    // 🔁 Si es /public/home, redirigimos a index-view que carga layout + fragmento
    if (isPublicHome) {
        console.warn("[navigateTo] Redirigiendo a index-view para /public/home");
        window.location.href = "/public/home"; // o la ruta que hayas definido
        return;
    }

    const changeLayout = targetLayout !== currentLayout;

    if (!force && changeLayout) {
        console.warn(`[navigateTo] Cambio de layout: 
                        ${currentLayout} -> ${targetLayout}`);

        sessionStorage.setItem("postLoginTarget", path);
        window.location.href = `/shell/${targetLayout}`;
        return;
    }

    console.log({ force, changeLayout, targetLayout, currentLayout });

    try {
        const html = await fetchHtml(path, targetLayout);
        if (!html) throw new Error("No content");

        const container = document.getElementById('content');
        if (!container) 
                throw new Error("No se encontró el contenedor #content");

        document.dispatchEvent(new CustomEvent('fragment:will-unload', { detail: { path } }));

        container.innerHTML = html;

        // Ejecuta los módulos embebidos del fragmento de forma controlada
        await executeFragmentModules(container, path);
        await maybeLoadExtras(path, container);

        // Actualiza la URL y dispara el evento de ruta cargada
        window.history.pushState({}, '', path);
        console.log(`[navigateTo] Ruta cargada dinámicamente: ${path}`);
        
        // 🔔 Un solo evento para scroll y demás hooks
        document.dispatchEvent(new CustomEvent('route:loaded', { detail: { path } }));

    } catch (err) {
        console.error(`[navigateTo] Error: ${err.message}`);
        alert("Error al navegar a la ruta solicitada.");
    }
}

export function detectLayout(path) {
  if (path === '/auth' || path.startsWith('/auth/')) return 'auth';
  if (path === '/private' || path.startsWith('/private/')) return 'private';
  if (path === '/admin' || path.startsWith('/admin/')) return 'private'; // o 'admin' si tienes un layout exclusivo
  return 'public';
}

async function fetchHtml(path, layout) {

    const token = localStorage.getItem("jwt");

    // Agrega el flag por querystring
    const sep = path.includes('?') ? '&' : '?' ;
    const url = `${path}${sep}fragment=1`;

    if (layout === 'private' && token) {
        return fetchWithAuth(url).then(res => res.ok ? res.text() : null);
    }

    return fetch(url).then(res => res.ok ? res.text() : null);
}

async function executeFragmentModules(container, path) {
  const scripts = container.querySelectorAll('script[type="module"]');
  console.log(`[navigation-handler-Diagnostic] Scripts encontrados en ${path}:`, scripts.length);

  for (const s of scripts) {
    console.log(`[navigation-handler-Diagnostic] Evaluando etiqueta script. src original: "${s.src}"`);
    if (s.src) {
      // Forzar re-ejecución del módulo usando cache-buster
      const u = new URL(s.src, location.origin);
      u.searchParams.set('v', Date.now());
      console.log(`[navigation-handler-Diagnostic] Disparando import dinámico a: ${u.href}`);
      const mod = await import(u.href);
      if (typeof mod.init === 'function') mod.init({ container, path });
    } else {
      // Ejecutar módulo inline como Blob (aislado)
      const blobUrl = URL.createObjectURL(new Blob([s.textContent], { type: 'text/javascript' }));
      const mod = await import(blobUrl);
      URL.revokeObjectURL(blobUrl);
      if (typeof mod.init === 'function') mod.init({ container, path });
    }
  }
}

async function maybeLoadExtras(path, container) {
  if (path === '/public/contact') {
    const { setupContact } = await import(`./contact.js?v=${Date.now()}`);
    if (typeof setupContact === 'function') {
      await setupContact({ container, path });
    }
  }
  if (path.startsWith('/private/sites/view-site')) {
    const { init } = await import(`./private/sites/view-site.js?v=${Date.now()}`);
    if (typeof init === 'function') {
      init({ container, path });
    }
  }
  // aquí puedes ir agregando más rutas especiales si hace falta
}

