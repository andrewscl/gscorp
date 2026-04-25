import{f as y}from"../../auth.js";import{n as f}from"../../navigation-handler.js";let l=[],s=[],d=null,c=null;const h=t=>document.querySelector(t),b=(()=>{let t=!1,e=null;return o=>(t||(e=new Promise((a,n)=>{const r=document.createElement("script");r.src=`https://maps.googleapis.com/maps/api/js?key=${o}&v=weekly`,r.async=!0,r.defer=!0,r.onload=()=>{console.log("[Google Maps API] Script cargado correctamente."),t=!0,a()},r.onerror=i=>{console.error("[Google Maps API] Error al cargar el script:",i),n(new Error("Error al cargar Google Maps API"))},document.head.appendChild(r)})),e)})(),w=async()=>{const t=document.getElementById("patrols-map-picker");if(!t){console.warn("[initMap] Contenedor #patrols-map-picker no encontrado en el DOM.");return}console.log("siteId en initMap: "+window.targetSiteId);try{const{Map:e}=await google.maps.importLibrary("maps"),o=new e(t,{center:{lat:-33.4489,lng:-70.6693},zoom:8,mapId:googleMapsConfig.mapId,disableDefaultUI:!0,mapTypeId:"hybrid"});console.log("[initMap] Mapa inicializado."),window.mapInstance=o,await k(),await $(),o.addListener("click",a=>{v(a.latLng)})}catch(e){console.error("[initMap] Error al inicializar el mapa:",e)}};async function k(){const t=document.getElementById("target-site-id"),e=t?t.value:null;if(!e){console.error("No se pudo obtener el ID del sitio del input oculto");return}try{const o=await y("/api/sites/projections-by-user",{method:"GET",headers:{Accept:"application/json"}});if(!o.ok){console.log("Error al cargar sitios. Intente nuevamente.");return}const a=await o.json();if(!Array.isArray(a)||!a.every(r=>r.id&&r.lat&&r.lon)){console.log("Datos de sitios inválidos.");return}console.log("Buscando siteId:",e),console.log("IDs disponibles en siteData:",a.map(r=>r.id));const n=a.find(r=>Number(r.id)===Number(e));console.log("site: "+n),x(n)}catch(o){console.error("Fallo al obtener sitios:",o),console.log("Error al cargar sitios. Intente nuevamente.")}}async function x(t){const{AdvancedMarkerElement:e,PinElement:o}=await google.maps.importLibrary("marker"),a=new google.maps.LatLngBounds,n=document.createElement("div");n.style.backgroundColor="#fff",n.style.border="1px solid grey",n.style.padding="4px 8px",n.style.borderRadius="8px",n.style.boxShadow="0 2px 6px rgba(0,0,0,0.3)",n.style.position="absolute",n.style.top="-40px",n.style.left="50%",n.style.transform="translateX(-50%)",n.style.whiteSpace="nowrap",n.style.display="none",n.textContent=`Sitio: ${t.name||"Sin Nombre"}`;const r=new o({scale:.8,glyphColor:"#3176e3",background:"#359dd1",borderColor:"#1d4d9b"}),i=document.createElement("div");i.style.position="relative",i.appendChild(r.element),i.appendChild(n),new e({map:window.mapInstance,position:{lat:t.lat,lng:t.lon},content:i}),i.addEventListener("mouseenter",()=>{n.style.display="block"}),i.addEventListener("mouseleave",()=>{n.style.display="none"}),a.extend(new google.maps.LatLng(t.lat,t.lon)),console.log(`[addSitesToMapAndSelect] Marcador añadido para sitio: ${t.id}`),window.mapInstance.fitBounds(a)}async function u(t,e){const{InfoWindow:o}=await google.maps.importLibrary("maps"),a=l[e];if(!a){console.error("No se encontró el punto para el índice:",e);return}const n=e===0?"display:none;":"display:block;";c&&c.close(),c=new o({content:`
            <div style="color:black; padding:10px; font-family: sans-serif; min-width: 200px;">
                <strong style="display:block; margin-bottom:8px; border-bottom: 1px solid #ccc;">
                    Configuración Punto ${e+1}
                </strong>
                
                <div style="margin-bottom: 8px;">
                    <label style="font-size: 11px; display:block;">Nombre del lugar:</label>
                    <input type="text" id="infowindow-name-${e}" 
                           value="${a.name||""}" 
                           oninput="updateCheckpointData(${e}, 'name', this.value)"
                           style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc; border-radius:4px;">
                </div>

                <div style="display: flex; gap: 10px; margin-bottom: 10px;">
                    <div style="flex:1;">
                        <label style="font-size: 11px; display:block;">Permanencia (min):</label>
                        <input type="number" 
                               value="${a.stayTime||5}" 
                               oninput="updateCheckpointData(${e}, 'stayTime', this.value)"
                               style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc;">
                    </div>
                    <div style="flex:1; ${n}">
                        <label style="font-size: 11px; display:block;">Tránsito (min):</label>
                        <input type="number" 
                               value="${a.transitTime||3}" 
                               oninput="updateCheckpointData(${e}, 'transitTime', this.value)"
                               style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc;">
                    </div>
                </div>

                <button class="btn btn-xs btn-danger" 
                        style="width:100%; padding: 5px; font-size: 11px; cursor:pointer;"
                        onclick="removeCheckpoint(${e})">
                    Eliminar Punto
                </button>
            </div>`}),c.open(window.mapInstance,t)}async function v(t){const{AdvancedMarkerElement:e,PinElement:o}=await google.maps.importLibrary("marker"),a=l.length+1,n=new o({glyph:a.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),r=new e({map:window.mapInstance,position:t,content:n.element,title:`Punto de control ${a}`});r.addListener("click",()=>{const i=s.indexOf(r);console.log("Marcador clickeado. Índice encontrado:",i),console.log("Total marcadores en array:",s.length),i!==-1?u(r,i):console.error("Error: El marcador clickeado no existe en checkpointMarkers.")}),l.push({lat:t.lat(),lng:t.lng(),order:a,name:`Punto ${a}`,stayTime:5,transitTime:a===1?0:3}),s.push(r),p(),m(),console.log("Checkpoints actuales:",l)}function p(){const t=document.getElementById("checkpoint-list-body");t.innerHTML="",l.forEach((e,o)=>{const a=o===0?'<span class="text-muted">---</span>':`${e.transitTime||0} min`,n=`
            <tr>
                <td class="text-center">
                    <span class="badge bg-primary">${o+1}</span>
                </td>
                <td>
                    <div class="fw-bold text-truncate" style="max-width: 150px;" title="${e.name||"Sin nombre"}">
                        ${e.name||'<i class="text-muted">Punto sin nombre</i>'}
                    </div>
                </td>
                <td class="small text-muted">
                    ${e.lat.toFixed(5)}, ${e.lng.toFixed(5)}
                </td>
                <td class="text-center">
                    ${e.stayTime||0} min
                </td>
                <td class="text-center">
                    ${a}
                </td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-danger border-0" 
                            onclick="removeCheckpoint(${o})" 
                            title="Eliminar punto">
                        <i class="bi bi-trash"></i> Eliminar
                    </button>
                </td>
            </tr>`;t.insertAdjacentHTML("beforeend",n)})}function I(){document.getElementById("btn-clear-path")?.addEventListener("click",()=>{confirm("¿Borrar todos los puntos?")&&C()}),h("#btn-confirm-map").addEventListener("click",M)}window.removeCheckpoint=function(t){s[t]&&s[t].setMap(null),l.splice(t,1),s.splice(t,1),l.forEach((e,o)=>e.order=o+1),E(),m(),p(),c&&c.close()};function E(){s.forEach((t,e)=>{const o=e+1,a=new google.maps.marker.PinElement({glyphText:o.toString(),background:"#FBBC04"});t.content=a.element,t.title=`Punto ${o}`,google.maps.event.clearInstanceListeners(t),t.addListener("click",()=>{u(t,e)})})}function m(){const t=l.map(e=>({lat:e.lat,lng:e.lng}));d?d.setPath(t):d=new google.maps.Polyline({path:t,geodesic:!0,strokeColor:"#FF0000",strokeOpacity:1,strokeWeight:3,map:window.mapInstance})}function C(){s.forEach(t=>t.setMap(null)),s=[],l=[],d&&d.setPath([]),p(),c&&c.close(),console.log("Ruta reseteada correctamente.")}async function M(){if(l.length===0){alert("Define al menos un punto en la ruta antes de confirmar.");return}localStorage.setItem("pending_checkpoints",JSON.stringify(l));const e=`/private/patrols/edit/${document.getElementById("target-patrol-externalId").value}`;console.log(`[MapPicker] Finalizando edición. Navegando a ${e}`),await f(e)}const $=async()=>{const t=document.getElementById("checkpoints-data");if(!t||!t.value){console.warn("No se encontraron puntos pre-cargados en el DOM.");return}try{const e=JSON.parse(t.value);if(e.length===0)return;const{AdvancedMarkerElement:o,PinElement:a}=await google.maps.importLibrary("marker");for(const n of e){const r={lat:parseFloat(n.latitude),lng:parseFloat(n.longitude)},i=new a({glyph:n.checkpointOrder.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),g=new o({map:window.mapInstance,position:r,content:i.element,title:n.name});l.push({externalId:n.externalId,lat:r.lat,lng:r.lng,order:n.checkpointOrder,name:n.name,stayTime:n.stayTime,transitTime:n.minutesToReach}),s.push(g)}m(),window.mapInstance.setCenter({lat:parseFloat(e[0].latitude),lng:parseFloat(e[0].longitude)})}catch(e){console.error("Error al parsear los puntos ocultos:",e)}};(async function(){console.log("[init] IIFE iniciado");const e=googleMapsConfig.apiKey;try{await b(e),w(),I()}catch(o){console.error("[site-map.js] Error al cargar la API de Google Maps:",o)}})();
