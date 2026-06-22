import{f as h}from"../../auth.js";import{n as f}from"../../navigation-handler.js";let r=[],s=[],d=null,c=null;const g=t=>document.querySelector(t),b=(()=>{let t=!1,e=null;return i=>(t||(e=new Promise((n,o)=>{const a=document.createElement("script");a.src=`https://maps.googleapis.com/maps/api/js?key=${i}&v=weekly`,a.async=!0,a.defer=!0,a.onload=()=>{console.log("[Google Maps API] Script cargado correctamente."),t=!0,n()},a.onerror=l=>{console.error("[Google Maps API] Error al cargar el script:",l),o(new Error("Error al cargar Google Maps API"))},document.head.appendChild(a)})),e)})(),v=async()=>{const t=document.getElementById("patrols-map-picker");if(!t){console.warn("[initMap] Contenedor #patrols-map-picker no encontrado en el DOM.");return}try{const{Map:e}=await google.maps.importLibrary("maps"),i=new e(t,{center:{lat:-33.4489,lng:-70.6693},zoom:12,mapId:googleMapsConfig.mapId,disableDefaultUI:!0,mapTypeId:"hybrid"});console.log("[initMap] Mapa inicializado."),window.mapInstance=i,await k(),await L(),i.addListener("click",n=>{E(n.latLng)})}catch(e){console.error("[initMap] Error al inicializar el mapa:",e)}};async function k(){const t=document.getElementById("target-site-id"),e=t?t.value:null;if(!e){console.error("No se pudo obtener el ID del sitio del input oculto");return}try{const i=await h("/api/sites/projections-by-user",{method:"GET",headers:{Accept:"application/json"}});if(!i.ok){console.log("Error al cargar sitios. Intente nuevamente.");return}const n=await i.json();if(!Array.isArray(n)||!n.every(a=>a.id&&a.lat&&a.lon)){console.log("Datos de sitios inválidos.");return}console.log("Buscando siteId:",e),console.log("IDs disponibles en siteData:",n.map(a=>a.id));const o=n.find(a=>Number(a.id)===Number(e));console.log("site: "+o),I(o)}catch(i){console.error("Fallo al obtener sitios:",i),console.log("Error al cargar sitios. Intente nuevamente.")}}async function I(t){const{AdvancedMarkerElement:e,PinElement:i}=await google.maps.importLibrary("marker"),n=new google.maps.LatLngBounds,o=document.createElement("div");o.style.backgroundColor="#fff",o.style.border="1px solid grey",o.style.padding="4px 8px",o.style.borderRadius="8px",o.style.boxShadow="0 2px 6px rgba(0,0,0,0.3)",o.style.position="absolute",o.style.top="-40px",o.style.left="50%",o.style.transform="translateX(-50%)",o.style.whiteSpace="nowrap",o.style.display="none",o.textContent=`Sitio: ${t.name||"Sin Nombre"}`;const a=new i({scale:.8,glyphColor:"#3176e3",background:"#359dd1",borderColor:"#1d4d9b"}),l=document.createElement("div");l.style.position="relative",l.appendChild(a.element),l.appendChild(o),new e({map:window.mapInstance,position:{lat:t.lat,lng:t.lon},content:l}),l.addEventListener("mouseenter",()=>{o.style.display="block"}),l.addEventListener("mouseleave",()=>{o.style.display="none"}),n.extend(new google.maps.LatLng(t.lat,t.lon)),console.log(`[addSitesToMapAndSelect] Marcador añadido para sitio: ${t.id}`),window.mapInstance.fitBounds(n)}async function y(t,e){const{InfoWindow:i}=await google.maps.importLibrary("maps"),n=r[e];if(!n){console.error("No se encontró el punto para el índice:",e);return}const o=e===0?"display:none;":"display:block;";c&&c.close(),c=new i({content:`
        <div class="custom-infowindow">

            <div class="iw-header">
                <strong>Configuración Punto ${e+1}</strong>
            </div>

            <div class="iw-body">
                <div class="iw-field">
                    <label>Nombre del punto</label>
                    <input type="text" id="infowindow-name-${e}" 
                        value="${n.name||""}" 
                        placeholder="Ej. Acceso Principal"
                        oninput="updateCheckpointData(${e}, 'name', this.value)">
                </div>

                <div class="iw-field">
                    <label>Descripción</label>
                    <input type="text" id="infowindow-description-${e}" 
                        value="${n.description||""}" 
                        placeholder="Ej. Revisar candados"
                        oninput="updateCheckpointData(${e}, 'description', this.value)">
                </div>

                <div class="iw-row">
                    <div class="iw-field">
                        <label>Permanencia (min)</label>
                        <input type="number" min="0"
                            value="${n.stayTime||5}" 
                            oninput="updateCheckpointData(${e}, 'stayTime', parseInt(this.value) || 0)">
                    </div>
                    <div class="iw-field" style="${o}">
                        <label>Tránsito (min)</label>
                        <input type="number" min="0"
                            value="${n.transitTime||3}" 
                            oninput="updateCheckpointData(${e}, 'transitTime', parseInt(this.value) || 0)">
                    </div>
                </div>
            </div>

            <div class="iw-actions">
                <button class="btn-drag" onclick="toggleDraggable(${e})">
                    Mover
                </button>
                <button class="btn-remove" onclick="removeCheckpoint(${e})">
                    Eliminar
                </button>
            </div>

        </div>`}),c.addListener("closeclick",()=>{M()}),c.open(window.mapInstance,t)}async function E(t){const{AdvancedMarkerElement:e,PinElement:i}=await google.maps.importLibrary("marker"),n=r.length+1,o=new i({glyph:n.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),a=new e({map:window.mapInstance,position:t,content:o.element,title:`Punto de control ${n}`});a.addListener("click",()=>{const l=s.indexOf(a);console.log("Marcador clickeado. Índice encontrado:",l),console.log("Total marcadores en array:",s.length),l!==-1?y(a,l):console.error("Error: El marcador clickeado no existe en checkpointMarkers.")}),r.push({latitude:t.lat(),longitude:t.lng(),checkpointOrder:n,name:`Punto ${n}`,stayTime:5,transitTime:n===1?0:3}),s.push(a),p(),u(),console.log("Checkpoints actuales:",r)}function p(){const t=document.getElementById("checkpoint-list-body");t.innerHTML="",r.forEach((e,i)=>{const n=i===0?'<span class="text-muted">---</span>':`${e.transitTime||0} min`,o=`
            <tr>
                <td class="text-center">
                    <span class="badge bg-primary">${i+1}</span>
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
                    ${n}
                </td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-danger border-0" 
                            onclick="removeCheckpoint(${i})" 
                            title="Eliminar punto">
                        <i class="bi bi-trash"></i> Eliminar
                    </button>
                </td>
            </tr>`;t.insertAdjacentHTML("beforeend",o)})}window.removeCheckpoint=function(t){s[t]&&s[t].setMap(null),r.splice(t,1),s.splice(t,1),r.forEach((e,i)=>e.order=i+1),$(),u(),p(),c&&c.close()};function $(){s.forEach((t,e)=>{const i=e+1,n=new google.maps.marker.PinElement({glyphText:i.toString(),background:"#FBBC04"});t.content=n.element,t.title=`Punto ${i}`,google.maps.event.clearInstanceListeners(t),t.addListener("click",()=>{y(t,e)})})}function u(){const t=r.map(e=>({lat:e.latitude,lng:e.longitude}));d?d.setPath(t):d=new google.maps.Polyline({path:t,geodesic:!0,strokeColor:"#FF0000",strokeOpacity:1,strokeWeight:3,map:window.mapInstance})}function C(){s.forEach(t=>t.setMap(null)),s=[],r=[],d&&d.setPath([]),p(),c&&c.close(),console.log("Ruta reseteada correctamente.")}function M(){const t=document.querySelector(".custom-infowindow");if(!t)return;const e=parseInt(t.getAttribute("data-current-index"));if(isNaN(e)||!r[e])return;const i=document.getElementById(`infowindow-name-${e}`),n=document.getElementById(`infowindow-description-${e}`),o=t.querySelectorAll('input[type="number"]');i&&(r[e].name=i.value,s[e]&&(s[e].title=i.value)),n&&(r[e].description=n.value,s[e]&&(s[e].description=n.value)),o.length>=2&&(r[e].stayTime=parseInt(o[0].value)||5,r[e].transitTime=parseInt(o[1].value)||3),console.log(`[InfoWindow] Datos del Punto ${e+1} guardados con éxito al cerrar.`)}async function P(){if(r.length===0){alert("Define al menos un punto en la ruta antes de confirmar.");return}localStorage.setItem("pending_checkpoints",JSON.stringify(r));const e=`/private/patrols/edit/${document.getElementById("target-patrol-externalId").value}`;console.log(`[MapPicker] Finalizando edición. Navegando a ${e}`),await f(e)}const L=async()=>{const t=document.getElementById("checkpoints-data");if(!t||!t.value){console.warn("No se encontraron puntos pre-cargados en el DOM.");return}try{const e=JSON.parse(t.value);if(e.length===0)return;const{AdvancedMarkerElement:i,PinElement:n}=await google.maps.importLibrary("marker"),o=new google.maps.LatLngBounds;for(const a of e){const l={lat:parseFloat(a.latitude),lng:parseFloat(a.longitude)};o.extend(l);const w=new n({glyph:a.checkpointOrder.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),m=new i({map:window.mapInstance,position:l,content:w.element,title:a.name,gmpDraggable:!1});m.description=a.description||"",r.push({externalId:a.externalId,latitude:l.lat,longitude:l.lng,checkpointOrder:a.checkpointOrder,name:a.name,description:a.description,stayTime:a.stayTime,transitTime:a.minutesToReach}),s.push(m)}u(),p(),window.mapInstance.fitBounds(o)}catch(e){console.error("Error al parsear los puntos ocultos:",e)}};async function T(){const t=document.getElementById("target-patrol-externalId").value;console.log(`[MapPicker] Navegando a patrol: ${t}`);const e=`/private/patrols/edit/${t}`;console.log(`[MapPicker] Cancelando edición en el mapa. Navegando a ${e}`),await f(e)}function x(){document.getElementById("btn-clear-path")?.addEventListener("click",()=>{confirm("¿Borrar todos los puntos?")&&C()}),g("#btn-confirm-map").addEventListener("click",P),g("#btn-cancel-path").addEventListener("click",T)}window.toggleDraggable=t=>{const e=s[t];e&&(e.gmpDraggable=!0,e.content&&(e.content.style.cursor="move",e.content.style.opacity="0.8"),window.infoWindowInstance&&window.infoWindowInstance.close(),e.addListener("dragend",i=>{const n=e.position,o=typeof n.lat=="function"?n.lat():n.lat,a=typeof n.lng=="function"?n.lng():n.lng;r[t].latitude=o,r[t].longitude=a,e.content&&(e.content.style.opacity="1"),u(),p(),console.log(`Punto ${t} movido a: ${o}, ${a}`)}),alert("Ahora puedes arrastrar el marcador amarillo en el mapa."))};(async function(){console.log("[init] IIFE iniciado");const e=googleMapsConfig.apiKey;try{await b(e),v(),x()}catch(i){console.error("[site-map.js] Error al cargar la API de Google Maps:",i)}})();
