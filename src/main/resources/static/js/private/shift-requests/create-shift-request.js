import{f as R}from"../../auth.js";import{n as A}from"../../navigation-handler.js";import{d as l}from"../../shared/display-alert.js";import"../../shared/dom-utils.js";const i=(t,e=document)=>e.querySelector(t),y=(t,e=document)=>Array.from((e||document).querySelectorAll(t)),v={Lunes:0,Martes:1,Miércoles:2,Jueves:3,Viernes:4,Sábado:5,Domingo:6},k=Object.keys(v);function D(t=document.querySelector("#shiftDayRanges")){y(".day-range-block",t).forEach((n,o)=>{y("input, select, textarea",n).forEach(a=>{const r=a.getAttribute("name");if(!r)return;const s=r.replace(/schedules\[\d+\]/,`schedules[${o}]`);a.setAttribute("name",s)})})}function w(t){const e=t.map(n=>{let o=v[n.dayFrom],a=v[n.dayTo];return o>a&&(a+=7),{from:o,to:a,orig:n}});for(let n=0;n<e.length;n++)for(let o=n+1;o<e.length;o++){const a=e[n],r=e[o];for(let s=a.from;s<=a.to;s++)for(let u=r.from;u<=r.to;u++)if(s%7===u%7)return[a.orig,r.orig]}return null}async function H(){const t=i("#submit"),e=i("#cancel"),n=i("#shiftRequestSite")?.value,o=i("#shiftRequestAccount")?.value,a=i("#shiftRequestServiceType")?.value,r=i("#shiftRequestStartDate")?.value,s=i("#shiftRequestEndDate")?.value||null,u=i("#shiftRequestDescription")?.value?.trim()||null,T=n?parseInt(n,10):null,L=o?parseInt(o,10):null;if(!T){l(alertError,"Debe seleccionar un sitio.");return}if(!a){l(alertError,"Debe seleccionar el tipo de servicio.");return}if(!r){l(alertError,"La fecha de inicio es obligatoria.");return}const p=[];if(y(".day-range-block").forEach((c,m)=>{const f=c.querySelector(".dayFrom")?.value,b=c.querySelector(".dayTo")?.value,S=c.querySelector('input[name$="[startTime]"]')?.value,q=c.querySelector('input[name$="[endTime]"]')?.value;f&&b&&S&&q&&p.push({dayFrom:f,dayTo:b,startTime:S,endTime:q})}),p.length===0){l(alertError,"Debe ingresar al menos un tramo de horario.");return}const d=w(p);if(d){l(alertError,`Solapamiento de días entre "${d[0].dayFrom}
                              a ${d[0].dayTo}" y "${d[1].dayFrom}
                              a ${d[1].dayTo}".
                              Ajuste los tramos para que no se crucen.`);return}t&&(t.disabled=!0),e&&(e.disabled=!0);try{const c=await R("/api/shift-requests/create",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({siteId:T,type:a,clientAccountId:L,startDate:r,endDate:s,description:u,schedules:p})});if(!c||!c.ok){let m="Ocurrió un problema al enviar el formulario.";if(c){const f=c.headers.get("content-type");f&&f.includes("application/json")&&(m=(await c.json()).message||m)}l(alertError,`Error: ${m}`),t&&(t.disabled=!1),e&&(e.disabled=!1);return}l(alertSuccess,"La asignación de turno ha sido creada correctamente.",2e3),setTimeout(()=>{A("/private/shift-assignments/list",!0)},2e3)}catch(c){console.error(`[onClickCreate] Ocurrio un problema: ${c.message}`,c),l(alertError,"Error inesperado. Intente más tarde.",2e3),t&&(t.disabled=!1),e&&(e.disabled=!1)}}function g(t){const e=i("#shiftDayRanges"),n=y(".day-range-block").length,o=k.map(s=>`<option value="${s}">${s}</option>`).join(""),a=k.map(s=>`<option value="${s}">${s}</option>`).join(""),r=document.createElement("div");r.className="day-range-block",r.innerHTML=`
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

    <div class="form-group input-with-icon">
      <label>Hora inicio</label>
      <div class="input-icon-wrap">
        <input type="time" name="schedules[${n}][startTime]" value="" required />
        <button type="button" class="icon-clock-btn" aria-label="Abrir selector hora">
        </button>
      </div>
    </div>

    <div class="form-group input-with-icon">
      <label>Hora término</label>
      <div class="input-icon-wrap">
        <input type="time" name="schedules[${n}][endTime]" value="" required />
        <button type="button" class="icon-clock-btn" aria-label="Abrir selector hora">
        </button>
      </div>
    </div>
    <div class="form-group">
      <button type="button" class="btn-mini btn-danger remove-day-range">Quitar</button>
    </div>
  `,r.querySelector(".remove-day-range").addEventListener("click",()=>{r.remove(),D(e),e.querySelector(".day-range-block")||g()}),e.appendChild(r),D(e),typeof flatpickr<"u"&&flatpickr(r.querySelectorAll("input[type='time']"),{enableTime:!0,noCalendar:!0,dateFormat:"H:i",time_24hr:!0})}function F(){const t=i("#shiftDayRanges"),e=i("#addDayRange");t&&e&&(e.addEventListener("click",()=>g()),t.querySelector(".day-range-block")||g())}function M(){document.addEventListener("click",t=>{const e=t.target.closest(".calendar-btn, .clock-btn, .icon-btn");if(!e)return;const n=e.closest(".input-icon-wrap");if(!n)return;const o=n.querySelector("input");if(o&&(o.focus(),typeof o.showPicker=="function"))try{o.showPicker()}catch{}})}function C(){typeof flatpickr>"u"||(flatpickr("input[type='date']",{locale:"es",altInput:!0,altFormat:"d-m-Y",dateFormat:"Y-m-d",allowInput:!0,clickOpens:!0}),flatpickr("input[type='time']",{enableTime:!0,noCalendar:!0,dateFormat:"H:i",time_24hr:!0}))}const j=()=>{l(alertWarning,"La solicitud de turno ha sido cancelada",1500),setTimeout(()=>A("/private/shift-requests/table-view"),1500)};function I(){const t=i("#submit");t&&t.addEventListener("click",H);const e=i("#cancel");e&&e.addEventListener("click",j)}const h=new Map;async function $(t,e,n){if(t.innerHTML='<option value="">Seleccione cuenta</option>',!e||e.length===0){t.innerHTML='<option value="">No hay cuentas asociadas</option>',t.disabled=!0;return}e.forEach(o=>{const a=document.createElement("option");a.value=o.id,a.textContent=o.name,t.appendChild(a)}),n&&Array.from(t.options).some(a=>a.value===String(n))&&(t.value=String(n)),t.disabled=!1}async function E(t){const e=i("#shiftRequestAccount");if(!e)return;const n=e.value||"";if(e.disabled=!0,e.innerHTML='<option value="">Cargando cuentas...</option>',!t){e.innerHTML='<option value="">Seleccione cuenta</option>',e.disabled=!0;return}if(h.has(t)){$(e,h.get(t),n);return}try{const o=`/api/shift-requests/sites/${t}/accounts`,a=await R(o,{method:"GET"});if(a.status===401){e.innerHTML='<option value="">No autenticado</option>';return}if(a.status===403){e.innerHTML='<option value="">Sin acceso a las cuentas</option>';return}if(!a.ok){e.innerHTML='<option value="">Error cargando cuentas</option>';return}const r=await a.json();h.set(t,r),$(e,r,n)}catch(o){console.error("Error cargando accounts:",o),e.innerHTML='<option value="">Error cargando cuentas</option>',e.disabled=!0}}function O(){const t=i("#shiftRequestSite");t&&(t.addEventListener("change",e=>{const n=e.target.value||null;E(n)}),t.value&&E(t.value))}(function(){I(),F(),M(),O(),C()})();
