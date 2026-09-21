import{f as T}from"../../auth.js";import{n as A}from"../../navigation-handler.js";import{d as l}from"../../shared/display-alert.js";import"../../shared/dom-utils.js";const r=(t,e=document)=>e.querySelector(t),v=(t,e=document)=>Array.from((e||document).querySelectorAll(t)),M=r(".alert-warning"),u=r(".alert-error"),w=r(".alert-success"),b={Lunes:0,Martes:1,Miércoles:2,Jueves:3,Viernes:4,Sábado:5,Domingo:6},E=Object.keys(b);function L(t=document.querySelector("#shiftDayRanges")){v(".day-range-block",t).forEach((n,a)=>{v("input, select, textarea",n).forEach(o=>{const i=o.getAttribute("name");if(!i)return;const s=i.replace(/schedules\[\d+\]/,`schedules[${a}]`);o.setAttribute("name",s)})})}function H(t){const e=t.map(n=>{let a=b[n.dayFrom],o=b[n.dayTo];return a>o&&(o+=7),{from:a,to:o,orig:n}});for(let n=0;n<e.length;n++)for(let a=n+1;a<e.length;a++){const o=e[n],i=e[a];for(let s=o.from;s<=o.to;s++)for(let d=i.from;d<=i.to;d++)if(s%7===d%7)return[o.orig,i.orig]}return null}async function C(){const t=r("#submit"),e=r("#cancel"),n=r("#shiftRequestSite")?.value,a=r("#shiftRequestAccount")?.value,o=r("#shiftRequestServiceType")?.value,i=r("#shiftRequestStartDate")?.value,s=r("#shiftRequestEndDate")?.value||null,d=r("#shiftRequestDescription")?.value?.trim()||null,q=n?parseInt(n,10):null,k=a?parseInt(a,10):null;if(!q){l(u,"Debe seleccionar un sitio.");return}if(!o){l(u,"Debe seleccionar el tipo de servicio.");return}if(!i){l(u,"La fecha de inicio es obligatoria.");return}const m=[];if(v(".day-range-block").forEach((c,h)=>{const f=c.querySelector(".dayFrom")?.value,y=c.querySelector(".dayTo")?.value,D=c.querySelector('input[name$="[startTime]"]')?.value,$=c.querySelector('input[name$="[endTime]"]')?.value;f&&y&&D&&$&&m.push({dayFrom:f,dayTo:y,startTime:D,endTime:$})}),m.length===0){l(u,"Debe ingresar al menos un tramo de horario.");return}const p=H(m);if(p){l(u,`Solapamiento de días entre "${p[0].dayFrom}
                              a ${p[0].dayTo}" y "${p[1].dayFrom}
                              a ${p[1].dayTo}".
                              Ajuste los tramos para que no se crucen.`);return}t&&(t.disabled=!0),e&&(e.disabled=!0);try{const c=await T("/api/shift-requests/create",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({siteId:q,type:o,clientAccountId:k,startDate:i,endDate:s,description:d,schedules:m})});if(!c||!c.ok){let h="Ocurrió un problema al enviar el formulario.";if(c){const f=c.headers.get("content-type");f&&f.includes("application/json")&&(h=(await c.json()).message||h)}l(u,`Error: ${h}`),t&&(t.disabled=!1),e&&(e.disabled=!1);return}l(w,"La asignación de turno ha sido creada correctamente.",2e3),setTimeout(()=>{A("/private/shift-assignments/list",!0)},2e3)}catch(c){console.error(`[onClickCreate] Ocurrio un problema: ${c.message}`,c),l(u,"Error inesperado. Intente más tarde.",2e3),t&&(t.disabled=!1),e&&(e.disabled=!1)}}function S(t){const e=r("#shiftDayRanges"),n=v(".day-range-block").length,a=E.map(s=>`<option value="${s}">${s}</option>`).join(""),o=E.map(s=>`<option value="${s}">${s}</option>`).join(""),i=document.createElement("div");i.className="day-range-block",i.innerHTML=`
    <div class="form-group">
      <label>Día desde</label>
      <select name="schedules[${n}][dayFrom]" class="dayFrom" required>
        <option value="">Desde</option>${a}
      </select>
    </div>

    <div class="form-group">
      <label>Día hasta</label>
      <select name="schedules[${n}][dayTo]" class="dayTo" required>
        <option value="">Hasta</option>${o}
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
  `,i.querySelector(".remove-schedule-btn").addEventListener("click",()=>{i.remove(),L(e),e.querySelector(".day-range-block")||S()}),e.appendChild(i),L(e)}const x=()=>{l(M,"La solicitud de turno ha sido cancelada",1500),setTimeout(()=>A("/private/shift-requests/table-view"),1500)};function I(){const t=r("#submit");t&&t.addEventListener("click",C);const e=r("#cancel");e&&e.addEventListener("click",x);const n=r("#shiftDayRanges"),a=r("#addDayRange");n&&a&&(a.addEventListener("click",()=>S()),n.querySelector(".day-range-block")||S());const o=r("#projectExtenalId");o&&o.addEventListener("change",N)}async function N(){const t=r("#projectExternalId")?.value,e=r("#shiftRequestSite");if(!t)return;const n=`/api/sites/projects/${t}/sites`,a=await T(n,{method:GET,headers:{Accept:"application/json"}});if(!a)throw new Error("No se pudieron obtener los sitios del proyecto seleccionado.");const o=await a.json();o.length===0?e.innerHTML='<option value="">No hay sitios disponibles para el proyecto seleccionado</option>':(e.innerHTML='<option value="">Seleccione un sitio</option>',o.forEach(i=>{const s=document.createElement("option");s.value=i.externalId,s.textContent=i.name,e.appendChild(s)}),e.disabled=!1)}const g=new Map;async function R(t,e,n){if(t.innerHTML='<option value="">Seleccione cuenta</option>',!e||e.length===0){t.innerHTML='<option value="">No hay cuentas asociadas</option>',t.disabled=!0;return}e.forEach(a=>{const o=document.createElement("option");o.value=a.id,o.textContent=a.name,t.appendChild(o)}),n&&Array.from(t.options).some(o=>o.value===String(n))&&(t.value=String(n)),t.disabled=!1}async function j(t){const e=r("#shiftRequestAccount");if(!e)return;const n=e.value||"";if(e.disabled=!0,e.innerHTML='<option value="">Cargando cuentas...</option>',!t){e.innerHTML='<option value="">Seleccione cuenta</option>',e.disabled=!0;return}if(g.has(t)){R(e,g.get(t),n);return}try{const a=`/api/shift-requests/sites/${t}/accounts`,o=await T(a,{method:"GET"});if(o.status===401){e.innerHTML='<option value="">No autenticado</option>';return}if(o.status===403){e.innerHTML='<option value="">Sin acceso a las cuentas</option>';return}if(!o.ok){e.innerHTML='<option value="">Error cargando cuentas</option>';return}const i=await o.json();g.set(t,i),R(e,i,n)}catch(a){console.error("Error cargando accounts:",a),e.innerHTML='<option value="">Error cargando cuentas</option>',e.disabled=!0}}function F(){const t=r("#shiftRequestSite");t&&(t.addEventListener("change",e=>{const n=e.target.value||null;j(n)}),t.value&&j(t.value))}(function(){I(),F()})();
