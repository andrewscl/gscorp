// /js/private/menu/private-menu.js
function setupPrivateDrawer() {
  const root = document.body;
  if (!root || root.dataset.layout !== 'private') return;

  const byId = (id) => document.getElementById(id);
  const sidebar   = document.querySelector('.sidebar');
  const btnTopbar = byId('topbarToggleSidebar'); // botón en topbar
  const btnInside = byId('toggleSidebar');       // botón dentro de la sidebar

  // crea scrim si no existe
  let scrim = byId('sidebar-scrim');
  if (!scrim) {
    scrim = document.createElement('div');
    scrim.id = 'sidebar-scrim';
    scrim.className = 'sidebar-scrim';
    document.body.appendChild(scrim);
  }

  if (!sidebar || !btnTopbar) {
    // Aún no están los fragmentos: reintenta cuando carguen
    const retry = () => { tryInit(); };
    document.addEventListener('topbar:loaded', retry, { once: true });
    document.addEventListener('sidebar:loaded', retry, { once: true });
    return;
  }

  // evita doble binding
  if (root.__drawerBound) return;
  root.__drawerBound = true;

  const open  = () => {
    root.classList.add('sidebar-open');
    btnTopbar.setAttribute('aria-expanded', 'true');
  };
  const close = () => {
    root.classList.remove('sidebar-open');
    btnTopbar.setAttribute('aria-expanded', 'false');
  };
  const toggle = () => (root.classList.contains('sidebar-open') ? close() : open());

  btnTopbar.addEventListener('click', toggle);
  btnInside?.addEventListener('click', toggle);
  scrim.addEventListener('click', close);
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape') close(); });
  document.addEventListener('route:loaded', close);
}

function tryInit() {
  // Espera a que el DOM esté listo y a que existan los fragmentos
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', setupPrivateDrawer, { once: true });
  } else {
    // pequeño delay para dar tiempo a que tu SPA inserte topbar/sidebar
    requestAnimationFrame(() => setupPrivateDrawer());
  }
}

// Auto-init: al cargar el módulo y cuando tus fragmentos avisen
tryInit();
document.addEventListener('topbar:loaded', tryInit);
document.addEventListener('sidebar:loaded', tryInit);
