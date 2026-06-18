import{f as c}from"../../auth.js";const n=s=>document.querySelector(s);let o=[];const d=async()=>{try{const s=await c("/api/employees/hr-dashboard-metrics",{credentials:"same-origin"});if(!s.ok)throw new Error(`Error cargando dashboard metrics: ${s.status}`);o=await s.json(),p(o),m(o)}catch(s){console.error("No se pudo cargar la lista de metrics:",s),o=[]}};function m(s){const t=s.employeeStatusSummary[0];t&&(n("#kpi-hired").innerText=t.hiredCount,n("#kpi-active").innerText=t.activeCount,n("#kpi-notice").innerText=t.noticeGivenCount,n("#kpi-inactive").innerText=t.inactiveCount,n("#kpi-settled").innerText=t.settledCount)}const p=s=>{const t=n("#companyStatsContainer");t&&s.companyEmployeesStatusSummary&&(s.companyEmployeesStatusSummary.length===0?t.innerHTML='<p class="text-muted text-center py-2">No hay datos de empresas</p>':t.innerHTML=s.companyEmployeesStatusSummary.map((a,e)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${a.companyName}</strong></span>
                        <span class="stat-badge">${a.activeCount} Activos</span>
                    </div>
                    <div class="stat-details">
                        <small>⏱️ ${a.hiredCount} Por Ingresar</small> | <small>⚠️ ${a.noticegivenCount} En Aviso</small>
                    </div>
                </div>
                ${e<s.companyEmployeesStatusSummary.length-1?"<hr>":""}
            `).join(""));const i=n("#clientStatsContainer");i&&s.companyEmployeesStatusSummary&&(s.companyEmployeesStatusSummary.length===0?i.innerHTML='<p class="text-muted text-center py-2">No hay datos de clientes</p>':i.innerHTML=s.companyEmployeesStatusSummary.map((a,e)=>{const l=a.activeCount+a.hiredCount+a.noticegivenCount;return`
                    <div class="stat-item">
                        <div class="stat-main-info">
                            <span class="stat-name"><strong>${a.clientName}</strong></span>
                            <span class="stat-badge">${l} Asignados</span>
                        </div>
                        <div class="stat-details">
                            <small>🟢 ${a.activeCount} Activos</small> | <small>⏱️ ${a.hiredCount} Próximos</small>
                        </div>
                    </div>
                    ${e<s.companyEmployeesStatusSummary.length-1?"<hr>":""}
                `}).join(""));const r=n("#userStatsContainer");r&&s.companyEmployeesStats&&(s.companyEmployeesStats.length===0?r.innerHTML='<p class="text-muted text-center py-2">No hay datos de usuarios</p>':r.innerHTML=s.companyEmployeesStats.map((a,e)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${a.companyName}</strong></span>
                        <span class="stat-badge user-active" style="background-color: var(--bs-success-soft); color: var(--bs-success);">${a.stats.activeUsersCount} En Línea</span>
                    </div>
                    <div class="stat-details">
                        <small>✉️ ${a.stats.invitedUsersCount} Invitados</small> | <small>🔴 ${a.stats.inactiveUsersCount} Inactivos</small> | <small>⏳ ${a.stats.expiredUsersCount} Expirados</small>
                    </div>
                </div>
                ${e<s.companyEmployeesStats.length-1?"<hr>":""}
            `).join(""))};(async function(){console.log("🚀 Inicializando Dashboard de Recursos Humanos..."),await d()})();
