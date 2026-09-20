import{f as R}from"../../auth.js";import{n as L}from"../../navigation-handler.js";import{d as l}from"../../shared/display-alert.js";import"../../shared/dom-utils.js";const i=(t,e=document)=>e.querySelector(t),y=(t,e=document)=>Array.from((e||document).querySelectorAll(t)),w=i(".alert-warning"),b={Lunes:0,Martes:1,Miércoles:2,Jueves:3,Viernes:4,Sábado:5,Domingo:6},k=Object.keys(b);function D(t=document.querySelector("#shiftDayRanges")){y(".day-range-block",t).forEach((n,r)=>{y("input, select, textarea",n).forEach(a=>{const o=a.getAttribute("name");if(!o)return;const s=o.replace(/schedules\[\d+\]/,`schedules[${r}]`);a.setAttribute("name",s)})})}function H(t){const e=t.map(n=>{let r=b[n.dayFrom],a=b[n.dayTo];return r>a&&(a+=7),{from:r,to:a,orig:n}});for(let n=0;n<e.length;n++)for(let r=n+1;r<e.length;r++){const a=e[n],o=e[r];for(let s=a.from;s<=a.to;s++)for(let u=o.from;u<=o.to;u++)if(s%7===u%7)return[a.orig,o.orig]}return null}async function F(){const t=i("#submit"),e=i("#cancel"),n=i("#shiftRequestSite")?.value,r=i("#shiftRequestAccount")?.value,a=i("#shiftRequestServiceType")?.value,o=i("#shiftRequestStartDate")?.value,s=i("#shiftRequestEndDate")?.value||null,u=i("#shiftRequestDescription")?.value?.trim()||null,T=n?parseInt(n,10):null,A=r?parseInt(r,10):null;if(!T){l(alertError,"Debe seleccionar un sitio.");return}if(!a){l(alertError,"Debe seleccionar el tipo de servicio.");return}if(!o){l(alertError,"La fecha de inicio es obligatoria.");return}const p=[];if(y(".day-range-block").forEach((c,m)=>{const f=c.querySelector(".dayFrom")?.value,h=c.querySelector(".dayTo")?.value,S=c.querySelector('input[name$="[startTime]"]')?.value,q=c.querySelector('input[name$="[endTime]"]')?.value;f&&h&&S&&q&&p.push({dayFrom:f,dayTo:h,startTime:S,endTime:q})}),p.length===0){l(alertError,"Debe ingresar al menos un tramo de horario.");return}const d=H(p);if(d){l(alertError,`Solapamiento de días entre "${d[0].dayFrom}
                              a ${d[0].dayTo}" y "${d[1].dayFrom}
                              a ${d[1].dayTo}".
                              Ajuste los tramos para que no se crucen.`);return}t&&(t.disabled=!0),e&&(e.disabled=!0);try{const c=await R("/api/shift-requests/create",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({siteId:T,type:a,clientAccountId:A,startDate:o,endDate:s,description:u,schedules:p})});if(!c||!c.ok){let m="Ocurrió un problema al enviar el formulario.";if(c){const f=c.headers.get("content-type");f&&f.includes("application/json")&&(m=(await c.json()).message||m)}l(alertError,`Error: ${m}`),t&&(t.disabled=!1),e&&(e.disabled=!1);return}l(alertSuccess,"La asignación de turno ha sido creada correctamente.",2e3),setTimeout(()=>{L("/private/shift-assignments/list",!0)},2e3)}catch(c){console.error(`[onClickCreate] Ocurrio un problema: ${c.message}`,c),l(alertError,"Error inesperado. Intente más tarde.",2e3),t&&(t.disabled=!1),e&&(e.disabled=!1)}}function g(t){const e=i("#shiftDayRanges"),n=y(".day-range-block").length,r=k.map(s=>`<option value="${s}">${s}</option>`).join(""),a=k.map(s=>`<option value="${s}">${s}</option>`).join(""),o=document.createElement("div");o.className="day-range-block",o.innerHTML=`
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
  `,o.querySelector("remove-schedule-btn").addEventListener("click",()=>{o.remove(),D(e),e.querySelector(".day-range-block")||g()}),e.appendChild(o),D(e),typeof flatpickr<"u"&&flatpickr(o.querySelectorAll("input[type='time']"),{enableTime:!0,noCalendar:!0,dateFormat:"H:i",time_24hr:!0})}function M(){const t=i("#shiftDayRanges"),e=i("#addDayRange");t&&e&&(e.addEventListener("click",()=>g()),t.querySelector(".day-range-block")||g())}function C(){document.addEventListener("click",t=>{const e=t.target.closest(".calendar-btn, .clock-btn, .icon-btn");if(!e)return;const n=e.closest(".input-icon-wrap");if(!n)return;const r=n.querySelector("input");if(r&&(r.focus(),typeof r.showPicker=="function"))try{r.showPicker()}catch{}})}function j(){typeof flatpickr>"u"||(flatpickr("input[type='date']",{locale:"es",altInput:!0,altFormat:"d-m-Y",dateFormat:"Y-m-d",allowInput:!0,clickOpens:!0}),flatpickr("input[type='time']",{enableTime:!0,noCalendar:!0,dateFormat:"H:i",time_24hr:!0}))}const I=()=>{l(w,"La solicitud de turno ha sido cancelada",1500),setTimeout(()=>L("/private/shift-requests/table-view"),1500)};function O(){const t=i("#submit");t&&t.addEventListener("click",F);const e=i("#cancel");e&&e.addEventListener("click",I)}const v=new Map;async function $(t,e,n){if(t.innerHTML='<option value="">Seleccione cuenta</option>',!e||e.length===0){t.innerHTML='<option value="">No hay cuentas asociadas</option>',t.disabled=!0;return}e.forEach(r=>{const a=document.createElement("option");a.value=r.id,a.textContent=r.name,t.appendChild(a)}),n&&Array.from(t.options).some(a=>a.value===String(n))&&(t.value=String(n)),t.disabled=!1}async function E(t){const e=i("#shiftRequestAccount");if(!e)return;const n=e.value||"";if(e.disabled=!0,e.innerHTML='<option value="">Cargando cuentas...</option>',!t){e.innerHTML='<option value="">Seleccione cuenta</option>',e.disabled=!0;return}if(v.has(t)){$(e,v.get(t),n);return}try{const r=`/api/shift-requests/sites/${t}/accounts`,a=await R(r,{method:"GET"});if(a.status===401){e.innerHTML='<option value="">No autenticado</option>';return}if(a.status===403){e.innerHTML='<option value="">Sin acceso a las cuentas</option>';return}if(!a.ok){e.innerHTML='<option value="">Error cargando cuentas</option>';return}const o=await a.json();v.set(t,o),$(e,o,n)}catch(r){console.error("Error cargando accounts:",r),e.innerHTML='<option value="">Error cargando cuentas</option>',e.disabled=!0}}function B(){const t=i("#shiftRequestSite");t&&(t.addEventListener("change",e=>{const n=e.target.value||null;E(n)}),t.value&&E(t.value))}(function(){O(),M(),C(),B(),j()})();
