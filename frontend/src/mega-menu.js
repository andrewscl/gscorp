export function setupMegamenu () {

  // Evitar re-inicializar si existen cambios de vista SPA
  if (window.__megaMenuInit) return ;
  window.__megaMenuInit = true;

  console.log("setupMegamenu activado");
  const navbar = document.getElementById("navbar");
  const btn = document.getElementById("menu-toggle");
  const links = document.getElementById("nav-links");

  if(!navbar) return;

  let lastScrolled = false;

  window.addEventListener("scroll", () => {
    const isNowScrolled = window.scrollY > 60;

    // Solo hacer cambios si el estado cambió
    if (isNowScrolled !== lastScrolled) {
      lastScrolled = isNowScrolled;

      navbar.classList.toggle("solid", isNowScrolled);
      navbar.classList.toggle("transparent", !isNowScrolled);

    }
  });

  if(btn && links) {
    //Accesibilidad
    btn.setAttribute("aria-controls", "nav-links");
    btn.setAttribute("aria-expanded", "false");
    btn.setAttribute("aria-label", "Abrir menu");

    const open = () => {
      links.classList.add("open");
      btn.setAttribute("aria-expanded", "true");
      document.documentElement.classList.add("no-scroll");
    };

    const close = () => {
      links.classList.remove("open");
      btn.setAttribute("aria-expanded", "false");
      document.documentElement.classList.remove("no-scroll");
    };

    const toggle = () => (links.classList.contains("open") ? close() : open());

    btn.addEventListener("click", toggle);

    // Cerrar al navegar dentro del menu
    links.addEventListener("click", (e) => {
      const a = e.target.closest("[data-path], a[href]");
      if (a) close();
    });

    // Cerrar con escape
    document.addEventListener("keydown", (e) => {
      if (e.key === "Escape") close();
    });

    // Si vuelve a desktop cierra
    window.addEventListener("resize", () => {
      if (window.innerWidth > 768) close();
    });
  }

  window.setupMegamenu = setupMegamenu;

}
