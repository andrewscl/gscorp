import{f as c}from"../auth.js";const r=t=>document.querySelector(t);async function d(t){if(!t)return[];const o=`/api/v1/site-zones/list?siteExternalId=${encodeURIComponent(t)}`,e=await c(o,{method:"GET",headers:{Accept:"application/json"}});if(!e.ok)throw new Error(`Error HTTP ${e.status} al obtener las zonas.`);return await e.json()}async function u(){const t=r("#siteExternalId")?.value||"";if(!t){displayAlert(alertError,"No existe un external ID válido para el sitio.");return}const o=r("#site-zones-body"),e=r("#site-zones-container"),s=r("#no-site-zones-msg");if(!o||!e||!s){displayAlert(alertError,"No existen los contenedores en el template.");return}try{const n=await d(t);o.innerHTML="";const a=Array.isArray(n)&&n.length>0;a&&n.forEach(i=>{const l=document.createElement("tr");l.innerHTML=`
                          <td>${i.name||"-"}</td>
                          <td>${i.status.displayName||"-"}</td>
                          <td>
                            <button type="button"
                                    class="btn btn-secondary"
                                    id="edit-zone-btn"
                                    data-id="${i.externalId}">
                              Ver
                            </button>
                          </td>
                        `,o.appendChild(l)}),e&&(e.style.display=a?"block":"none"),s&&(s.style.display=a?"none":"block")}catch(n){throw console.error("[site-zones] Error el procesar zonas",n),n}}export{u as l};
