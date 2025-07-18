let stompClient = null;
let username = null;

function connectChat() {
    const token = localStorage.getItem("jwt");
    username = token ? null : localStorage.getItem("chat_username");

    if (!token && !username) {
        username = prompt("Ingresa tu nombre:");
        localStorage.setItem("chat_username", username);
    }

    const socket = new SockJS(token ? `/chat?token=${token}` : "/chat");
    stompClient = Stomp.over(socket); // ✅ corregido

    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/messages', function (msg) {
            const data = JSON.parse(msg.body);
            showMessage(data);
        });
    });
}

function sendMessage() {
    const input = document.getElementById("message");
    const content = input.value.trim();
    if (!content || !stompClient || !stompClient.connected) return;

    const token = localStorage.getItem("jwt");
    const user = token ? null : localStorage.getItem("chat_username") || "Invitado";

    const msg = {
        content,
        from: user,
        timestamp: new Date().toISOString()
    };

    stompClient.send("/app/send-message", {}, JSON.stringify(msg));
    input.value = "";
}

function showMessage(msg) {
    const container = document.getElementById("chat-messages");
    const div = document.createElement("div");
    const hora = new Date(msg.timestamp).toLocaleTimeString();
    div.textContent = `[${hora}] ${msg.from}: ${msg.content}`;
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
}

document.addEventListener("DOMContentLoaded", () => {
    connectChat();

    // ✅ toggle mostrar/ocultar chat
    const toggleBtn = document.getElementById("chat-button");
    const chatContainer = document.getElementById("chat-container");

    if (toggleBtn && chatContainer) {
        toggleBtn.addEventListener("click", () => {
            chatContainer.classList.toggle("hidden");
        });
    }

    // ✅ botón enviar
    const sendBtn = document.getElementById("send-btn");
    if (sendBtn) {
        sendBtn.addEventListener("click", sendMessage);
    }

    // ✅ enter para enviar
    const input = document.getElementById("message");
    if (input) {
        input.addEventListener("keyup", (e) => {
            if (e.key === "Enter") sendMessage();
        });
    }
});
