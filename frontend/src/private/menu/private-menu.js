// /js/private/menu/private-menu.js
import { navigateTo } from '../../navigation-handler.js'; // ajusta la ruta si es necesario

function setupPrivateDrawer() {
  const root = document.body;
  if (!root || root.dataset.layout !== 'private') return;

  const $id = (id) => document.getElementById(id);
  const sidebar  = document.querySelector('.sidebar');
  const menu     = document.querySelector('.sidebar .menu');
  const btnTopbar = $id('topbarToggleSidebar');
  const btnInside = $id('toggleSidebar');

  // crea scrim si no existe
  let scrim = $id('sidebar-scrim');
  if (!scrim) {
    scrim = document.createElement('div');
    scrim.id = 'sidebar-scrim';
    scrim.className = 'sidebar-scrim';
    document.body.appendChild(scrim);
  }

  if (!sidebar || !btnTopbar || !menu) {
    const retry = () => tryInit();
    document.addEventListener('topbar:loaded', retry, { once: true });
    document.addEventListener('sidebar:loaded', retry, { once: true });
    return;
  }

  if (root.__drawerBound) return;
  root.__drawerBound = true;

  // ----- Drawer -----
  const open  = () => { root.classList.add('sidebar-open');  btnTopbar.setAttribute('aria-expanded','true'); };
  const close = () => { root.classList.remove('sidebar-open'); btnTopbar.setAttribute('aria-expanded','false'); };
  const toggle = () => (root.classList.contains('sidebar-open') ? close() : open());

  btnTopbar.addEventListener('click', toggle);
  btnInside?.addEventListener('click', toggle);
  scrim.addEventListener('click', close);
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape') close(); });
  document.addEventListener('route:loaded', close);

  // ----- Acordeón: un <details> abierto a la vez -----
  menu.addEventListener('toggle', (e) => {
    const d = e.target;
    if (!(d instanceof HTMLDetailsElement) || !d.open) return;
    menu.querySelectorAll('details.menu-group[open]').forEach((other) => {
      if (other !== d) other.open = false;
    });
  });

  // ----- Navegación SPA con data-path -----
  menu.addEventListener('click', async (e) => {
    const link = e.target.closest('[data-path]');
    if (!link) return;

    // Evita el comportamiento por defecto del <a href="#">
    e.preventDefault();

    const path = link.dataset.path;
    if (!path) return;

    // Cierra grupos y drawer (móvil) ANTES de navegar (mejor UX)
    menu.querySelectorAll('details.menu-group[open]').forEach((d) => (d.open = false));
    close();

    // Usa tu SPA navigation-handler
    try {
      await navigateTo(path, false);
    } catch (err) {
      console.error('[private-menu] navigateTo error', err);
    }
  });
}

function tryInit() {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', setupPrivateDrawer, { once: true });
  } else {
    requestAnimationFrame(setupPrivateDrawer);
  }
}

tryInit();
document.addEventListener('topbar:loaded', tryInit);
document.addEventListener('sidebar:loaded', tryInit);
