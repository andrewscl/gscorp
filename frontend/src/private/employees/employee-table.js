// Ajusta variable CSS --site-visit-header-height según el alto real del header de empleados
// Similar al snippet provisto, pero adaptado a .employees-header y con soporte a ResizeObserver + MutationObserver
(function () {
  const elSelector = '.employees-header';
  const cssVarName = '--site-visit-header-height';
  const root = document.documentElement;

  let rafId = null;
  let timeoutId = null;
  let resizeObserver = null;
  let mutationObserver = null;

  function setHeaderVar() {
    const el = document.querySelector(elSelector);
    if (!el) return;
    // offsetHeight incluye padding y border; si usas box-sizing distinto, ajústalo
    const h = Math.round(el.offsetHeight);
    const current = root.style.getPropertyValue(cssVarName).trim();
    const newVal = `${h}px`;
    if (current !== newVal) {
      root.style.setProperty(cssVarName, newVal);
      // para debug (descomentar si necesitas)
      // console.debug('[employees-table] set', cssVarName, newVal);
    }
  }

  // Debounce / throttle: requestAnimationFrame + timeout para evitar recalculos intensos
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
    // ResizeObserver para detectar cambios de tamaño del header directamente
    if (typeof ResizeObserver !== 'undefined') {
      try {
        resizeObserver = new ResizeObserver(() => scheduleRecalc());
        const el = document.querySelector(elSelector);
        if (el) resizeObserver.observe(el);
      } catch (e) {
        resizeObserver = null;
      }
    }

    // MutationObserver como fallback/extra: detecta cambios en el DOM (texto, filtros añadidos/removidos, etc.)
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
    // inicializar inmediatamente si ya hay DOM
    setHeaderVar();
    window.addEventListener('resize', onResize, { passive: true });
    initObservers();
  }

  // cleanup (no expuesto globalmente; si necesitas exponerlo, haz window.__employeeTableCleanup = destroy)
  function destroy() {
    if (rafId) cancelAnimationFrame(rafId);
    if (timeoutId) clearTimeout(timeoutId);
    window.removeEventListener('resize', onResize);
    destroyObservers();
  }

  // arrancar en DOMContentLoaded si aún no está listo
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }

  // Exponer cleanup opcional en caso de navegación SPA o hot-reload
  // eslint-disable-next-line no-undef
  if (typeof window !== 'undefined') {
    window.__employeeTableHeaderCleanup = destroy;
  }
})();