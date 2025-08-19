import { navigateTo } from './navigation-handler.js';
import { setupRouter } from './router.js';
import { setupMegamenu } from './mega-menu.js';
import { setupSlideshow } from './slideshow-video.js';
import { setupChat } from './chat/chat.js';

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

window.initRouter = () => {

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