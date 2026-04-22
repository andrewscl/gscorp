import{f as g}from"../../auth.js";import{n as y}from"../../navigation-handler.js";let l=[],s=[],d=null,c=null;const b=e=>document.querySelector(e),f=(()=>{let e=!1,t=null;return n=>(e||(t=new Promise((a,o)=>{const i=document.createElement("script");i.src=`https://maps.googleapis.com/maps/api/js?key=${n}&v=weekly`,i.async=!0,i.defer=!0,i.onload=()=>{console.log("[Google Maps API] Script cargado correctamente."),e=!0,a()},i.onerror=r=>{console.error("[Google Maps API] Error al cargar el script:",r),o(new Error("Error al cargar Google Maps API"))},document.head.appendChild(i)})),t)})(),h=async()=>{const e=document.getElementById("patrols-map-picker");if(!e){console.warn("[initMap] Contenedor #patrols-map-picker no encontrado en el DOM.");return}console.log("siteId en initMap: "+window.targetSiteId);try{const{Map:t}=await google.maps.importLibrary("maps"),n=new t(e,{center:{lat:-33.4489,lng:-70.6693},zoom:8,mapId:googleMapsConfig.mapId,disableDefaultUI:!0,mapTypeId:"hybrid"});console.log("[initMap] Mapa inicializado."),window.mapInstance=n,await w(),n.addListener("click",a=>{x(a.latLng)})}catch(t){console.error("[initMap] Error al inicializar el mapa:",t)}};async function w(){const e=document.getElementById("target-site-id"),t=e?e.value:null;if(!t){console.error("No se pudo obtener el ID del sitio del input oculto");return}try{const n=await g("/api/sites/projections-by-user",{method:"GET",headers:{Accept:"application/json"}});if(!n.ok){console.log("Error al cargar sitios. Intente nuevamente.");return}const a=await n.json();if(!Array.isArray(a)||!a.every(i=>i.id&&i.lat&&i.lon)){console.log("Datos de sitios inválidos.");return}console.log("Buscando siteId:",t),console.log("IDs disponibles en siteData:",a.map(i=>i.id));const o=a.find(i=>Number(i.id)===Number(t));console.log("site: "+o),k(o)}catch(n){console.error("Fallo al obtener sitios:",n),console.log("Error al cargar sitios. Intente nuevamente.")}}async function k(e){const{AdvancedMarkerElement:t,PinElement:n}=await google.maps.importLibrary("marker"),a=new google.maps.LatLngBounds,o=document.createElement("div");o.style.backgroundColor="#fff",o.style.border="1px solid grey",o.style.padding="4px 8px",o.style.borderRadius="8px",o.style.boxShadow="0 2px 6px rgba(0,0,0,0.3)",o.style.position="absolute",o.style.top="-40px",o.style.left="50%",o.style.transform="translateX(-50%)",o.style.whiteSpace="nowrap",o.style.display="none",o.textContent=`Sitio: ${e.name||"Sin Nombre"}`;const i=new n({scale:.8,glyphColor:"#3176e3",background:"#359dd1",borderColor:"#1d4d9b"}),r=document.createElement("div");r.style.position="relative",r.appendChild(i.element),r.appendChild(o),new t({map:window.mapInstance,position:{lat:e.lat,lng:e.lon},content:r}),r.addEventListener("mouseenter",()=>{o.style.display="block"}),r.addEventListener("mouseleave",()=>{o.style.display="none"}),a.extend(new google.maps.LatLng(e.lat,e.lon)),console.log(`[addSitesToMapAndSelect] Marcador añadido para sitio: ${e.id}`),window.mapInstance.fitBounds(a)}async function m(e,t){const{InfoWindow:n}=await google.maps.importLibrary("maps");c&&c.close(),c=new n({content:`
            <div style="color:black; padding:10px; font-family: sans-serif; min-width: 200px;">
                <strong style="display:block; margin-bottom:8px; border-bottom: 1px solid #ccc;">
                    Configuración Punto ${t+1}
                </strong>
                
                <div style="margin-bottom: 8px;">
                    <label style="font-size: 11px; display:block;">Nombre del lugar:</label>
                    <input type="text" id="infowindow-name-${t}" 
                           value="${point.name||""}" 
                           oninput="updateCheckpointData(${t}, 'name', this.value)"
                           style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc; border-radius:4px;">
                </div>

                <div style="display: flex; gap: 10px; margin-bottom: 10px;">
                    <div style="flex:1;">
                        <label style="font-size: 11px; display:block;">Permanencia (min):</label>
                        <input type="number" 
                               value="${point.stayTime||5}" 
                               oninput="updateCheckpointData(${t}, 'stayTime', this.value)"
                               style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc;">
                    </div>
                    <div style="flex:1; ${transitDisplay}">
                        <label style="font-size: 11px; display:block;">Tránsito (min):</label>
                        <input type="number" 
                               value="${point.transitTime||3}" 
                               oninput="updateCheckpointData(${t}, 'transitTime', this.value)"
                               style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc;">
                    </div>
                </div>

                <button class="btn btn-xs btn-danger" 
                        style="width:100%; padding: 5px; font-size: 11px; cursor:pointer;"
                        onclick="removeCheckpoint(${t})">
                    Eliminar Punto
                </button>
            </div>`}),c.open(window.mapInstance,e)}async function x(e){const{AdvancedMarkerElement:t,PinElement:n}=await google.maps.importLibrary("marker"),a=l.length+1,o=new n({glyph:a.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),i=new t({map:window.mapInstance,position:e,content:o.element,title:`Punto de control ${a}`});i.addListener("click",()=>{const r=s.indexOf(i);console.log("Marcador clickeado. Índice encontrado:",r),console.log("Total marcadores en array:",s.length),r!==-1?m(i,r):console.error("Error: El marcador clickeado no existe en checkpointMarkers.")}),l.push({lat:e.lat(),lng:e.lng(),order:a,name:`Punto ${a}`,stayTime:5,transitTime:a===1?0:3}),s.push(i),p(),u(),console.log("Checkpoints actuales:",l)}function p(){const e=document.getElementById("checkpoint-list-body");e.innerHTML="",l.forEach((t,n)=>{const a=n===0?'<span class="text-muted">---</span>':`${t.transitTime||0} min`,o=`
            <tr>
                <td class="text-center">
                    <span class="badge bg-primary">${n+1}</span>
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
                    ${a}
                </td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-danger border-0" 
                            onclick="removeCheckpoint(${n})" 
                            title="Eliminar punto">
                        <i class="bi bi-trash"></i> Eliminar
                    </button>
                </td>
            </tr>`;e.insertAdjacentHTML("beforeend",o)})}function v(){document.getElementById("btn-clear-path")?.addEventListener("click",()=>{confirm("¿Borrar todos los puntos?")&&I()}),b("#btn-confirm-map").addEventListener("click",C)}window.removeCheckpoint=function(e){s[e]&&s[e].setMap(null),l.splice(e,1),s.splice(e,1),l.forEach((t,n)=>t.order=n+1),E(),u(),p(),c&&c.close()};function E(){s.forEach((e,t)=>{const n=t+1,a=new google.maps.marker.PinElement({glyphText:n.toString(),background:"#FBBC04"});e.content=a.element,e.title=`Punto ${n}`,google.maps.event.clearInstanceListeners(e),e.addListener("click",()=>{m(e,t)})})}function u(){const e=l.map(t=>({lat:t.lat,lng:t.lng}));d?d.setPath(e):d=new google.maps.Polyline({path:e,geodesic:!0,strokeColor:"#FF0000",strokeOpacity:1,strokeWeight:3,map:window.mapInstance})}function I(){s.forEach(e=>e.setMap(null)),s=[],l=[],d&&d.setPath([]),p(),c&&c.close(),console.log("Ruta reseteada correctamente.")}async function C(){if(l.length===0){alert("Define al menos un punto en la ruta antes de confirmar.");return}localStorage.setItem("pending_checkpoints",JSON.stringify(l));const t=`/private/patrols/edit/${document.getElementById("target-patrol-externalId").value}`;console.log(`[MapPicker] Finalizando edición. Navegando a ${t}`),await y(t)}(async function(){console.log("[init] IIFE iniciado");const t=googleMapsConfig.apiKey;try{await f(t),h(),v()}catch(n){console.error("[site-map.js] Error al cargar la API de Google Maps:",n)}})();
