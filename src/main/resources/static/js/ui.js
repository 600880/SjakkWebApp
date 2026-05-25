import { state } from './state.js';

export function switchTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.style.display = 'none';
    });

    // Show selected tab
    const selectedTab = document.getElementById(tabName);
    if (selectedTab) {
        selectedTab.style.display = 'block';
    }

    // Update active tab styling
    document.querySelectorAll('.tab').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Find tab by data-tab instead of onclick
    const activeTabElement = document.querySelector(`.tab[data-tab="${tabName}"]`);
    if (activeTabElement) {
        activeTabElement.classList.add('active');
    }
}

export function updateUIState(running) {
    state.gameRunning = running;
    const playBtn = document.getElementById('playBtn');
    const simulateBtn = document.getElementById('simulateBtn');
    const resetBtn = document.getElementById('resetBtn');
    const cpuSelector = document.getElementById('cpuLevel');
    const cpuLevelBar = document.getElementById('cpuLevelBar');
    const askAiBtn = document.getElementById('askAiBtn');
    const startTimeInput = document.getElementById('startTime');
    const incrementInput = document.getElementById('increment');
    const colorToggleBtn = document.getElementById('colorToggleBtn');

    if (playBtn) playBtn.disabled = running;
    if (simulateBtn) simulateBtn.disabled = running;
    if (cpuSelector) cpuSelector.disabled = running;
    if (cpuLevelBar) cpuLevelBar.classList.toggle('disabled', running);
    if (startTimeInput) startTimeInput.disabled = running;
    if (incrementInput) incrementInput.disabled = running;
    if (colorToggleBtn) colorToggleBtn.disabled = running;
    if (resetBtn) resetBtn.disabled = !running;
    if (askAiBtn) askAiBtn.disabled = !running;

    updateTimers();
    updateCapturedPieces();
}

export function updateCapturedPieces() {
    const whiteCapturedContainer = document.getElementById('captured-black'); // Pieces captured BY white
    const blackCapturedContainer = document.getElementById('captured-white'); // Pieces captured BY black
    if (!whiteCapturedContainer || !blackCapturedContainer) return;

    whiteCapturedContainer.innerHTML = '';
    blackCapturedContainer.innerHTML = '';

    const board = state.game.board();
    const currentPieces = { w: [], b: [] };

    // Count current pieces on board
    board.forEach(row => {
        row.forEach(square => {
            if (square) {
                currentPieces[square.color].push(square.type);
            }
        });
    });

    const startingPieces = ['p', 'p', 'p', 'p', 'p', 'p', 'p', 'p', 'r', 'r', 'n', 'n', 'b', 'b', 'q', 'k'];

    const getCaptured = (color) => {
        const captured = [];
        const current = [...currentPieces[color]];
        
        startingPieces.forEach(type => {
            const index = current.indexOf(type);
            if (index > -1) {
                current.splice(index, 1);
            } else {
                captured.push({ color, type });
            }
        });
        return captured;
    };

    // Sort pieces by value for better display
    const pieceOrder = { 'q': 0, 'r': 1, 'b': 2, 'n': 3, 'p': 4, 'k': 5 };
    const sortPieces = (a, b) => pieceOrder[a.type] - pieceOrder[b.type];

    const whiteLost = getCaptured('w').sort(sortPieces);
    const blackLost = getCaptured('b').sort(sortPieces);

    const createPieceImg = (p) => {
        const img = document.createElement('img');
        const code = p.color + p.type.toUpperCase();
        img.src = `/img/chesspieces/wikipedia/${code}.png`;
        img.className = 'captured-piece';
        img.title = p.type.toUpperCase();
        return img;
    };

    // Respective player sees opponent's eliminated pieces on the right
    // White player container (bottom) shows Black pieces that were eliminated
    blackLost.forEach(p => whiteCapturedContainer.appendChild(createPieceImg(p)));
    
    // Black player container (top) shows White pieces that were eliminated
    whiteLost.forEach(p => blackCapturedContainer.appendChild(createPieceImg(p)));
}

export function updateTimers() {
    const whiteTimer = document.getElementById('white-timer');
    const blackTimer = document.getElementById('black-timer');
    const whiteContainer = document.getElementById('white-timer-container');
    const blackContainer = document.getElementById('black-timer-container');

    if (!whiteTimer || !blackTimer) return;

    const formatTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    };

    whiteTimer.textContent = formatTime(state.timers.w);
    blackTimer.textContent = formatTime(state.timers.b);

    const turn = state.game.turn();
    if (state.gameRunning) {
        whiteContainer.classList.toggle('active', turn === 'w');
        blackContainer.classList.toggle('active', turn === 'b');
    } else {
        whiteContainer.classList.remove('active');
        blackContainer.classList.remove('active');
    }

    const whiteName = document.getElementById('white-player-name');
    const blackName = document.getElementById('black-player-name');
    if (whiteName) whiteName.textContent = state.whitePlayer;
    if (blackName) blackName.textContent = state.blackPlayer;
}

export function displayOutput(msg, isError = false) {
    const output = document.getElementById('output');
    if (output) {
        output.innerText = (isError ? 'Error: ' : '') + msg;
        output.style.width = "500px"; // Ensure width matches board
    }
}

export function appendChatMessage(msg) {
    const container = document.getElementById('chat-messages');
    if (container) {
        const div = document.createElement('div');
        div.style.marginBottom = '5px';
        div.innerText = msg;
        container.appendChild(div);
        container.scrollTop = container.scrollHeight;
    }
}

export function toggleChat(enable) {
    const chatInput = document.getElementById('chatInput');
    const sendBtn = document.getElementById('sendChatBtn');
    if (chatInput) chatInput.disabled = !enable;
    if (sendBtn) sendBtn.disabled = !enable;
    
    if (!enable) {
        const chatMessages = document.getElementById('chat-messages');
        if (chatMessages) chatMessages.innerHTML = '';
        if (chatInput) chatInput.value = '';
    }
}

export function showChallengeNotification(challenger, onAccept) {
    const container = document.getElementById('notification-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerHTML = `
        <div class="toast-header">New Challenge!</div>
        <div class="toast-body"><strong>${challenger}</strong> has challenged you to a game.</div>
        <div class="toast-actions">
            <button class="btn-accept">Accept</button>
            <button class="btn-decline">Decline</button>
        </div>
    `;
    
    toast.querySelector('.btn-accept').addEventListener('click', () => {
        onAccept(challenger);
        toast.remove();
    });
    
    toast.querySelector('.btn-decline').addEventListener('click', () => {
        toast.remove();
    });

    container.appendChild(toast);

    // Auto-remove after 20 seconds
    setTimeout(() => {
        if (toast.parentElement) toast.remove();
    }, 20000);
}
