/**
 * Sincroniza la altura de un elemento con una variable CSS en el :root
 * @param {string} selector - El selector del elemento a medir (ej: '.hs-table-header')
 * @param {string} variableName - El nombre de la variable CSS (ej: '--header-height')
 */
export function initHeaderSync(selector = '.hs-table-header', variableName = '--header-height') {
    const root = document.documentElement;
    let rafId = null;
    let timeoutId = null;

    function setVar() {
        const el = document.querySelector(selector);
        if (el) {
            const h = el.offsetHeight;
            root.style.setProperty(variableName, `${h}px`);
        }
    }

    function onResize() {
        if (rafId) cancelAnimationFrame(rafId);
        rafId = requestAnimationFrame(() => {
            if (timeoutId) clearTimeout(timeoutId);
            timeoutId = setTimeout(setVar, 80);
        });
    }

    // Ejecución inmediata
    setVar();

    // Listener de resize (se limpia automáticamente si la página se recarga, 
    // pero en SPA hay que tener cuidado de no duplicarlos)
    window.removeEventListener('resize', onResize); // Evita duplicados
    window.addEventListener('resize', onResize);
    
    // Retornamos la función por si necesitamos dispararla manualmente
    return setVar;
}