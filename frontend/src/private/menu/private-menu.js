import { navigateTo } from '../../navigation-handler.js';

(function () {
  const LAYOUT = 'private';

  const isPrivate = () => document.body?.dataset?.layout === LAYOUT;

  // Sidebar open/close for mobile
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

  // SPA navigation, only closes drawer, does not collapse menu groups
  document.addEventListener('click', async (e) => {
    if (!isPrivate()) return;
    const link = e.target.closest('[data-path]');
    if (!link) return;

    e.preventDefault();
    const path = link.getAttribute('data-path');
    if (!path) return;

    closeDrawer();

    try {
      await navigateTo(path, false);
    } catch (err) {
      console.error('[private-menu] navigateTo error', err);
      alert('No se pudo navegar a la ruta solicitada.');
    }
  });

  // Custom Acordeón: only one menu-group open at a time (details)
  document.addEventListener('click', (e) => {
    if (!isPrivate()) return;
    // Click on summary.menu-item inside details.menu-group
    const summary = e.target.closest('summary.menu-item');
    if (!summary) return;
    const details = summary.parentElement;
    if (!(details instanceof HTMLDetailsElement) || !details.classList.contains('menu-group')) return;
    if (!details.open) {
      // Cierra otros menu-group abiertos
      document.querySelectorAll('.sidebar .menu-group[open]').forEach(d => {
        if (d !== details) d.open = false;
      });
      // Abre el seleccionado (dejamos al browser hacerlo por defecto)
    }
    // Si ya está abierto, el browser lo cerrará
  });

  // Burger & scrim for drawer
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

  // Scrim
  let s = document.getElementById('sidebar-scrim');
  if (!s) {
    s = document.createElement('div');
    s.id = 'sidebar-scrim';
    s.className = 'sidebar-scrim';
    document.body.appendChild(s);
  }
  if (!s.__bound) {
    s.__bound = true;
    s.addEventListener('click', closeDrawer);
  }
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape') closeDrawer(); });
  document.addEventListener('route:loaded', closeDrawer);

  // Observe DOM for dynamic fragments
  const mo = new MutationObserver(() => {
    if (!isPrivate()) return;
    bindBurgerOnce(document.getElementById('topbarToggleSidebar'));
    bindInsideTogglerOnce(document.getElementById('toggleSidebar'));
  });
  mo.observe(document.body, { childList: true, subtree: true });
  bindBurgerOnce(document.getElementById('topbarToggleSidebar'));
  bindInsideTogglerOnce(document.getElementById('toggleSidebar'));
})();