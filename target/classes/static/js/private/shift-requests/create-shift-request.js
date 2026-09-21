import{f as b}from"../../auth.js";import{n as j}from"../../navigation-handler.js";import{d}from"../../shared/display-alert.js";import"../../shared/dom-utils.js";const r=(e,t=document)=>t.querySelector(e),g=(e,t=document)=>Array.from((t||document).querySelectorAll(e)),A=r(".alert-warning"),m=r(".alert-error"),I=r(".alert-success"),S={Lunes:0,Martes:1,Miércoles:2,Jueves:3,Viernes:4,Sábado:5,Domingo:6},L=Object.keys(S);function D(e=document.querySelector("#shiftDayRanges")){g(".day-range-block",e).forEach((o,s)=>{g("input, select, textarea",o).forEach(n=>{const a=n.getAttribute("name");if(!a)return;const i=a.replace(/schedules\[\d+\]/,`schedules[${s}]`);n.setAttribute("name",i)})})}function R(e){const t=e.map(o=>{let s=S[o.dayFrom],n=S[o.dayTo];return s>n&&(n+=7),{from:s,to:n,orig:o}});for(let o=0;o<t.length;o++)for(let s=o+1;s<t.length;s++){const n=t[o],a=t[s];for(let i=n.from;i<=n.to;i++)for(let c=a.from;c<=a.to;c++)if(i%7===c%7)return[n.orig,a.orig]}return null}async function x(){const e=r("#submit"),t=r("#cancel"),o=r("#siteExternalId")?.value||"",s=r("#shiftRequestAccount")?.value,n=r("#shiftRequestServiceType")?.value,a=r("#shiftRequestStartDate")?.value,i=r("#shiftRequestEndDate")?.value||null,c=r("#shiftRequestDescription")?.value?.trim()||null,f=s?parseInt(s,10):null;if(!o){d(m,"Debe seleccionar un sitio.");return}if(!n){d(m,"Debe seleccionar el tipo de servicio.");return}if(!a){d(m,"La fecha de inicio es obligatoria.");return}const y=[];if(g(".day-range-block").forEach((l,v)=>{const h=l.querySelector(".dayFrom")?.value,E=l.querySelector(".dayTo")?.value,T=l.querySelector('input[name$="[startTime]"]')?.value,$=l.querySelector('input[name$="[endTime]"]')?.value;h&&E&&T&&$&&y.push({dayFrom:h,dayTo:E,startTime:T,endTime:$})}),y.length===0){d(m,"Debe ingresar al menos un tramo de horario.");return}const p=R(y);if(p){d(m,`Solapamiento de días entre "${p[0].dayFrom}
                              a ${p[0].dayTo}" y "${p[1].dayFrom}
                              a ${p[1].dayTo}".
                              Ajuste los tramos para que no se crucen.`);return}e&&(e.disabled=!0),t&&(t.disabled=!0);try{const l=await b("/api/shift-requests/create",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({siteId,type:n,clientAccountId:f,startDate:a,endDate:i,description:c,schedules:y})});if(!l||!l.ok){let v="Ocurrió un problema al enviar el formulario.";if(l){const h=l.headers.get("content-type");h&&h.includes("application/json")&&(v=(await l.json()).message||v)}d(m,`Error: ${v}`),e&&(e.disabled=!1),t&&(t.disabled=!1);return}d(I,"La asignación de turno ha sido creada correctamente.",2e3),setTimeout(()=>{j("/private/shift-assignments/list",!0)},2e3)}catch(l){console.error(`[onClickCreate] Ocurrio un problema: ${l.message}`,l),d(m,"Error inesperado. Intente más tarde.",2e3),e&&(e.disabled=!1),t&&(t.disabled=!1)}}function q(e){const t=r("#shiftDayRanges"),o=g(".day-range-block").length,s=L.map(i=>`<option value="${i}">${i}</option>`).join(""),n=L.map(i=>`<option value="${i}">${i}</option>`).join(""),a=document.createElement("div");a.className="day-range-block",a.innerHTML=`
    <div class="form-group">
      <label>Día desde</label>
      <select name="schedules[${o}][dayFrom]" class="dayFrom" required>
        <option value="">Desde</option>${s}
      </select>
    </div>

    <div class="form-group">
      <label>Día hasta</label>
      <select name="schedules[${o}][dayTo]" class="dayTo" required>
        <option value="">Hasta</option>${n}
      </select>
    </div>

    <div class="form-group">
      <label>Hora inicio</label>
      <div class="time-field">
        <input type="time" name="schedules[${o}][startTime]" value="" required />
        <span class="time-icon"></span>
      </div>
    </div>

    <div class="form-group">
      <label>Hora fin</label>
      <div class="time-field">
        <input type="time" name="schedules[${o}][endTime]" value="" required />
        <span class="time-icon"></span>
      </div>
    </div>
    <div class="form-group">
      <button type="button" class="remove-schedule-btn">Quitar</button>
    </div>
  `,a.querySelector(".remove-schedule-btn").addEventListener("click",()=>{a.remove(),D(t),t.querySelector(".day-range-block")||q()}),t.appendChild(a),D(t)}const k=()=>{d(A,"La solicitud de turno ha sido cancelada",1500),setTimeout(()=>j("/private/shift-requests/table-view"),1500)};function w(){const e=r("#submit");e&&e.addEventListener("click",x);const t=r("#cancel");t&&t.addEventListener("click",k);const o=r("#shiftDayRanges"),s=r("#addDayRange");o&&s&&(s.addEventListener("click",()=>q()),o.querySelector(".day-range-block")||q());const n=r("#projectExternalId");n&&n.addEventListener("change",C);const a=r("#siteExternalId");a&&a.addEventListener("change",P)}async function C(){const e=r("#shiftRequestAccount"),t=r("#projectExternalId")?.value,o=r("#siteExternalId");if(t)try{const s=`/api/sites/projects/${t}/sites`,n=await b(s,{method:"GET",headers:{Accept:"application/json"}});if(!n)throw new Error("No se pudieron obtener los sitios del proyecto seleccionado.");const a=await n.json();a.length===0?u({selectEl:o,items:[],emptyLabel:"Primero seleccione un proyecto."}):(u({selectEl:o,items:a,defaultLabel:"Seleccione un proyecto",emptyLabel:"Sin sitios asociados",valueKey:"externalId",preserveValue:prevAccount}),u({selectEl:e,items:[],emptyLabel:"Primero seleccione un sitio."}),o.disabled=!1)}catch(s){console.error("Error en HandleProjectChange:",s),u({selectEl:o,items:[],emptyLabel:"Error al cargar los sitios."}),u({selectEl:e,items:[],emptyLabel:"Primero seleccione un sitio."})}}function u({selectEl:e,items:t,defaultLabel:o,emptyLabel:s,valueKey:n="externalId",preserveValue:a=""}){if(e){if(!t||t.length===0){e.innerHTML=`<option value="">${s}</option>`,e.disabled=!0;return}e.innerHTML=`<option value="">${o}</option>`,t.forEach(i=>{const c=document.createElement("option");c.value=i[n]||i.id||"",c.textContent=i.name||"",e.appendChild(c)}),a&&Array.from(e.options).some(c=>c.value===String(a))&&(e.value=String(a)),e.disabled=!1}}async function P(){const e=r("#shiftRequestAccount"),t=r("#siteZoneExternalId"),o=r("#siteExternalId")?.value;if(!e||!t)return;if(!o){u({selectEl:e,items:[],emptyLabel:"Primero seleccione un sitio."}),u({selectEl:t,items:[],emptyLabel:"Primero seleccione un sitio."});return}const s=e.value||"",n=t.value||"";try{const a=`/api/shift-requests/sites/${o}/accounts`,i=`/api/v1/site-zones/site/${o}/site-zones`,[c,f]=await Promise.all([b(a,{method:"GET",headers:{Accept:"application/json"}}),b(i,{method:"GET",headers:{Accept:"application/json"}})]);if(!c)throw new Error("No se pudieron obtener las cuentas del proyecto seleccionado.");if(!f)throw new Error("No se pudieron obtener las zonas del sitio seleccionado.");const y=c&&c.ok?await c.json():[],p=f&&f.ok?await f.json():[];u({selectEl:e,items:y,defaultLabel:"Seleccione una cuenta (opcional)",emptyLabel:"Sin cuentas asociadas",valueKey:"externalId",preserveValue:s}),u({selectEl:t,items:p,defaultLabel:"Seleccione una zona",emptyLabel:"El sitio no tiene zonas creadas (Requerido)",valueKey:"externalId",preserveValue:n})}catch(a){console.error("Error en HandleSiteChange:",a),u({selectEl:e,items:[],emptyLabel:"Error al cargar las cuentas."}),u({selectEl:t,items:[],emptyLabel:"Error al cargar las zonas."})}}(function(){w()})();
