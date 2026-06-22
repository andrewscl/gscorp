import{f as b}from"../../auth.js";import{n as f}from"../../navigation-handler.js";let r=[],c=[],p=null,l=null;const y=t=>document.querySelector(t),v=(()=>{let t=!1,e=null;return n=>(t||(e=new Promise((a,i)=>{const o=document.createElement("script");o.src=`https://maps.googleapis.com/maps/api/js?key=${n}&v=weekly`,o.async=!0,o.defer=!0,o.onload=()=>{console.log("[Google Maps API] Script cargado correctamente."),t=!0,a()},o.onerror=s=>{console.error("[Google Maps API] Error al cargar el script:",s),i(new Error("Error al cargar Google Maps API"))},document.head.appendChild(o)})),e)})(),k=async()=>{const t=document.getElementById("patrols-map-picker");if(!t){console.warn("[initMap] Contenedor #patrols-map-picker no encontrado en el DOM.");return}try{const{Map:e}=await google.maps.importLibrary("maps"),n=new e(t,{center:{lat:-33.4489,lng:-70.6693},zoom:12,mapId:googleMapsConfig.mapId,disableDefaultUI:!0,mapTypeId:"hybrid"});console.log("[initMap] Mapa inicializado."),window.mapInstance=n,await I(),await P(),n.addListener("click",a=>{C(a.latLng)})}catch(e){console.error("[initMap] Error al inicializar el mapa:",e)}};async function I(){const t=document.getElementById("target-site-id"),e=t?t.value:null;if(!e){console.error("No se pudo obtener el ID del sitio del input oculto");return}try{const n=await b("/api/sites/projections-by-user",{method:"GET",headers:{Accept:"application/json"}});if(!n.ok){console.log("Error al cargar sitios. Intente nuevamente.");return}const a=await n.json();if(!Array.isArray(a)||!a.every(o=>o.id&&o.lat&&o.lon)){console.log("Datos de sitios inválidos.");return}console.log("Buscando siteId:",e),console.log("IDs disponibles en siteData:",a.map(o=>o.id));const i=a.find(o=>Number(o.id)===Number(e));console.log("site: "+i),E(i)}catch(n){console.error("Fallo al obtener sitios:",n),console.log("Error al cargar sitios. Intente nuevamente.")}}async function E(t){const{AdvancedMarkerElement:e,PinElement:n}=await google.maps.importLibrary("marker"),a=new google.maps.LatLngBounds,i=document.createElement("div");i.style.backgroundColor="#fff",i.style.border="1px solid grey",i.style.padding="4px 8px",i.style.borderRadius="8px",i.style.boxShadow="0 2px 6px rgba(0,0,0,0.3)",i.style.position="absolute",i.style.top="-40px",i.style.left="50%",i.style.transform="translateX(-50%)",i.style.whiteSpace="nowrap",i.style.display="none",i.textContent=`Sitio: ${t.name||"Sin Nombre"}`;const o=new n({scale:.8,glyphColor:"#3176e3",background:"#359dd1",borderColor:"#1d4d9b"}),s=document.createElement("div");s.style.position="relative",s.appendChild(o.element),s.appendChild(i),new e({map:window.mapInstance,position:{lat:t.lat,lng:t.lon},content:s}),s.addEventListener("mouseenter",()=>{i.style.display="block"}),s.addEventListener("mouseleave",()=>{i.style.display="none"}),a.extend(new google.maps.LatLng(t.lat,t.lon)),console.log(`[addSitesToMapAndSelect] Marcador añadido para sitio: ${t.id}`),window.mapInstance.fitBounds(a)}async function w(t,e){const{InfoWindow:n}=await google.maps.importLibrary("maps"),a=r[e];if(!a){console.error("No se encontró el punto para el índice:",e);return}const i=e===0?"display:none;":"display:block;";l&&l.close(),l=new n({content:`
        <div class="custom-infowindow">

            <div class="iw-header">
                <strong>Configuración Punto ${e+1}</strong>
            </div>

            <div class="iw-body">
                <div class="iw-field">
                    <label>Nombre del punto</label>
                    <input type="text" id="infowindow-name-${e}" 
                        value="${a.name||""}" 
                        placeholder="Ej. Acceso Principal"
                        oninput="updateCheckpointData(${e}, 'name', this.value)">
                </div>

                <div class="iw-field">
                    <label>Descripción</label>
                    <input type="text" id="infowindow-description-${e}" 
                        value="${a.description||""}" 
                        placeholder="Ej. Revisar candados"
                        oninput="updateCheckpointData(${e}, 'description', this.value)">
                </div>

                <div class="iw-row">
                    <div class="iw-field">
                        <label>Permanencia (min)</label>
                        <input type="number" min="0"
                            value="${a.stayTime||5}" 
                            oninput="updateCheckpointData(${e}, 'stayTime', parseInt(this.value) || 0)">
                    </div>
                    <div class="iw-field" style="${i}">
                        <label>Tránsito (min)</label>
                        <input type="number" min="0"
                            value="${a.transitTime||3}" 
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

        </div>`}),l&&(d(),l.close()),l.addListener("closeclick",()=>{d()}),l.open(window.mapInstance,t)}async function C(t){const{AdvancedMarkerElement:e,PinElement:n}=await google.maps.importLibrary("marker"),a=r.length+1,i=new n({glyph:a.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),o=new e({map:window.mapInstance,position:t,content:i.element,title:`Punto de control ${a}`});o.addListener("click",()=>{const s=c.indexOf(o);console.log("Marcador clickeado. Índice encontrado:",s),console.log("Total marcadores en array:",c.length),s!==-1?w(o,s):console.error("Error: El marcador clickeado no existe en checkpointMarkers.")}),r.push({latitude:t.lat(),longitude:t.lng(),checkpointOrder:a,name:`Punto ${a}`,stayTime:5,transitTime:a===1?0:3}),c.push(o),u(),m(),console.log("Checkpoints actuales:",r)}function u(){const t=document.getElementById("checkpoint-list-body");t.innerHTML="",r.forEach((e,n)=>{const a=n===0?'<span class="text-muted">---</span>':`${e.transitTime||0} min`,i=`
            <tr>
                <td class="text-center">
                    <span class="badge bg-primary">${n+1}</span>
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
                    ${a}
                </td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-danger border-0" 
                            onclick="removeCheckpoint(${n})" 
                            title="Eliminar punto">
                        <i class="bi bi-trash"></i> Eliminar
                    </button>
                </td>
            </tr>`;t.insertAdjacentHTML("beforeend",i)})}window.removeCheckpoint=function(t){c[t]&&c[t].setMap(null),r.splice(t,1),c.splice(t,1),r.forEach((e,n)=>e.order=n+1),$(),m(),u(),l&&l.close()};function $(){c.forEach((t,e)=>{const n=e+1,a=new google.maps.marker.PinElement({glyphText:n.toString(),background:"#FBBC04"});t.content=a.element,t.title=`Punto ${n}`,google.maps.event.clearInstanceListeners(t),t.addListener("click",()=>{w(t,e)})})}function m(){const t=r.map(e=>({lat:e.latitude,lng:e.longitude}));p?p.setPath(t):p=new google.maps.Polyline({path:t,geodesic:!0,strokeColor:"#FF0000",strokeOpacity:1,strokeWeight:3,map:window.mapInstance})}function M(){c.forEach(t=>t.setMap(null)),c=[],r=[],p&&p.setPath([]),u(),l&&l.close(),console.log("Ruta reseteada correctamente.")}function d(){const t=document.querySelector(".custom-infowindow");if(t){const n=parseInt(t.getAttribute("data-current-index"));if(!isNaN(n)&&r[n]){const a=document.getElementById(`infowindow-name-${n}`),i=document.getElementById(`infowindow-description-${n}`),o=t.querySelectorAll('input[type="number"]');a&&(r[n].name=a.value),i&&(r[n].description=i.value),o.length>=2&&(r[n].stayTime=parseInt(o[0].value)||5,r[n].transitTime=parseInt(o[1].value)||3)}}const e=r.map(n=>({externalId:n.externalId||null,name:n.name,description:n.description||"",latitude:n.latitude,longitude:n.longitude,checkpointOrder:n.checkpointOrder,stayTime:parseInt(n.stayTime)||5,transitTime:parseInt(n.transitTime)||3}));localStorage.setItem("pending_checkpoints",JSON.stringify(e)),console.log("[Storage] Estado de los checkpoints sincronizado y guardado.")}async function T(){if(r.length===0){alert("Define al menos un punto en la ruta antes de confirmar.");return}localStorage.setItem("pending_checkpoints",JSON.stringify(r));const e=`/private/patrols/edit/${document.getElementById("target-patrol-externalId").value}`;console.log(`[MapPicker] Finalizando edición. Navegando a ${e}`),await f(e)}const P=async()=>{const t=document.getElementById("checkpoints-data");if(!t||!t.value){console.warn("No se encontraron puntos pre-cargados en el DOM.");return}try{const e=JSON.parse(t.value);if(e.length===0)return;const{AdvancedMarkerElement:n,PinElement:a}=await google.maps.importLibrary("marker"),i=new google.maps.LatLngBounds;for(const o of e){const s={lat:parseFloat(o.latitude),lng:parseFloat(o.longitude)};i.extend(s);const h=new a({glyph:o.checkpointOrder.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),g=new n({map:window.mapInstance,position:s,content:h.element,title:o.name,gmpDraggable:!1});g.description=o.description||"",r.push({externalId:o.externalId,latitude:s.lat,longitude:s.lng,checkpointOrder:o.checkpointOrder,name:o.name,description:o.description,stayTime:o.stayTime,transitTime:o.minutesToReach}),c.push(g)}m(),u(),window.mapInstance.fitBounds(i)}catch(e){console.error("Error al parsear los puntos ocultos:",e)}};async function L(){const t=document.getElementById("target-patrol-externalId").value;console.log(`[MapPicker] Navegando a patrol: ${t}`);const e=`/private/patrols/edit/${t}`;console.log(`[MapPicker] Cancelando edición en el mapa. Navegando a ${e}`),await f(e)}function S(){document.getElementById("btn-clear-path")?.addEventListener("click",()=>{confirm("¿Borrar todos los puntos?")&&M()}),y("#btn-confirm-map").addEventListener("click",T),y("#btn-cancel-path").addEventListener("click",L)}window.toggleDraggable=t=>{typeof d=="function"&&d();const e=c[t];e&&(e.gmpDraggable=!0,e.content&&(e.content.style.cursor="move",e.content.style.opacity="0.8"),window.infoWindowInstance&&window.infoWindowInstance.close(),e.addListener("dragend",n=>{const a=e.position,i=typeof a.lat=="function"?a.lat():a.lat,o=typeof a.lng=="function"?a.lng():a.lng;r[t].latitude=i,r[t].longitude=o,typeof d=="function"&&d(),e.content&&(e.content.style.opacity="1"),m(),u(),console.log(`Punto ${t} movido a: ${i}, ${o}`)}),alert("Ahora puedes arrastrar el marcador amarillo en el mapa."))};(async function(){console.log("[init] IIFE iniciado");const e=googleMapsConfig.apiKey;try{await v(e),k(),S()}catch(n){console.error("[site-map.js] Error al cargar la API de Google Maps:",n)}})();
