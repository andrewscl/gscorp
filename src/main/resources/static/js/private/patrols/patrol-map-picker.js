import{f as h}from"../../auth.js";import{n as g}from"../../navigation-handler.js";let l=[],s=[],d=null,c=null;const u=t=>document.querySelector(t),b=(()=>{let t=!1,e=null;return i=>(t||(e=new Promise((n,a)=>{const o=document.createElement("script");o.src=`https://maps.googleapis.com/maps/api/js?key=${i}&v=weekly`,o.async=!0,o.defer=!0,o.onload=()=>{console.log("[Google Maps API] Script cargado correctamente."),t=!0,n()},o.onerror=r=>{console.error("[Google Maps API] Error al cargar el script:",r),a(new Error("Error al cargar Google Maps API"))},document.head.appendChild(o)})),e)})(),v=async()=>{const t=document.getElementById("patrols-map-picker");if(!t){console.warn("[initMap] Contenedor #patrols-map-picker no encontrado en el DOM.");return}try{const{Map:e}=await google.maps.importLibrary("maps"),i=new e(t,{center:{lat:-33.4489,lng:-70.6693},zoom:12,mapId:googleMapsConfig.mapId,disableDefaultUI:!0,mapTypeId:"hybrid"});console.log("[initMap] Mapa inicializado."),window.mapInstance=i,await k(),await P(),i.addListener("click",n=>{E(n.latLng)})}catch(e){console.error("[initMap] Error al inicializar el mapa:",e)}};async function k(){const t=document.getElementById("target-site-id"),e=t?t.value:null;if(!e){console.error("No se pudo obtener el ID del sitio del input oculto");return}try{const i=await h("/api/sites/projections-by-user",{method:"GET",headers:{Accept:"application/json"}});if(!i.ok){console.log("Error al cargar sitios. Intente nuevamente.");return}const n=await i.json();if(!Array.isArray(n)||!n.every(o=>o.id&&o.lat&&o.lon)){console.log("Datos de sitios inválidos.");return}console.log("Buscando siteId:",e),console.log("IDs disponibles en siteData:",n.map(o=>o.id));const a=n.find(o=>Number(o.id)===Number(e));console.log("site: "+a),I(a)}catch(i){console.error("Fallo al obtener sitios:",i),console.log("Error al cargar sitios. Intente nuevamente.")}}async function I(t){const{AdvancedMarkerElement:e,PinElement:i}=await google.maps.importLibrary("marker"),n=new google.maps.LatLngBounds,a=document.createElement("div");a.style.backgroundColor="#fff",a.style.border="1px solid grey",a.style.padding="4px 8px",a.style.borderRadius="8px",a.style.boxShadow="0 2px 6px rgba(0,0,0,0.3)",a.style.position="absolute",a.style.top="-40px",a.style.left="50%",a.style.transform="translateX(-50%)",a.style.whiteSpace="nowrap",a.style.display="none",a.textContent=`Sitio: ${t.name||"Sin Nombre"}`;const o=new i({scale:.8,glyphColor:"#3176e3",background:"#359dd1",borderColor:"#1d4d9b"}),r=document.createElement("div");r.style.position="relative",r.appendChild(o.element),r.appendChild(a),new e({map:window.mapInstance,position:{lat:t.lat,lng:t.lon},content:r}),r.addEventListener("mouseenter",()=>{a.style.display="block"}),r.addEventListener("mouseleave",()=>{a.style.display="none"}),n.extend(new google.maps.LatLng(t.lat,t.lon)),console.log(`[addSitesToMapAndSelect] Marcador añadido para sitio: ${t.id}`),window.mapInstance.fitBounds(n)}async function y(t,e){const{InfoWindow:i}=await google.maps.importLibrary("maps"),n=l[e];if(!n){console.error("No se encontró el punto para el índice:",e);return}const a=e===0?"display:none;":"display:block;";c&&c.close(),c=new i({content:`
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
                    <div class="iw-field" style="${a}">
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

        </div>`}),c.open(window.mapInstance,t)}async function E(t){const{AdvancedMarkerElement:e,PinElement:i}=await google.maps.importLibrary("marker"),n=l.length+1,a=new i({glyph:n.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),o=new e({map:window.mapInstance,position:t,content:a.element,title:`Punto de control ${n}`});o.addListener("click",()=>{const r=s.indexOf(o);console.log("Marcador clickeado. Índice encontrado:",r),console.log("Total marcadores en array:",s.length),r!==-1?y(o,r):console.error("Error: El marcador clickeado no existe en checkpointMarkers.")}),l.push({latitude:t.lat(),longitude:t.lng(),checkpointOrder:n,name:`Punto ${n}`,stayTime:5,transitTime:n===1?0:3}),s.push(o),p(),m(),console.log("Checkpoints actuales:",l)}function p(){const t=document.getElementById("checkpoint-list-body");t.innerHTML="",l.forEach((e,i)=>{const n=i===0?'<span class="text-muted">---</span>':`${e.transitTime||0} min`,a=`
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
            </tr>`;t.insertAdjacentHTML("beforeend",a)})}window.removeCheckpoint=function(t){s[t]&&s[t].setMap(null),l.splice(t,1),s.splice(t,1),l.forEach((e,i)=>e.order=i+1),C(),m(),p(),c&&c.close()};function C(){s.forEach((t,e)=>{const i=e+1,n=new google.maps.marker.PinElement({glyphText:i.toString(),background:"#FBBC04"});t.content=n.element,t.title=`Punto ${i}`,google.maps.event.clearInstanceListeners(t),t.addListener("click",()=>{y(t,e)})})}function m(){const t=l.map(e=>({lat:e.latitude,lng:e.longitude}));d?d.setPath(t):d=new google.maps.Polyline({path:t,geodesic:!0,strokeColor:"#FF0000",strokeOpacity:1,strokeWeight:3,map:window.mapInstance})}function $(){s.forEach(t=>t.setMap(null)),s=[],l=[],d&&d.setPath([]),p(),c&&c.close(),console.log("Ruta reseteada correctamente.")}async function M(){if(l.length===0){alert("Define al menos un punto en la ruta antes de confirmar.");return}localStorage.setItem("pending_checkpoints",JSON.stringify(l));const e=`/private/patrols/edit/${document.getElementById("target-patrol-externalId").value}`;console.log(`[MapPicker] Finalizando edición. Navegando a ${e}`),await g(e)}const P=async()=>{const t=document.getElementById("checkpoints-data");if(!t||!t.value){console.warn("No se encontraron puntos pre-cargados en el DOM.");return}try{const e=JSON.parse(t.value);if(e.length===0)return;const{AdvancedMarkerElement:i,PinElement:n}=await google.maps.importLibrary("marker"),a=new google.maps.LatLngBounds;for(const o of e){const r={lat:parseFloat(o.latitude),lng:parseFloat(o.longitude)};a.extend(r);const w=new n({glyph:o.checkpointOrder.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),f=new i({map:window.mapInstance,position:r,content:w.element,title:o.name,gmpDraggable:!1});l.push({externalId:o.externalId,latitude:r.lat,longitude:r.lng,checkpointOrder:o.checkpointOrder,name:o.name,stayTime:o.stayTime,transitTime:o.minutesToReach}),s.push(f)}m(),p(),window.mapInstance.fitBounds(a)}catch(e){console.error("Error al parsear los puntos ocultos:",e)}};async function L(){const t=document.getElementById("target-patrol-externalId").value;console.log(`[MapPicker] Navegando a patrol: ${t}`);const e=`/private/patrols/edit/${t}`;console.log(`[MapPicker] Cancelando edición en el mapa. Navegando a ${e}`),await g(e)}function T(){document.getElementById("btn-clear-path")?.addEventListener("click",()=>{confirm("¿Borrar todos los puntos?")&&$()}),u("#btn-confirm-map").addEventListener("click",M),u("#btn-cancel-path").addEventListener("click",L)}window.toggleDraggable=t=>{const e=s[t];e&&(e.gmpDraggable=!0,e.content&&(e.content.style.cursor="move",e.content.style.opacity="0.8"),window.infoWindowInstance&&window.infoWindowInstance.close(),e.addListener("dragend",i=>{const n=e.position,a=typeof n.lat=="function"?n.lat():n.lat,o=typeof n.lng=="function"?n.lng():n.lng;l[t].latitude=a,l[t].longitude=o,e.content&&(e.content.style.opacity="1"),m(),p(),console.log(`Punto ${t} movido a: ${a}, ${o}`)}),alert("Ahora puedes arrastrar el marcador amarillo en el mapa."))};(async function(){console.log("[init] IIFE iniciado");const e=googleMapsConfig.apiKey;try{await b(e),v(),T()}catch(i){console.error("[site-map.js] Error al cargar la API de Google Maps:",i)}})();
