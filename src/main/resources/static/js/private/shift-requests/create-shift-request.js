import{f as x}from"../../auth.js";import{n as k}from"../../navigation-handler.js";const a=(t,e=document)=>e.querySelector(t),y=(t,e=document)=>Array.from((e||document).querySelectorAll(t)),b={Lunes:0,Martes:1,Miércoles:2,Jueves:3,Viernes:4,Sábado:5,Domingo:6},R=Object.keys(b);function D(t=document.querySelector("#shiftDayRanges")){y(".day-range-block",t).forEach((n,r)=>{y("input, select, textarea",n).forEach(o=>{const i=o.getAttribute("name");if(!i)return;const c=i.replace(/schedules\[\d+\]/,`schedules[${r}]`);o.setAttribute("name",c)})})}function A(t){const e=t.map(n=>{let r=b[n.dayFrom],o=b[n.dayTo];return r>o&&(o+=7),{from:r,to:o,orig:n}});for(let n=0;n<e.length;n++)for(let r=n+1;r<e.length;r++){const o=e[n],i=e[r];for(let c=o.from;c<=o.to;c++)for(let l=i.from;l<=i.to;l++)if(c%7===l%7)return[o.orig,i.orig]}return null}async function E(t){t.preventDefault();const e=a("#shiftRequestSite")?.value,n=a("#shiftRequestAccount")?.value,r=a("#shiftRequestServiceType")?.value,o=a("#shiftRequestStartDate")?.value,i=a("#shiftRequestEndDate")?.value||null,c=a("#shiftRequestDescription")?.value?.trim()||null,l=e?parseInt(e,10):null,q=n?parseInt(n,10):null,s=a("#createShiftRequestError"),p=a("#createShiftRequestOk");if(s&&(s.textContent=""),p&&(p.style.display="none"),!l){s&&(s.textContent="Debe seleccionar un sitio.");return}if(!q){s&&(s.textContent="Debe seleccionar una cuenta.");return}if(!r){s&&(s.textContent="Debe seleccionar el tipo de servicio.");return}if(!o){s&&(s.textContent="La fecha de inicio es obligatoria.");return}const m=[];if(y(".day-range-block").forEach((u,f)=>{const S=u.querySelector(".dayFrom")?.value,T=u.querySelector(".dayTo")?.value,w=u.querySelector('input[name$="[startTime]"]')?.value,C=u.querySelector('input[name$="[endTime]"]')?.value;S&&T&&w&&C&&m.push({dayFrom:S,dayTo:T,startTime:w,endTime:C})}),m.length===0){s&&(s.textContent="Debe ingresar al menos un tramo de horario.");return}const d=A(m);if(d){s&&(s.textContent=`Solapamiento de días entre "${d[0].dayFrom} a ${d[0].dayTo}" y "${d[1].dayFrom} a ${d[1].dayTo}". Ajuste los tramos para que no se crucen.`);return}const h=t.submitter||a('#createShiftRequestForm button[type="submit"]');h&&(h.disabled=!0);try{const u=await x("/api/shift-requests/create",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({siteId:l,type:r,accountId:q,startDate:o,endDate:i,description:c,schedules:m})});if(!u.ok){let f="";try{f=await u.text()}catch{}throw f||(f=`Error ${u.status}`),new Error(f)}p&&(p.style.display="block"),setTimeout(()=>{k("/private/shift-requests/table-view")},600)}catch(u){s&&(s.textContent=u.message)}finally{h&&(h.disabled=!1)}}function g(t){const e=a("#shiftDayRanges"),n=y(".day-range-block").length,r=R.map(c=>`<option value="${c}">${c}</option>`).join(""),o=R.map(c=>`<option value="${c}">${c}</option>`).join(""),i=document.createElement("div");i.className="day-range-block",i.innerHTML=`
    <div class="form-group">
      <label>Día desde</label>
      <select name="schedules[${n}][dayFrom]" class="dayFrom" required>
        <option value="">Desde</option>${r}
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

    <div class="form-group">
      <button type="button" class="btn-mini btn-danger remove-day-range">Quitar</button>
    </div>
  `,i.querySelector(".remove-day-range").addEventListener("click",()=>{i.remove(),D(e),e.querySelector(".day-range-block")||g()}),e.appendChild(i),D(e),typeof flatpickr<"u"&&flatpickr(i.querySelectorAll("input[type='time']"),{enableTime:!0,noCalendar:!0,dateFormat:"H:i",time_24hr:!0})}function F(){const t=a("#shiftDayRanges"),e=a("#addDayRange");t&&e&&(e.addEventListener("click",()=>g()),t.querySelector(".day-range-block")||g())}function H(){document.addEventListener("click",t=>{const e=t.target.closest(".calendar-btn, .clock-btn, .icon-btn");if(!e)return;const n=e.closest(".input-icon-wrap");if(!n)return;const r=n.querySelector("input");if(r&&(r.focus(),typeof r.showPicker=="function"))try{r.showPicker()}catch{}})}function M(){typeof flatpickr>"u"||(flatpickr("input[type='date']",{locale:"es",altInput:!0,altFormat:"d-m-Y",dateFormat:"Y-m-d",allowInput:!0,clickOpens:!0}),flatpickr("input[type='time']",{enableTime:!0,noCalendar:!0,dateFormat:"H:i",time_24hr:!0}))}function O(){a("#createShiftRequestForm")?.addEventListener("submit",E)}function j(){a("#cancelCreateShiftRequest")?.addEventListener("click",t=>{t.preventDefault(),k("/private/shift-requests/table-view")})}function I(){a("#closeCreateShiftRequest")?.addEventListener("click",t=>{t.preventDefault(),k("/private/shift-requests/table-view")})}const v=new Map;async function $(t,e,n){if(t.innerHTML='<option value="">Seleccione cuenta</option>',!e||e.length===0){t.innerHTML='<option value="">No hay cuentas asociadas</option>',t.disabled=!0;return}e.forEach(r=>{const o=document.createElement("option");o.value=r.id,o.textContent=r.name,t.appendChild(o)}),n&&Array.from(t.options).some(o=>o.value===String(n))&&(t.value=String(n)),t.disabled=!1}async function L(t){const e=a("#shiftRequestAccount");if(!e)return;const n=e.value||"";if(e.disabled=!0,e.innerHTML='<option value="">Cargando cuentas...</option>',!t){e.innerHTML='<option value="">Seleccione cuenta</option>',e.disabled=!0;return}if(v.has(t)){$(e,v.get(t),n);return}try{const r=`/api/shift-requests/sites/${t}/accounts`,o=await x(r,{method:"GET"});if(o.status===401){e.innerHTML='<option value="">No autenticado</option>';return}if(o.status===403){e.innerHTML='<option value="">Sin acceso a las cuentas</option>';return}if(!o.ok){e.innerHTML='<option value="">Error cargando cuentas</option>';return}const i=await o.json();v.set(t,i),$(e,i,n)}catch(r){console.error("Error cargando accounts:",r),e.innerHTML='<option value="">Error cargando cuentas</option>',e.disabled=!0}}function N(){const t=a("#shiftRequestSite");t&&(t.addEventListener("change",e=>{const n=e.target.value||null;L(n)}),t.value&&L(t.value))}(function(){O(),j(),I(),F(),H(),N(),M()})();
