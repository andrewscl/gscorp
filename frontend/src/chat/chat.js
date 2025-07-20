import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

let stompClient = null;
let username = null;

function connectChat() {
    console.log("🔌 connectChat");

    const token = localStorage.getItem("jwt");
    username = token ? null : localStorage.getItem("chat_username");

    if (!token && !username) {
        username = prompt("Ingresa tu nombre:");
        localStorage.setItem("chat_username", username);
    }

    const socket = new SockJS(token ? `/chat?token=${token}` : "/chat");

    stompClient = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000, // reconexión automática
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

    stompClient.activate();
}

function sendMessage() {
    const input = document.getElementById("message");
    const content = input?.value.trim();
    if (!content || !stompClient || !stompClient.connected) return;

    const token = localStorage.getItem("jwt");
    const user = token ? null : localStorage.getItem("chat_username") || "Invitado";

    const msg = {
        content,
        from: user,
        timestamp: new Date().toISOString()
    };

    stompClient.publish({
        destination: "/app/send-message",
        body: JSON.stringify(msg)
    });

    input.value = "";
}

function showMessage(msg) {
    const container = document.getElementById("chat-messages");
    if (!container) return;

    const div = document.createElement("div");
    const hora = new Date(msg.timestamp).toLocaleTimeString();
    div.textContent = `[${hora}] ${msg.from}: ${msg.content}`;
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
}

export function setupChat() {
    console.log("🔌 Chat inicializado");

    const widget = document.getElementById("chat-widget");
    if (!widget) {
        console.warn("⚠️ chat-widget no encontrado. Chat no activado.");
        return;
    }

    connectChat();

    const toggleBtn = document.getElementById("chat-button");
    const chatContainer = document.getElementById("chat-container");

    if (toggleBtn && chatContainer) {
        toggleBtn.addEventListener("click", () => {
            chatContainer.classList.toggle("hidden");
        });
    }

    const sendBtn = document.getElementById("send-btn");
    if (sendBtn) {
        sendBtn.addEventListener("click", sendMessage);
    }

    const input = document.getElementById("message");
    if (input) {
        input.addEventListener("keyup", (e) => {
            if (e.key === "Enter") sendMessage();
        });
    }
}
