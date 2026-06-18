import{f as c}from"../../auth.js";const e=a=>document.querySelector(a);let r=[];const d=async()=>{try{const a=await c("/api/employees/hr-dashboard-metrics",{credentials:"same-origin"});if(!a.ok)throw new Error(`Error cargando dashboard metrics: ${a.status}`);r=await a.json(),p(r)}catch(a){console.error("No se pudo cargar la lista de metrics:",a),r=[]}};function m(a){const t=a.employeeStatusSummary[0];t&&(e("#kpi-hired").innerText=t.hiredCount,e("#kpi-active").innerText=t.activeCount,e("#kpi-notice").innerText=t.noticeGivenCount,e("#kpi-inactive").innerText=t.inactiveCount,e("#kpi-settled").innerText=t.settledCount)}const p=a=>{const t=document.getElementById("companyStatsContainer");t&&a.companyEmployeesStatusSummary&&(a.companyEmployeesStatusSummary.length===0?t.innerHTML='<p class="text-muted text-center py-2">No hay datos de empresas</p>':t.innerHTML=a.companyEmployeesStatusSummary.map((s,n)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${s.companyName}</strong></span>
                        <span class="stat-badge">${s.activeCount} Activos</span>
                    </div>
                    <div class="stat-details">
                        <small>⏱️ ${s.hiredCount} Por Ingresar</small> | <small>⚠️ ${s.noticegivenCount} En Aviso</small>
                    </div>
                </div>
                ${n<a.companyEmployeesStatusSummary.length-1?"<hr>":""}
            `).join(""));const o=document.getElementById("clientStatsContainer");o&&a.companyEmployeesStatusSummary&&(a.companyEmployeesStatusSummary.length===0?o.innerHTML='<p class="text-muted text-center py-2">No hay datos de clientes</p>':o.innerHTML=a.companyEmployeesStatusSummary.map((s,n)=>{const l=s.activeCount+s.hiredCount+s.noticegivenCount;return`
                    <div class="stat-item">
                        <div class="stat-main-info">
                            <span class="stat-name"><strong>${s.clientName}</strong></span>
                            <span class="stat-badge">${l} Asignados</span>
                        </div>
                        <div class="stat-details">
                            <small>🟢 ${s.activeCount} Activos</small> | <small>⏱️ ${s.hiredCount} Próximos</small>
                        </div>
                    </div>
                    ${n<a.companyEmployeesStatusSummary.length-1?"<hr>":""}
                `}).join(""));const i=document.getElementById("userStatsContainer");i&&a.companyEmployeesStats&&(a.companyEmployeesStats.length===0?i.innerHTML='<p class="text-muted text-center py-2">No hay datos de usuarios</p>':i.innerHTML=a.companyEmployeesStats.map((s,n)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${s.companyName}</strong></span>
                        <span class="stat-badge user-active" style="background-color: var(--bs-success-soft); color: var(--bs-success);">${s.stats.activeUsersCount} En Línea</span>
                    </div>
                    <div class="stat-details">
                        <small>✉️ ${s.stats.invitedUsersCount} Invitados</small> | <small>🔴 ${s.stats.inactiveUsersCount} Inactivos</small> | <small>⏳ ${s.stats.expiredUsersCount} Expirados</small>
                    </div>
                </div>
                ${n<a.companyEmployeesStats.length-1?"<hr>":""}
            `).join(""))};(async function(){console.log("🚀 Inicializando Dashboard de Recursos Humanos..."),await d(),await m()})();
