import { state } from './state.js';
import { fetchText, fetchJson, abortGameRequest, acceptChallengeRequest, sendChallengeRequest } from './api.js';
import { updateUIState, displayOutput, showChallengeNotification, toggleChat, appendChatMessage } from './ui.js';
import { initPlayBoard, startPvPBoard } from './board.js';

export function getCPULevel() {
    const selector = document.getElementById('cpuLevel');
    return selector ? selector.value : 4;
}

export function startInteractive() {
    if (state.gameRunning) return;
    const level = getCPULevel();
    updateUIState(true);
    state.game.reset();
    if (state.board) state.board.destroy();
    initPlayBoard(true);
    
    fetchText(`/spill?dybde=${level}`)
        .then(data => {
            if (data) displayOutput(`Interactive Game started (Level ${level})! You are White.`);
        })
        .catch(err => {
            updateUIState(false);
            displayOutput(err, true);
        });
}

export function startSimulation() {
    if (state.gameRunning) return;
    const level = getCPULevel();
    updateUIState(true);
    state.game.reset();
    if (state.board) state.board.destroy();
    initPlayBoard(false);
    
    fetchText(`/simulate?dybde=${level}`)
        .then(data => {
            if (data) displayOutput(`Simulation started (Level ${level})...`);
        })
        .catch(err => {
            updateUIState(false);
            displayOutput(err, true);
        });
}

export function resetGame() {
    abortGameRequest()
        .then(() => {
            updateUIState(false);
            toggleChat(false);
            state.game.reset();
            if (state.board) state.board.position('start');
            displayOutput('Choose a mode to start');
        })
        .catch(err => console.error('Error aborting game:', err));
}

export function askAI() {
    displayOutput("AI is thinking...");
    fetchText('/ask-ai', { method: 'POST' })
        .then(data => {
            if (data) {
                let formatted = data
                    .replace(/\\n/g, '<br>')
                    .replace(/\\u([0-9a-fA-F]{4})/g, (match, grp) => String.fromCharCode(parseInt(grp, 16)))
                    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
                const output = document.getElementById('output');
                if (output) output.innerHTML = formatted;
            }
        })
        .catch(err => displayOutput("AI Error: " + err, true));
}

export function refreshOnlineUsers() {
    fetchJson('/users/online')
        .then(users => {
            const container = document.getElementById('online-users');
            if (!container) return;
            container.innerHTML = '';
            if (!users || users.length === 0) {
                container.innerHTML = '<li>No other users online</li>';
            } else {
                users.forEach(user => {
                    const li = document.createElement('li');
                    li.style.marginBottom = '5px';
                    li.innerHTML = `${user} <button class="challenge-btn" data-user="${user}" style="padding:2px 5px; font-size:12px;">Challenge</button>`;
                    container.appendChild(li);
                });
                container.querySelectorAll('.challenge-btn').forEach(btn => {
                    btn.addEventListener('click', () => challenge(btn.dataset.user));
                });
            }
        });
}

export function challenge(user) {
    sendChallengeRequest(user).then(data => {
        if (data) displayOutput("Challenge sent to " + user + "... waiting for response.");
    });
}

export function handleChallenge(challenger) {
    showChallengeNotification(challenger, (c) => {
        acceptChallengeRequest(c).then(res => {
            if (!res.ok) alert("Failed to accept challenge. Maybe it expired?");
        });
    });
}

export function startPvPGame(orientation) {
    updateUIState(true);
    toggleChat(true);
    state.game.reset();
    startPvPBoard(orientation);
    displayOutput("PvP Game started! You are " + orientation);
}

export function handleChatSend() {
    const input = document.getElementById('chatInput');
    const msg = input.value.trim();
    if (msg) {
        import('./api.js').then(api => {
            api.sendChatMessage(msg).then(res => {
                if (res.ok) {
                    appendChatMessage("You: " + msg);
                    input.value = '';
                } else {
                    alert("Failed to send message.");
                }
            });
        });
    }
}
