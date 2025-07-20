import { navigateTo } from './navigation-handler.js';
import { setupRouter } from './router.js';
import { setupMegamenu } from './mega-menu.js';
import { setupSlideshow } from './slideshow-video.js';
import { setupChat } from './chat/chat.js';


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

}

//ejecutar automaticamente el init-router.
window.initRouter();