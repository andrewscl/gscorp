import{f as $}from"../../auth.js";import{n as g}from"../../navigation-handler.js";const s=(n,e=document)=>e.querySelector(n),y=(n,e=document)=>Array.from((e||document).querySelectorAll(n)),b={Lunes:0,Martes:1,Miércoles:2,Jueves:3,Viernes:4,Sábado:5,Domingo:6},C=Object.keys(b);function R(n=document.querySelector("#shiftDayRanges")){y(".day-range-block",n).forEach((t,r)=>{y("input, select, textarea",t).forEach(i=>{const a=i.getAttribute("name");if(!a)return;const c=a.replace(/schedules\[\d+\]/,`schedules[${r}]`);i.setAttribute("name",c)})})}function x(n){const e=n.map(t=>{let r=b[t.dayFrom],i=b[t.dayTo];return r>i&&(i+=7),{from:r,to:i,orig:t}});for(let t=0;t<e.length;t++)for(let r=t+1;r<e.length;r++){const i=e[t],a=e[r];for(let c=i.from;c<=i.to;c++)for(let u=a.from;u<=a.to;u++)if(c%7===u%7)return[i.orig,a.orig]}return null}async function A(n){n.preventDefault();const e=s("#shiftRequestSite")?.value,t=s("#shiftRequestAccount")?.value,r=s("#shiftRequestServiceType")?.value,i=s("#shiftRequestStartDate")?.value,a=s("#shiftRequestEndDate")?.value||null,c=s("#shiftRequestDescription")?.value?.trim()||null,u=e?parseInt(e,10):null,k=t?parseInt(t,10):null,o=s("#createShiftRequestError"),p=s("#createShiftRequestOk");if(o&&(o.textContent=""),p&&(p.style.display="none"),!u){o&&(o.textContent="Debe seleccionar un sitio.");return}if(!k){o&&(o.textContent="Debe seleccionar una cuenta.");return}if(!r){o&&(o.textContent="Debe seleccionar el tipo de servicio.");return}if(!i){o&&(o.textContent="La fecha de inicio es obligatoria.");return}const m=[];if(y(".day-range-block").forEach((l,f)=>{const q=l.querySelector(".dayFrom")?.value,w=l.querySelector(".dayTo")?.value,S=l.querySelector('input[name$="[startTime]"]')?.value,D=l.querySelector('input[name$="[endTime]"]')?.value,T=l.querySelector('input[name$="[lunchTime]"]')?.value||null;q&&w&&S&&D&&m.push({dayFrom:q,dayTo:w,startTime:S,endTime:D,lunchTime:T})}),m.length===0){o&&(o.textContent="Debe ingresar al menos un tramo de horario.");return}const d=x(m);if(d){o&&(o.textContent=`Solapamiento de días entre "${d[0].dayFrom} a ${d[0].dayTo}" y "${d[1].dayFrom} a ${d[1].dayTo}". Ajuste los tramos para que no se crucen.`);return}const h=n.submitter||s('#createShiftRequestForm button[type="submit"]');h&&(h.disabled=!0);try{const l=await $("/api/shift-requests/create",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({siteId:u,type:r,accountId:k,startDate:i,endDate:a,description:c,schedules:m})});if(!l.ok){let f="";try{f=await l.text()}catch{}throw f||(f=`Error ${l.status}`),new Error(f)}p&&(p.style.display="block"),setTimeout(()=>{g("/private/shift-requests/table-view")},600)}catch(l){o&&(o.textContent=l.message)}finally{h&&(h.disabled=!1)}}function v(n){const e=s("#shiftDayRanges"),t=y(".day-range-block").length,r=C.map(c=>`<option value="${c}">${c}</option>`).join(""),i=C.map(c=>`<option value="${c}">${c}</option>`).join(""),a=document.createElement("div");a.className="day-range-block",a.innerHTML=`
    <div class="form-group">
      <label>Día desde</label>
      <select name="schedules[${t}][dayFrom]" class="dayFrom" required>
        <option value="">Desde</option>${r}
      </select>
    </div>

    <div class="form-group">
      <label>Día hasta</label>
      <select name="schedules[${t}][dayTo]" class="dayTo" required>
        <option value="">Hasta</option>${i}
      </select>
    </div>

    <div class="form-group input-with-icon">
      <label>Hora inicio</label>
      <div class="input-icon-wrap">
        <input type="time" name="schedules[${t}][startTime]" value="" required />
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
        <input type="time" name="schedules[${t}][endTime]" value="" required />
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
        <input type="time" name="schedules[${t}][lunchTime]" value="" />
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
  `,a.querySelector(".remove-day-range").addEventListener("click",()=>{a.remove(),R(e),e.querySelector(".day-range-block")||v()}),e.appendChild(a),R(e),typeof flatpickr<"u"&&flatpickr(a.querySelectorAll("input[type='time']"),{enableTime:!0,noCalendar:!0,dateFormat:"H:i",time_24hr:!0})}function E(){const n=s("#shiftDayRanges"),e=s("#addDayRange");n&&e&&(e.addEventListener("click",()=>v()),n.querySelector(".day-range-block")||v())}function F(){document.addEventListener("click",n=>{const e=n.target.closest(".calendar-btn, .clock-btn, .icon-btn");if(!e)return;const t=e.closest(".input-icon-wrap");if(!t)return;const r=t.querySelector("input");if(r&&(r.focus(),typeof r.showPicker=="function"))try{r.showPicker()}catch{}})}function I(){typeof flatpickr>"u"||(flatpickr("input[type='date']",{locale:"es",altInput:!0,altFormat:"d-m-Y",dateFormat:"Y-m-d",allowInput:!0,clickOpens:!0}),flatpickr("input[type='time']",{enableTime:!0,noCalendar:!0,dateFormat:"H:i",time_24hr:!0}))}function L(){s("#createShiftRequestForm")?.addEventListener("submit",A)}function O(){s("#cancelCreateShiftRequest")?.addEventListener("click",n=>{n.preventDefault(),g("/private/shift-requests/table-view")})}function B(){s("#closeCreateShiftRequest")?.addEventListener("click",n=>{n.preventDefault(),g("/private/shift-requests/table-view")})}(function(){L(),O(),B(),E(),F(),I()})();
