// mega-menu.js
export function setupMegamenu () {
  if (window.__megaMenuInit) return;
  window.__megaMenuInit = true;

  const navbar = document.getElementById("navbar");
  const btn    = document.getElementById("menu-toggle");
  const links  = document.getElementById("nav-links");
  if (!navbar) return;

  const isMobile = () => window.matchMedia("(max-width: 768px)").matches;

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

  // Utilidades submenús
  const parentAnchors = Array.from(document.querySelectorAll(".nav-item.has-mega-menu > a"));
  const closeAllSubmenus = (exceptLi) => {
    document.querySelectorAll(".nav-item.has-mega-menu.open-sub").forEach(li => {
      if (li !== exceptLi) {
        li.classList.remove("open-sub");
        const a = li.querySelector(":scope > a");
        a?.setAttribute("aria-expanded", "false");
      }
    });
  };

  // Toggle contenedor móvil (hamburguesa)
  let close; // definimos close antes para usarla abajo
  if (btn && links) {
    btn.setAttribute("aria-controls", "nav-links");
    btn.setAttribute("aria-expanded", "false");
    btn.setAttribute("aria-label", "Abrir menú");

    const open = () => {
      links.classList.add("open");
      btn.classList.add("is-open");
      btn.setAttribute("aria-expanded", "true");
      btn.setAttribute("aria-label", "Cerrar menú");
      document.documentElement.classList.add("no-scroll");
    };

    close = () => {
      links.classList.remove("open");
      btn.classList.remove("is-open");
      btn.setAttribute("aria-expanded", "false");
      btn.setAttribute("aria-label", "Abrir menú");
      document.documentElement.classList.remove("no-scroll");
      closeAllSubmenus(); // <- cierra submenús también
    };

    const toggle = () => (links.classList.contains("open") ? close() : open());
    btn.addEventListener("click", toggle);

    // Cerrar al navegar, pero NO si el click fue en el <a> PADRE de un mega-menu en móvil
    links.addEventListener("click", (e) => {
      const a = e.target.closest("a");
      if (!a) return;

      const li = a.closest(".nav-item.has-mega-menu");
      const isParentMega = li && li.querySelector(":scope > a") === a;

      if (isMobile() && isParentMega) {
        // lo maneja el toggler de submenú (abajo)
        e.preventDefault();
        return;
      }

      // si es SPA, evitar el salto a '#'
      if (a.hasAttribute("data-path")) e.preventDefault();
      close();
    });

    // Click fuera del navbar cierra todo
    document.addEventListener("click", (e) => {
      if (!links.classList.contains("open")) return;
      if (e.target.closest("#navbar")) return;
      close();
    });

    document.addEventListener("keydown", (e) => { if (e.key === "Escape") close(); });
    window.addEventListener("resize", () => { if (!isMobile()) close(); });
    window.addEventListener("popstate", close);
  }

  // === Submenús (móvil): el <a> padre actúa como botón ===
  parentAnchors.forEach(a => {
    a.setAttribute("aria-haspopup", "true");
    a.setAttribute("aria-expanded", "false");

    a.addEventListener("click", (e) => {
      if (!isMobile()) return; // en desktop manda el hover
      e.preventDefault();
      const li = a.closest(".nav-item.has-mega-menu");
      const willOpen = !li.classList.contains("open-sub");
      closeAllSubmenus(li);
      li.classList.toggle("open-sub", willOpen);
      a.setAttribute("aria-expanded", String(willOpen));
    });

    // Accesibilidad teclado en móvil
    a.addEventListener("keydown", (e) => {
      if (!isMobile()) return;
      if (e.key === "Enter" || e.key === " ") {
        e.preventDefault();
        a.click();
      }
    });
  });

  // Exponer por si lo necesitas
  window.setupMegamenu = setupMegamenu;
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", () => setupMegamenu());
} else {
  setupMegamenu();
}
