// mega-menu.js
export function setupMegamenu () {
  if (window.__megaMenuInit) return;
  window.__megaMenuInit = true;

  const navbar = document.getElementById("navbar");
  const btn    = document.getElementById("menu-toggle");
  const links  = document.getElementById("nav-links");
  if (!navbar || !links) return;

  const isMobile = () => window.matchMedia("(max-width: 768px)").matches;

  // --- 0) Altura real del navbar -> CSS vars (px exactos)
  function updateNavHeightVar() {
    const h = navbar.offsetHeight || 0;
    // Mantén ambas por compatibilidad con tu SCSS
    document.documentElement.style.setProperty("--nav-h-px", h + "px");
    document.documentElement.style.setProperty("--nav-h",    h + "px"); // <-- asegura top consistente
  }
  updateNavHeightVar();
  window.addEventListener("resize", updateNavHeightVar);
  window.addEventListener("orientationchange", updateNavHeightVar);

  // --- 1) Estado transparente/solid en scroll
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

  // --- 2) Hamburguesa (panel sólo del menú)
  btn?.setAttribute("aria-controls", "nav-links");
  btn?.setAttribute("aria-expanded", "false");
  btn?.setAttribute("aria-label", "Abrir menú");

  const openMenu = () => {
    links.classList.add("open");
    btn?.classList.add("is-open");
    btn?.setAttribute("aria-expanded", "true");
    btn?.setAttribute("aria-label", "Cerrar menú");
    document.documentElement.classList.add("no-scroll");
  };
  const closeMenu = () => {
    links.classList.remove("open");
    btn?.classList.remove("is-open");
    btn?.setAttribute("aria-expanded", "false");
    btn?.setAttribute("aria-label", "Abrir menú");
    document.documentElement.classList.remove("no-scroll");
    // cierra todos los submenús
    links.querySelectorAll(".nav-item.has-mega-menu.open-sub")
         .forEach(li => li.classList.remove("open-sub"));
    // resetea aria de títulos padres
    links.querySelectorAll(".nav-item.has-mega-menu > a[aria-expanded]")
         .forEach(a => a.setAttribute("aria-expanded", "false"));
  };
  btn?.addEventListener("click", () => {
    links.classList.contains("open") ? closeMenu() : openMenu();
  });

  // --- 3) Delegación de clicks (sobrevive a cambios SPA) + acordeón móvil
  links.addEventListener("click", (e) => {
    const a = e.target.closest("a");
    if (!a) return;

    const li = a.closest(".nav-item.has-mega-menu");
    const isParentOfMega = li && li.querySelector(":scope > a") === a;

    // A) Click en título padre (sólo móvil): abre/cierra su mega y cierra los demás
    if (isMobile() && isParentOfMega) {
      e.preventDefault();
      // accesibilidad
      a.setAttribute("aria-haspopup", "true");
      const willOpen = !li.classList.contains("open-sub");
      // cierra otros
      links.querySelectorAll(".nav-item.has-mega-menu.open-sub")
           .forEach(other => { if (other !== li) other.classList.remove("open-sub"); });
      // toggle actual
      li.classList.toggle("open-sub", willOpen);
      a.setAttribute("aria-expanded", String(willOpen));
      return; // NO cerrar el panel en este click
    }

    // B) Click en una opción interna del mega-menú: cerrar panel y navegar (si SPA)
    if (a.closest(".mega-menu")) {
      if (a.hasAttribute("data-path")) {
        e.preventDefault();
        const path = a.getAttribute("data-path");
        try {
          if (path && typeof window.navigateTo === "function") {
            window.navigateTo(path);
          } else if (path) {
            // fallback: cambiar location si no hay router
            window.location.href = path;
          }
        } catch (_) { /* noop */ }
      }
      // cerrar panel sin mover el resto de la página
      closeMenu();
      return;
    }

    // C) Enlaces simples del menú (sin mega): cerrar panel y navegar si data-path
    if (a.hasAttribute("data-path")) {
      e.preventDefault();
      const path = a.getAttribute("data-path");
      closeMenu();
      try {
        if (path && typeof window.navigateTo === "function") {
          window.navigateTo(path);
        } else if (path) {
          window.location.href = path;
        }
      } catch (_) { /* noop */ }
      return;
    }

    // Por defecto, cerrar panel
    closeMenu();
  });

  // --- 4) Cierre por click fuera / Escape / navegación
  document.addEventListener("click", (e) => {
    if (!links.classList.contains("open")) return;
    if (e.target.closest("#navbar")) return;
    closeMenu();
  });
  document.addEventListener("keydown", (e) => { if (e.key === "Escape") closeMenu(); });
  window.addEventListener("popstate", closeMenu);

  // --- 5) Limpieza al cambiar de breakpoint (evita estados trabados)
  let wasMobile = isMobile();
  window.addEventListener("resize", () => {
    const nowMobile = isMobile();
    if (nowMobile !== wasMobile) {
      closeMenu(); // cierra panel y submenús al cruzar el breakpoint
      wasMobile = nowMobile;
    }
  });
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", setupMegamenu);
} else {
  setupMegamenu();
}
