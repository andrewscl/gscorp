import { fetchWithAuth } from "../../../auth";

const qs = (s) => document.querySelector(s);

export async function fetchSiteZones(siteExternalId){
    if(!siteExternalId) return [];
    const url = `/api/v1/site-zones/list?siteExternalId=${encodeURIComponent(siteExternalId)}`;
    const response = await fetchWithAuth(url, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    });
    if (!response.ok) {
        throw new Error(`Error HTTP ${response.status} al obtener las zonas.`)
    }
    return await response.json();
}

export async function loadAndRenderSiteZones() {
    const siteExternalId = qs('#siteExternalId')?.value || '';
    if (!siteExternalId){
        displayAlert(alertError, 'No existe un external ID válido para el sitio.');
        return;
    }
    const tbody = qs('#site-zones-body');
    const container = qs('#site-zones-container');
    const emptyMsg = qs('#no-site-zones-msg');
    if (!tbody || !container || !emptyMsg){
        displayAlert(alertError, 'No existen los contenedores en el template.');
        return;
    }
    try {
        const siteZones = await fetchSiteZones(siteExternalId);
        tbody.innerHTML = '';
        const hasZones = Array.isArray(siteZones) && siteZones.length > 0;
        if (hasZones) {
            siteZones.forEach(zone => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                          <td>${zone.name || '-'}</td>
                          <td>${zone.status.displayName || '-'}</td>
                          <td>
                            <button type="button"
                                    class="btn btn-secondary"
                                    id="edit-zone-btn"
                                    data-id="${zone.externalId}">
                              Ver
                            </button>
                          </td>
                        `;
                tbody.appendChild(tr);
            });
        }
        if (container) container.style.display = hasZones ? 'block' : 'none';
        if (emptyMsg) emptyMsg.style.display = hasZones ? 'none' : 'block';
    } catch (error) {
        console.error('[site-zones] Error el procesar zonas', error);
        throw error;
    }
}