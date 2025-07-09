import { navigateTo } from './navigation-handler.js';

let routerInitialized = false;

export function setupRouter () {
  if (routerInitialized) return; //Evita una doble inicialización
  routerInitialized = true;

  console.log("Router activado");

  document.addEventListener('click', e => {
    const link = e.target.closest('[data-path]');
    if (link) {
      e.preventDefault();
      const path = link.dataset.path;
      const target = link.dataset.target || path;
      if(target) navigateTo(target);
    }
  });
}