export function setupMegamenu () {

  console.log("setupMegamenu activado");
  const navbar = document.getElementById("navbar");

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

  window.setupMegamenu = setupMegamenu;

}
