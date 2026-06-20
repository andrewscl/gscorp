import{f as E}from"../../../auth.js";import{n as b}from"../../../navigation-handler.js";import{l as A}from"../../../shared/maps/googlemaps-loader.js";import{i as C}from"../../../shared/maps/init-map.js";import{a as M}from"../../../shared/maps/advanced-marker.js";import{d as r}from"../../../shared/display-alert.js";import{g as $}from"../../../shared/maps/map-utils.js";import"../../../shared/dom-utils.js";let s=null,c=null;const k=35,i=a=>document.querySelector(a),w=i(".alert-success"),d=i(".alert-error");i(".alert-warning");const g=i(".alert-info");async function P(){if(!navigator.geolocation){r(d,"Geolocalización no soportada.",3e3);return}r(g,"Detectando el sitio mas cercano...",3e3);try{const a=await new Promise((o,t)=>navigator.geolocation.getCurrentPosition(o,t,{enableHighAccuracy:!0,timeout:15e3}));if(!p.length){r(d,"No hay sitios configurados.",3e3);return}const e=$(a.coords.latitude,a.coords.longitude,p);e&&e.distance<=k?(r(w,`Estás en el sitio "${e.name}".
                                          Puedes comenzar un patrullaje desde aquí.`,3e3),e&&(await f(e),await I(e.externalId))):e?(r(g,`El sitio más cercano es "${e.name}" a
            ${e.distance.toFixed(1)} metros. Acércate para acceder a las opciones.`,3e3),e&&await f(e),setTimeout(()=>b("/private/employees/dashboard"),3e3)):r(d,"No se encontró ningún sitio cercano.",5e3)}catch(a){r(d,"No fue posible obtener la ubicación: "+(a.message||"Tiempo de espera agotado."),5e3)}}const f=async a=>{const e=googleMapsConfig.apiKey;try{return s||(console.log("Loading Google Maps API..."),await A(e),s=await C("map",{mapTypeId:"hybrid",zoom:10,center:{lat:-33.4489,lng:-70.6693}})),a?(c&&c.setMap(null),c=await M(s,a.name,a.lat,a.lon),s.setCenter({lat:parseFloat(a.lat),lng:parseFloat(a.lon)}),s.setZoom(17),{map:s,nearestSite:a,initialMarker:c}):{map:s}}catch(o){console.error("[patrol-dashboard.js] Error al cargar la API de Google Maps:",o)}};let p=[];async function N(){try{const a=await E("/api/sites/user-sites",{credentials:"same-origin"});if(!a.ok)throw new Error(`Error cargando sitios: ${a.status}`);p=await a.json()}catch(a){console.error("No se pudo cargar la lista de sitios:",a),p=[]}}let m=[];const I=async a=>{const e=i("#patrolSchedulesContainer");try{const o=await E(`/api/patrol-schedules/next-24h-site-patrol-schedules/${a}`,{credentials:"same-origin"});if(!o.ok)throw new Error(`Error cargando schedules: ${o.status}`);m=await o.json(),L(m,e)}catch(o){console.error("No se pudo cargar la lista de sitios:",o),m=[],TableBody&&(TableBody.innerHTML=`
                <div class="text-center text-danger small p-3">
                     ❌ Error al cargar la agenda de rondas para las próximas 24 horas.
                </div>`)}},L=(a,e)=>{if(!e)return;if(!a||a.length===0){e.innerHTML=`
            <div class="patrol-loader-placeholder text-muted small">
                📅 No tienes rondas programadas para hoy en este turno.
            </div>`;return}let o=!1;e.innerHTML=a.map((t,h)=>{const u=t.startTime?t.startTime.substring(0,5):"--:--";let n="is-scheduled",l="📅",y=!1;t.status==="En progreso"?(n="is-free",l="🔄"):t.status==="Completada"?(n="is-success",l="✅"):t.status==="No realizada"?(n="is-danger",l="❌"):t.status==="Programada"&&(o||(y=!0,o=!0));const v=t.status==="Programada"&&!y,T=v?"patrol-card-item is-locked":"patrol-card-item patrol-action-card",x=v?"":`data-url="/private/patrol-executions/schedule-execute/${t.externalId}/"`;return`
        <div class=${T} ${x}>
            <div class="patrol-card-item__icon-box ${n}">
                ${l}
            </div>
            <div class="patrol-card-item__body">
                <div class="card-main-info">
                    <h5>${t.patrolName}</h5> 
                    <span class="time-tag">${u}</span>
                </div>
                <p class="text-muted small">Ronda Planificada • Estado: ${t.status==="SCHEDULED"?"Pendiente":t.status}</p>
            </div>
            <div class="patrol-card-item__arrow">
                <i class="bi bi-chevron-right"></i>
            </div>
        </div>
    `}).join(""),e&&e.querySelectorAll(".patrol-action-card").forEach(t=>{t.addEventListener("click",h=>{const u=h.currentTarget.getAttribute("data-url");r(w,"Abriendo bitácora de ronda...",1500),setTimeout(()=>b(u,1500))})})},_=()=>{setTimeout(()=>b("/private/employees/dashboard",!0),1e3)};function D(){const a=i("#backToEmployeeDashboard");a&&a.addEventListener("click",_)}async function j(){r(g,"Conectando con el servicio de Georreferenciación...",1500);try{await N(),await f(null),await P()}catch(a){console.error("[patrol-dashboard] initComponent failed",a)}}(async function(){D(),await j(),console.log("View patrol-dashboard page initialized.")})();
