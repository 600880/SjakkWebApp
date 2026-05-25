import { state } from './state.js';
import { fetchText, fetchJson, abortGameRequest, acceptChallengeRequest, sendChallengeRequest, saveGame } from './api.js';
import { updateUIState, displayOutput, showChallengeNotification, toggleChat, appendChatMessage, updateTimers, updateCapturedPieces } from './ui.js';
import { initPlayBoard, startPvPBoard } from './board.js';
import { triggerFireworks, triggerTomatoes, playCheckAlarm } from './effects.js';

export function checkGameOver() {
    if (state.game.game_over()) {
        stopTimer();
        updateCapturedPieces();
        if (state.userColor === 'w') {
            const pgn = state.game.pgn();
            saveGame(state.whitePlayer, state.blackPlayer, pgn)
                .then(res => {
                    if (res.ok) console.log("Game saved to database");
                    else console.error("Failed to save game");
                });
        }

        if (state.game.in_checkmate()) {
            const winner = state.game.turn() === 'w' ? 'Black' : 'White';
            const userWon = (state.game.turn() !== state.userColor);
            
            displayOutput(`Checkmate! ${winner} wins.`);
            setTimeout(() => {
                if (userWon) {
                    triggerFireworks();
                } else {
                    triggerTomatoes();
                }
            }, 500);
        } else if (state.game.in_draw()) {
            displayOutput("Game over! It's a draw.");
        } else if (state.game.in_stalemate()) {
            displayOutput("Game over! Stalemate.");
        } else if (state.game.in_threefold_repetition()) {
            displayOutput("Game over! Threefold repetition.");
        } else {
            displayOutput("Game over!");
        }
        updateUIState(false);
        return true;
    } else {
        if (state.game.in_check()) {
            playCheckAlarm();
            displayOutput("Check!", false);
        }
    }
    updateCapturedPieces();
    return false;
}

export function startTimer() {
    if (state.timerInterval) clearInterval(state.timerInterval);
    
    state.timerInterval = setInterval(() => {
        const turn = state.game.turn();
        state.timers[turn]--;

        updateTimers();

        if (state.timers[turn] <= 0) {
            stopTimer();
            updateCapturedPieces();
            const winner = turn === 'w' ? 'Black' : 'White';
            const userWon = (turn !== state.userColor);
            
            displayOutput(`Time out! ${winner} wins.`);
            
            // Save game on timeout
            if (state.userColor === 'w') {
                saveGame(state.whitePlayer, state.blackPlayer, state.game.pgn() + ` {${winner} wins on time}`);
            }

            setTimeout(() => {
                if (userWon) triggerFireworks();
                else triggerTomatoes();
            }, 500);

            updateUIState(false);
        }
    }, 1000);
}

export function stopTimer() {
    if (state.timerInterval) {
        clearInterval(state.timerInterval);
        state.timerInterval = null;
    }
}

export function applyIncrement() {
    if (!state.gameRunning) return;
    // The turn has already switched in chess.js, so we increment the OTHER player
    const previousTurn = state.game.turn() === 'w' ? 'b' : 'w';
    state.timers[previousTurn] += state.timerIncrement;
    updateTimers();
    updateCapturedPieces();
}

export function resetTimers() {
    stopTimer();
    const startTimeInput = document.getElementById('startTime');
    const incrementInput = document.getElementById('increment');
    
    const startMin = startTimeInput ? parseInt(startTimeInput.value) : 5;
    const incSec = incrementInput ? parseInt(incrementInput.value) : 0;
    
    console.log(`Setting timers to ${startMin}m with ${incSec}s increment`);
    
    state.timers.w = startMin * 60;
    state.timers.b = startMin * 60;
    state.timerIncrement = incSec;
    
    updateTimers();
}

export function getCPULevel() {
    const activeBtn = document.querySelector('.level-btn.active');
    return activeBtn ? parseInt(activeBtn.dataset.level) : 4;
}

export function startInteractive() {
    if (state.gameRunning) return;
    const level = getCPULevel();
    const color = state.preferredColor;
    
    state.game.reset();
    resetTimers();
    updateUIState(true);
    if (state.board) state.board.destroy();
    initPlayBoard(true);
    
    state.userColor = (color === 'white' ? 'w' : 'b');
    state.board.orientation(color);

    if (state.userColor === 'w') {
        state.whitePlayer = state.currentUser || 'User';
        state.blackPlayer = 'cpu@cpu.no';
    } else {
        state.whitePlayer = 'cpu@cpu.no';
        state.blackPlayer = state.currentUser || 'User';
    }

    updateTimers();
    updateCapturedPieces();

    fetchText(`/spill?dybde=${level}&color=${color}`)
        .then(data => {
            if (data) {
                displayOutput(`Interactive Game started (Level ${level})! You are ${color.charAt(0).toUpperCase() + color.slice(1)}.`);
                startTimer();
            }
        })
        .catch(err => {
            updateUIState(false);
            displayOutput(err, true);
        });
}

export function startSimulation() {
    if (state.gameRunning) return;
    const level = getCPULevel();
    
    state.game.reset();
    resetTimers();
    updateUIState(true);
    if (state.board) state.board.destroy();
    initPlayBoard(false);
    
    updateCapturedPieces();

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
            state.game.reset();
            stopTimer();
            resetTimers();
            updateUIState(false);
            toggleChat(false);
            if (state.board) {
                state.board.position('start');
                state.board.orientation(state.preferredColor);
            }
            updateCapturedPieces();
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
    state.lastOpponent = user;
    const color = state.preferredColor;
    sendChallengeRequest(user, color).then(data => {
        if (data) displayOutput(`Challenge sent to ${user} (playing as ${color})... waiting for response.`);
    });
}

export function handleChallenge(challengeData) {
    // Challenge data is "challengerName:challengerColor"
    const [challenger, challengerColor] = challengeData.split(':');
    state.lastOpponent = challenger;
    
    showChallengeNotification(challenger, (c) => {
        acceptChallengeRequest(c, challengerColor).then(res => {
            if (!res.ok) alert("Failed to accept challenge. Maybe it expired?");
        });
    });
}

export function startPvPGame(assignedColor) {
    state.game.reset();
    resetTimers();
    updateUIState(true);
    toggleChat(true);

    state.userColor = (assignedColor === 'white' ? 'w' : 'b');

    if (assignedColor === 'white') {
        state.whitePlayer = state.currentUser || 'User';
        state.blackPlayer = state.lastOpponent || 'Opponent';
    } else {
        state.whitePlayer = state.lastOpponent || 'Opponent';
        state.blackPlayer = state.currentUser || 'User';
    }

    startPvPBoard(assignedColor);
    state.board.orientation(assignedColor);
    updateTimers();
    updateCapturedPieces();
    startTimer();
    displayOutput("PvP Game started! You are " + assignedColor);
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

