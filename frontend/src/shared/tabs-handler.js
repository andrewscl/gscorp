import { qsa } from './dom-utils';

/**
 * Inicializa el comportamiento de todos los tab-panel-cards del contenedor especificado.
 * @param {HTMLElement|Document} context - El contenedor donde buscar los paneles (por defecto todo el DOM)
 */
export function initTabPanels(context = document) {
    const tabPanels = qsa('.tab-panel-card', context);

    tabPanels.forEach(panel => {
        const tabsContainer = panel.querySelector('.tabs-container');
        if (!tabsContainer || tabsContainer.dataset.tabsInitialized) return;

        // Marcamos el contenedor como inicializado para evitar duplicar listeners
        tabsContainer.dataset.tabsInitialized = "true";

        tabsContainer.addEventListener('click', (e) => {
            const clickedTab = e.target.closest('.tab');
            if (!clickedTab) return;

            const tabIndex = clickedTab.dataset.index;

            // 1. Desactivar pestañas y activar la seleccionada
            qsa('.tab', panel).forEach(t => t.classList.remove('active'));
            clickedTab.classList.add('active');

            // 2. Ocultar contenidos y mostrar el seleccionado
            qsa('.tab-content', panel).forEach(c => c.classList.remove('active'));
            
            const targetContent = panel.querySelector(`#tab-${tabIndex}`);
            if (targetContent) targetContent.classList.add('active');
        });
    });
}
