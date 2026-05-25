import { initPlayBoard } from './js/board.js';
import { initSSE } from './js/sse.js';
import { updateUIState, switchTab } from './js/ui.js';
import { startInteractive, startSimulation, resetGame, askAI, refreshOnlineUsers, handleChatSend, resetTimers } from './js/game.js';
import { loadArchive, backToGameList, firstMove, prevMove, nextMove, lastMove } from './js/archive.js';
import { state } from './js/state.js';
import { fetchText } from './js/api.js';

/* ===== INITIALIZATION ===== */

window.addEventListener('load', () => {
    // Initialize play board if present
    if (document.getElementById('board')) {
        initPlayBoard();
        initSSE();
        refreshOnlineUsers();
        updateUIState(false);
        setupEventListeners();

        // Fetch current user
        fetchText('/currentUser')
            .then(user => {
                if (user) state.currentUser = user;
            })
            .catch(err => console.error('Failed to fetch current user:', err));

        // Initial timer setup
        resetTimers();

        // Initial special effects setup
        const specialEffectsCheckbox = document.getElementById('specialEffects');
        if (specialEffectsCheckbox) {
            state.specialEffectsEnabled = specialEffectsCheckbox.checked;
            specialEffectsCheckbox.addEventListener('change', (e) => {
                state.specialEffectsEnabled = e.target.checked;
            });
        }

        // Initial color preference setup
        const colorToggleBtn = document.getElementById('colorToggleBtn');
        if (colorToggleBtn) {
            colorToggleBtn.addEventListener('click', () => {
                const isWhite = colorToggleBtn.classList.contains('white');
                if (isWhite) {
                    colorToggleBtn.classList.remove('white');
                    colorToggleBtn.classList.add('black');
                    state.preferredColor = 'black';
                } else {
                    colorToggleBtn.classList.remove('black');
                    colorToggleBtn.classList.add('white');
                    state.preferredColor = 'white';
                }
                
                if (state.board && !state.gameRunning) {
                    state.board.orientation(state.preferredColor);
                }
            });
            
            // Sync initial state
            state.preferredColor = colorToggleBtn.classList.contains('white') ? 'white' : 'black';
        }

        // Initial CPU level bar setup
        const levelBtns = document.querySelectorAll('.level-btn');
        levelBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                if (state.gameRunning) return;
                levelBtns.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
            });
        });
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

    // Timer inputs
    document.getElementById('startTime')?.addEventListener('input', resetTimers);
    document.getElementById('increment')?.addEventListener('input', resetTimers);

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
