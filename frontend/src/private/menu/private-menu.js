function setupPrivateDrawer(){
  const root = document.body;
  if (root.getAttribute('data-layout') !== 'private') return;

  const byId = id => document.getElementById(id);
  const sidebar   = document.querySelector('.sidebar');
  const scrim     = byId('sidebar-scrim');
  const btnTopbar = byId('topbarToggleSidebar'); // nuevo botón en la topbar
  const btnInside = byId('toggleSidebar');       // botón existente dentro de la sidebar

  if (!sidebar || !scrim) return;

  const open  = () => { root.classList.add('sidebar-open');  btnTopbar?.setAttribute('aria-expanded','true'); };
  const close = () => { root.classList.remove('sidebar-open'); btnTopbar?.setAttribute('aria-expanded','false'); };

  btnTopbar?.addEventListener('click', () => {
    root.classList.contains('sidebar-open') ? close() : open();
  });
  btnInside?.addEventListener('click', () => {
    root.classList.contains('sidebar-open') ? close() : open();
  });
  scrim.addEventListener('click', close);
  document.addEventListener('keydown', e => { if (e.key === 'Escape') close(); });
  document.addEventListener('route:loaded', close); // al navegar en tu SPA, cierra el drawer
}

