import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

let stompClient = null;
let username = null;

/* ---------- Helpers ---------- */
function generateGuestName() {
  return "Invitado-" + Math.random().toString(36).slice(2, 6).toUpperCase();
}

// Si quieres que siempre aparezca la bienvenida al reabrir:
const SHOW_WELCOME_ON_EACH_OPEN = true;

// Reset UI del chat al cerrar (soft por defecto)
function resetChatUI() {
  document.getElementById('lead-form')?.remove();
  const input = document.getElementById('message');
  if (input) input.value = '';
  sessionStorage.removeItem('chat_welcome_shown'); // para que se re-muestre en la próxima
}


function scrollMessagesBottom() {
  const container = document.getElementById("chat-messages");
  if (container) container.scrollTop = container.scrollHeight;
}

function showMessage(msg) {
  const container = document.getElementById("chat-messages");
  if (!container) return;

  const div = document.createElement("div");
  div.className = "message";
  const hora = msg.timestamp ? new Date(msg.timestamp).toLocaleTimeString() : '';
  div.textContent = hora
    ? `[${hora}] ${msg.from || 'Agente'}: ${msg.content}`
    : `${msg.from || 'Agente'}: ${msg.content}`;
  container.appendChild(div);
  scrollMessagesBottom();
}

function showSystemMessage(text, { key } = {}) {
  const container = document.getElementById("chat-messages");
  if (!container) return;

  if (key && container.querySelector(`.message.system[data-key="${key}"]`)) return;

  const div = document.createElement("div");
  div.className = "message system";
  if (key) div.dataset.key = key;
  div.textContent = text;
  container.appendChild(div);
  container.scrollTop = container.scrollHeight;
}

function maybeShowWelcome() {
  // muestra solo si no se mostró en esta apertura
  if (sessionStorage.getItem('chat_welcome_shown')) return;
  showSystemMessage("👋 ¡Hola! Contáctanos, estamos para ayudarte.", { key: 'welcome' });
  sessionStorage.setItem('chat_welcome_shown', '1');
}

// Quita maybeShowWelcome() de onConnect y déjalo solo al abrir desde el botón.


/* ---------- STOMP ---------- */
function connectChat() {
  console.log("🔌 connectChat");

  const token = localStorage.getItem("jwt");
  username = token ? null : (localStorage.getItem("chat_username") || null);

  const socket = new SockJS(token ? `/chat?token=${token}` : "/chat");

  if (stompClient?.active) return; // evita reconexiones múltiples

  stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    onConnect: () => {
      console.log("✅ Conectado al servidor WebSocket");

      stompClient.subscribe('/topic/messages', (msg) => {
        const data = JSON.parse(msg.body);
        showMessage(data);
      });

    },
    onStompError: (frame) => {
      console.error('STOMP error:', frame.headers['message'], frame.body);
    },
    onWebSocketError: (error) => {
      console.error('WebSocket error:', error);
    }
  });

  // exposicion opcional (si en otro lado consultas window.stompClient)
  window.stompClient = stompClient;
  stompClient.activate();
}

/* ---------- Envío de mensajes ---------- */
function sendMessage() {
  const input = document.getElementById("message");
  const content = input?.value.trim();
  if (!content || !stompClient || !stompClient.connected) return;

  const token = localStorage.getItem("jwt");
  const user = token ? null : (username || localStorage.getItem("chat_username") || "Invitado");

  const msg = {
    content,
    from: user,
    timestamp: new Date().toISOString()
  };

  stompClient.publish({
    destination: "/app/send-message",
    body: JSON.stringify(msg)
  });

  showMessage(msg); // pinta mi propio mensaje
  input.value = "";
}

/* ---------- CTA: Conoce nuestros servicios (form) ---------- */
function renderServicesForm() {
  const wrap = document.getElementById("chat-messages");
  if (!wrap) return;

  const existing = wrap.querySelector("#lead-form");
  if (existing) { existing.querySelector('input, textarea')?.focus(); scrollMessagesBottom(); return; }

  const form = document.createElement("form");
  form.id = "lead-form";
  form.className = "message lead-form";
  form.innerHTML = `
    <div class="lead-form__title">Conoce nuestros servicios</div>

    <label class="lead-form__field">
      <span>Nombre</span>
      <input id="lead-name" type="text" name="name" required placeholder="Tu nombre">
    </label>

    <label class="lead-form__field">
      <span>Correo</span>
      <input id="lead-email" type="email" name="email" required placeholder="tucorreo@dominio.com">
    </label>

    <label class="lead-form__field">
      <span>Teléfono</span>
      <input id="lead-phone" type="tel" name="phone" required placeholder="+56 9 1234 5678">
    </label>

    <label class="lead-form__field">
      <span>Comentario</span>
      <textarea id="lead-message" name="message" rows="3" required placeholder="Cuéntanos qué necesitas"></textarea>
    </label>

    <div class="lead-form__actions">
      <button type="submit" class="btn-primary">Enviar solicitud</button>
      <button type="button" class="btn-ghost" id="lead-cancel">Cancelar</button>
    </div>
  `;


  wrap.appendChild(form);
  scrollMessagesBottom();

form.addEventListener("submit", async (e) => {
  e.preventDefault();

  // 1) Tomar valores
  const name    = document.getElementById("lead-name")?.value.trim() || "";
  const email   = document.getElementById("lead-email")?.value.trim() || "";
  const phone   = document.getElementById("lead-phone")?.value.trim() || "";
  const message = document.getElementById("lead-message")?.value.trim() || "";

  // 2) Validación simple
  const emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  if (!name || !emailOk || !phone || !message) {
    showSystemMessage("Por favor completa todos los campos correctamente.");
    return;
  }

  // 3) Deshabilitar mientras envía
  const submitBtn = form.querySelector(".btn-primary");
  const cancelBtn = form.querySelector("#lead-cancel");
  const restore = () => {
    if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = "Enviar solicitud"; }
    if (cancelBtn)  cancelBtn.disabled = false;
  };
  if (submitBtn) { submitBtn.disabled = true; submitBtn.textContent = "Enviando…"; }
  if (cancelBtn)  cancelBtn.disabled = true;

  // 4) Construir payload para tu API
  const data = {
    name, email, phone, message,
    source: "chat",                                 // útil para tu ContactService
    path: location.pathname + location.search + location.hash,
    title: document.title,
    tz: Intl.DateTimeFormat().resolvedOptions().timeZone
  };

  // 5) Headers y CSRF (si usas Spring Security con cookies)
  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

  const headers = { "Content-Type": "application/json", "X-Requested-With": "XMLHttpRequest" };
  if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

  try {
    // 6) Enviar a tu API
    const response = await fetch("/public/api/contact", {
      method: "POST",
      headers,
      body: JSON.stringify(data),
      credentials: "same-origin"
    });

    if (!response.ok) {
      const text = await response.text().catch(() => "");
      throw new Error(`HTTP ${response.status}${text ? `: ${text}` : ""}`);
    }

    // 7) Feedback en UI
    showSystemMessage("✅ ¡Gracias! Hemos recibido tu solicitud. Te contactaremos pronto.");

    const container = document.getElementById("chat-messages");
    const copy = document.createElement("div");
    copy.className = "message me";
    copy.textContent = `Solicitud enviada: ${name}, ${email}, ${phone}. Mensaje: ${message}`;
    container.appendChild(copy);

    // 8) Opcional: Analytics
    if (window.dataLayer) {
      window.dataLayer.push({
        event: "lead_submit",
        lead_topic: "servicios",
        lead_email: email,
        lead_phone: phone
      });
    }

    // 9) Cerrar/eliminar form y limpiar
    form.remove();
    // Si deseas cerrar el chat al enviar:
    // resetChatUI({ hard: true });
    // document.getElementById("chat-container")?.classList.add("hidden");
    // document.getElementById("chat-button")?.setAttribute("aria-expanded", "false");

  } catch (err) {
    console.error(err);
    showSystemMessage("❌ Ocurrió un problema al enviar. Intenta nuevamente.");
  } finally {
    restore();
  }
});


  form.querySelector("#lead-cancel")?.addEventListener("click", () => {
    form.remove();
    showSystemMessage("Formulario cancelado.");
  });

  form.querySelector('input[name="name"]')?.focus();
}

/* ---------- Setup ---------- */
export function setupChat() {
  console.log("🔌 Chat inicializado");

  const widget = document.getElementById("chat-widget");
  if (!widget) {
    console.warn("⚠️ chat-widget no encontrado. Chat no activado.");
    return;
  }

  const toggleBtn = document.getElementById("chat-button");
  const chatContainer = document.getElementById("chat-container");
  const closeBtn = document.getElementById("chat-close");

// Handlers
if (toggleBtn && chatContainer) {
  toggleBtn.addEventListener("click", () => {
    const wasHidden = chatContainer.classList.contains("hidden");
    chatContainer.classList.toggle("hidden");
    toggleBtn.setAttribute('aria-expanded', String(wasHidden));

    if (wasHidden) {
      // Se está ABRIENDO
      const token = localStorage.getItem("jwt");
      if (!token) {
        let name = localStorage.getItem("chat_username");
        if (!name) {
          name = generateGuestName();
          localStorage.setItem("chat_username", name);
        }
        username = name;
      }
      connectChat();
      maybeShowWelcome();
    } else {
      // Se está CERRANDO
      resetChatUI(); // soft reset
    }
  });
}

closeBtn?.addEventListener('click', () => {
  resetChatUI(); // soft reset
  chatContainer?.classList.add('hidden');
  toggleBtn?.setAttribute('aria-expanded', 'false');
});


  const sendBtn = document.getElementById("send-btn");
  if (sendBtn) sendBtn.addEventListener("click", sendMessage);

  const input = document.getElementById("message");
  if (input) {
    input.addEventListener("keyup", (e) => {
      if (e.key === "Enter") sendMessage();
    });
  }

  const ctaBtn = document.getElementById("cta-services");
  if (ctaBtn) {
    ctaBtn.addEventListener("click", () => {
      renderServicesForm();
    });
  }
}
