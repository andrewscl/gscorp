import{f as l}from"../../auth.js";let i=[];const c=async()=>{try{const a=await l("/api/employees/hr-dashboard-metrics",{credentials:"same-origin"});if(!a.ok)throw new Error(`Error cargando dashboard metrics: ${a.status}`);i=await a.json(),r(i)}catch(a){console.error("No se pudo cargar la lista de metrics:",a),i=[]}},r=a=>{const n=document.getElementById("companyStatsContainer");n&&a.companyStats&&(a.companyStats.length===0?n.innerHTML='<p class="text-muted text-center py-2">No hay datos de empresas</p>':n.innerHTML=a.companyStats.map((s,t)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${s.companyName}</strong></span>
                        <span class="stat-badge">${s.activeCount} Activos</span>
                    </div>
                    <div class="stat-details">
                        <small>⏱️ ${s.pendingCount} Por Ingresar</small> | <small>⚠️ ${s.noticeCount} En Aviso</small>
                    </div>
                </div>
                ${t<a.companyStats.length-1?"<hr>":""}
            `).join(""));const e=document.getElementById("clientStatsContainer");e&&a.clientStats&&(a.clientStats.length===0?e.innerHTML='<p class="text-muted text-center py-2">No hay datos de clientes</p>':e.innerHTML=a.clientStats.map((s,t)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${s.clientName}</strong></span>
                        <span class="stat-badge">${s.activeCount+s.pendingCount} Asignados</span>
                    </div>
                    <div class="stat-details">
                        <small>🟢 ${s.activeCount} Activos</small> | <small>⏱️ ${s.pendingCount} Próximos</small>
                    </div>
                </div>
                ${t<a.clientStats.length-1?"<hr>":""}
            `).join(""));const o=document.getElementById("companyUserStatsContainer");o&&a.companyUserStats&&(a.companyUserStats.length===0?o.innerHTML='<p class="text-muted text-center py-2">No hay datos de cuentas</p>':o.innerHTML=a.companyUserStats.map((s,t)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${s.companyName}</strong></span>
                        <span class="stat-badge">${s.totalEmployees} Totales</span>
                    </div>
                    <div class="stat-details mt-1">
                        <span class="text-success">🟢 ${s.activeUsers} Activos</span> | 
                        <span class="text-warning">🟡 ${s.blockedUsers} Bloqueados</span> | 
                        <span class="text-danger">🔴 ${s.withoutUser} Sin Cuenta</span>
                    </div>
                </div>
                ${t<a.companyUserStats.length-1?"<hr>":""}
            `).join(""))};(async function(){console.log("🚀 Inicializando Dashboard de Recursos Humanos..."),await c()})();
