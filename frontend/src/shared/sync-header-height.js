/**
 * Sincroniza la altura de un elemento con una variable CSS en el :root
 * @param {string} selector - El selector del elemento a medir (ej: '.hs-table-header')
 * @param {string} variableName - El nombre de la variable CSS (ej: '--header-height')
 */
// shared/sync-header.js

export function initHeaderSync(selector = '.hs-table-header', variableName = '--header-height') {
    const root = document.documentElement;
    let rafId = null;
    let timeoutId = null;

    function setVar() {
        const el = document.querySelector(selector);
        if (el) {
            const h = el.offsetHeight;
            root.style.setProperty(variableName, `${h}px`);
            console.log(`✅ Sincronizado: ${selector} = ${h}px`);
        } else {
            // Si no lo encuentra, reintenta en el siguiente frame
            requestAnimationFrame(setVar);
        }
    }

    function onResize() {
        if (rafId) cancelAnimationFrame(rafId);
        rafId = requestAnimationFrame(() => {
            if (timeoutId) clearTimeout(timeoutId);
            timeoutId = setTimeout(setVar, 80);
        });
    }

    // Ejecutar inmediatamente
    setVar();

    // Limpiar y añadir el evento
    window.removeEventListener('resize', onResize);
    window.addEventListener('resize', onResize);
}