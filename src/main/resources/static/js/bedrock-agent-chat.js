(function () {
    const messagesEl = document.getElementById('messages');
    const promptEl = document.getElementById('prompt');

    const sessionId = crypto.randomUUID();

    function addBubble(role, text) {
        const bubble = document.createElement('div');
        bubble.className = 'chat-bubble ' + role;
        bubble.textContent = text;
        messagesEl.appendChild(bubble);
        messagesEl.scrollTop = messagesEl.scrollHeight;
        return bubble;
    }

    function sendMessage() {
        const text = promptEl.value.trim();
        if (!text || promptEl.disabled) {
            return;
        }

        addBubble('user', text);
        promptEl.value = '';
        promptEl.disabled = true;

        const pending = addBubble('assistant pending', 'Thinking...');

        fetch('/bedrock/agent/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sessionId: sessionId, message: text })
        })
            .then(response => response.json())
            .then(data => {
                pending.remove();
                if (data.error) {
                    addBubble('assistant error', data.error);
                } else {
                    addBubble('assistant', data.content);
                }
            })
            .catch(() => {
                pending.remove();
                addBubble('assistant error', 'Could not reach the server.');
            })
            .finally(() => {
                promptEl.disabled = false;
                promptEl.focus();
            });
    }

    promptEl.addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            sendMessage();
        }
    });
})();
