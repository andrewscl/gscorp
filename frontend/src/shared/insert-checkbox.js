export function populateCheckboxes({ containerEl, items, emptyLabel = 'Sin ítems disponibles', valueKey = 'externalId', inputName = 'projectIds', preservedValues = [] }) {
    if (!containerEl) return;

    // 1. Caso sin ítems o arreglo vacío
    if (!items || items.length === 0) {
        containerEl.innerHTML = `<span class="empty-msg" style="font-size:0.85rem; color:#64748b;">${emptyLabel}</span>`;
        return;
    }

    // 2. Renderizar los checkboxes
    containerEl.innerHTML = items.map(item => {
        const value = item[valueKey] || item.id || '';
        const isChecked = preservedValues.includes(String(value)) ? 'checked' : '';
        const elementId = `project_${value}`;

        return `
            <div class="checkbox-item">
                <input type="checkbox" 
                       name="${inputName}" 
                       id="${elementId}" 
                       value="${value}" 
                       ${isChecked} />
                <label for="${elementId}">${item.name || ''}</label>
            </div>
        `;
    }).join('');
}