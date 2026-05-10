console.log('[Module] sync header height loaded');

export function syncHeaderHeight(selector = '.hs-table-header', variableName = '--header-height') {
    const root = document.documentElement;
    let timeoutId = null;

    function update() {
        const el = document.querySelector(selector);
        if (el) {
            // Usamos getBoundingClientRect para mayor precisión con zooms o escalas
            const height = el.getBoundingClientRect().height;
            root.style.setProperty(variableName, `${height}px`);
        }
    }
    // cambia de tamaño internamente (ej: aparece una alerta o se expande un filtro)
    const observer = new ResizeObserver(() => {
        if (timeoutId) clearTimeout(timeoutId);
        timeoutId = setTimeout(update, 50);
    });

    const target = document.querySelector(selector);
    if (target) {
        observer.observe(target);
        update();
    }
}

function init (){
    syncHeaderHeight('.hs-table-header', '--header-height');
};