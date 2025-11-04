import{f as C}from"../../auth.js";import{n as v}from"../../navigation-handler.js";const n=t=>document.querySelector(t),$=t=>Array.from(document.querySelectorAll(t)),h={Lunes:0,Martes:1,Miércoles:2,Jueves:3,Viernes:4,Sábado:5,Domingo:6},D=Object.keys(h);function x(t){const o=t.map(a=>{let r=h[a.dayFrom],i=h[a.dayTo];return r>i&&(i+=7),{from:r,to:i,orig:a}});for(let a=0;a<o.length;a++)for(let r=a+1;r<o.length;r++){const i=o[a],l=o[r];for(let s=i.from;s<=i.to;s++)for(let c=l.from;c<=l.to;c++)if(s%7===c%7)return[i.orig,l.orig]}return null}async function E(t){t.preventDefault();const o=n("#shiftRequestCode")?.value?.trim(),a=n("#shiftRequestSite")?.value,r=n("#shiftRequestType")?.value,i=n("#shiftRequestStartDate")?.value,l=n("#shiftRequestEndDate")?.value||null,s=n("#shiftRequestStatus")?.value,c=n("#shiftRequestDescription")?.value?.trim()||null,e=n("#createShiftRequestError"),m=n("#createShiftRequestOk");if(e&&(e.textContent=""),m&&(m.style.display="none"),!o){e&&(e.textContent="El código es obligatorio.");return}if(!a){e&&(e.textContent="Debe seleccionar un sitio.");return}if(!r){e&&(e.textContent="Debe seleccionar un tipo.");return}if(!i){e&&(e.textContent="La fecha de inicio es obligatoria.");return}if(!s){e&&(e.textContent="Debe seleccionar un estado.");return}const y=[];if($(".day-range-block").forEach((u,f)=>{const b=u.querySelector(".dayFrom")?.value,g=u.querySelector(".dayTo")?.value,q=u.querySelector('input[name^="schedules"][name$="[startTime]"]')?.value,S=u.querySelector('input[name^="schedules"][name$="[endTime]"]')?.value,T=u.querySelector('input[name^="schedules"][name$="[lunchTime]"]')?.value||null;b&&g&&q&&S&&y.push({dayFrom:b,dayTo:g,startTime:q,endTime:S,lunchTime:T})}),y.length===0){e&&(e.textContent="Debe ingresar al menos un tramo de horario.");return}const d=x(y);if(d){e&&(e.textContent=`Solapamiento de días entre "${d[0].dayFrom} a ${d[0].dayTo}" y "${d[1].dayFrom} a ${d[1].dayTo}". Ajuste los tramos para que no se crucen.`);return}const p=t.submitter||n('#createShiftRequestForm button[type="submit"]');p&&(p.disabled=!0);try{const u=await C("/api/shift-requests/create",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({code:o,siteId:a,type:r,startDate:i,endDate:l,status:s,description:c,schedules:y})});if(!u.ok){let f="";try{f=await u.text()}catch{}throw f||(f=`Error ${u.status}`),new Error(f)}m&&(m.style.display="block"),setTimeout(()=>{v("/private/shift-requests/table-view")},600)}catch(u){e&&(e.textContent=u.message)}finally{p&&(p.disabled=!1)}}function R(t){const o=n("#shiftDayRanges"),a=$(".day-range-block").length,r=D.map(s=>`<option value="${s}">${s}</option>`).join(""),i=D.map(s=>`<option value="${s}">${s}</option>`).join(""),l=document.createElement("div");l.className="form-row day-range-block",l.innerHTML=`
    <div class="form-group">
      <label>Día desde</label>
      <select name="schedules[${a}][dayFrom]" class="dayFrom" required>
        <option value="">Desde</option>${r}
      </select>
    </div>
    <div class="form-group">
      <label>Día hasta</label>
      <select name="schedules[${a}][dayTo]" class="dayTo" required>
        <option value="">Hasta</option>${i}
      </select>
    </div>
    <div class="form-group">
      <label>Hora inicio</label>
      <input type="time" name="schedules[${a}][startTime]" value="" required />
    </div>
    <div class="form-group">
      <label>Hora término</label>
      <input type="time" name="schedules[${a}][endTime]" value="" required />
    </div>
    <div class="form-group">
      <label>Colación</label>
      <input type="time" name="schedules[${a}][lunchTime]" value="" />
    </div>
    <div class="form-group">
      <button type="button" class="btn-mini btn-danger remove-day-range">Quitar</button>
    </div>
  `,l.querySelector(".remove-day-range").onclick=()=>l.remove(),o.appendChild(l)}function k(){const t=n("#shiftDayRanges"),o=n("#addDayRange");t&&o&&(o.addEventListener("click",()=>R()),t.querySelector(".day-range-block")||R())}function F(){n("#createShiftRequestForm")?.addEventListener("submit",E)}function w(){n("#cancelCreateShiftRequest")?.addEventListener("click",t=>{t.preventDefault(),v("/private/shift-requests/table-view")})}function A(){n("#closeCreateShiftRequest")?.addEventListener("click",t=>{t.preventDefault(),v("/private/shift-requests/table-view")})}(function(){F(),w(),A(),k()})();
