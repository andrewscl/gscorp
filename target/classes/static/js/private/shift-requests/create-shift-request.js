import{f as b}from"../../auth.js";import{n as A}from"../../navigation-handler.js";import{d}from"../../shared/display-alert.js";import"../../shared/dom-utils.js";const i=(e,t=document)=>t.querySelector(e),g=(e,t=document)=>Array.from((t||document).querySelectorAll(e)),I=i(".alert-warning"),p=i(".alert-error"),j=i(".alert-success"),q={Lunes:0,Martes:1,Miércoles:2,Jueves:3,Viernes:4,Sábado:5,Domingo:6},L=Object.keys(q);function R(e=document.querySelector("#shiftDayRanges")){g(".day-range-block",e).forEach((n,r)=>{g("input, select, textarea",n).forEach(a=>{const o=a.getAttribute("name");if(!o)return;const s=o.replace(/schedules\[\d+\]/,`schedules[${r}]`);a.setAttribute("name",s)})})}function w(e){const t=e.map(n=>{let r=q[n.dayFrom],a=q[n.dayTo];return r>a&&(a+=7),{from:r,to:a,orig:n}});for(let n=0;n<t.length;n++)for(let r=n+1;r<t.length;r++){const a=t[n],o=t[r];for(let s=a.from;s<=a.to;s++)for(let c=o.from;c<=o.to;c++)if(s%7===c%7)return[a.orig,o.orig]}return null}async function k(){const e=i("#submit"),t=i("#cancel"),n=i("#shiftRequestSite")?.value,r=i("#shiftRequestAccount")?.value,a=i("#shiftRequestServiceType")?.value,o=i("#shiftRequestStartDate")?.value,s=i("#shiftRequestEndDate")?.value||null,c=i("#shiftRequestDescription")?.value?.trim()||null,u=n?parseInt(n,10):null,S=r?parseInt(r,10):null;if(!u){d(p,"Debe seleccionar un sitio.");return}if(!a){d(p,"Debe seleccionar el tipo de servicio.");return}if(!o){d(p,"La fecha de inicio es obligatoria.");return}const f=[];if(g(".day-range-block").forEach((l,y)=>{const v=l.querySelector(".dayFrom")?.value,E=l.querySelector(".dayTo")?.value,$=l.querySelector('input[name$="[startTime]"]')?.value,D=l.querySelector('input[name$="[endTime]"]')?.value;v&&E&&$&&D&&f.push({dayFrom:v,dayTo:E,startTime:$,endTime:D})}),f.length===0){d(p,"Debe ingresar al menos un tramo de horario.");return}const h=w(f);if(h){d(p,`Solapamiento de días entre "${h[0].dayFrom}
                              a ${h[0].dayTo}" y "${h[1].dayFrom}
                              a ${h[1].dayTo}".
                              Ajuste los tramos para que no se crucen.`);return}e&&(e.disabled=!0),t&&(t.disabled=!0);try{const l=await b("/api/shift-requests/create",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({siteId:u,type:a,clientAccountId:S,startDate:o,endDate:s,description:c,schedules:f})});if(!l||!l.ok){let y="Ocurrió un problema al enviar el formulario.";if(l){const v=l.headers.get("content-type");v&&v.includes("application/json")&&(y=(await l.json()).message||y)}d(p,`Error: ${y}`),e&&(e.disabled=!1),t&&(t.disabled=!1);return}d(j,"La asignación de turno ha sido creada correctamente.",2e3),setTimeout(()=>{A("/private/shift-assignments/list",!0)},2e3)}catch(l){console.error(`[onClickCreate] Ocurrio un problema: ${l.message}`,l),d(p,"Error inesperado. Intente más tarde.",2e3),e&&(e.disabled=!1),t&&(t.disabled=!1)}}function T(e){const t=i("#shiftDayRanges"),n=g(".day-range-block").length,r=L.map(s=>`<option value="${s}">${s}</option>`).join(""),a=L.map(s=>`<option value="${s}">${s}</option>`).join(""),o=document.createElement("div");o.className="day-range-block",o.innerHTML=`
    <div class="form-group">
      <label>Día desde</label>
      <select name="schedules[${n}][dayFrom]" class="dayFrom" required>
        <option value="">Desde</option>${r}
      </select>
    </div>

    <div class="form-group">
      <label>Día hasta</label>
      <select name="schedules[${n}][dayTo]" class="dayTo" required>
        <option value="">Hasta</option>${a}
      </select>
    </div>

    <div class="form-group">
      <label>Hora inicio</label>
      <div class="time-field">
        <input type="time" name="schedules[${n}][startTime]" value="" required />
        <span class="time-icon"></span>
      </div>
    </div>

    <div class="form-group">
      <label>Hora fin</label>
      <div class="time-field">
        <input type="time" name="schedules[${n}][endTime]" value="" required />
        <span class="time-icon"></span>
      </div>
    </div>
    <div class="form-group">
      <button type="button" class="remove-schedule-btn">Quitar</button>
    </div>
  `,o.querySelector(".remove-schedule-btn").addEventListener("click",()=>{o.remove(),R(t),t.querySelector(".day-range-block")||T()}),t.appendChild(o),R(t)}const x=()=>{d(I,"La solicitud de turno ha sido cancelada",1500),setTimeout(()=>A("/private/shift-requests/table-view"),1500)};function C(){const e=i("#submit");e&&e.addEventListener("click",k);const t=i("#cancel");t&&t.addEventListener("click",x);const n=i("#shiftDayRanges"),r=i("#addDayRange");n&&r&&(r.addEventListener("click",()=>T()),n.querySelector(".day-range-block")||T());const a=i("#projectExternalId");a&&a.addEventListener("change",F);const o=i("#shiftRequestSite");o&&o.addEventListener("change",H(siteId))}async function F(){const e=i("#projectExternalId")?.value,t=i("#siteExternalId");if(!e)return;const n=`/api/sites/projects/${e}/sites`,r=await b(n,{method:"GET",headers:{Accept:"application/json"}});if(!r)throw new Error("No se pudieron obtener los sitios del proyecto seleccionado.");const a=await r.json();a.length===0?t.innerHTML='<option value="">No hay sitios disponibles para el proyecto seleccionado</option>':(t.innerHTML='<option value="">Seleccione un sitio</option>',a.forEach(o=>{const s=document.createElement("option");s.value=o.externalId,s.textContent=o.name,t.appendChild(s)}),t.disabled=!1)}function m({selectEl:e,items:t,defaultLabel:n,emptyLabel:r,valueKey:a="externalId",preserveValue:o=""}){if(e){if(!t||t.length===0){e.innerHTML=`<option value="">${r}</option>`,e.disabled=!0;return}e.innerHTML=`<option value="">${n}</option>`,t.forEach(s=>{const c=document.createElement("option");c.value=s[a]||s.id||"",c.textContent=s.name||"",e.appendChild(c)}),o&&Array.from(e.options).some(c=>c.value===String(o))&&(e.value=String(o)),e.disabled=!1}}async function H(){const e=i("#shiftRequestAccount"),t=i("#siteZoneExternalId"),n=i("#siteExternalId")?.value;if(!e||!t)return;if(!n){m({selectEl:e,items:[],emptyLabel:"Primero seleccione un sitio."}),m({selectEl:t,items:[],emptyLabel:"Primero seleccione un sitio."});return}const r=e.value||"",a=t.value||"";try{const o=`/api/shift-requests/sites/${n}/accounts`,s=`/api/v1/site-zones/site/${n}/site-zones`,[c,u]=await Promise.all([b(o,{method:"GET",headers:{Accept:"application/json"}}),b(s,{method:"GET",headers:{Accept:"application/json"}})]);if(!c)throw new Error("No se pudieron obtener las cuentas del proyecto seleccionado.");if(!u)throw new Error("No se pudieron obtener las zonas del sitio seleccionado.");const S=c&&c.ok?await c.json():[],f=u&&u.ok?await u.json():[];m({selectEl:e,items:S,defaultLabel:"Seleccione una cuenta (opcional)",emptyLabel:"Sin cuentas asociadas",valueKey:"externalId",preserveValue:r}),m({selectEl:t,items:f,defaultLabel:"Seleccione una zona",emptyLabel:"El sitio no tiene zonas creadas (Requerido)",valueKey:"externalId",preserveValue:a})}catch(o){console.error("Error en HandleSiteChange:",o),m({selectEl:e,items:[],emptyLabel:"Error al cargar las cuentas."}),m({selectEl:t,items:[],emptyLabel:"Error al cargar las zonas."})}}function N(){const e=i("#shiftRequestSite");e&&(e.addEventListener("change",t=>{const n=t.target.value||null;loadAccountsForSite(n)}),e.value&&loadAccountsForSite(e.value))}(function(){C(),N()})();
