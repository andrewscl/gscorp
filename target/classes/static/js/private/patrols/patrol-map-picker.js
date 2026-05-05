import{f as v}from"../../auth.js";import{n as w}from"../../navigation-handler.js";let l=[],s=[],m=null,c=null;const b=t=>document.querySelector(t),E=(()=>{let t=!1,e=null;return a=>(t||(e=new Promise((i,o)=>{const n=document.createElement("script");n.src=`https://maps.googleapis.com/maps/api/js?key=${a}&v=weekly`,n.async=!0,n.defer=!0,n.onload=()=>{console.log("[Google Maps API] Script cargado correctamente."),t=!0,i()},n.onerror=r=>{console.error("[Google Maps API] Error al cargar el script:",r),o(new Error("Error al cargar Google Maps API"))},document.head.appendChild(n)})),e)})(),I=async()=>{const t=document.getElementById("patrols-map-picker");if(!t){console.warn("[initMap] Contenedor #patrols-map-picker no encontrado en el DOM.");return}try{const{Map:e}=await google.maps.importLibrary("maps"),a=new e(t,{center:{lat:-33.4489,lng:-70.6693},zoom:15,mapId:googleMapsConfig.mapId,disableDefaultUI:!0,mapTypeId:"hybrid"});console.log("[initMap] Mapa inicializado."),window.mapInstance=a,await C(),await S(),a.addListener("click",i=>{$(i.latLng)})}catch(e){console.error("[initMap] Error al inicializar el mapa:",e)}};async function C(){const t=document.getElementById("target-site-id"),e=t?t.value:null;if(!e){console.error("No se pudo obtener el ID del sitio del input oculto");return}try{const a=await v("/api/sites/projections-by-user",{method:"GET",headers:{Accept:"application/json"}});if(!a.ok){console.log("Error al cargar sitios. Intente nuevamente.");return}const i=await a.json();if(!Array.isArray(i)||!i.every(n=>n.id&&n.lat&&n.lon)){console.log("Datos de sitios inválidos.");return}console.log("Buscando siteId:",e),console.log("IDs disponibles en siteData:",i.map(n=>n.id));const o=i.find(n=>Number(n.id)===Number(e));console.log("site: "+o),M(o)}catch(a){console.error("Fallo al obtener sitios:",a),console.log("Error al cargar sitios. Intente nuevamente.")}}async function M(t){const{AdvancedMarkerElement:e,PinElement:a}=await google.maps.importLibrary("marker"),i=new google.maps.LatLngBounds,o=document.createElement("div");o.style.backgroundColor="#fff",o.style.border="1px solid grey",o.style.padding="4px 8px",o.style.borderRadius="8px",o.style.boxShadow="0 2px 6px rgba(0,0,0,0.3)",o.style.position="absolute",o.style.top="-40px",o.style.left="50%",o.style.transform="translateX(-50%)",o.style.whiteSpace="nowrap",o.style.display="none",o.textContent=`Sitio: ${t.name||"Sin Nombre"}`;const n=new a({scale:.8,glyphColor:"#3176e3",background:"#359dd1",borderColor:"#1d4d9b"}),r=document.createElement("div");r.style.position="relative",r.appendChild(n.element),r.appendChild(o),new e({map:window.mapInstance,position:{lat:t.lat,lng:t.lon},content:r}),r.addEventListener("mouseenter",()=>{o.style.display="block"}),r.addEventListener("mouseleave",()=>{o.style.display="none"}),i.extend(new google.maps.LatLng(t.lat,t.lon)),console.log(`[addSitesToMapAndSelect] Marcador añadido para sitio: ${t.id}`),window.mapInstance.fitBounds(i)}async function k(t,e){const{InfoWindow:a}=await google.maps.importLibrary("maps"),i=l[e];if(!i){console.error("No se encontró el punto para el índice:",e);return}const o=e===0?"display:none;":"display:block;";c&&c.close(),c=new a({content:`
            <div style="color:black; padding:10px; font-family: sans-serif; min-width: 200px;">
                <strong style="display:block; margin-bottom:8px; border-bottom: 1px solid #ccc;">
                    Configuración Punto ${e+1}
                </strong>
                
                <div style="margin-bottom: 8px;">
                    <label style="font-size: 11px; display:block;">Nombre del lugar:</label>
                    <input type="text" id="infowindow-name-${e}" 
                           value="${i.name||""}" 
                           oninput="updateCheckpointData(${e}, 'name', this.value)"
                           style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc; border-radius:4px;">
                </div>

                <div style="display: flex; gap: 10px; margin-bottom: 10px;">
                    <div style="flex:1;">
                        <label style="font-size: 11px; display:block;">Permanencia (min):</label>
                        <input type="number" 
                               value="${i.stayTime||5}" 
                               oninput="updateCheckpointData(${e}, 'stayTime', this.value)"
                               style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc;">
                    </div>
                    <div style="flex:1; ${o}">
                        <label style="font-size: 11px; display:block;">Tránsito (min):</label>
                        <input type="number" 
                               value="${i.transitTime||3}" 
                               oninput="updateCheckpointData(${e}, 'transitTime', this.value)"
                               style="width:100%; font-size:12px; padding:4px; border:1px solid #ccc;">
                    </div>
                </div>

                <button class="btn btn-xs btn-danger" 
                        style="width:100%; padding: 5px; font-size: 11px; cursor:pointer;"
                        onclick="removeCheckpoint(${e})">
                    Eliminar Punto
                </button>
            </div>`}),c.open(window.mapInstance,t)}async function $(t){const{AdvancedMarkerElement:e,PinElement:a}=await google.maps.importLibrary("marker"),i=l.length+1,o=new a({glyph:i.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),n=new e({map:window.mapInstance,position:t,content:o.element,title:`Punto de control ${i}`});n.addListener("click",()=>{const r=s.indexOf(n);console.log("Marcador clickeado. Índice encontrado:",r),console.log("Total marcadores en array:",s.length),r!==-1?k(n,r):console.error("Error: El marcador clickeado no existe en checkpointMarkers.")}),l.push({latitude:t.lat(),longitude:t.lng(),checkpointOrder:i,name:`Punto ${i}`,stayTime:5,transitTime:i===1?0:3}),s.push(n),p(),u(),console.log("Checkpoints actuales:",l)}function p(){const t=document.getElementById("checkpoint-list-body");t.innerHTML="",l.forEach((e,a)=>{const i=a===0?'<span class="text-muted">---</span>':`${e.transitTime||0} min`,o=`
            <tr>
                <td class="text-center">
                    <span class="badge bg-primary">${a+1}</span>
                </td>
                <td>
                    <div class="fw-bold text-truncate" style="max-width: 150px;" title="${e.name||"Sin nombre"}">
                        ${e.name||'<i class="text-muted">Punto sin nombre</i>'}
                    </div>
                </td>
                <td class="small text-muted">
                    ${e.latitude.toFixed(5)}, ${e.longitude.toFixed(5)}
                </td>
                <td class="text-center">
                    ${e.stayTime||0} min
                </td>
                <td class="text-center">
                    ${i}
                </td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-danger border-0" 
                            onclick="removeCheckpoint(${a})" 
                            title="Eliminar punto">
                        <i class="bi bi-trash"></i> Eliminar
                    </button>
                </td>
            </tr>`;t.insertAdjacentHTML("beforeend",o)})}window.removeCheckpoint=function(t){s[t]&&s[t].setMap(null),l.splice(t,1),s.splice(t,1),l.forEach((e,a)=>e.order=a+1),P(),u(),p(),c&&c.close()};function P(){s.forEach((t,e)=>{const a=e+1,i=new google.maps.marker.PinElement({glyphText:a.toString(),background:"#FBBC04"});t.content=i.element,t.title=`Punto ${a}`,google.maps.event.clearInstanceListeners(t),t.addListener("click",()=>{k(t,e)})})}function u(){const t=l.map(e=>({lat:e.latitude,lng:e.longitude}));m?m.setPath(t):m=new google.maps.Polyline({path:t,geodesic:!0,strokeColor:"#FF0000",strokeOpacity:1,strokeWeight:3,map:window.mapInstance})}function L(){s.forEach(t=>t.setMap(null)),s=[],l=[],m&&m.setPath([]),p(),c&&c.close(),console.log("Ruta reseteada correctamente.")}async function T(){if(l.length===0){alert("Define al menos un punto en la ruta antes de confirmar.");return}localStorage.setItem("pending_checkpoints",JSON.stringify(l));const e=`/private/patrols/edit/${document.getElementById("target-patrol-externalId").value}`;console.log(`[MapPicker] Finalizando edición. Navegando a ${e}`),await w(e)}const S=async()=>{const t=document.getElementById("checkpoints-data");if(!t||!t.value){console.warn("No se encontraron puntos pre-cargados en el DOM.");return}try{const e=JSON.parse(t.value);if(e.length===0)return;const{AdvancedMarkerElement:a,PinElement:i}=await google.maps.importLibrary("marker"),o=new google.maps.LatLngBounds;for(const n of e){const r={lat:parseFloat(n.latitude),lng:parseFloat(n.longitude)};o.extend(r);const x=new i({glyph:n.checkpointOrder.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),g=new a({map:window.mapInstance,position:r,content:x.element,title:n.name,gmpDraggable:!0});g.addListener("dragend",()=>{const d=g.position,f=typeof d.lat=="function"?d.lat():d.lat,h=typeof d.lng=="function"?d.lng():d.lng,y=s.indexOf(g);y!==-1&&(l[y].latitude=f,l[y].longitude=h,console.log(`Punto ${l[y].name} actualizado a:`,f,h),typeof u=="function"&&u(),typeof p=="function"&&p(),localStorage.setItem("pending_checkpoints",JSON.stringify(l)))}),l.push({externalId:n.externalId,latitude:r.lat,longitude:r.lng,checkpointOrder:n.checkpointOrder,name:n.name,stayTime:n.stayTime,transitTime:n.minutesToReach}),s.push(g)}u(),p(),window.mapInstance.fitBounds(o)}catch(e){console.error("Error al parsear los puntos ocultos:",e)}};async function B(){const t=document.getElementById("target-patrol-externalId").value;console.log(`[MapPicker] Navegando a patrol: ${t}`);const e=`/private/patrols/edit/${t}`;console.log(`[MapPicker] Cancelando edición en el mapa. Navegando a ${e}`),await w(e)}function A(){document.getElementById("btn-clear-path")?.addEventListener("click",()=>{confirm("¿Borrar todos los puntos?")&&L()}),b("#btn-confirm-map").addEventListener("click",T),b("#btn-cancel-path").addEventListener("click",B)}(async function(){console.log("[init] IIFE iniciado");const e=googleMapsConfig.apiKey;try{await E(e),I(),A()}catch(a){console.error("[site-map.js] Error al cargar la API de Google Maps:",a)}})();
