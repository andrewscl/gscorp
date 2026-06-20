import{f as v}from"../../../auth.js";import{n as b}from"../../../navigation-handler.js";import{l as x}from"../../../shared/maps/googlemaps-loader.js";import{i as A}from"../../../shared/maps/init-map.js";import{a as C}from"../../../shared/maps/advanced-marker.js";import{d as o}from"../../../shared/display-alert.js";import{g as M}from"../../../shared/maps/map-utils.js";import"../../../shared/dom-utils.js";let r=null,c=null;const $=35,s=a=>document.querySelector(a),E=s(".alert-success"),d=s(".alert-error");s(".alert-warning");const g=s(".alert-info");async function k(){if(!navigator.geolocation){o(d,"Geolocalización no soportada.",3e3);return}o(g,"Detectando el sitio mas cercano...",3e3);try{const a=await new Promise((e,i)=>navigator.geolocation.getCurrentPosition(e,i,{enableHighAccuracy:!0,timeout:15e3}));if(!p.length){o(d,"No hay sitios configurados.",3e3);return}const t=M(a.coords.latitude,a.coords.longitude,p);t&&t.distance<=$?(o(E,`Estás en el sitio "${t.name}".
                                          Puedes comenzar un patrullaje desde aquí.`,3e3),t&&(await f(t),await N(t.externalId))):t?(o(g,`El sitio más cercano es "${t.name}" a
            ${t.distance.toFixed(1)} metros. Acércate para acceder a las opciones.`,3e3),t&&await f(t),setTimeout(()=>b("/private/employees/dashboard"),3e3)):o(d,"No se encontró ningún sitio cercano.",5e3)}catch(a){o(d,"No fue posible obtener la ubicación: "+(a.message||"Tiempo de espera agotado."),5e3)}}const f=async a=>{const t=googleMapsConfig.apiKey;try{return r||(console.log("Loading Google Maps API..."),await x(t),r=await A("map",{mapTypeId:"hybrid",zoom:10,center:{lat:-33.4489,lng:-70.6693}})),a?(c&&c.setMap(null),c=await C(r,a.name,a.lat,a.lon),r.setCenter({lat:parseFloat(a.lat),lng:parseFloat(a.lon)}),r.setZoom(17),{map:r,nearestSite:a,initialMarker:c}):{map:r}}catch(e){console.error("[patrol-dashboard.js] Error al cargar la API de Google Maps:",e)}};let p=[];async function P(){try{const a=await v("/api/sites/user-sites",{credentials:"same-origin"});if(!a.ok)throw new Error(`Error cargando sitios: ${a.status}`);p=await a.json()}catch(a){console.error("No se pudo cargar la lista de sitios:",a),p=[]}}let m=[];const N=async a=>{const t=s("#patrolSchedulesContainer");try{const e=await v(`/api/patrol-schedules/next-24h-site-patrol-schedules/${a}`,{credentials:"same-origin"});if(!e.ok)throw new Error(`Error cargando schedules: ${e.status}`);m=await e.json(),I(m,t)}catch(e){console.error("No se pudo cargar la lista de sitios:",e),m=[],TableBody&&(TableBody.innerHTML=`
                <div class="text-center text-danger small p-3">
                     ❌ Error al cargar la agenda de rondas para las próximas 24 horas.
                </div>`)}},I=(a,t)=>{if(t){if(!a||a.length===0){t.innerHTML=`
            <div class="patrol-loader-placeholder text-muted small">
                📅 No tienes rondas programadas para hoy en este turno.
            </div>`;return}t.innerHTML=a.map((e,i)=>{const u=e.startTime?e.startTime.substring(0,5):"--:--";let n="is-scheduled",l="📅",h=!1;e.status==="En progreso"?(n="is-free",l="🔄"):e.status==="Completada"?(n="is-success",l="✅"):e.status==="No realizada"?(n="is-danger",l="❌"):e.status==="Programada"&&(nextAsigned||(h=!0),nextAsigned=!0);const y=e.status==="Programada"&&!h,w=y?"patrol-card-item is-locked":"patrol-card-item patrol-action-card",T=y?"":`data-url="/private/patrol-executions/schedule-execute/${e.externalId}/"`;return`
        <div class=${w} ${T}>
            <div class="patrol-card-item__icon-box ${n}">
                ${l}
            </div>
            <div class="patrol-card-item__body">
                <div class="card-main-info">
                    <h5>${e.patrolName}</h5> 
                    <span class="time-tag">${u}</span>
                </div>
                <p class="text-muted small">Ronda Planificada • Estado: ${e.status==="SCHEDULED"?"Pendiente":e.status}</p>
            </div>
            <div class="patrol-card-item__arrow">
                <i class="bi bi-chevron-right"></i>
            </div>
        </div>
    `}).join(""),t&&t.querySelectorAll(".patrol-action-card").forEach(e=>{e.addEventListener("click",i=>{const u=i.currentTarget.getAttribute("data-url");o(E,"Abriendo bitácora de ronda...",1500),setTimeout(()=>b(u,1500))})})}},L=()=>{setTimeout(()=>b("/private/employees/dashboard",!0),1e3)};function _(){const a=s("#backToEmployeeDashboard");a&&a.addEventListener("click",L)}async function D(){o(g,"Conectando con el servicio de Georreferenciación...",1500);try{await P(),await f(null),await k()}catch(a){console.error("[patrol-dashboard] initComponent failed",a)}}(async function(){_(),await D(),console.log("View patrol-dashboard page initialized.")})();
