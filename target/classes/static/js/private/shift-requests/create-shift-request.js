import{f as E}from"../../auth.js";import{n as R}from"../../navigation-handler.js";import{d as l}from"../../shared/display-alert.js";import"../../shared/dom-utils.js";const i=(t,e=document)=>e.querySelector(t),h=(t,e=document)=>Array.from((e||document).querySelectorAll(t)),b={Lunes:0,Martes:1,Miércoles:2,Jueves:3,Viernes:4,Sábado:5,Domingo:6},q=Object.keys(b);function w(t=document.querySelector("#shiftDayRanges")){h(".day-range-block",t).forEach((n,o)=>{h("input, select, textarea",n).forEach(r=>{const a=r.getAttribute("name");if(!a)return;const s=a.replace(/schedules\[\d+\]/,`schedules[${o}]`);r.setAttribute("name",s)})})}function L(t){const e=t.map(n=>{let o=b[n.dayFrom],r=b[n.dayTo];return o>r&&(r+=7),{from:o,to:r,orig:n}});for(let n=0;n<e.length;n++)for(let o=n+1;o<e.length;o++){const r=e[n],a=e[o];for(let s=r.from;s<=r.to;s++)for(let u=a.from;u<=a.to;u++)if(s%7===u%7)return[r.orig,a.orig]}return null}async function C(){const t=i("#submit"),e=i("#cancel"),n=i("#shiftRequestSite")?.value,o=i("#shiftRequestAccount")?.value,r=i("#shiftRequestServiceType")?.value,a=i("#shiftRequestStartDate")?.value,s=i("#shiftRequestEndDate")?.value||null,u=i("#shiftRequestDescription")?.value?.trim()||null,k=n?parseInt(n,10):null,A=o?parseInt(o,10):null;if(!k){l(alertError,"Debe seleccionar un sitio.");return}if(!r){l(alertError,"Debe seleccionar el tipo de servicio.");return}if(!a){l(alertError,"La fecha de inicio es obligatoria.");return}const f=[];if(h(".day-range-block").forEach((c,m)=>{const p=c.querySelector(".dayFrom")?.value,y=c.querySelector(".dayTo")?.value,T=c.querySelector('input[name$="[startTime]"]')?.value,S=c.querySelector('input[name$="[endTime]"]')?.value;p&&y&&T&&S&&f.push({dayFrom:p,dayTo:y,startTime:T,endTime:S})}),f.length===0){l(alertError,"Debe ingresar al menos un tramo de horario.");return}const d=L(f);if(d){l(alertError,`Solapamiento de días entre "${d[0].dayFrom}
                              a ${d[0].dayTo}" y "${d[1].dayFrom}
                              a ${d[1].dayTo}".
                              Ajuste los tramos para que no se crucen.`);return}t&&(t.disabled=!0),e&&(e.disabled=!0);try{const c=await E("/api/shift-requests/create",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({siteId:k,type:r,clientAccountId:A,startDate:a,endDate:s,description:u,schedules:f})});if(!c||!c.ok){let m="Ocurrió un problema al enviar el formulario.";if(c){const p=c.headers.get("content-type");p&&p.includes("application/json")&&(m=(await c.json()).message||m)}l(alertError,`Error: ${m}`),t&&(t.disabled=!1),e&&(e.disabled=!1);return}l(alertSuccess,"La asignación de turno ha sido creada correctamente.",2e3),setTimeout(()=>{R("/private/shift-assignments/list",!0)},2e3)}catch(c){console.error(`[onClickCreate] Ocurrio un problema: ${c.message}`,c),l(alertError,"Error inesperado. Intente más tarde.",2e3),t&&(t.disabled=!1),e&&(e.disabled=!1)}}function g(t){const e=i("#shiftDayRanges"),n=h(".day-range-block").length,o=q.map(s=>`<option value="${s}">${s}</option>`).join(""),r=q.map(s=>`<option value="${s}">${s}</option>`).join(""),a=document.createElement("div");a.className="day-range-block",a.innerHTML=`
    <div class="form-group">
      <label>Día desde</label>
      <select name="schedules[${n}][dayFrom]" class="dayFrom" required>
        <option value="">Desde</option>${o}
      </select>
    </div>

    <div class="form-group">
      <label>Día hasta</label>
      <select name="schedules[${n}][dayTo]" class="dayTo" required>
        <option value="">Hasta</option>${r}
      </select>
    </div>

    <div class="form-group input-with-icon">
      <label>Hora inicio</label>
      <div class="input-icon-wrap">
        <input type="time" name="schedules[${n}][startTime]" value="" required />
        <button type="button" class="icon-btn clock-btn" aria-label="Abrir selector hora">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.4"/>
            <path d="M12 7v5l3 2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </div>

    <div class="form-group input-with-icon">
      <label>Hora término</label>
      <div class="input-icon-wrap">
        <input type="time" name="schedules[${n}][endTime]" value="" required />
        <button type="button" class="icon-btn clock-btn" aria-label="Abrir selector hora">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.4"/>
            <path d="M12 7v5l3 2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </div>

    <div class="form-group">
      <button type="button" class="btn-mini btn-danger remove-day-range">Quitar</button>
    </div>
  `,a.querySelector(".remove-day-range").addEventListener("click",()=>{a.remove(),w(e),e.querySelector(".day-range-block")||g()}),e.appendChild(a),w(e),typeof flatpickr<"u"&&flatpickr(a.querySelectorAll("input[type='time']"),{enableTime:!0,noCalendar:!0,dateFormat:"H:i",time_24hr:!0})}function M(){const t=i("#shiftDayRanges"),e=i("#addDayRange");t&&e&&(e.addEventListener("click",()=>g()),t.querySelector(".day-range-block")||g())}function H(){document.addEventListener("click",t=>{const e=t.target.closest(".calendar-btn, .clock-btn, .icon-btn");if(!e)return;const n=e.closest(".input-icon-wrap");if(!n)return;const o=n.querySelector("input");if(o&&(o.focus(),typeof o.showPicker=="function"))try{o.showPicker()}catch{}})}function F(){typeof flatpickr>"u"||(flatpickr("input[type='date']",{locale:"es",altInput:!0,altFormat:"d-m-Y",dateFormat:"Y-m-d",allowInput:!0,clickOpens:!0}),flatpickr("input[type='time']",{enableTime:!0,noCalendar:!0,dateFormat:"H:i",time_24hr:!0}))}const j=()=>{l(alertWarning,"La solicitud de turno ha sido cancelada",1500),setTimeout(()=>R("/private/shift-requests/table-view"),1500)};function x(){const t=i("#submit");t&&t.addEventListener("click",C);const e=i("#cancel");e&&e.addEventListener("click",j)}const v=new Map;async function D(t,e,n){if(t.innerHTML='<option value="">Seleccione cuenta</option>',!e||e.length===0){t.innerHTML='<option value="">No hay cuentas asociadas</option>',t.disabled=!0;return}e.forEach(o=>{const r=document.createElement("option");r.value=o.id,r.textContent=o.name,t.appendChild(r)}),n&&Array.from(t.options).some(r=>r.value===String(n))&&(t.value=String(n)),t.disabled=!1}async function $(t){const e=i("#shiftRequestAccount");if(!e)return;const n=e.value||"";if(e.disabled=!0,e.innerHTML='<option value="">Cargando cuentas...</option>',!t){e.innerHTML='<option value="">Seleccione cuenta</option>',e.disabled=!0;return}if(v.has(t)){D(e,v.get(t),n);return}try{const o=`/api/shift-requests/sites/${t}/accounts`,r=await E(o,{method:"GET"});if(r.status===401){e.innerHTML='<option value="">No autenticado</option>';return}if(r.status===403){e.innerHTML='<option value="">Sin acceso a las cuentas</option>';return}if(!r.ok){e.innerHTML='<option value="">Error cargando cuentas</option>';return}const a=await r.json();v.set(t,a),D(e,a,n)}catch(o){console.error("Error cargando accounts:",o),e.innerHTML='<option value="">Error cargando cuentas</option>',e.disabled=!0}}function B(){const t=i("#shiftRequestSite");t&&(t.addEventListener("change",e=>{const n=e.target.value||null;$(n)}),t.value&&$(t.value))}(function(){x(),M(),H(),B(),F()})();
