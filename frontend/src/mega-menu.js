// mega-menu.js
export function setupMegamenu () {
  if (window.__megaMenuInit) return;
  window.__megaMenuInit = true;

  const navbar = document.getElementById("navbar");
  const btn    = document.getElementById("menu-toggle");
  const links  = document.getElementById("nav-links");
  if (!navbar || !links) return;

  const isMobile = () => window.matchMedia("(max-width: 768px)").matches;

  // --- 0) Altura real del navbar -> CSS var (px exactos)
  function updateNavHeightVar() {
    const h = navbar.offsetHeight || 0;
    document.documentElement.style.setProperty("--nav-h-px", h + "px");
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

  // --- 2) Hamburguesa
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
  };
  btn?.addEventListener("click", () => {
    links.classList.contains("open") ? closeMenu() : openMenu();
  });

  // --- 3) Delegación de clicks (sobrevive a cambios SPA)
  links.addEventListener("click", (e) => {
    const a = e.target.closest("a");
    if (!a) return;

    const li = a.closest(".nav-item.has-mega-menu");
    const isParentOfMega = li && li.querySelector(":scope > a") === a;

    if (isMobile() && isParentOfMega) {
      // acordeón en móvil: abre uno y cierra los demás
      e.preventDefault();
      const willOpen = !li.classList.contains("open-sub");
      links.querySelectorAll(".nav-item.has-mega-menu.open-sub")
           .forEach(other => { if (other !== li) other.classList.remove("open-sub"); });
      li.classList.toggle("open-sub", willOpen);
      return; // NO cerrar el panel en este click
    }

    // cualquier enlace interno (item del mega) => cierra todo el panel
    if (a.closest(".mega-menu")) {
      // si usas router SPA y data-path:
      if (a.hasAttribute("data-path")) e.preventDefault();
      closeMenu();
      // opcional: desplaza al top para que no tape el título
      window.scrollTo({ top: 0 });
      return;
    }

    // enlaces simples (sin mega) también cierran el panel
    if (a.hasAttribute("data-path")) {
      e.preventDefault();
      closeMenu();
      // aquí llamarías a navigateTo(a.dataset.path)
    } else {
      closeMenu();
    }
  });

  // --- 4) Cierre por click fuera / Escape / navegación
  document.addEventListener("click", (e) => {
    if (!links.classList.contains("open")) return;
    if (e.target.closest("#navbar")) return;
    closeMenu();
  });
  document.addEventListener("keydown", (e) => { if (e.key === "Escape") closeMenu(); });
  window.addEventListener("popstate", closeMenu);
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", setupMegamenu);
} else {
  setupMegamenu();
}
