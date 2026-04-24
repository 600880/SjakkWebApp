import { initPlayBoard } from './js/board.js';
import { initSSE } from './js/sse.js';
import { updateUIState, switchTab } from './js/ui.js';
import { startInteractive, startSimulation, resetGame, askAI, refreshOnlineUsers, handleChatSend } from './js/game.js';
import { loadArchive, backToGameList, firstMove, prevMove, nextMove, lastMove } from './js/archive.js';

/* ===== INITIALIZATION ===== */

window.addEventListener('load', () => {
    // Initialize play board if present
    if (document.getElementById('board')) {
        initPlayBoard();
        initSSE();
        refreshOnlineUsers();
        updateUIState(false);
        setupEventListeners();
    }
});

function setupEventListeners() {
    // Tab switching
    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', () => {
            const tabName = tab.dataset.tab;
            switchTab(tabName);
            if (tabName === 'archive') {
                loadArchive();
            }
        });
    });

    // Game controls
    document.getElementById('playBtn')?.addEventListener('click', startInteractive);
    document.getElementById('simulateBtn')?.addEventListener('click', startSimulation);
    document.getElementById('resetBtn')?.addEventListener('click', resetGame);
    document.getElementById('askAiBtn')?.addEventListener('click', askAI);
    
    // Online users
    document.getElementById('refreshUsersBtn')?.addEventListener('click', refreshOnlineUsers);

    // Chat
    document.getElementById('sendChatBtn')?.addEventListener('click', handleChatSend);
    document.getElementById('chatInput')?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') handleChatSend();
    });

    // Archive / Replay
    document.getElementById('backToArchiveBtn')?.addEventListener('click', backToGameList);
    document.getElementById('firstMoveBtn')?.addEventListener('click', firstMove);
    document.getElementById('prevMoveBtn')?.addEventListener('click', prevMove);
    document.getElementById('nextMoveBtn')?.addEventListener('click', nextMove);
    document.getElementById('lastMoveBtn')?.addEventListener('click', lastMove);
}
