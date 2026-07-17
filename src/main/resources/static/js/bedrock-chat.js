(function () {
    const modelSelect = document.getElementById('model');
    const messagesEl = document.getElementById('messages');
    const promptEl = document.getElementById('prompt');

    const history = [];

    function addBubble(role, text) {
        const bubble = document.createElement('div');
        bubble.className = 'chat-bubble ' + role;
        bubble.textContent = text;
        messagesEl.appendChild(bubble);
        messagesEl.scrollTop = messagesEl.scrollHeight;
        return bubble;
    }

    function loadModels() {
        fetch('/bedrock/api/models')
            .then(response => response.json())
            .then(models => {
                modelSelect.innerHTML = '';
                models.forEach(m => {
                    const option = document.createElement('option');
                    option.value = m.id;
                    option.textContent = m.name;
                    modelSelect.appendChild(option);
                });
            })
            .catch(() => addBubble('assistant', 'Could not load the model list.'));
    }

    function sendMessage() {
        const text = promptEl.value.trim();
        if (!text || promptEl.disabled) {
            return;
        }

        history.push({ role: 'user', content: text });
        addBubble('user', text);
        promptEl.value = '';
        promptEl.disabled = true;

        const pending = addBubble('assistant pending', 'Thinking...');

        fetch('/bedrock/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ model: modelSelect.value, messages: history })
        })
            .then(response => response.json())
            .then(data => {
                pending.remove();
                if (data.error) {
                    addBubble('assistant error', data.error);
                } else {
                    history.push({ role: 'assistant', content: data.content });
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

    loadModels();
})();
