// site-supervision-table.js (SPA-friendly)
// - mide la altura de .site-visit-header
// - actualiza --site-visit-header-height
// - crea/actualiza .site-visit-header-spacer
// - aplica margin-top inline a .site-visit-table-container (defensa extra)
// - observa cambios: ResizeObserver sobre header, MutationObserver sobre header y sobre el contenedor root
// - expone API pública para integrarlo en hooks de router
(function () {
  var debounce = function (fn, wait) {
    var t;
    return function () {
      var args = arguments, ctx = this;
      clearTimeout(t);
      t = setTimeout(function () { fn.apply(ctx, args); }, wait || 100);
    };
  };

  var state = {
    resizeObserver: null,
    mutationObserver: null,
    rootObserver: null,
    initializedForHeader: null
  };

  function measureAndApply() {
    var header = document.querySelector('.site-visit-header');
    if (!header) return;

    var rect = header.getBoundingClientRect();
    var h = Math.ceil(rect.height || header.offsetHeight || 112);
    var total = h + 2; // margen de seguridad

    // actualizar CSS var
    document.documentElement.style.setProperty('--site-visit-header-height', total + 'px');

    // spacer
    var next = header.nextElementSibling;
    if (next && next.classList && next.classList.contains('site-visit-header-spacer')) {
      next.style.height = total + 'px';
    } else {
      var spacer = document.createElement('div');
      spacer.className = 'site-visit-header-spacer';
      spacer.style.height = total + 'px';
      // insertar después del header
      if (header.parentNode) header.parentNode.insertBefore(spacer, header.nextSibling);
    }

    // margin-top inline en contenedor tabla como defensa
    var tableContainer = document.querySelector('.site-visit-table-container');
    if (tableContainer) {
      tableContainer.style.marginTop = (total + 16) + 'px';
    }
  }

  var debouncedMeasure = debounce(measureAndApply, 100);

  // Inicializa observers ligados al header actual (si existe)
  function attachObserversForHeader() {
    detachHeaderObservers();

    var header = document.querySelector('.site-visit-header');
    if (!header) return;

    // ResizeObserver para cambios de tamaño del header
    if (window.ResizeObserver) {
      try {
        state.resizeObserver = new ResizeObserver(debouncedMeasure);
        state.resizeObserver.observe(header);
      } catch (e) { state.resizeObserver = null; }
    }

    // MutationObserver para cambios internos del header
    if (window.MutationObserver) {
      try {
        state.mutationObserver = new MutationObserver(debouncedMeasure);
        state.mutationObserver.observe(header, { attributes: true, childList: true, subtree: true });
      } catch (e) { state.mutationObserver = null; }
    }

    // marcar que observadores están para este header
    state.initializedForHeader = header;
  }

  function detachHeaderObservers() {
    try { if (state.resizeObserver) { state.resizeObserver.disconnect(); state.resizeObserver = null; } } catch(e){}
    try { if (state.mutationObserver) { state.mutationObserver.disconnect(); state.mutationObserver = null; } } catch(e){}
    state.initializedForHeader = null;
  }

  // Observador del contenedor root para detectar reemplazos del header en SPA
  function attachRootObserver() {
    // observar .main-content .content o body como fallback
    var root = document.querySelector('.main-content .content') || document.body;
    if (!root || !window.MutationObserver) return;

    // ya conectado?
    if (state.rootObserver) return;

    state.rootObserver = new MutationObserver(function (mutations) {
      var header = document.querySelector('.site-visit-header');
      // Si el header fue creado o cambiado (nuevo nodo) reaplicar observers y medición
      if (header && state.initializedForHeader !== header) {
        attachObserversForHeader();
        debouncedMeasure();
      }
      // Si header eliminado, cleanup
      if (!header) {
        detachHeaderObservers();
      }
    });

    state.rootObserver.observe(root, { childList: true, subtree: true, attributes: false });
  }

  function detachRootObserver() {
    try { if (state.rootObserver) { state.rootObserver.disconnect(); state.rootObserver = null; } } catch(e){}
  }

  // Public API
  function init(options) {
    options = options || {};
    // initial measure (delay a 50ms para que CSS se aplique)
    setTimeout(function () {
      attachObserversForHeader();
      attachRootObserver();
      debouncedMeasure();
      // medidas adicionales por si CSS asíncrono llega tarde
      setTimeout(debouncedMeasure, 300);
      setTimeout(debouncedMeasure, 900);
    }, 50);
  }

  function update() {
    debouncedMeasure();
  }

  function destroy() {
    detachHeaderObservers();
    detachRootObserver();
    // remove spacer if exists
    var sp = document.querySelector('.site-visit-header-spacer');
    if (sp && sp.parentNode) sp.parentNode.removeChild(sp);
    // remove inline margin-top
    var tc = document.querySelector('.site-visit-table-container');
    if (tc) tc.style.marginTop = '';
    window.removeEventListener('resize', debouncedMeasure);
  }

  // Exponer en namespace global para que la SPA pueda invocar después de rutas
  window.siteSupervision = window.siteSupervision || {};
  window.siteSupervision.initHeaderSpacer = init;
  window.siteSupervision.updateHeaderSpacer = update;
  window.siteSupervision.destroyHeaderSpacer = destroy;

  // Auto-initialize
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    setTimeout(init, 50);
  }
})();