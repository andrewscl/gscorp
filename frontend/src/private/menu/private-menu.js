// /js/private/menu/private-menu.js

import { navigateTo } from '../../navigation-handler.js';

(function () {
  const LAYOUT = 'private';

  /** Utils **/
  const isPrivate = () => document.body?.dataset?.layout === LAYOUT;
  const ensureScrim = () => {
    let s = document.getElementById('sidebar-scrim');
    if (!s) {
      s = document.createElement('div');
      s.id = 'sidebar-scrim';
      s.className = 'sidebar-scrim';
      document.body.appendChild(s);
    }
    return s;
  };
  const openDrawer = () => {
    if (!isPrivate()) return;
    document.body.classList.add('sidebar-open');
    document.getElementById('topbarToggleSidebar')?.setAttribute('aria-expanded','true');
  };
  const closeDrawer = () => {
    if (!isPrivate()) return;
    document.body.classList.remove('sidebar-open');
    document.getElementById('topbarToggleSidebar')?.setAttribute('aria-expanded','false');
  };
  const toggleDrawer = () => {
    if (document.body.classList.contains('sidebar-open')) closeDrawer();
    else openDrawer();
  };

  /** Delegación global de navegación SPA **/
  document.addEventListener('click', async (e) => {
    if (!isPrivate()) return;
    const link = e.target.closest('[data-path]');
    if (!link) return;

    e.preventDefault();
    const path = link.getAttribute('data-path');
    if (!path) return;

    // Solo cierra drawer en móvil, ya no colapsa <details> abiertos.
    closeDrawer();

    // Navega por SPA
    try {
      await navigateTo(path, false);
    } catch (err) {
      console.error('[private-menu] navigateTo error', err);
      alert('No se pudo navegar a la ruta solicitada.');
    }
  });

  /** Delegación para acordeón (<details>) – sólo uno abierto **/
  document.addEventListener('toggle', (e) => {
    if (!isPrivate()) return;
    const d = e.target;
    if (!(d instanceof HTMLDetailsElement)) return;
    if (!d.classList.contains('menu-group')) return;
    if (!d.open) return; // Solo cuando se abre

    const menu = d.closest('.menu');
    if (!menu) return;
    menu.querySelectorAll('details.menu-group[open]').forEach(other => {
      if (other !== d) other.open = false;
    });
  });

  /** Burger + scrim con MutationObserver (funciona aunque reemplaces innerHTML) **/
  const bindBurgerOnce = (btn) => {
    if (!btn || btn.__bound) return;
    btn.__bound = true;
    btn.addEventListener('click', toggleDrawer);
  };

  const bindInsideTogglerOnce = (btn) => {
    if (!btn || btn.__bound) return;
    btn.__bound = true;
    btn.addEventListener('click', toggleDrawer);
  };

  const scrim = ensureScrim();
  if (!scrim.__bound) {
    scrim.__bound = true;
    scrim.addEventListener('click', closeDrawer);
  }
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape') closeDrawer(); });
  document.addEventListener('route:loaded', closeDrawer);

  // Observa el body por si llegan/ cambian los fragmentos
  const mo = new MutationObserver(() => {
    if (!isPrivate()) return;
    // Burger en topbar
    bindBurgerOnce(document.getElementById('topbarToggleSidebar'));
    // Botón dentro de la sidebar (opcional)
    bindInsideTogglerOnce(document.getElementById('toggleSidebar'));
  });
  mo.observe(document.body, { childList: true, subtree: true });

  // Intento inicial (por si ya están en DOM)
  bindBurgerOnce(document.getElementById('topbarToggleSidebar'));
  bindInsideTogglerOnce(document.getElementById('toggleSidebar'));
})();