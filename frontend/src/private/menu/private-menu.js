// /js/private/menu/private-menu.js
import { navigateTo } from '../../navigation-handler.js'; // ajusta ruta si es necesario

(function () {
  const ROOT_ATTR = 'data-layout';
  const LAYOUT_PRIVATE = 'private';

  function ensureScrim() {
    let scrim = document.getElementById('sidebar-scrim');
    if (!scrim) {
      scrim = document.createElement('div');
      scrim.id = 'sidebar-scrim';
      scrim.className = 'sidebar-scrim';
      document.body.appendChild(scrim);
    }
    return scrim;
  }

  function closeDrawer() {
    const root = document.body;
    root.classList.remove('sidebar-open');
    const btnTop = document.getElementById('topbarToggleSidebar');
    btnTop?.setAttribute('aria-expanded', 'false');
  }

  function openDrawer() {
    const root = document.body;
    root.classList.add('sidebar-open');
    const btnTop = document.getElementById('topbarToggleSidebar');
    btnTop?.setAttribute('aria-expanded', 'true');
  }

  function toggleDrawer() {
    const root = document.body;
    if (root.classList.contains('sidebar-open')) closeDrawer();
    else openDrawer();
  }

  // Vincula burger, scrim y acordeón UNA VEZ por carga de sidebar
  function bindSidebar() {
    if (document.body.getAttribute(ROOT_ATTR) !== LAYOUT_PRIVATE) return;

    const menu = document.querySelector('.sidebar .menu');
    const sidebar = document.querySelector('.sidebar');
    const btnTopbar = document.getElementById('topbarToggleSidebar');
    const btnInside = document.getElementById('toggleSidebar');
    const scrim = ensureScrim();

    if (!menu || !sidebar) return;

    // Evita doble-bind en la MISMA instancia de sidebar
    if (sidebar.__bound) return;
    sidebar.__bound = true;

    // Burger / scrim
    btnTopbar?.addEventListener('click', toggleDrawer);
    btnInside?.addEventListener('click', toggleDrawer);
    scrim.addEventListener('click', closeDrawer);
    document.addEventListener('keydown', (e) => { if (e.key === 'Escape') closeDrawer(); });

    // Acordeón: un <details> abierto a la vez
    menu.addEventListener('toggle', (e) => {
      const d = e.target;
      if (!(d instanceof HTMLDetailsElement) || !d.open) return;
      menu.querySelectorAll('details.menu-group[open]').forEach((other) => {
        if (other !== d) other.open = false;
      });
    });

    // Delegación de clicks en data-path (SPA)
    menu.addEventListener('click', async (e) => {
      const link = e.target.closest('[data-path]');
      if (!link) return;
      e.preventDefault();
      const path = link.dataset.path;
      if (!path) return;

      // UX: cierra grupos y drawer antes de navegar
      menu.querySelectorAll('details.menu-group[open]').forEach((d) => (d.open = false));
      closeDrawer();

      try {
        await navigateTo(path, false);
      } catch (err) {
        console.error('[private-menu] navigateTo error', err);
      }
    });
  }

  // Vincula elementos de topbar (burger) tras cargarse el fragmento
  function bindTopbar() {
    if (document.body.getAttribute(ROOT_ATTR) !== LAYOUT_PRIVATE) return;
    const btnTopbar = document.getElementById('topbarToggleSidebar');
    if (!btnTopbar || btnTopbar.__bound) return;
    btnTopbar.__bound = true;
    btnTopbar.addEventListener('click', toggleDrawer);
  }

  // Cierra el drawer después de navegar
  document.addEventListener('route:loaded', closeDrawer);

  // Re-bind SIEMPRE que vuelvas a cargar los fragmentos
  document.addEventListener('sidebar:loaded', bindSidebar);
  document.addEventListener('topbar:loaded', bindTopbar);

  // Primer intento al cargar la página (por si el layout ya trae los fragments)
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => { bindSidebar(); bindTopbar(); }, { once: true });
  } else {
    bindSidebar();
    bindTopbar();
  }
})();
