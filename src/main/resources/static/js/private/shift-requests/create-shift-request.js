import{f as x}from"../../auth.js";import{n as h}from"../../navigation-handler.js";const a=t=>document.querySelector(t),T=t=>Array.from(document.querySelectorAll(t)),v={Lunes:0,Martes:1,Miércoles:2,Jueves:3,Viernes:4,Sábado:5,Domingo:6},D=Object.keys(v);function E(t){const o=t.map(n=>{let r=v[n.dayFrom],i=v[n.dayTo];return r>i&&(i+=7),{from:r,to:i,orig:n}});for(let n=0;n<o.length;n++)for(let r=n+1;r<o.length;r++){const i=o[n],s=o[r];for(let l=i.from;l<=i.to;l++)for(let c=s.from;c<=s.to;c++)if(l%7===c%7)return[i.orig,s.orig]}return null}async function k(t){t.preventDefault();const o=a("#shiftRequestCode")?.value?.trim(),n=a("#shiftRequestSite")?.value,r=a("#shiftRequestAccount")?.value,i=a("#shiftRequestServiceType")?.value,s=a("#shiftRequestStartDate")?.value,l=a("#shiftRequestEndDate")?.value||null,c=a("#shiftRequestStatus")?.value,$=a("#shiftRequestDescription")?.value?.trim()||null,e=a("#createShiftRequestError"),m=a("#createShiftRequestOk");if(e&&(e.textContent=""),m&&(m.style.display="none"),!o){e&&(e.textContent="El código es obligatorio.");return}if(!n){e&&(e.textContent="Debe seleccionar un sitio.");return}if(!r){e&&(e.textContent="Debe seleccionar una cuenta.");return}if(!i){e&&(e.textContent="Debe seleccionar el tipo de servicio.");return}if(!s){e&&(e.textContent="La fecha de inicio es obligatoria.");return}if(!c){e&&(e.textContent="Debe seleccionar un estado.");return}const y=[];if(T(".day-range-block").forEach((u,f)=>{const b=u.querySelector(".dayFrom")?.value,g=u.querySelector(".dayTo")?.value,q=u.querySelector('input[name^="schedules"][name$="[startTime]"]')?.value,S=u.querySelector('input[name^="schedules"][name$="[endTime]"]')?.value,C=u.querySelector('input[name^="schedules"][name$="[lunchTime]"]')?.value||null;b&&g&&q&&S&&y.push({dayFrom:b,dayTo:g,startTime:q,endTime:S,lunchTime:C})}),y.length===0){e&&(e.textContent="Debe ingresar al menos un tramo de horario.");return}const d=E(y);if(d){e&&(e.textContent=`Solapamiento de días entre "${d[0].dayFrom} a ${d[0].dayTo}" y "${d[1].dayFrom} a ${d[1].dayTo}". Ajuste los tramos para que no se crucen.`);return}const p=t.submitter||a('#createShiftRequestForm button[type="submit"]');p&&(p.disabled=!0);try{const u=await x("/api/shift-requests/create",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({code:o,siteId:n,accountId:r,serviceType:i,startDate:s,endDate:l,status:c,description:$,schedules:y})});if(!u.ok){let f="";try{f=await u.text()}catch{}throw f||(f=`Error ${u.status}`),new Error(f)}m&&(m.style.display="block"),setTimeout(()=>{h("/private/shift-requests/table-view")},600)}catch(u){e&&(e.textContent=u.message)}finally{p&&(p.disabled=!1)}}function R(t){const o=a("#shiftDayRanges"),n=T(".day-range-block").length,r=D.map(l=>`<option value="${l}">${l}</option>`).join(""),i=D.map(l=>`<option value="${l}">${l}</option>`).join(""),s=document.createElement("div");s.className="form-row day-range-block",s.innerHTML=`
    <div class="form-group">
      <label>Día desde</label>
      <select name="schedules[${n}][dayFrom]" class="dayFrom" required>
        <option value="">Desde</option>${r}
      </select>
    </div>
    <div class="form-group">
      <label>Día hasta</label>
      <select name="schedules[${n}][dayTo]" class="dayTo" required>
        <option value="">Hasta</option>${i}
      </select>
    </div>
    <div class="form-group">
      <label>Hora inicio</label>
      <input type="time" name="schedules[${n}][startTime]" value="" required />
    </div>
    <div class="form-group">
      <label>Hora término</label>
      <input type="time" name="schedules[${n}][endTime]" value="" required />
    </div>
    <div class="form-group">
      <label>Colación</label>
      <input type="time" name="schedules[${n}][lunchTime]" value="" />
    </div>
    <div class="form-group">
      <button type="button" class="btn-mini btn-danger remove-day-range">Quitar</button>
    </div>
  `,s.querySelector(".remove-day-range").onclick=()=>s.remove(),o.appendChild(s)}function F(){const t=a("#shiftDayRanges"),o=a("#addDayRange");t&&o&&(o.addEventListener("click",()=>R()),t.querySelector(".day-range-block")||R())}function A(){a("#createShiftRequestForm")?.addEventListener("submit",k)}function w(){a("#cancelCreateShiftRequest")?.addEventListener("click",t=>{t.preventDefault(),h("/private/shift-requests/table-view")})}function L(){a("#closeCreateShiftRequest")?.addEventListener("click",t=>{t.preventDefault(),h("/private/shift-requests/table-view")})}(function(){A(),w(),L(),F()})();
