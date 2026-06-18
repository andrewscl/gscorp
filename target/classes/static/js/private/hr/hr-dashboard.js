import{f as c}from"../../auth.js";let l=[];const r=async()=>{try{const s=await c("/api/employees/hr-dashboard-metrics",{credentials:"same-origin"});if(!s.ok)throw new Error(`Error cargando dashboard metrics: ${s.status}`);l=await s.json(),d(l)}catch(s){console.error("No se pudo cargar la lista de metrics:",s),l=[]}},d=s=>{const n=document.getElementById("companyStatsContainer");n&&s.companyEmployeesStats&&(s.companyEmployeesStats.length===0?n.innerHTML='<p class="text-muted text-center py-2">No hay datos de empresas</p>':n.innerHTML=s.companyEmployeesStats.map((a,t)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${a.companyName}</strong></span>
                        <span class="stat-badge">${a.activeCount} Activos</span>
                    </div>
                    <div class="stat-details">
                        <small>⏱️ ${a.hiredCount} Por Ingresar</small> | <small>⚠️ ${a.noticegivenCount} En Aviso</small>
                    </div>
                </div>
                ${t<s.companyEmployeesStats.length-1?"<hr>":""}
            `).join(""));const e=document.getElementById("clientStatsContainer");e&&s.clientEmployeesStats&&(s.clientEmployeesStats.length===0?e.innerHTML='<p class="text-muted text-center py-2">No hay datos de clientes</p>':e.innerHTML=s.clientEmployeesStats.map((a,t)=>{const i=a.activeCount+a.hiredCount+a.noticegivenCount;return`
                    <div class="stat-item">
                        <div class="stat-main-info">
                            <span class="stat-name"><strong>${a.clientName}</strong></span>
                            <span class="stat-badge">${i} Asignados</span>
                        </div>
                        <div class="stat-details">
                            <small>🟢 ${a.activeCount} Activos</small> | <small>⏱️ ${a.hiredCount} Próximos</small>
                        </div>
                    </div>
                    ${t<s.clientEmployeesStats.length-1?"<hr>":""}
                `}).join(""));const o=document.getElementById("userStatsContainer");o&&s.companyEmployeesStats&&(s.companyEmployeesStats.length===0?o.innerHTML='<p class="text-muted text-center py-2">No hay datos de usuarios</p>':o.innerHTML=s.companyEmployeesStats.map((a,t)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${a.companyName}</strong></span>
                        <span class="stat-badge user-active" style="background-color: var(--bs-success-soft); color: var(--bs-success);">${a.stats.activeUsersCount} En Línea</span>
                    </div>
                    <div class="stat-details">
                        <small>✉️ ${a.stats.invitedUsersCount} Invitados</small> | <small>🔴 ${a.stats.inactiveUsersCount} Inactivos</small> | <small>⏳ ${a.stats.expiredUsersCount} Expirados</small>
                    </div>
                </div>
                ${t<s.companyEmployeesStats.length-1?"<hr>":""}
            `).join(""))};(async function(){console.log("🚀 Inicializando Dashboard de Recursos Humanos..."),await r()})();
