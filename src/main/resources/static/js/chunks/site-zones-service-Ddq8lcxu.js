import{f as l}from"../auth.js";async function c(n){if(!n)return[];const o=`/api/v1/site-zones/list?siteExternalId=${encodeURIComponent(n)}`,t=await l(o,{method:"GET",headers:{Accept:"application/json"}});if(!t.ok)throw new Error(`Error HTTP ${t.status} al obtener las zonas.`);return await t.json()}async function p(){const n=qs("#siteExternalId")?.value||"";if(!n){displayAlert(alertError,"No existe un external ID válido para el sitio.");return}const o=qs("#site-zones-body"),t=qs("#site-zones-container"),s=qs("#no-site-zones-msg");if(!o||!t||!s){displayAlert(alertError,"No existen los contenedores en el template.");return}try{const e=await c(n);o.innerHTML="";const r=Array.isArray(e)&&e.length>0;r&&e.forEach(a=>{const i=document.createElement("tr");i.innerHTML=`
                          <td>${a.name||"-"}</td>
                          <td>${a.status.displayName||"-"}</td>
                          <td>
                            <button type="button"
                                    class="btn btn-secondary"
                                    id="edit-zone-btn"
                                    data-id="${a.externalId}">
                              Ver
                            </button>
                          </td>
                        `,o.appendChild(i)}),t&&(t.style.display=r?"block":"none"),s&&(s.style.display=r?"none":"block")}catch(e){throw console.error("[site-zones] Error el procesar zonas",e),e}}export{p as l};
