import{f as h}from"../../../auth.js";import{n as g}from"../../../navigation-handler.js";import{l as v}from"../../../shared/maps/googlemaps-loader.js";import{i as w}from"../../../shared/maps/init-map.js";import{a as E}from"../../../shared/maps/advanced-marker.js";import{d as o}from"../../../shared/display-alert.js";import{g as T}from"../../../shared/maps/map-utils.js";import"../../../shared/dom-utils.js";let r=null,n=null;const M=35,s=a=>document.querySelector(a),b=s(".alert-success"),l=s(".alert-error");s(".alert-warning");const u=s(".alert-info");async function C(){if(!navigator.geolocation){o(l,"Geolocalización no soportada.",3e3);return}o(u,"Detectando el sitio mas cercano...",3e3);try{const a=await new Promise((t,i)=>navigator.geolocation.getCurrentPosition(t,i,{enableHighAccuracy:!0,timeout:15e3}));if(!c.length){o(l,"No hay sitios configurados.",3e3);return}const e=T(a.coords.latitude,a.coords.longitude,c);e&&e.distance<=M?(o(b,`Estás en el sitio "${e.name}".
                                          Puedes comenzar un patrullaje desde aquí.`,3e3),e&&(await m(e),await x(e.externalId))):e?(o(u,`El sitio más cercano es "${e.name}" a
            ${e.distance.toFixed(1)} metros. Acércate para acceder a las opciones.`,3e3),e&&await m(e),setTimeout(()=>g("/private/employees/dashboard"),3e3)):o(l,"No se encontró ningún sitio cercano.",5e3)}catch(a){o(l,"No fue posible obtener la ubicación: "+(a.message||"Tiempo de espera agotado."),5e3)}}const m=async a=>{const e=googleMapsConfig.apiKey;try{return r||(console.log("Loading Google Maps API..."),await v(e),r=await w("map",{mapTypeId:"hybrid",zoom:10,center:{lat:-33.4489,lng:-70.6693}})),a?(n&&n.setMap(null),n=await E(r,a.name,a.lat,a.lon),r.setCenter({lat:parseFloat(a.lat),lng:parseFloat(a.lon)}),r.setZoom(17),{map:r,nearestSite:a,initialMarker:n}):{map:r}}catch(t){console.error("[patrol-dashboard.js] Error al cargar la API de Google Maps:",t)}};let c=[];async function A(){try{const a=await h("/api/sites/user-sites",{credentials:"same-origin"});if(!a.ok)throw new Error(`Error cargando sitios: ${a.status}`);c=await a.json()}catch(a){console.error("No se pudo cargar la lista de sitios:",a),c=[]}}let p=[];const x=async a=>{const e=s("#patrolSchedulesContainer");try{const t=await h(`/api/patrol-schedules/next-24h-site-patrol-schedules/${a}`,{credentials:"same-origin"});if(!t.ok)throw new Error(`Error cargando schedules: ${t.status}`);p=await t.json(),k(p,e)}catch(t){console.error("No se pudo cargar la lista de sitios:",t),p=[],TableBody&&(TableBody.innerHTML=`
                <div class="text-center text-danger small p-3">
                     ❌ Error al cargar la agenda de rondas para las próximas 24 horas.
                </div>`)}},k=(a,e)=>{if(e){if(!a||a.length===0){e.innerHTML=`
            <div class="patrol-loader-placeholder text-muted small">
                📅 No tienes rondas programadas para hoy en este turno.
            </div>`;return}e.innerHTML=a.map((t,i)=>{const d=t.startTime?t.startTime.substring(0,5):"--:--",f=["is-scheduled","is-free","is-supervision"],y=f[i%f.length];return`
        <div class="patrol-card-item patrol-action-card" data-url="/private/patrol-executions/schedule-execute/${t.externalId}/">
            <div class="patrol-card-item__icon-box ${y}">
                <i class="bi bi-journal-check"></i>
            </div>
            <div class="patrol-card-item__body">
                <div class="card-main-info">
                    <h5>${t.patrolName}</h5> 
                    <span class="time-tag">${d}</span>
                </div>
                <p class="text-muted small">Ronda Planificada • Estado: ${t.status==="SCHEDULED"?"Pendiente":t.status}</p>
            </div>
            <div class="patrol-card-item__arrow">
                <i class="bi bi-chevron-right"></i>
            </div>
        </div>
    `}).join(""),e&&e.querySelectorAll(".patrol-action-card").forEach(t=>{t.addEventListener("click",i=>{const d=i.currentTarget.getAttribute("data-url");o(b,"Abriendo bitácora de ronda...",1500),setTimeout(()=>g(d,1500))})})}},$=()=>{setTimeout(()=>g("/private/employees/dashboard",!0),1e3)};function I(){const a=s("#backToEmployeeDashboard");a&&a.addEventListener("click",$)}async function N(){o(u,"Conectando con el servicio de Georreferenciación...",1500);try{await A(),await m(null),await C()}catch(a){console.error("[patrol-dashboard] initComponent failed",a)}}(async function(){I(),await N(),console.log("View patrol-dashboard page initialized.")})();
