(function () {
  const elSelector = '.shift-requests-header';
  const root = document.documentElement;
  let rafId = null;
  let timeoutId = null;
  let mo = null;

  function setHeaderVar() {
    const el = document.querySelector(elSelector);
    if (!el) return;
    // offsetHeight incluye padding y border; ajusta si necesitas margen extra
    const h = el.offsetHeight;
    root.style.setProperty('--shift-requests-header-height', `${h}px`);
  }

  // debounce resize using requestAnimationFrame + timeout
  function onResize() {
    if (rafId) cancelAnimationFrame(rafId);
    rafId = requestAnimationFrame(() => {
      if (timeoutId) clearTimeout(timeoutId);
      timeoutId = setTimeout(() => {
        setHeaderVar();
      }, 80);
    });
  }

  // Observe header for content changes (filters applied, count updates, etc.)
  function observeHeaderMutations() {
    const el = document.querySelector(elSelector);
    if (!el) return;
    // If there's an existing observer, disconnect it first
    if (mo) mo.disconnect();
    mo = new MutationObserver(() => {
      // slight delay to allow layout to stabilize
      setTimeout(setHeaderVar, 24);
    });
    mo.observe(el, { childList: true, subtree: true, characterData: true });
  }

  function init() {
    setHeaderVar();
    observeHeaderMutations();
    window.addEventListener('resize', onResize);
    // Recompute after fonts/images load if they can affect height
    window.addEventListener('load', () => setTimeout(setHeaderVar, 50));
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  // Optional cleanup if needed in SPA/unload flows
  // Expose a cleanup function in case fragments are removed and re-initialized
  window.__shiftRequestsHeaderCleanup = function () {
    try { window.removeEventListener('resize', onResize); } catch (e) {}
    try { window.removeEventListener('load', setHeaderVar); } catch (e) {}
    if (mo) { try { mo.disconnect(); } catch (e) {} mo = null; }
    if (rafId) { try { cancelAnimationFrame(rafId); } catch (e) {} rafId = null; }
    if (timeoutId) { try { clearTimeout(timeoutId); } catch (e) {} timeoutId = null; }
  };
})();