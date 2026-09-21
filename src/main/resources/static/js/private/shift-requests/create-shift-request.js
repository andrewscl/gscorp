import{f as b}from"../../auth.js";import{n as j}from"../../navigation-handler.js";import{d}from"../../shared/display-alert.js";import"../../shared/dom-utils.js";const r=(t,e=document)=>e.querySelector(t),g=(t,e=document)=>Array.from((e||document).querySelectorAll(t)),I=r(".alert-warning"),p=r(".alert-error"),x=r(".alert-success"),S={Lunes:0,Martes:1,Miércoles:2,Jueves:3,Viernes:4,Sábado:5,Domingo:6},D=Object.keys(S);function L(t=document.querySelector("#shiftDayRanges")){g(".day-range-block",t).forEach((o,i)=>{g("input, select, textarea",o).forEach(a=>{const n=a.getAttribute("name");if(!n)return;const s=n.replace(/schedules\[\d+\]/,`schedules[${i}]`);a.setAttribute("name",s)})})}function A(t){const e=t.map(o=>{let i=S[o.dayFrom],a=S[o.dayTo];return i>a&&(a+=7),{from:i,to:a,orig:o}});for(let o=0;o<e.length;o++)for(let i=o+1;i<e.length;i++){const a=e[o],n=e[i];for(let s=a.from;s<=a.to;s++)for(let c=n.from;c<=n.to;c++)if(s%7===c%7)return[a.orig,n.orig]}return null}async function R(){const t=r("#submit"),e=r("#cancel"),o=r("#siteExternalId")?.value||"",i=r("#shiftRequestAccount")?.value,a=r("#shiftRequestServiceType")?.value,n=r("#shiftRequestStartDate")?.value,s=r("#shiftRequestEndDate")?.value||null,c=r("#shiftRequestDescription")?.value?.trim()||null,f=i?parseInt(i,10):null;if(!o){d(p,"Debe seleccionar un sitio.");return}if(!a){d(p,"Debe seleccionar el tipo de servicio.");return}if(!n){d(p,"La fecha de inicio es obligatoria.");return}const m=[];if(g(".day-range-block").forEach((l,v)=>{const y=l.querySelector(".dayFrom")?.value,E=l.querySelector(".dayTo")?.value,q=l.querySelector('input[name$="[startTime]"]')?.value,$=l.querySelector('input[name$="[endTime]"]')?.value;y&&E&&q&&$&&m.push({dayFrom:y,dayTo:E,startTime:q,endTime:$})}),m.length===0){d(p,"Debe ingresar al menos un tramo de horario.");return}const u=A(m);if(u){d(p,`Solapamiento de días entre "${u[0].dayFrom}
                              a ${u[0].dayTo}" y "${u[1].dayFrom}
                              a ${u[1].dayTo}".
                              Ajuste los tramos para que no se crucen.`);return}t&&(t.disabled=!0),e&&(e.disabled=!0);try{const l=await b("/api/shift-requests/create",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({siteId,type:a,clientAccountId:f,startDate:n,endDate:s,description:c,schedules:m})});if(!l||!l.ok){let v="Ocurrió un problema al enviar el formulario.";if(l){const y=l.headers.get("content-type");y&&y.includes("application/json")&&(v=(await l.json()).message||v)}d(p,`Error: ${v}`),t&&(t.disabled=!1),e&&(e.disabled=!1);return}d(x,"La asignación de turno ha sido creada correctamente.",2e3),setTimeout(()=>{j("/private/shift-assignments/list",!0)},2e3)}catch(l){console.error(`[onClickCreate] Ocurrio un problema: ${l.message}`,l),d(p,"Error inesperado. Intente más tarde.",2e3),t&&(t.disabled=!1),e&&(e.disabled=!1)}}function T(t){const e=r("#shiftDayRanges"),o=g(".day-range-block").length,i=D.map(s=>`<option value="${s}">${s}</option>`).join(""),a=D.map(s=>`<option value="${s}">${s}</option>`).join(""),n=document.createElement("div");n.className="day-range-block",n.innerHTML=`
    <div class="form-group">
      <label>Día desde</label>
      <select name="schedules[${o}][dayFrom]" class="dayFrom" required>
        <option value="">Desde</option>${i}
      </select>
    </div>

    <div class="form-group">
      <label>Día hasta</label>
      <select name="schedules[${o}][dayTo]" class="dayTo" required>
        <option value="">Hasta</option>${a}
      </select>
    </div>

    <div class="form-group">
      <label>Hora inicio</label>
      <div class="time-field">
        <input type="time" name="schedules[${o}][startTime]" value="" required />
        <span class="time-icon"></span>
      </div>
    </div>

    <div class="form-group">
      <label>Hora fin</label>
      <div class="time-field">
        <input type="time" name="schedules[${o}][endTime]" value="" required />
        <span class="time-icon"></span>
      </div>
    </div>
    <div class="form-group">
      <button type="button" class="remove-schedule-btn">Quitar</button>
    </div>
  `,n.querySelector(".remove-schedule-btn").addEventListener("click",()=>{n.remove(),L(e),e.querySelector(".day-range-block")||T()}),e.appendChild(n),L(e)}const k=()=>{d(I,"La solicitud de turno ha sido cancelada",1500),setTimeout(()=>j("/private/shift-requests/table-view"),1500)};function w(){const t=r("#submit");t&&t.addEventListener("click",R);const e=r("#cancel");e&&e.addEventListener("click",k);const o=r("#shiftDayRanges"),i=r("#addDayRange");o&&i&&(i.addEventListener("click",()=>T()),o.querySelector(".day-range-block")||T());const a=r("#projectExternalId");a&&a.addEventListener("change",C);const n=r("#siteExternalId");n&&n.addEventListener("change",H)}async function C(){const t=r("#projectExternalId")?.value,e=r("#siteExternalId");if(!t)return;const o=`/api/sites/projects/${t}/sites`,i=await b(o,{method:"GET",headers:{Accept:"application/json"}});if(!i)throw new Error("No se pudieron obtener los sitios del proyecto seleccionado.");const a=await i.json();a.length===0?e.innerHTML='<option value="">No hay sitios disponibles para el proyecto seleccionado</option>':(e.innerHTML='<option value="">Seleccione un sitio</option>',a.forEach(n=>{const s=document.createElement("option");s.value=n.externalId,s.textContent=n.name,e.appendChild(s)}),e.disabled=!1)}function h({selectEl:t,items:e,defaultLabel:o,emptyLabel:i,valueKey:a="externalId",preserveValue:n=""}){if(t){if(!e||e.length===0){t.innerHTML=`<option value="">${i}</option>`,t.disabled=!0;return}t.innerHTML=`<option value="">${o}</option>`,e.forEach(s=>{const c=document.createElement("option");c.value=s[a]||s.id||"",c.textContent=s.name||"",t.appendChild(c)}),n&&Array.from(t.options).some(c=>c.value===String(n))&&(t.value=String(n)),t.disabled=!1}}async function H(){const t=r("#shiftRequestAccount"),e=r("#siteZoneExternalId"),o=r("#siteExternalId")?.value;if(!t||!e)return;if(!o){h({selectEl:t,items:[],emptyLabel:"Primero seleccione un sitio."}),h({selectEl:e,items:[],emptyLabel:"Primero seleccione un sitio."});return}const i=t.value||"",a=e.value||"";try{const n=`/api/shift-requests/sites/${o}/accounts`,s=`/api/v1/site-zones/site/${o}/site-zones`,[c,f]=await Promise.all([b(n,{method:"GET",headers:{Accept:"application/json"}}),b(s,{method:"GET",headers:{Accept:"application/json"}})]);if(!c)throw new Error("No se pudieron obtener las cuentas del proyecto seleccionado.");if(!f)throw new Error("No se pudieron obtener las zonas del sitio seleccionado.");const m=c&&c.ok?await c.json():[],u=f&&f.ok?await f.json():[];h({selectEl:t,items:m,defaultLabel:"Seleccione una cuenta (opcional)",emptyLabel:"Sin cuentas asociadas",valueKey:"externalId",preserveValue:i}),h({selectEl:e,items:u,defaultLabel:"Seleccione una zona",emptyLabel:"El sitio no tiene zonas creadas (Requerido)",valueKey:"externalId",preserveValue:a})}catch(n){console.error("Error en HandleSiteChange:",n),h({selectEl:t,items:[],emptyLabel:"Error al cargar las cuentas."}),h({selectEl:e,items:[],emptyLabel:"Error al cargar las zonas."})}}(function(){w()})();
