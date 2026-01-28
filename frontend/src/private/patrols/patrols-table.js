(function () {
  const elSelector = '.patrols-header';
  const root = document.documentElement;
  let rafId = null;
  let timeoutId = null;

  function setHeaderVar() {
    const el = document.querySelector(elSelector);
    if (!el) return;
    // offsetHeight incluye padding y border; ajusta si necesitas margen extra
    const h = el.offsetHeight;
    root.style.setProperty('--patrols-header-height', `${h}px`);
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

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      setHeaderVar();
      window.addEventListener('resize', onResize);
    });
  } else {
    setHeaderVar();
    window.addEventListener('resize', onResize);
  }

})();