import { navigateTo } from '../../navigation-handler.js';

(function () {
  const form = document.getElementById('att-filter');
  if (!form) return;

  // Atajos rápidos
  const quick = form.querySelector('.quick-range');
  const from = form.querySelector('input[name="from"]');
  const to   = form.querySelector('input[name="to"]');

  function fmt(d) { return d.toISOString().slice(0,10); } // yyyy-MM-dd

  quick?.addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-range]');
    if (!btn) return;
    const now = new Date();
    let dFrom = new Date(now), dTo = new Date(now);

    switch (btn.dataset.range) {
      case 'today':
        // dFrom = hoy 00:00, dTo = hoy
        break;
      case 'week': {
        const day = (now.getDay()+6)%7; // lunes=0
        dFrom.setDate(now.getDate()-day);
        break;
      }
      case 'month':
        dFrom = new Date(now.getFullYear(), now.getMonth(), 1);
        break;
    }
    from.value = fmt(dFrom);
    to.value   = fmt(dTo);
  });

  form.addEventListener('submit', (e) => {
    e.preventDefault();
    const u = new URL(form.dataset.path || '/private/attendance', location.origin);
    const fd = new FormData(form);
    const params = new URLSearchParams(fd);
    navigateTo(u.pathname + '?' + params.toString(), true);
  });
})();
