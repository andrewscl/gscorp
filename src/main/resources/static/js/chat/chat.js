let stompClient = null;
let username = null;

function connectChat(){

    const token = localStorage.getItem("jwt");
    username = token ? null : localStorage.getItem("chat_username");

    if(!token && !username) {
        username = prompt("Ingresa tu nomnre:");
        localStorage.setItem("chat_username", username);
    }

    const socket = new SockJS(token ? `/chat?token=${token}` : "/chat")
    stompClient = stompClient.over(socket);

    stompClient.connect({}, function (){
        stompClient.subscribe('/topic/messages', function (msg) {
            const data = JSON.parse(msg.body);
            showMessage(data);
        });
    });
}

function sendMessage () {
    const content = document.getElementById("message").value;
    if(!content) return;

    const msg = {
        content,
        from: username,
        timestamp: new Date().toISOString()
    };

    stompClient.send("/app/send-message", {}, JSON.stringify(msg));
    document.getElementById("message").value = "";
}

function showMessage(msg) {
    const container = document.getElementById("chat-messages");
    const div = document.createElement("div");
    div.textContent = `[${msg.timestamp}] ${msg.from}: ${msg.content}`;
    container.appendChild(div);
}

window.addEventListener("DOMContentLoaded", connectChat);