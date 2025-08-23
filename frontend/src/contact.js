export function setupContact(){

  const form = document.getElementById("contactForm");

  if(!form) return;

  const showToast = (message, type = "success") => {
    const container = document.getElementById("toast-container");
    const toast = document.createElement("Div");
    toast.className = `toast ${type}`;
    toast.textContent = message;
    container.appendChild(toast);

    //Mostrar con fade-in
    setTimeout(() => {
      toast.classList.add("visible");
    }, 10);

    //Oculta y eliminar luego de 5 segundos
    setTimeout(() => {
      toast.classList.remove("visible");
      setTimeout(() => container.removeChild(toast), 300);
    }, 5000);

  };

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const name = document.getElementById("name").value;
    const email = document.getElementById("email").value;
    const phone = document.getElementById("phone").value;
    const message = document.getElementById("message").value;

    const data = { name, email, phone, message};
    console.log("Datos enviados: " + data);

    try {
      console.log("contact-handler activado");
      const response = await fetch("/public/api/contact", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      if (response.ok) {
        form.reset();
        showToast("✅ Gracias por contactarnos. Te responderemos a la brevedad.", "success");
      } else {
        showToast("❌ Ocurrió un error al enviar su mensaje. Intente nuevamente más tarde.", "error");
      }
    } catch (error) {
        showToast("❌ Error de conexión con el servidor. Verifica tu red.", "error");
    }
  });
}
