import{f as g}from"../../auth.js";import{n as y}from"../../navigation-handler.js";let l=[],s=[],d=null,c=null;const b=t=>document.querySelector(t),f=(()=>{let t=!1,e=null;return o=>(t||(e=new Promise((n,a)=>{const i=document.createElement("script");i.src=`https://maps.googleapis.com/maps/api/js?key=${o}&v=weekly`,i.async=!0,i.defer=!0,i.onload=()=>{console.log("[Google Maps API] Script cargado correctamente."),t=!0,n()},i.onerror=r=>{console.error("[Google Maps API] Error al cargar el script:",r),a(new Error("Error al cargar Google Maps API"))},document.head.appendChild(i)})),e)})(),h=async()=>{const t=document.getElementById("patrols-map-picker");if(!t){console.warn("[initMap] Contenedor #patrols-map-picker no encontrado en el DOM.");return}console.log("siteId en initMap: "+window.targetSiteId);try{const{Map:e}=await google.maps.importLibrary("maps"),o=new e(t,{center:{lat:-33.4489,lng:-70.6693},zoom:8,mapId:googleMapsConfig.mapId,disableDefaultUI:!0,mapTypeId:"hybrid"});console.log("[initMap] Mapa inicializado."),window.mapInstance=o,await w(),o.addListener("click",n=>{x(n.latLng)})}catch(e){console.error("[initMap] Error al inicializar el mapa:",e)}};async function w(){const t=document.getElementById("target-site-id"),e=t?t.value:null;if(!e){console.error("No se pudo obtener el ID del sitio del input oculto");return}try{const o=await g("/api/sites/projections-by-user",{method:"GET",headers:{Accept:"application/json"}});if(!o.ok){console.log("Error al cargar sitios. Intente nuevamente.");return}const n=await o.json();if(!Array.isArray(n)||!n.every(i=>i.id&&i.lat&&i.lon)){console.log("Datos de sitios inválidos.");return}console.log("Buscando siteId:",e),console.log("IDs disponibles en siteData:",n.map(i=>i.id));const a=n.find(i=>Number(i.id)===Number(e));console.log("site: "+a),k(a)}catch(o){console.error("Fallo al obtener sitios:",o),console.log("Error al cargar sitios. Intente nuevamente.")}}async function k(t){const{AdvancedMarkerElement:e,PinElement:o}=await google.maps.importLibrary("marker"),n=new google.maps.LatLngBounds,a=document.createElement("div");a.style.backgroundColor="#fff",a.style.border="1px solid grey",a.style.padding="4px 8px",a.style.borderRadius="8px",a.style.boxShadow="0 2px 6px rgba(0,0,0,0.3)",a.style.position="absolute",a.style.top="-40px",a.style.left="50%",a.style.transform="translateX(-50%)",a.style.whiteSpace="nowrap",a.style.display="none",a.textContent=`Sitio: ${t.name||"Sin Nombre"}`;const i=new o({scale:.8,glyphColor:"#3176e3",background:"#359dd1",borderColor:"#1d4d9b"}),r=document.createElement("div");r.style.position="relative",r.appendChild(i.element),r.appendChild(a),new e({map:window.mapInstance,position:{lat:t.lat,lng:t.lon},content:r}),r.addEventListener("mouseenter",()=>{a.style.display="block"}),r.addEventListener("mouseleave",()=>{a.style.display="none"}),n.extend(new google.maps.LatLng(t.lat,t.lon)),console.log(`[addSitesToMapAndSelect] Marcador añadido para sitio: ${t.id}`),window.mapInstance.fitBounds(n)}async function m(t,e){const{InfoWindow:o}=await google.maps.importLibrary("maps"),n=l[e];if(!n){console.error("No se encontró el punto para el índice:",e);return}const a=e===0?"display:none;":"display:block;";c&&c.close(),c=new o({content:`
            <div style="color:black; padding:10px; font-family: sans-serif; min-width: 200px;">
                <strong style="display:block; margin-bottom:8px; border-bottom: 1px solid #ccc;">
                    Configuración Punto ${e+1}
                </strong>
                
                <div style="margin-bottom: 8px;">
                    <label style="font-size: 11px; display:block;">Nombre del lugar:</label>
                    <input type="text" id="infowindow-name-${e}" 
                           value="${n.name||""}" 
                           oninput="updateCheckpointData(${e}, 'name', this.value)"
                           style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc; border-radius:4px;">
                </div>

                <div style="display: flex; gap: 10px; margin-bottom: 10px;">
                    <div style="flex:1;">
                        <label style="font-size: 11px; display:block;">Permanencia (min):</label>
                        <input type="number" 
                               value="${n.stayTime||5}" 
                               oninput="updateCheckpointData(${e}, 'stayTime', this.value)"
                               style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc;">
                    </div>
                    <div style="flex:1; ${a}">
                        <label style="font-size: 11px; display:block;">Tránsito (min):</label>
                        <input type="number" 
                               value="${n.transitTime||3}" 
                               oninput="updateCheckpointData(${e}, 'transitTime', this.value)"
                               style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc;">
                    </div>
                </div>

                <button class="btn btn-xs btn-danger" 
                        style="width:100%; padding: 5px; font-size: 11px; cursor:pointer;"
                        onclick="removeCheckpoint(${e})">
                    Eliminar Punto
                </button>
            </div>`}),c.open(window.mapInstance,t)}async function x(t){const{AdvancedMarkerElement:e,PinElement:o}=await google.maps.importLibrary("marker"),n=l.length+1,a=new o({glyph:n.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),i=new e({map:window.mapInstance,position:t,content:a.element,title:`Punto de control ${n}`});i.addListener("click",()=>{const r=s.indexOf(i);console.log("Marcador clickeado. Índice encontrado:",r),console.log("Total marcadores en array:",s.length),r!==-1?m(i,r):console.error("Error: El marcador clickeado no existe en checkpointMarkers.")}),l.push({lat:t.lat(),lng:t.lng(),order:n,name:`Punto ${n}`,stayTime:5,transitTime:n===1?0:3}),s.push(i),p(),u(),console.log("Checkpoints actuales:",l)}function p(){const t=document.getElementById("checkpoint-list-body");t.innerHTML="",l.forEach((e,o)=>{const n=o===0?'<span class="text-muted">---</span>':`${e.transitTime||0} min`,a=`
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
                    ${n}
                </td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-danger border-0" 
                            onclick="removeCheckpoint(${o})" 
                            title="Eliminar punto">
                        <i class="bi bi-trash"></i> Eliminar
                    </button>
                </td>
            </tr>`;t.insertAdjacentHTML("beforeend",a)})}function v(){document.getElementById("btn-clear-path")?.addEventListener("click",()=>{confirm("¿Borrar todos los puntos?")&&I()}),b("#btn-confirm-map").addEventListener("click",C)}window.removeCheckpoint=function(t){s[t]&&s[t].setMap(null),l.splice(t,1),s.splice(t,1),l.forEach((e,o)=>e.order=o+1),E(),u(),p(),c&&c.close()};function E(){s.forEach((t,e)=>{const o=e+1,n=new google.maps.marker.PinElement({glyphText:o.toString(),background:"#FBBC04"});t.content=n.element,t.title=`Punto ${o}`,google.maps.event.clearInstanceListeners(t),t.addListener("click",()=>{m(t,e)})})}function u(){const t=l.map(e=>({lat:e.lat,lng:e.lng}));d?d.setPath(t):d=new google.maps.Polyline({path:t,geodesic:!0,strokeColor:"#FF0000",strokeOpacity:1,strokeWeight:3,map:window.mapInstance})}function I(){s.forEach(t=>t.setMap(null)),s=[],l=[],d&&d.setPath([]),p(),c&&c.close(),console.log("Ruta reseteada correctamente.")}async function C(){if(l.length===0){alert("Define al menos un punto en la ruta antes de confirmar.");return}localStorage.setItem("pending_checkpoints",JSON.stringify(l));const e=`/private/patrols/edit/${document.getElementById("target-patrol-externalId").value}`;console.log(`[MapPicker] Finalizando edición. Navegando a ${e}`),await y(e)}(async function(){console.log("[init] IIFE iniciado");const e=googleMapsConfig.apiKey;try{await f(e),h(),v()}catch(o){console.error("[site-map.js] Error al cargar la API de Google Maps:",o)}})();
