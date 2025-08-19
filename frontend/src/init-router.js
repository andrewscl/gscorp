import { navigateTo } from './navigation-handler.js';
import { setupRouter } from './router.js';
import { setupMegamenu } from './mega-menu.js';
import { setupSlideshow } from './slideshow-video.js';
import { setupChat } from './chat/chat.js';

function doScrollTo(pathLike) {
  // Doble rAF: asegura que el DOM del fragmento ya está pintado
  requestAnimationFrame(() => requestAnimationFrame(() => {
    const url  = new URL(pathLike || location.href, location.origin);
    const hash = url.hash?.slice(1);

    if (hash) {
      const el = document.getElementById(decodeURIComponent(hash));
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'start' });
        return;
      }
    }
    // Sin hash o no existe el elemento → arriba del todo
    window.scrollTo({ top: 0, behavior: 'auto' }); // “auto” evita doble suavizado
  }));
}

window.initRouter = () => {

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

  //Scroll hacia arriba al cargar un nuevo fragmento o pagina 
  document.addEventListener('route:loaded', (e) => {
    const path = e.detail?.path || (location.pathname + location.hash);
    doScrollTo(path);
  });

}

//ejecutar automaticamente el init-router.
window.initRouter();