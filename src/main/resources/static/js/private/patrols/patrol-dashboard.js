import{f as u}from"../../auth.js";import{n as g}from"../../navigation-handler.js";import{l as b}from"../../shared/maps/googlemaps-loader.js";import{i as h}from"../../shared/maps/init-map.js";import{a as w}from"../../shared/maps/advanced-marker.js";import{d as e}from"../../shared/display-alert.js";import{g as E}from"../../shared/maps/map-utils.js";import"../../shared/dom-utils.js";let n=null,i=null;const T=35,r=a=>document.querySelector(a),M=r(".alert-success"),c=r(".alert-error");r(".alert-warning");const p=r(".alert-info"),P=async a=>{a&&a.preventDefault()};async function k(){e(p,"Conectando con el servicio de Georreferenciación...",1500);try{await v(),await m(null),await x()}catch(a){console.error("[patrol-dashboard] initComponent failed",a)}}async function x(){if(!navigator.geolocation){e(c,"Geolocalización no soportada.",3e3);return}e(p,"Detectando el sitio mas cercano...",3e3);try{const a=await new Promise((o,s)=>navigator.geolocation.getCurrentPosition(o,s,{enableHighAccuracy:!0,timeout:15e3}));if(!l.length){e(c,"No hay sitios configurados.",3e3);return}const t=E(a.coords.latitude,a.coords.longitude,l);t&&t.distance<=T?(e(M,`Estás en el sitio "${t.name}".
                                          Puedes comenzar un patrullaje desde aquí.`,3e3),t&&(await m(t),await I(t.externalId))):t?(e(p,`El sitio más cercano es "${t.name}" a
            ${t.distance.toFixed(1)} metros. Acércate para acceder a las opciones.`,3e3),t&&await m(t),setTimeout(()=>g("/private/employees/dashboard"),1500)):e(c,"No se encontró ningún sitio cercano.",5e3)}catch(a){e(c,"No fue posible obtener la ubicación: "+(a.message||"Tiempo de espera agotado."),5e3)}}const m=async a=>{const t=googleMapsConfig.apiKey;try{return n||(console.log("Loading Google Maps API..."),await b(t),n=await h("map",{mapTypeId:"hybrid",zoom:10,center:{lat:-33.4489,lng:-70.6693}})),a?(i&&i.setMap(null),i=await w(n,a.name,a.lat,a.lon),n.setCenter({lat:parseFloat(a.lat),lng:parseFloat(a.lon)}),n.setZoom(17),{map:n,nearestSite:a,initialMarker:i}):{map:n}}catch(o){console.error("[patrol-dashboard.js] Error al cargar la API de Google Maps:",o)}};let l=[];async function v(){try{const a=await u("/api/sites/user-sites",{credentials:"same-origin"});if(!a.ok)throw new Error(`Error cargando sitios: ${a.status}`);l=await a.json()}catch(a){console.error("No se pudo cargar la lista de sitios:",a),l=[]}}let d=[];const I=async a=>{const t=r("#patrolSchedulesTableBody");try{const o=await u(`/api/patrols/today-site-patrol-schedules/${a}`,{credentials:"same-origin"});if(!o.ok)throw new Error(`Error cargando schedules: ${o.status}`);d=await o.json(),A(d)}catch(o){console.error("No se pudo cargar la lista de sitios:",o),d=[],t&&(t.innerHTML=`
                  <tr>
                      <td colspan="3" class="text-center text-danger py-3">
                          ❌ Error al cargar la agenda de rondas.
                      </td>
                  </tr>`)}},A=a=>{const t=r("#patrolSchedulesTableBody");if(t){if(t.innerHTML="",!a||a.length===0){t.innerHTML=`
            <tr>
                <td colspan="3" class="text-center text-muted py-3">
                    📅 No hay rondas programadas para hoy en este sitio.
                </td>
            </tr>`;return}a.forEach(o=>{const s=document.createElement("tr"),f=o.startTime?o.startTime.substring(0,5):"--:--",y=o.name||"Ronda";s.innerHTML=`
            <td class="fw-bold text-dark">${f}</td>
            <td>${y}</td>
            <td>
                <button class="btn btn-sm btn-outline-primary py-1 px-3" 
                        onclick="initiateManualPatrol('${o.externalId}')">
                    🟢 Iniciar
                </button>
            </td>
        `,t.appendChild(s)})}},C=()=>{setTimeout(()=>g("/private/employees/dashboard",!0),1e3)};function N(){const a=r("#addPatrolExecution");a&&a.addEventListener("click",P);const t=r("#backToEmployeeDashboard");t&&t.addEventListener("click",C)}(async function(){N(),await k(),console.log("View patrol-dashboard page initialized.")})();
