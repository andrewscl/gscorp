import{f as g}from"../../auth.js";import{n as y}from"../../navigation-handler.js";let l=[],s=[],d=null,c=null;const f=e=>document.querySelector(e),b=(()=>{let e=!1,t=null;return o=>(e||(t=new Promise((n,a)=>{const r=document.createElement("script");r.src=`https://maps.googleapis.com/maps/api/js?key=${o}&v=weekly`,r.async=!0,r.defer=!0,r.onload=()=>{console.log("[Google Maps API] Script cargado correctamente."),e=!0,n()},r.onerror=i=>{console.error("[Google Maps API] Error al cargar el script:",i),a(new Error("Error al cargar Google Maps API"))},document.head.appendChild(r)})),t)})(),h=async()=>{const e=document.getElementById("patrols-map-picker");if(!e){console.warn("[initMap] Contenedor #patrols-map-picker no encontrado en el DOM.");return}console.log("siteId en initMap: "+window.targetSiteId);try{const{Map:t}=await google.maps.importLibrary("maps"),o=new t(e,{center:{lat:-33.4489,lng:-70.6693},zoom:8,mapId:googleMapsConfig.mapId,disableDefaultUI:!0,mapTypeId:"hybrid"});console.log("[initMap] Mapa inicializado."),window.mapInstance=o,await w(),o.addListener("click",n=>{x(n.latLng)})}catch(t){console.error("[initMap] Error al inicializar el mapa:",t)}};async function w(){const e=document.getElementById("target-site-id"),t=e?e.value:null;if(!t){console.error("No se pudo obtener el ID del sitio del input oculto");return}try{const o=await g("/api/sites/projections-by-user",{method:"GET",headers:{Accept:"application/json"}});if(!o.ok){console.log("Error al cargar sitios. Intente nuevamente.");return}const n=await o.json();if(!Array.isArray(n)||!n.every(r=>r.id&&r.lat&&r.lon)){console.log("Datos de sitios inválidos.");return}console.log("Buscando siteId:",t),console.log("IDs disponibles en siteData:",n.map(r=>r.id));const a=n.find(r=>Number(r.id)===Number(t));console.log("site: "+a),k(a)}catch(o){console.error("Fallo al obtener sitios:",o),console.log("Error al cargar sitios. Intente nuevamente.")}}async function k(e){const{AdvancedMarkerElement:t,PinElement:o}=await google.maps.importLibrary("marker"),n=new google.maps.LatLngBounds,a=document.createElement("div");a.style.backgroundColor="#fff",a.style.border="1px solid grey",a.style.padding="4px 8px",a.style.borderRadius="8px",a.style.boxShadow="0 2px 6px rgba(0,0,0,0.3)",a.style.position="absolute",a.style.top="-40px",a.style.left="50%",a.style.transform="translateX(-50%)",a.style.whiteSpace="nowrap",a.style.display="none",a.textContent=`Sitio: ${e.name||"Sin Nombre"}`;const r=new o({scale:.8,glyphColor:"#3176e3",background:"#359dd1",borderColor:"#1d4d9b"}),i=document.createElement("div");i.style.position="relative",i.appendChild(r.element),i.appendChild(a),new t({map:window.mapInstance,position:{lat:e.lat,lng:e.lon},content:i}),i.addEventListener("mouseenter",()=>{a.style.display="block"}),i.addEventListener("mouseleave",()=>{a.style.display="none"}),n.extend(new google.maps.LatLng(e.lat,e.lon)),console.log(`[addSitesToMapAndSelect] Marcador añadido para sitio: ${e.id}`),window.mapInstance.fitBounds(n)}async function m(e,t){const{InfoWindow:o}=await google.maps.importLibrary("maps");c&&c.close();const n=l[t];if(!n){console.error("No se encontró el punto para el índice:",t);return}c=new o({content:`
            <div style="color:black; padding:10px; font-family: sans-serif; min-width: 200px;">
                <strong style="display:block; margin-bottom:8px; border-bottom: 1px solid #ccc;">
                    Configuración Punto ${t+1}
                </strong>
                
                <div style="margin-bottom: 8px;">
                    <label style="font-size: 11px; display:block;">Nombre del lugar:</label>
                    <input type="text" id="infowindow-name-${t}" 
                           value="${n.name||""}" 
                           oninput="updateCheckpointData(${t}, 'name', this.value)"
                           style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc; border-radius:4px;">
                </div>

                <div style="display: flex; gap: 10px; margin-bottom: 10px;">
                    <div style="flex:1;">
                        <label style="font-size: 11px; display:block;">Permanencia (min):</label>
                        <input type="number" 
                               value="${n.stayTime||5}" 
                               oninput="updateCheckpointData(${t}, 'stayTime', this.value)"
                               style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc;">
                    </div>
                    <div style="flex:1; ${transitDisplay}">
                        <label style="font-size: 11px; display:block;">Tránsito (min):</label>
                        <input type="number" 
                               value="${n.transitTime||3}" 
                               oninput="updateCheckpointData(${t}, 'transitTime', this.value)"
                               style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc;">
                    </div>
                </div>

                <button class="btn btn-xs btn-danger" 
                        style="width:100%; padding: 5px; font-size: 11px; cursor:pointer;"
                        onclick="removeCheckpoint(${t})">
                    Eliminar Punto
                </button>
            </div>`}),c.open(window.mapInstance,e)}async function x(e){const{AdvancedMarkerElement:t,PinElement:o}=await google.maps.importLibrary("marker"),n=l.length+1,a=new o({glyph:n.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),r=new t({map:window.mapInstance,position:e,content:a.element,title:`Punto de control ${n}`});r.addListener("click",()=>{const i=s.indexOf(r);console.log("Marcador clickeado. Índice encontrado:",i),console.log("Total marcadores en array:",s.length),i!==-1?m(r,i):console.error("Error: El marcador clickeado no existe en checkpointMarkers.")}),l.push({lat:e.lat(),lng:e.lng(),order:n,name:`Punto ${n}`,stayTime:5,transitTime:n===1?0:3}),s.push(r),p(),u(),console.log("Checkpoints actuales:",l)}function p(){const e=document.getElementById("checkpoint-list-body");e.innerHTML="",l.forEach((t,o)=>{const n=o===0?'<span class="text-muted">---</span>':`${t.transitTime||0} min`,a=`
            <tr>
                <td class="text-center">
                    <span class="badge bg-primary">${o+1}</span>
                </td>
                <td>
                    <div class="fw-bold text-truncate" style="max-width: 150px;" title="${t.name||"Sin nombre"}">
                        ${t.name||'<i class="text-muted">Punto sin nombre</i>'}
                    </div>
                </td>
                <td class="small text-muted">
                    ${t.lat.toFixed(5)}, ${t.lng.toFixed(5)}
                </td>
                <td class="text-center">
                    ${t.stayTime||0} min
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
            </tr>`;e.insertAdjacentHTML("beforeend",a)})}function v(){document.getElementById("btn-clear-path")?.addEventListener("click",()=>{confirm("¿Borrar todos los puntos?")&&I()}),f("#btn-confirm-map").addEventListener("click",C)}window.removeCheckpoint=function(e){s[e]&&s[e].setMap(null),l.splice(e,1),s.splice(e,1),l.forEach((t,o)=>t.order=o+1),E(),u(),p(),c&&c.close()};function E(){s.forEach((e,t)=>{const o=t+1,n=new google.maps.marker.PinElement({glyphText:o.toString(),background:"#FBBC04"});e.content=n.element,e.title=`Punto ${o}`,google.maps.event.clearInstanceListeners(e),e.addListener("click",()=>{m(e,t)})})}function u(){const e=l.map(t=>({lat:t.lat,lng:t.lng}));d?d.setPath(e):d=new google.maps.Polyline({path:e,geodesic:!0,strokeColor:"#FF0000",strokeOpacity:1,strokeWeight:3,map:window.mapInstance})}function I(){s.forEach(e=>e.setMap(null)),s=[],l=[],d&&d.setPath([]),p(),c&&c.close(),console.log("Ruta reseteada correctamente.")}async function C(){if(l.length===0){alert("Define al menos un punto en la ruta antes de confirmar.");return}localStorage.setItem("pending_checkpoints",JSON.stringify(l));const t=`/private/patrols/edit/${document.getElementById("target-patrol-externalId").value}`;console.log(`[MapPicker] Finalizando edición. Navegando a ${t}`),await y(t)}(async function(){console.log("[init] IIFE iniciado");const t=googleMapsConfig.apiKey;try{await b(t),h(),v()}catch(o){console.error("[site-map.js] Error al cargar la API de Google Maps:",o)}})();
