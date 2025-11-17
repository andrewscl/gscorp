import{f as A}from"../../auth.js";import{n as k}from"../../navigation-handler.js";const a=(t,e=document)=>e.querySelector(t),v=(t,e=document)=>Array.from((e||document).querySelectorAll(t)),b={Lunes:0,Martes:1,Miércoles:2,Jueves:3,Viernes:4,Sábado:5,Domingo:6},R=Object.keys(b);function D(t=document.querySelector("#shiftDayRanges")){v(".day-range-block",t).forEach((n,i)=>{v("input, select, textarea",n).forEach(o=>{const r=o.getAttribute("name");if(!r)return;const c=r.replace(/schedules\[\d+\]/,`schedules[${i}]`);o.setAttribute("name",c)})})}function E(t){const e=t.map(n=>{let i=b[n.dayFrom],o=b[n.dayTo];return i>o&&(o+=7),{from:i,to:o,orig:n}});for(let n=0;n<e.length;n++)for(let i=n+1;i<e.length;i++){const o=e[n],r=e[i];for(let c=o.from;c<=o.to;c++)for(let l=r.from;l<=r.to;l++)if(c%7===l%7)return[o.orig,r.orig]}return null}async function F(t){t.preventDefault();const e=a("#shiftRequestSite")?.value,n=a("#shiftRequestAccount")?.value,i=a("#shiftRequestServiceType")?.value,o=a("#shiftRequestStartDate")?.value,r=a("#shiftRequestEndDate")?.value||null,c=a("#shiftRequestDescription")?.value?.trim()||null,l=e?parseInt(e,10):null,q=n?parseInt(n,10):null,s=a("#createShiftRequestError"),p=a("#createShiftRequestOk");if(s&&(s.textContent=""),p&&(p.style.display="none"),!l){s&&(s.textContent="Debe seleccionar un sitio.");return}if(!q){s&&(s.textContent="Debe seleccionar una cuenta.");return}if(!i){s&&(s.textContent="Debe seleccionar el tipo de servicio.");return}if(!o){s&&(s.textContent="La fecha de inicio es obligatoria.");return}const h=[];if(v(".day-range-block").forEach((u,f)=>{const S=u.querySelector(".dayFrom")?.value,w=u.querySelector(".dayTo")?.value,T=u.querySelector('input[name$="[startTime]"]')?.value,C=u.querySelector('input[name$="[endTime]"]')?.value,L=u.querySelector('input[name$="[lunchTime]"]')?.value||null;S&&w&&T&&C&&h.push({dayFrom:S,dayTo:w,startTime:T,endTime:C,lunchTime:L})}),h.length===0){s&&(s.textContent="Debe ingresar al menos un tramo de horario.");return}const d=E(h);if(d){s&&(s.textContent=`Solapamiento de días entre "${d[0].dayFrom} a ${d[0].dayTo}" y "${d[1].dayFrom} a ${d[1].dayTo}". Ajuste los tramos para que no se crucen.`);return}const m=t.submitter||a('#createShiftRequestForm button[type="submit"]');m&&(m.disabled=!0);try{const u=await A("/api/shift-requests/create",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({siteId:l,type:i,accountId:q,startDate:o,endDate:r,description:c,schedules:h})});if(!u.ok){let f="";try{f=await u.text()}catch{}throw f||(f=`Error ${u.status}`),new Error(f)}p&&(p.style.display="block"),setTimeout(()=>{k("/private/shift-requests/table-view")},600)}catch(u){s&&(s.textContent=u.message)}finally{m&&(m.disabled=!1)}}function g(t){const e=a("#shiftDayRanges"),n=v(".day-range-block").length,i=R.map(c=>`<option value="${c}">${c}</option>`).join(""),o=R.map(c=>`<option value="${c}">${c}</option>`).join(""),r=document.createElement("div");r.className="day-range-block",r.innerHTML=`
    <div class="form-group">
      <label>Día desde</label>
      <select name="schedules[${n}][dayFrom]" class="dayFrom" required>
        <option value="">Desde</option>${i}
      </select>
    </div>

    <div class="form-group">
      <label>Día hasta</label>
      <select name="schedules[${n}][dayTo]" class="dayTo" required>
        <option value="">Hasta</option>${o}
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

    <div class="form-group input-with-icon">
      <label>Colación</label>
      <div class="input-icon-wrap">
        <input type="time" name="schedules[${n}][lunchTime]" value="" />
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
  `,r.querySelector(".remove-day-range").addEventListener("click",()=>{r.remove(),D(e),e.querySelector(".day-range-block")||g()}),e.appendChild(r),D(e),typeof flatpickr<"u"&&flatpickr(r.querySelectorAll("input[type='time']"),{enableTime:!0,noCalendar:!0,dateFormat:"H:i",time_24hr:!0})}function M(){const t=a("#shiftDayRanges"),e=a("#addDayRange");t&&e&&(e.addEventListener("click",()=>g()),t.querySelector(".day-range-block")||g())}function H(){document.addEventListener("click",t=>{const e=t.target.closest(".calendar-btn, .clock-btn, .icon-btn");if(!e)return;const n=e.closest(".input-icon-wrap");if(!n)return;const i=n.querySelector("input");if(i&&(i.focus(),typeof i.showPicker=="function"))try{i.showPicker()}catch{}})}function O(){typeof flatpickr>"u"||(flatpickr("input[type='date']",{locale:"es",altInput:!0,altFormat:"d-m-Y",dateFormat:"Y-m-d",allowInput:!0,clickOpens:!0}),flatpickr("input[type='time']",{enableTime:!0,noCalendar:!0,dateFormat:"H:i",time_24hr:!0}))}function j(){a("#createShiftRequestForm")?.addEventListener("submit",F)}function B(){a("#cancelCreateShiftRequest")?.addEventListener("click",t=>{t.preventDefault(),k("/private/shift-requests/table-view")})}function I(){a("#closeCreateShiftRequest")?.addEventListener("click",t=>{t.preventDefault(),k("/private/shift-requests/table-view")})}const y=new Map;async function $(t,e,n){if(t.innerHTML='<option value="">Seleccione cuenta</option>',!e||e.length===0){t.innerHTML='<option value="">No hay cuentas asociadas</option>',t.disabled=!0;return}e.forEach(i=>{const o=document.createElement("option");o.value=i.id,o.textContent=i.name,t.appendChild(o)}),n&&Array.from(t.options).some(o=>o.value===String(n))&&(t.value=String(n)),t.disabled=!1}async function x(t){const e=a("#shiftRequestAccount");if(!e)return;const n=e.value||"";if(e.disabled=!0,e.innerHTML='<option value="">Cargando cuentas...</option>',!t){e.innerHTML='<option value="">Seleccione cuenta</option>',e.disabled=!0;return}if(y.has(t)){$(e,y.get(t),n);return}try{const i=`/api/shift-requests/sites/${t}/accounts`,o=await A(i,{method:"GET"});if(o.status===401){e.innerHTML='<option value="">No autenticado</option>';return}if(o.status===403){e.innerHTML='<option value="">Sin acceso a las cuentas</option>';return}if(!o.ok){e.innerHTML='<option value="">Error cargando cuentas</option>';return}const r=await o.json();y.set(t,r),$(e,r,n)}catch(i){console.error("Error cargando accounts:",i),e.innerHTML='<option value="">Error cargando cuentas</option>',e.disabled=!0}}function N(){const t=a("#shiftRequestSite");t&&(t.addEventListener("change",e=>{const n=e.target.value||null;x(n)}),t.value&&x(t.value))}(function(){j(),B(),I(),M(),H(),N(),O()})();
