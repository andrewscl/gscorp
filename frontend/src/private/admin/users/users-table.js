// Ajusta variable CSS --site-visit-header-height según el alto real del header de usuarios
(function () {
  const elSelector = '.users-header';
  const cssVarName = '--site-visit-header-height';
  const root = document.documentElement;

  let rafId = null;
  let timeoutId = null;
  let resizeObserver = null;
  let mutationObserver = null;

  function setHeaderVar() {
    const el = document.querySelector(elSelector);
    if (!el) return;
    // offsetHeight incluye padding y border; ajusta según box-sizing si es necesario
    const h = Math.round(el.offsetHeight);
    const current = root.style.getPropertyValue(cssVarName).trim();
    const newVal = `${h}px`;
    if (current !== newVal) {
      root.style.setProperty(cssVarName, newVal);
      // Para debug (opcional)
      // console.debug('[users-table] set', cssVarName, newVal);
    }
  }

  // Debounce / throttle: requestAnimationFrame + setTimeout para evitar recalculos intensos
  function scheduleRecalc() {
    if (rafId) cancelAnimationFrame(rafId);
    rafId = requestAnimationFrame(() => {
      if (timeoutId) clearTimeout(timeoutId);
      timeoutId = setTimeout(() => {
        setHeaderVar();
      }, 80);
    });
  }

  function onResize() {
    scheduleRecalc();
  }

  function initObservers() {
    // ResizeObserver para detectar cambios de tamaño directamente
    if (typeof ResizeObserver !== 'undefined') {
      try {
        resizeObserver = new ResizeObserver(() => scheduleRecalc());
        const el = document.querySelector(elSelector);
        if (el) resizeObserver.observe(el);
      } catch (e) {
        resizeObserver = null;
      }
    }

    // MutationObserver como fallback: detecta cambios en el DOM del header
    if (typeof MutationObserver !== 'undefined') {
      mutationObserver = new MutationObserver(() => scheduleRecalc());
      const el = document.querySelector(elSelector);
      if (el) {
        mutationObserver.observe(el, { childList: true, subtree: true, attributes: true, characterData: true });
      }
    }
  }

  function destroyObservers() {
    if (resizeObserver) {
      try { resizeObserver.disconnect(); } catch (_) {}
      resizeObserver = null;
    }
    if (mutationObserver) {
      try { mutationObserver.disconnect(); } catch (_) {}
      mutationObserver = null;
    }
  }

  function init() {
    // Ejecutar inmediatamente si el DOM ya está listo
    setHeaderVar();
    window.addEventListener('resize', onResize, { passive: true });
    initObservers();
  }

  function destroy() {
    if (rafId) cancelAnimationFrame(rafId);
    if (timeoutId) clearTimeout(timeoutId);
    window.removeEventListener('resize', onResize);
    destroyObservers();
  }

  // Ejecutar en DOMContentLoaded si el documento está cargando
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }

  // Exponer cleanup opcional en caso de navegación SPA o hot-reload
  if (typeof window !== 'undefined') {
    window.__usersTableHeaderCleanup = destroy;
  }
})();