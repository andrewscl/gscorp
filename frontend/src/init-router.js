import { navigateTo } from './navigation-handler.js';
import { setupRouter } from './router.js';
import { setupMegamenu } from './mega-menu.js';
import { setupSlideshow } from './slideshow-video.js';
import { setupChat } from './chat/chat.js';
import { fetchWithAuth } from './auth.js';

function doScrollTo(pathLike) {
  // si el menú móvil dejó bloqueado el scroll, lo limpiamos
  document.documentElement.classList.remove('no-scroll');
  document.body.classList.remove('no-scroll');

  // doble rAF: asegura que el DOM del fragmento ya se pintó
  requestAnimationFrame(() => requestAnimationFrame(() => {
    const url  = new URL(pathLike || location.href, location.origin);
    const hash = url.hash?.slice(1);

    // target principal: ventana
    const scrollWindow = () => window.scrollTo({ top: 0, behavior: 'auto' });

    // si usas contenedor con overflow (por ejemplo #content)
    const content = document.getElementById('content');
    const canScrollContent = content && getComputedStyle(content).overflowY !== 'visible';

    if (hash) {
      const el = document.getElementById(decodeURIComponent(hash));
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'start' });
        return;
      }
    }

    // sin hash o no existe → arriba
    if (canScrollContent) {
      content.scrollTo({ top: 0, behavior: 'auto' });
    }
    scrollWindow();
  }));
}

//Carga sidebar y topbar cuando el shell es "private"
async function loadChromeIfNeeded(){
  if(document.body.getAttribute('data-layout') != 'private')
    return;

  try{
    const[sbRes, tbRes] = await Promise.all([
      fetchWithAuth('/private/sidebar?fragment=1'),
      fetchWithAuth('/private/topbar?fragment=1')
    ]);

    if(sbRes.status === 401 || tbRes.status === 401){
      localStorage.removeItem('jwt');
      window.location.href = '/auth/signin';
      return;
    }

    if(sbRes.ok) {
      const html = await sbRes.text();
      const sb = document.getElementById('sidebar');
      if (sb) sb.innerHTML = html;
      document.dispatchEvent(new CustomEvent('sidebar:loaded'));
    }

    if(tbRes.ok) {
      const html = await tbRes.text();
      const tb = document.querySelector('.topbar');
      if (tb) tb.innerHTML = html;
      document.dispatchEvent(new CustomEvent('topbar:loaded'));
    }
  } catch (e) {
    console.warn('[init-router] No se pudieron cargar sidebar/topbar', e);
  }
}

window.initRouter = async () => {

  // 1) BIND de listeners ANTES de cualquier navegación
  if (!window.__routeScrollBound) {
    document.addEventListener('route:loaded', (e) => {
      const path = e.detail?.path || (location.pathname + location.hash);
      doScrollTo(path);
    });
    // opcional: cuando el usuario usa Atrás/Adelante
    window.addEventListener('popstate', () => doScrollTo(location.href));
    window.__routeScrollBound = true;
  }

  console.log("Router reinicializado");
  setupRouter();

  //Carga chrome del rol si estamos en shell/private
  await loadChromeIfNeeded();

  const target = sessionStorage.getItem("postLoginTarget");

  if(target) {
    sessionStorage.removeItem("postLoginTarget");
    console.log
          ("[init-router] Redirigiendo a:", target);
    navigateTo(target, true);
  } else {
    console.log
          ("[init-router] Reiniciando router", target);
  }
  //inicializar componentes
  setupMegamenu();
  setupSlideshow();
  setupChat();

}

//ejecutar automaticamente el init-router.
window.initRouter();