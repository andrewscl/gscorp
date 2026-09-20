import{f as A}from"../../auth.js";import{n as E}from"../../navigation-handler.js";import{d as l}from"../../shared/display-alert.js";import"../../shared/dom-utils.js";const i=(t,e=document)=>e.querySelector(t),h=(t,e=document)=>Array.from((e||document).querySelectorAll(t)),H=i(".alert-warning"),u=i(".alert-error"),F=i(".alert-success"),g={Lunes:0,Martes:1,Miércoles:2,Jueves:3,Viernes:4,Sábado:5,Domingo:6},D=Object.keys(g);function $(t=document.querySelector("#shiftDayRanges")){h(".day-range-block",t).forEach((n,o)=>{h("input, select, textarea",n).forEach(a=>{const r=a.getAttribute("name");if(!r)return;const s=r.replace(/schedules\[\d+\]/,`schedules[${o}]`);a.setAttribute("name",s)})})}function M(t){const e=t.map(n=>{let o=g[n.dayFrom],a=g[n.dayTo];return o>a&&(a+=7),{from:o,to:a,orig:n}});for(let n=0;n<e.length;n++)for(let o=n+1;o<e.length;o++){const a=e[n],r=e[o];for(let s=a.from;s<=a.to;s++)for(let d=r.from;d<=r.to;d++)if(s%7===d%7)return[a.orig,r.orig]}return null}async function C(){const t=i("#submit"),e=i("#cancel"),n=i("#shiftRequestSite")?.value,o=i("#shiftRequestAccount")?.value,a=i("#shiftRequestServiceType")?.value,r=i("#shiftRequestStartDate")?.value,s=i("#shiftRequestEndDate")?.value||null,d=i("#shiftRequestDescription")?.value?.trim()||null,S=n?parseInt(n,10):null,w=o?parseInt(o,10):null;if(!S){l(u,"Debe seleccionar un sitio.");return}if(!a){l(u,"Debe seleccionar el tipo de servicio.");return}if(!r){l(u,"La fecha de inicio es obligatoria.");return}const m=[];if(h(".day-range-block").forEach((c,y)=>{const p=c.querySelector(".dayFrom")?.value,v=c.querySelector(".dayTo")?.value,q=c.querySelector('input[name$="[startTime]"]')?.value,k=c.querySelector('input[name$="[endTime]"]')?.value;p&&v&&q&&k&&m.push({dayFrom:p,dayTo:v,startTime:q,endTime:k})}),m.length===0){l(u,"Debe ingresar al menos un tramo de horario.");return}const f=M(m);if(f){l(u,`Solapamiento de días entre "${f[0].dayFrom}
                              a ${f[0].dayTo}" y "${f[1].dayFrom}
                              a ${f[1].dayTo}".
                              Ajuste los tramos para que no se crucen.`);return}t&&(t.disabled=!0),e&&(e.disabled=!0);try{const c=await A("/api/shift-requests/create",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({siteId:S,type:a,clientAccountId:w,startDate:r,endDate:s,description:d,schedules:m})});if(!c||!c.ok){let y="Ocurrió un problema al enviar el formulario.";if(c){const p=c.headers.get("content-type");p&&p.includes("application/json")&&(y=(await c.json()).message||y)}l(u,`Error: ${y}`),t&&(t.disabled=!1),e&&(e.disabled=!1);return}l(F,"La asignación de turno ha sido creada correctamente.",2e3),setTimeout(()=>{E("/private/shift-assignments/list",!0)},2e3)}catch(c){console.error(`[onClickCreate] Ocurrio un problema: ${c.message}`,c),l(u,"Error inesperado. Intente más tarde.",2e3),t&&(t.disabled=!1),e&&(e.disabled=!1)}}function T(t){const e=i("#shiftDayRanges"),n=h(".day-range-block").length,o=D.map(s=>`<option value="${s}">${s}</option>`).join(""),a=D.map(s=>`<option value="${s}">${s}</option>`).join(""),r=document.createElement("div");r.className="day-range-block",r.innerHTML=`
    <div class="form-group">
      <label>Día desde</label>
      <select name="schedules[${n}][dayFrom]" class="dayFrom" required>
        <option value="">Desde</option>${o}
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
  `,r.querySelector(".remove-schedule-btn").addEventListener("click",()=>{r.remove(),$(e),e.querySelector(".day-range-block")||T()}),e.appendChild(r),$(e),typeof flatpickr<"u"&&flatpickr(r.querySelectorAll("input[type='time']"),{enableTime:!0,noCalendar:!0,dateFormat:"H:i",time_24hr:!0})}function j(){const t=i("#shiftDayRanges"),e=i("#addDayRange");t&&e&&(e.addEventListener("click",()=>T()),t.querySelector(".day-range-block")||T())}function I(){document.addEventListener("click",t=>{const e=t.target.closest(".calendar-btn, .clock-btn, .icon-btn");if(!e)return;const n=e.closest(".input-icon-wrap");if(!n)return;const o=n.querySelector("input");if(o&&(o.focus(),typeof o.showPicker=="function"))try{o.showPicker()}catch{}})}function O(){typeof flatpickr>"u"||(flatpickr("input[type='date']",{locale:"es",altInput:!0,altFormat:"d-m-Y",dateFormat:"Y-m-d",allowInput:!0,clickOpens:!0}),flatpickr("input[type='time']",{enableTime:!0,noCalendar:!0,dateFormat:"H:i",time_24hr:!0}))}const B=()=>{l(H,"La solicitud de turno ha sido cancelada",1500),setTimeout(()=>E("/private/shift-requests/table-view"),1500)};function N(){const t=i("#submit");t&&t.addEventListener("click",C);const e=i("#cancel");e&&e.addEventListener("click",B)}const b=new Map;async function R(t,e,n){if(t.innerHTML='<option value="">Seleccione cuenta</option>',!e||e.length===0){t.innerHTML='<option value="">No hay cuentas asociadas</option>',t.disabled=!0;return}e.forEach(o=>{const a=document.createElement("option");a.value=o.id,a.textContent=o.name,t.appendChild(a)}),n&&Array.from(t.options).some(a=>a.value===String(n))&&(t.value=String(n)),t.disabled=!1}async function L(t){const e=i("#shiftRequestAccount");if(!e)return;const n=e.value||"";if(e.disabled=!0,e.innerHTML='<option value="">Cargando cuentas...</option>',!t){e.innerHTML='<option value="">Seleccione cuenta</option>',e.disabled=!0;return}if(b.has(t)){R(e,b.get(t),n);return}try{const o=`/api/shift-requests/sites/${t}/accounts`,a=await A(o,{method:"GET"});if(a.status===401){e.innerHTML='<option value="">No autenticado</option>';return}if(a.status===403){e.innerHTML='<option value="">Sin acceso a las cuentas</option>';return}if(!a.ok){e.innerHTML='<option value="">Error cargando cuentas</option>';return}const r=await a.json();b.set(t,r),R(e,r,n)}catch(o){console.error("Error cargando accounts:",o),e.innerHTML='<option value="">Error cargando cuentas</option>',e.disabled=!0}}function x(){const t=i("#shiftRequestSite");t&&(t.addEventListener("change",e=>{const n=e.target.value||null;L(n)}),t.value&&L(t.value))}(function(){N(),j(),I(),x(),O()})();
