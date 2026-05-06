import{f as w}from"../../auth.js";import{n as g}from"../../navigation-handler.js";let l=[],s=[],d=null,c=null;const u=e=>document.querySelector(e),b=(()=>{let e=!1,t=null;return o=>(e||(t=new Promise((r,a)=>{const n=document.createElement("script");n.src=`https://maps.googleapis.com/maps/api/js?key=${o}&v=weekly`,n.async=!0,n.defer=!0,n.onload=()=>{console.log("[Google Maps API] Script cargado correctamente."),e=!0,r()},n.onerror=i=>{console.error("[Google Maps API] Error al cargar el script:",i),a(new Error("Error al cargar Google Maps API"))},document.head.appendChild(n)})),t)})(),k=async()=>{const e=document.getElementById("patrols-map-picker");if(!e){console.warn("[initMap] Contenedor #patrols-map-picker no encontrado en el DOM.");return}try{const{Map:t}=await google.maps.importLibrary("maps"),o=new t(e,{center:{lat:-33.4489,lng:-70.6693},zoom:15,mapId:googleMapsConfig.mapId,disableDefaultUI:!0,mapTypeId:"hybrid"});console.log("[initMap] Mapa inicializado."),window.mapInstance=o,await v(),await $(),o.addListener("click",r=>{E(r.latLng)})}catch(t){console.error("[initMap] Error al inicializar el mapa:",t)}};async function v(){const e=document.getElementById("target-site-id"),t=e?e.value:null;if(!t){console.error("No se pudo obtener el ID del sitio del input oculto");return}try{const o=await w("/api/sites/projections-by-user",{method:"GET",headers:{Accept:"application/json"}});if(!o.ok){console.log("Error al cargar sitios. Intente nuevamente.");return}const r=await o.json();if(!Array.isArray(r)||!r.every(n=>n.id&&n.lat&&n.lon)){console.log("Datos de sitios inválidos.");return}console.log("Buscando siteId:",t),console.log("IDs disponibles en siteData:",r.map(n=>n.id));const a=r.find(n=>Number(n.id)===Number(t));console.log("site: "+a),I(a)}catch(o){console.error("Fallo al obtener sitios:",o),console.log("Error al cargar sitios. Intente nuevamente.")}}async function I(e){const{AdvancedMarkerElement:t,PinElement:o}=await google.maps.importLibrary("marker"),r=new google.maps.LatLngBounds,a=document.createElement("div");a.style.backgroundColor="#fff",a.style.border="1px solid grey",a.style.padding="4px 8px",a.style.borderRadius="8px",a.style.boxShadow="0 2px 6px rgba(0,0,0,0.3)",a.style.position="absolute",a.style.top="-40px",a.style.left="50%",a.style.transform="translateX(-50%)",a.style.whiteSpace="nowrap",a.style.display="none",a.textContent=`Sitio: ${e.name||"Sin Nombre"}`;const n=new o({scale:.8,glyphColor:"#3176e3",background:"#359dd1",borderColor:"#1d4d9b"}),i=document.createElement("div");i.style.position="relative",i.appendChild(n.element),i.appendChild(a),new t({map:window.mapInstance,position:{lat:e.lat,lng:e.lon},content:i}),i.addEventListener("mouseenter",()=>{a.style.display="block"}),i.addEventListener("mouseleave",()=>{a.style.display="none"}),r.extend(new google.maps.LatLng(e.lat,e.lon)),console.log(`[addSitesToMapAndSelect] Marcador añadido para sitio: ${e.id}`),window.mapInstance.fitBounds(r)}async function y(e,t){const{InfoWindow:o}=await google.maps.importLibrary("maps");if(!l[t]){console.error("No se encontró el punto para el índice:",t);return}c&&c.close(),c=new o({content:`
        <div class="custom-infowindow">
            <div class="iw-header">
                <i class="fa-solid fa-gear"></i>
                <span>Configuración ${cp.name}</span>
            </div>
            
            <div class="iw-body">
                <div class="iw-field">
                    <label>Nombre del lugar:</label>
                    <input type="text" value="${cp.name}" id="iw-name-${cp.externalId}">
                </div>
                <div class="iw-field">
                    <label>Permanencia (min):</label>
                    <input type="number" value="${cp.stayTime}" id="iw-stay-${cp.externalId}">
                </div>
            </div>

            <div class="iw-actions">
                <button class="btn-iw-move" id="btn-move-${cp.externalId}">
                    <i class="fa-solid fa-arrows-up-down-left-right"></i> Mover Punto
                </button>
                <button class="btn-iw-delete" onclick="removeCheckpoint('${cp.externalId}')">
                    <i class="fa-solid fa-trash"></i> Eliminar
                </button>
            </div>
        </div>
        `}),c.open(window.mapInstance,e)}async function E(e){const{AdvancedMarkerElement:t,PinElement:o}=await google.maps.importLibrary("marker"),r=l.length+1,a=new o({glyph:r.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),n=new t({map:window.mapInstance,position:e,content:a.element,title:`Punto de control ${r}`});n.addListener("click",()=>{const i=s.indexOf(n);console.log("Marcador clickeado. Índice encontrado:",i),console.log("Total marcadores en array:",s.length),i!==-1?y(n,i):console.error("Error: El marcador clickeado no existe en checkpointMarkers.")}),l.push({latitude:e.lat(),longitude:e.lng(),checkpointOrder:r,name:`Punto ${r}`,stayTime:5,transitTime:r===1?0:3}),s.push(n),p(),m(),console.log("Checkpoints actuales:",l)}function p(){const e=document.getElementById("checkpoint-list-body");e.innerHTML="",l.forEach((t,o)=>{const r=o===0?'<span class="text-muted">---</span>':`${t.transitTime||0} min`,a=`
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
                    ${t.latitude.toFixed(5)}, ${t.longitude.toFixed(5)}
                </td>
                <td class="text-center">
                    ${t.stayTime||0} min
                </td>
                <td class="text-center">
                    ${r}
                </td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-danger border-0" 
                            onclick="removeCheckpoint(${o})" 
                            title="Eliminar punto">
                        <i class="bi bi-trash"></i> Eliminar
                    </button>
                </td>
            </tr>`;e.insertAdjacentHTML("beforeend",a)})}window.removeCheckpoint=function(e){s[e]&&s[e].setMap(null),l.splice(e,1),s.splice(e,1),l.forEach((t,o)=>t.order=o+1),x(),m(),p(),c&&c.close()};function x(){s.forEach((e,t)=>{const o=t+1,r=new google.maps.marker.PinElement({glyphText:o.toString(),background:"#FBBC04"});e.content=r.element,e.title=`Punto ${o}`,google.maps.event.clearInstanceListeners(e),e.addListener("click",()=>{y(e,t)})})}function m(){const e=l.map(t=>({lat:t.latitude,lng:t.longitude}));d?d.setPath(e):d=new google.maps.Polyline({path:e,geodesic:!0,strokeColor:"#FF0000",strokeOpacity:1,strokeWeight:3,map:window.mapInstance})}function C(){s.forEach(e=>e.setMap(null)),s=[],l=[],d&&d.setPath([]),p(),c&&c.close(),console.log("Ruta reseteada correctamente.")}async function M(){if(l.length===0){alert("Define al menos un punto en la ruta antes de confirmar.");return}localStorage.setItem("pending_checkpoints",JSON.stringify(l));const t=`/private/patrols/edit/${document.getElementById("target-patrol-externalId").value}`;console.log(`[MapPicker] Finalizando edición. Navegando a ${t}`),await g(t)}const $=async()=>{const e=document.getElementById("checkpoints-data");if(!e||!e.value){console.warn("No se encontraron puntos pre-cargados en el DOM.");return}try{const t=JSON.parse(e.value);if(t.length===0)return;const{AdvancedMarkerElement:o,PinElement:r}=await google.maps.importLibrary("marker"),a=new google.maps.LatLngBounds;for(const n of t){const i={lat:parseFloat(n.latitude),lng:parseFloat(n.longitude)};a.extend(i);const f=new r({glyph:n.checkpointOrder.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),h=new o({map:window.mapInstance,position:i,content:f.element,title:n.name});l.push({externalId:n.externalId,latitude:i.lat,longitude:i.lng,checkpointOrder:n.checkpointOrder,name:n.name,stayTime:n.stayTime,transitTime:n.minutesToReach}),s.push(h)}m(),p(),window.mapInstance.fitBounds(a)}catch(t){console.error("Error al parsear los puntos ocultos:",t)}};async function P(){const e=document.getElementById("target-patrol-externalId").value;console.log(`[MapPicker] Navegando a patrol: ${e}`);const t=`/private/patrols/edit/${e}`;console.log(`[MapPicker] Cancelando edición en el mapa. Navegando a ${t}`),await g(t)}function L(){document.getElementById("btn-clear-path")?.addEventListener("click",()=>{confirm("¿Borrar todos los puntos?")&&C()}),u("#btn-confirm-map").addEventListener("click",M),u("#btn-cancel-path").addEventListener("click",P)}(async function(){console.log("[init] IIFE iniciado");const t=googleMapsConfig.apiKey;try{await b(t),k(),L()}catch(o){console.error("[site-map.js] Error al cargar la API de Google Maps:",o)}})();
