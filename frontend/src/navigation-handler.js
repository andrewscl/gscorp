import { fetchWithAuth } from './auth.js';
import { setupRouter } from './router.js';

    export async function navigateTo(path, force = false) {

    const targetLayout = detectLayout(path);
    const currentLayout = document.body.getAttribute('data-layout');

/*    const isPublicHome = path === "/public/home";
    // 🔁 Si es /public/home, redirigimos a index-view que carga layout + fragmento
    if (isPublicHome) {
        console.warn("[navigateTo] Redirigiendo a index-view para /public/home");
        window.location.href = "/public/home"; // o la ruta que hayas definido
        return;
    }
*/

    const changeLayout = targetLayout !== currentLayout;

    if (!force && changeLayout) {
        console.warn(`[navigateTo] Cambio de layout: 
                        ${currentLayout} -> ${targetLayout}`);
        const token = localStorage.getItem("jwt");
        const isPrivate = 
                targetLayout ==='private' || targetLayout === 'admin';
        
        //Si requiere token, forzamos navigateTo tras layout reload
        if(isPrivate && token) {

            sessionStorage.setItem("postLoginTarget", path);
            window.location.href = `/shell/${targetLayout}`;
            return;
        }

        sessionStorage.setItem("postLoginTarget", path);
        window.location.href = `/shell/${targetLayout}`;
        return;
    }

    try {
        const html = await fetchHtml(path, targetLayout);
        if (!html) throw new Error("No content");

        const container = document.getElementById('content');
        if (!container) 
                throw new Error("No se encontró el contenedor #content");

        container.innerHTML = html;

        //Reinsertar scripts embebidos (fragments)
        const scripts = container.querySelectorAll('script[type="module"]');
        scripts.forEach(script => {
            const newScript = document.createElement('script');
            newScript.type = 'module';
            if (script.src) {
                newScript.src = script.src;
            } else {
                newScript.textContent = script.textContent;
            }
            document.body.appendChild(newScript);
        });

        window.history.pushState({}, '', path);
        setupRouter();
        console.log(`[navigateTo] Ruta cargada dinámicamente: ${path}`);

        if(path === '/public/contact'){
            import ('./contact.js')
                .then(({setupContact}) => {
                setupContact();
                console.log(`[navigateTo] setupContact activado`);
            })
        }

    } catch (err) {
        console.error(`[navigateTo] Error: ${err.message}`);
        alert("Error al navegar a la ruta solicitada.");
    }
}

export function detectLayout(path) {
  if (path.startsWith('/auth/')) return 'auth';
  if (path.startsWith('/private/')) return 'private';
  if (path.startsWith('/admin/')) return 'private'; // o 'admin' si tienes un layout exclusivo
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