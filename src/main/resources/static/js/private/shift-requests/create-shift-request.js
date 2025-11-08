import{f as C}from"../../auth.js";import{n as v}from"../../navigation-handler.js";const a=t=>document.querySelector(t),$=t=>Array.from(document.querySelectorAll(t)),h={Lunes:0,Martes:1,Miércoles:2,Jueves:3,Viernes:4,Sábado:5,Domingo:6},D=Object.keys(h);function x(t){const o=t.map(n=>{let s=h[n.dayFrom],i=h[n.dayTo];return s>i&&(i+=7),{from:s,to:i,orig:n}});for(let n=0;n<o.length;n++)for(let s=n+1;s<o.length;s++){const i=o[n],r=o[s];for(let l=i.from;l<=i.to;l++)for(let c=r.from;c<=r.to;c++)if(l%7===c%7)return[i.orig,r.orig]}return null}async function k(t){t.preventDefault();const o=a("#shiftRequestSite")?.value,n=a("#shiftRequestAccount")?.value,s=a("#shiftRequestServiceType")?.value,i=a("#shiftRequestStartDate")?.value,r=a("#shiftRequestEndDate")?.value||null,l=a("#shiftRequestStatus")?.value,c=a("#shiftRequestDescription")?.value?.trim()||null,e=a("#createShiftRequestError"),m=a("#createShiftRequestOk");if(e&&(e.textContent=""),m&&(m.style.display="none"),!o){e&&(e.textContent="Debe seleccionar un sitio.");return}if(!n){e&&(e.textContent="Debe seleccionar una cuenta.");return}if(!s){e&&(e.textContent="Debe seleccionar el tipo de servicio.");return}if(!i){e&&(e.textContent="La fecha de inicio es obligatoria.");return}if(!l){e&&(e.textContent="Debe seleccionar un estado.");return}const y=[];if($(".day-range-block").forEach((u,f)=>{const b=u.querySelector(".dayFrom")?.value,q=u.querySelector(".dayTo")?.value,g=u.querySelector('input[name^="schedules"][name$="[startTime]"]')?.value,S=u.querySelector('input[name^="schedules"][name$="[endTime]"]')?.value,T=u.querySelector('input[name^="schedules"][name$="[lunchTime]"]')?.value||null;b&&q&&g&&S&&y.push({dayFrom:b,dayTo:q,startTime:g,endTime:S,lunchTime:T})}),y.length===0){e&&(e.textContent="Debe ingresar al menos un tramo de horario.");return}const d=x(y);if(d){e&&(e.textContent=`Solapamiento de días entre "${d[0].dayFrom} a ${d[0].dayTo}" y "${d[1].dayFrom} a ${d[1].dayTo}". Ajuste los tramos para que no se crucen.`);return}const p=t.submitter||a('#createShiftRequestForm button[type="submit"]');p&&(p.disabled=!0);try{const u=await C("/api/shift-requests/create",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({siteId:o,type:s,accountId:n,startDate:i,endDate:r,description:c,schedules:y})});if(!u.ok){let f="";try{f=await u.text()}catch{}throw f||(f=`Error ${u.status}`),new Error(f)}m&&(m.style.display="block"),setTimeout(()=>{v("/private/shift-requests/table-view")},600)}catch(u){e&&(e.textContent=u.message)}finally{p&&(p.disabled=!1)}}function R(t){const o=a("#shiftDayRanges"),n=$(".day-range-block").length,s=D.map(l=>`<option value="${l}">${l}</option>`).join(""),i=D.map(l=>`<option value="${l}">${l}</option>`).join(""),r=document.createElement("div");r.className="form-row day-range-block",r.innerHTML=`
    <div class="form-group">
      <label>Día desde</label>
      <select name="schedules[${n}][dayFrom]" class="dayFrom" required>
        <option value="">Desde</option>${s}
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
  `,r.querySelector(".remove-day-range").onclick=()=>r.remove(),o.appendChild(r)}function E(){const t=a("#shiftDayRanges"),o=a("#addDayRange");t&&o&&(o.addEventListener("click",()=>R()),t.querySelector(".day-range-block")||R())}function F(){a("#createShiftRequestForm")?.addEventListener("submit",k)}function A(){a("#cancelCreateShiftRequest")?.addEventListener("click",t=>{t.preventDefault(),v("/private/shift-requests/table-view")})}function w(){a("#closeCreateShiftRequest")?.addEventListener("click",t=>{t.preventDefault(),v("/private/shift-requests/table-view")})}(function(){F(),A(),w(),E()})();
