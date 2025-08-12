// mega-menu.js
export function setupMegamenu () {
  // Evitar re-inicializar si existen cambios de vista SPA
  if (window.__megaMenuInit) return;
  window.__megaMenuInit = true;

  const navbar = document.getElementById("navbar");
  const btn    = document.getElementById("menu-toggle");
  const links  = document.getElementById("nav-links");
  if (!navbar) return;

  // Fondo transparente/solid al hacer scroll
  let lastScrolled = false;
  const onScroll = () => {
    const scrolled = window.scrollY > 60;
    if (scrolled !== lastScrolled) {
      lastScrolled = scrolled;
      navbar.classList.toggle("solid", scrolled);
      navbar.classList.toggle("transparent", !scrolled);
    }
  };
  window.addEventListener("scroll", onScroll);
  onScroll();

  if (btn && links) {
    // Accesibilidad
    btn.setAttribute("aria-controls", "nav-links");
    btn.setAttribute("aria-expanded", "false");
    btn.setAttribute("aria-label", "Abrir menú");

    const open = () => {
      links.classList.add("open");
      btn.classList.add("is-open");                     // (1) animación hamburguesa
      btn.setAttribute("aria-expanded", "true");
      btn.setAttribute("aria-label", "Cerrar menú");    // (1) label accesible
      document.documentElement.classList.add("no-scroll");
    };

    const close = () => {
      links.classList.remove("open");
      btn.classList.remove("is-open");                  // (1) animación hamburguesa
      btn.setAttribute("aria-expanded", "false");
      btn.setAttribute("aria-label", "Abrir menú");     // (1) label accesible
      document.documentElement.classList.remove("no-scroll");
    };

    const toggle = () => (links.classList.contains("open") ? close() : open());
    btn.addEventListener("click", toggle);

    // Cerrar al navegar dentro del menú
    links.addEventListener("click", (e) => {
      const a = e.target.closest("[data-path], a[href]");
      if (!a) return;
      if (a.hasAttribute("data-path")) e.preventDefault();  // (2) evita salto a '#'
      close();
    });

    // (4) Cerrar al hacer click fuera del navbar
    document.addEventListener("click", (e) => {
      if (!links.classList.contains("open")) return;
      if (e.target.closest("#navbar")) return;
      close();
    });

    // Cerrar con Escape
    document.addEventListener("keydown", (e) => { if (e.key === "Escape") close(); });

    // Si vuelve a desktop cierra
    window.addEventListener("resize", () => { if (window.innerWidth > 768) close(); });

    // (4) Cerrar en back/forward
    window.addEventListener("popstate", close);
  }

  // Exponer por si lo necesitas
  window.setupMegamenu = setupMegamenu;
}

// (3) Auto-init si el módulo se carga directo en el layout
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", () => setupMegamenu());
} else {
  setupMegamenu();
}
