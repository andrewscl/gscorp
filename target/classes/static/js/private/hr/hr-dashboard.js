import{f as c}from"../../auth.js";let l=[];const r=async()=>{try{const s=await c("/api/employees/hr-dashboard-metrics",{credentials:"same-origin"});if(!s.ok)throw new Error(`Error cargando dashboard metrics: ${s.status}`);l=await s.json(),d(l)}catch(s){console.error("No se pudo cargar la lista de metrics:",s),l=[]}},d=s=>{const n=document.getElementById("companyStatsContainer");n&&s.companyEmployeesStats&&(s.companyEmployeesStats.length===0?n.innerHTML='<p class="text-muted text-center py-2">No hay datos de empresas</p>':n.innerHTML=s.companyEmployeesStats.map((t,a)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${t.companyName}</strong></span>
                        <span class="stat-badge">${t.stats.activeCount} Activos</span>
                    </div>
                    <div class="stat-details">
                        <small>⏱️ ${t.stats.hiredCount} Por Ingresar</small> | <small>⚠️ ${t.stats.noticeGivenCount} En Aviso</small>
                    </div>
                </div>
                ${a<s.companyEmployeesStats.length-1?"<hr>":""}
            `).join(""));const e=document.getElementById("clientStatsContainer");e&&s.clientEmployeesStats&&(s.clientEmployeesStats.length===0?e.innerHTML='<p class="text-muted text-center py-2">No hay datos de clientes</p>':e.innerHTML=s.clientEmployeesStats.map((t,a)=>{const i=t.stats.activeCount+t.stats.hiredCount;return`
                    <div class="stat-item">
                        <div class="stat-main-info">
                            <span class="stat-name"><strong>${t.clientName}</strong></span>
                            <span class="stat-badge">${i} Asignados</span>
                        </div>
                        <div class="stat-details">
                            <small>🟢 ${t.stats.activeCount} Activos</small> | <small>⏱️ ${t.stats.hiredCount} Próximos</small>
                        </div>
                    </div>
                    ${a<s.clientEmployeesStats.length-1?"<hr>":""}
                `}).join(""));const o=document.getElementById("userStatsContainer");o&&s.companyEmployeesStats&&(s.companyEmployeesStats.length===0?o.innerHTML='<p class="text-muted text-center py-2">No hay datos de usuarios</p>':o.innerHTML=s.companyEmployeesStats.map((t,a)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${t.companyName}</strong></span>
                        <span class="stat-badge user-active" style="background-color: var(--bs-success-soft); color: var(--bs-success);">${t.stats.activeUsersCount} En Línea</span>
                    </div>
                    <div class="stat-details">
                        <small>✉️ ${t.stats.invitedUsersCount} Invitados</small> | <small>🔴 ${t.stats.inactiveUsersCount} Inactivos</small> | <small>⏳ ${t.stats.expiredUsersCount} Expirados</small>
                    </div>
                </div>
                ${a<s.companyEmployeesStats.length-1?"<hr>":""}
            `).join(""))};(async function(){console.log("🚀 Inicializando Dashboard de Recursos Humanos..."),await r()})();
