export function populateSelect({selectEl, items, defaultLabel, emptyLabel, valueKey = 'externalId', preserveValue=''}) {
  if(!selectEl) return;
  // 1.- Caso sin items o arreglo vacio
  if(!items || items.length === 0){
    selectEl.innerHTML = `<option value="">${emptyLabel}</option>`;
    selectEl.disabled = true;
    return;
  }
  // 2.- Opción por defecto
  selectEl.innerHTML = `<option value="">${defaultLabel}</option>`;
  // 3.- Renderizar las opciones
  items.forEach(item => {
    const opt = document.createElement('option');
    opt.value = item[valueKey] || item.id || '';
    opt.textContent = item.name || '';
    selectEl.appendChild(opt);
  });
  // 4.- Preservar el valor seleccionado si aun existe
  if (preserveValue) {
    const stillThere = Array.from(selectEl.options).some(o => o.value === String(preserveValue));
    if (stillThere) selectEl.value = String(preserveValue);
  }
  selectEl.disabled = false;
}