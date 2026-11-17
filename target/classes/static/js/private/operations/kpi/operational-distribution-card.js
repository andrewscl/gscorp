import{f as h}from"../../../auth.js";import"../../../signup.js";import"../../../navigation-handler.js";const u=e=>document.querySelector(e);let r=[];const m=async()=>{try{const e=await h("/api/operations/ops-dashboard-metrics",{credentials:"same-origin"});if(!e.ok)throw new Error(`Error cargando dashboard metrics: ${e.status}`);r=await e.json(),f(r)}catch(e){console.error("No se pudo cargar la lista de attendance metrics:",e),r=[]}},f=e=>{const i=u("#operationStatsContainer");if(!i)return;const c=e?.projectSiteShiftsSummary||[],l=e?.projectSiteAttendancesSummary||[];if(c.length===0&&l.length===0){i.innerHTML='<p class="text-muted text-center py-2">No hay datos de asistencia</p>';return}const a={},d=(t,s,o,n)=>(a[t]||(a[t]={projectName:s||"Proyecto General",sites:{}}),a[t].sites[o]||(a[t].sites[o]={siteName:n||"Instalación",attendanceCount:0,pendingShiftsCount:0}),a[t].sites[o]);l.forEach(t=>{const s=d(t.projectId||0,t.projectName,t.siteId||0,t.siteName);s.attendanceCount=t.attendanceCount??0}),c.forEach(t=>{const s=d(t.projectId||0,t.projectName,t.siteId||0,t.siteName);s.pendingShiftsCount=t.totalShiftsToday??0}),i.innerHTML=Object.values(a).map((t,s,o)=>`
    <div class="project-group">
            <div class="project-header">
                📂 ${t.projectName}
            </div>
            
            <div class="project-sites-list">
                ${Object.values(t.sites).map(n=>{const p=(n.pendingShiftsCount??0)>0;return`
                        <div class="stat-item-row">
                            <span class="site-cell-name">🏢 ${n.siteName}</span>
                            
                            <span class="metric-cell-val text-primary fw-semibold">
                                ${n.attendanceCount}
                            </span>
                            
                            <span class="metric-cell-val text-center">
                                <span class="pill-value ${p?"pill-pending":"pill-ok"}">
                                    ${n.totalShiftsToday}
                                </span>
                            </span>
                        </div>
                    `}).join("")}
            </div>
        </div>
        ${s<o.length-1?"<hr>":""}
    `).join("")};(async function(){console.log("🚀 Inicializando Dashboard de Operaciones..."),await m()})();
