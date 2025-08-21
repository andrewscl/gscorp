import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

let stompClient = null;
let username = null;

function generateGuestName() {
  return "Invitado-" + Math.random().toString(36).slice(2, 6).toUpperCase();
}

function connectChat() {
  console.log("🔌 connectChat");

  const token = localStorage.getItem("jwt");
  username = token ? null : (localStorage.getItem("chat_username") || null);

  // crea el socket según si hay token o no
  const socket = new SockJS(token ? `/chat?token=${token}` : "/chat");

  // evita reconexiones múltiples
  if (stompClient?.active) return;

  stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    onConnect: () => {
      console.log("✅ Conectado al servidor WebSocket");

      stompClient.subscribe('/topic/messages', (msg) => {
        const data = JSON.parse(msg.body);
        showMessage(data);
      });

      // muestra bienvenida si aún no se mostró en esta sesión
      maybeShowWelcome();
    },
    onStompError: (frame) => {
      console.error('STOMP error:', frame.headers['message'], frame.body);
    },
    onWebSocketError: (error) => {
      console.error('WebSocket error:', error);
    }
  });

  stompClient.activate();
}

function showMessage(msg) {
  const container = document.getElementById("chat-messages");
  if (!container) return;

  const div = document.createElement("div");
  div.className = "message";
  const hora = msg.timestamp ? new Date(msg.timestamp).toLocaleTimeString() : '';
  div.textContent = hora ? `[${hora}] ${msg.from || 'Agente'}: ${msg.content}` : `${msg.from || 'Agente'}: ${msg.content}`;
  container.appendChild(div);
  container.scrollTop = container.scrollHeight;
}

// Mensaje “sistema” (bienvenida)
function showSystemMessage(text) {
  const container = document.getElementById("chat-messages");
  if (!container) return;
  const div = document.createElement("div");
  div.className = "message system";
  div.textContent = text;
  container.appendChild(div);
  container.scrollTop = container.scrollHeight;
}

// Mostrar bienvenida solo una vez por sesión
function maybeShowWelcome() {
  if (sessionStorage.getItem('chat_welcome_shown')) return;
  showSystemMessage("👋 ¡Hola! Contáctanos, estamos para ayudarte.");
  sessionStorage.setItem('chat_welcome_shown', '1');
}

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

  // pinta mi propio mensaje
  showMessage(msg);

  input.value = "";
}

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

  // 👉 Conectamos SOLO cuando el usuario abre el widget
  if (toggleBtn && chatContainer) {
    toggleBtn.addEventListener("click", () => {
      const wasHidden = chatContainer.classList.contains("hidden");
      chatContainer.classList.toggle("hidden");
      toggleBtn.setAttribute('aria-expanded', String(wasHidden));

      if (wasHidden) {
        const token = localStorage.getItem("jwt");
        if (!token) {
          let name = localStorage.getItem("chat_username");
          if (!name) {
            name = generateGuestName();
            localStorage.setItem("chat_username", name);
          }
          username = name;
        }
        connectChat();          // ← aquí conecta
        maybeShowWelcome();     // ← y muestra el mensaje (si no se mostró)
      }
    });
  }

  closeBtn?.addEventListener('click', () => {
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
}
