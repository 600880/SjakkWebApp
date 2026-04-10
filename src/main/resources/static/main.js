/* ========================================
   CHESS SIMULATOR - MAIN APPLICATION
   ======================================== */

/* ===== STATE VARIABLES ===== */
let board = null;
let game = new Chess();
let currentGame = null;
let replayChess = null;
let replayBoard = null;
let currentMoveIndex = 0;
let moves = [];
let evtSource = null;
let gameRunning = false;


/* ===== UI / TAB MANAGEMENT ===== */

function switchTab(tabName) {
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
    
    const activeTabElement = Array.from(document.querySelectorAll('.tab'))
        .find(tab => tab.getAttribute('onclick')?.includes(tabName));
    
    if (activeTabElement) {
        activeTabElement.classList.add('active');
    }

    // Load archive if switching to archive tab
    if (tabName === 'archive') {
        loadArchive();
    }
}


/* ===== PLAY / SIMULATION ===== */

function getCPULevel() {
    const selector = document.getElementById('cpuLevel');
    return selector ? selector.value : 3;
}

function updateUIState(running) {
    gameRunning = running;
    const playBtn = document.getElementById('playBtn');
    const simulateBtn = document.getElementById('simulateBtn');
    const resetBtn = document.getElementById('resetBtn');
    const cpuSelector = document.getElementById('cpuLevel');

    if (playBtn) playBtn.disabled = running;
    if (simulateBtn) simulateBtn.disabled = running;
    if (cpuSelector) cpuSelector.disabled = running;
    if (resetBtn) resetBtn.disabled = !running;
}

function startInteractive() {
    if (gameRunning) return;
    
    const level = getCPULevel();
    updateUIState(true);
    
    game.reset();
    if (board) {
        board.destroy();
    }
    initPlayBoard(true); // Draggable
    
    fetch(`/spill?dybde=${level}`)
        .then(res => res.text())
        .then(data => {
            const output = document.getElementById('output');
            if (output) output.innerText = `Interactive Game started (Level ${level})! You are White.`;
        })
        .catch(err => {
            updateUIState(false);
            displaySimulationError(err);
        });
}

function startSimulation() {
    if (gameRunning) return;

    const level = getCPULevel();
    updateUIState(true);

    game.reset();
    if (board) {
        board.destroy();
    }
    initPlayBoard(false); // Not draggable
    
    fetch(`/simulate?dybde=${level}`)
        .then(res => res.text())
        .then(data => {
            const output = document.getElementById('output');
            if (output) output.innerText = `Simulation started (Level ${level})...`;
        })
        .catch(err => {
            updateUIState(false);
            displaySimulationError(err);
        });
}

function resetGame() {
    updateUIState(false);
    game.reset();
    if (board) {
        board.position('start');
        board.destroy();
        initPlayBoard(true);
    }
    const output = document.getElementById('output');
    if (output) output.innerText = 'Choose a mode to start';
}

function runProgram() {
    startInteractive();
}

function handleSimulationResponse(res) {
    // Check for unauthorized access
    if (res.status === 401) {
        window.location.href = '/index';
        return null;
    }
    return res.text();
}

function displaySimulationOutput(data) {
    if (data === null) return;
    
    const output = document.getElementById('output');
    if (output) {
        output.innerText = data;
    }
}

function displaySimulationError(err) {
    const output = document.getElementById('output');
    if (output) {
        output.innerText = 'Error: ' + err;
    }
    console.error('Simulation error:', err);
}


/* ===== ARCHIVE / GAME LIST ===== */

function loadArchive() {
    fetch('/minePartier')
        .then(handleArchiveResponse)
        .then(populateGameList)
        .catch(handleArchiveError);
}

function handleArchiveResponse(res) {
    // Check for unauthorized access
    if (res.status === 401) {
        window.location.href = '/index';
        return null;
    }
    return res.json();
}

function populateGameList(games) {
    if (!games) return;

    const tbody = document.getElementById('archive-list');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (games.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3">No saved games.</td></tr>';
    } else {
        // Sort games newest first
        games.sort((a, b) => Number(b.id) - Number(a.id));

        games.forEach(game => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${game.hvit}</td>
                <td>${game.svart}</td>
                <td><button class="replay-btn" onclick="loadGameReplay(${game.id})">Replay</button></td>
            `;
            tbody.appendChild(row);
        });
    }
}

function handleArchiveError(err) {
    console.error('Error loading archive:', err);
    const tbody = document.getElementById('archive-list');
    if (tbody) {
        tbody.innerHTML = '<tr><td colspan="3">Error loading archive</td></tr>';
    }
}


/* ===== REPLAY / GAME VIEWER ===== */

function loadGameReplay(gameId) {
    fetch(`/partier/${gameId}`)
        .then(res => {
            if (!res.ok) throw new Error('Game not found');
            return res.json();
        })
        .then(game => {
            currentGame = game;
            showReplayView(game);
        })
        .catch(err => {
            console.error('Error loading game:', err);
            alert('Failed to load game');
        });
}

function showReplayView(game) {
    // Switch views
    document.getElementById('gameListView').style.display = 'none';
    document.getElementById('replayView').style.display = 'block';

    // Initialize board
    initReplayBoard();

    // Parse moves from PGN
    parsePGN(game.pgn);

    // Display player names
    document.getElementById('replayPlayers').textContent = `${game.hvit} vs ${game.svart}`;

    // Wait for DOM to be ready before updating
    setTimeout(() => {
        firstMove();
    }, 100);
}

function backToGameList() {
    document.getElementById('replayView').style.display = 'none';
    document.getElementById('gameListView').style.display = 'block';
}


/* ===== REPLAY CONTROLS ===== */

function firstMove() {
    currentMoveIndex = 0;
    updateReplayBoard();
}

function prevMove() {
    if (currentMoveIndex > 0) {
        currentMoveIndex--;
        updateReplayBoard();
    }
}

function nextMove() {
    if (currentMoveIndex < moves.length) {
        currentMoveIndex++;
        updateReplayBoard();
    }
}

function lastMove() {
    currentMoveIndex = moves.length;
    updateReplayBoard();
}

function jumpToMove(moveIndex) {
    currentMoveIndex = moveIndex;
    updateReplayBoard();
}


/* ===== PGN PARSING & BOARD UPDATE ===== */

function parsePGN(pgn) {
    moves = [];
    if (!pgn) return;

    // Split PGN into tokens
    const tokens = pgn.trim().split(/\s+/);

    for (let token of tokens) {
        // Skip move numbers (e.g., "1.", "2.")
        if (token.endsWith('.')) {
            continue;
        }

        // Stop at game result (e.g., "1-0", "1/2")
        if (/^\d/.test(token) && !token.endsWith('.')) {
            break;
        }

        if (token !== '') {
            moves.push(token);
        }
    }
}

function updateReplayBoard() {
    if (!replayChess || !replayBoard) {
        console.error('Replay board not initialized');
        return;
    }

    // Reset board
    replayChess.reset();
    const errorMsg = document.getElementById('errorMsg');
    errorMsg.textContent = '';

    // Apply moves up to current index
    for (let i = 0; i < currentMoveIndex && i < moves.length; i++) {
        const moveResult = applyMove(i);
        
        if (!moveResult.success) {
            errorMsg.textContent = moveResult.error;
            break;
        }
    }

    // Update board display
    replayBoard.position(replayChess.fen());

    // Update move counter
    updateMoveCounter();

    // Update PGN display
    renderPGNDisplay();
}

function applyMove(moveIndex) {
    const moveNotation = moves[moveIndex].trim();

    try {
        // Try parsing with sloppy mode (most lenient)
        let moveObj = replayChess.move(moveNotation, { sloppy: true });

        // If failed, try alternate formats
        if (!moveObj) {
            moveObj = tryAlternateFormats(moveNotation);
        }

        if (moveObj) {
            return { success: true };
        } else {
            return {
                success: false,
                error: `Error at move ${moveIndex + 1}: Invalid notation "${moveNotation}"`
            };
        }
    } catch (e) {
        return {
            success: false,
            error: `Error at move ${moveIndex + 1}: ${e.message}`
        };
    }
}

function tryAlternateFormats(notation) {
    // Try piece notation as destination only (e.g., "Kg2" → "g2")
    if (/^[KQRBN][a-h][1-8]$/.test(notation)) {
        const destination = notation.substring(1);
        let moveObj = replayChess.move(destination, { sloppy: true });

        // Try as capture if destination fails
        if (!moveObj) {
            const captureMove = notation[0] + 'x' + destination;
            moveObj = replayChess.move(captureMove, { sloppy: true });
        }

        return moveObj;
    }

    return null;
}

function updateMoveCounter() {
    const counterEl = document.getElementById('currentMoveNum');
    if (counterEl) {
        counterEl.textContent = `${currentMoveIndex}/${moves.length}`;
    }
}

function renderPGNDisplay() {
    const pgnContainer = document.getElementById('pgnDisplay');
    let html = '';

    // Create move pairs (white, black)
    for (let i = 0; i < moves.length; i += 2) {
        const moveNumber = Math.floor(i / 2) + 1;
        const whiteMove = moves[i];
        const blackMove = moves[i + 1] || null;

        // Build move pair HTML
        html += '<div>';
        html += `<span style="font-weight:bold; margin-right:4px;">${moveNumber}.</span>`;
        html += buildMoveSpan(i, whiteMove);

        if (blackMove) {
            html += ' ';
            html += buildMoveSpan(i + 1, blackMove);
        }

        html += '</div>';
    }

    pgnContainer.innerHTML = html;
}

function buildMoveSpan(index, move) {
    const isPlayed = index < currentMoveIndex - 1;
    const isCurrent = index === currentMoveIndex - 1;
    const isFuture = index >= currentMoveIndex;

    let styles = 'cursor:pointer;';
    
    if (isPlayed) {
        styles += 'color:#666;';
    } else if (isCurrent) {
        styles += 'color:#d32f2f; font-weight:bold;';
    } else if (isFuture) {
        styles += 'color:#999;';
    }

    return `<span style="${styles}" onclick="jumpToMove(${index + 1})">${move}</span>`;
}


/* ===== BOARD INITIALIZATION ===== */

function initReplayBoard() {
    const replayElement = document.getElementById('replayBoard');
    if (!replayElement) {
        console.error('Replay board element not found');
        return;
    }

    // Initialize chess.js instance
    replayChess = new Chess();

    // Initialize chessboard.js instance
    try {
        replayBoard = Chessboard('replayBoard', {
            draggable: false,
            position: 'start',
            pieceTheme: '/img/chesspieces/wikipedia/{piece}.png'
        });
    } catch (e) {
        console.error('Failed to initialize replay board:', e);
    }
}

function onDrop(source, target) {
    // see if the move is legal
    let move = game.move({
        from: source,
        to: target,
        promotion: 'q' // NOTE: always promote to a queen for simplicity
    });

    // illegal move
    if (move === null) return 'snapback';

    // send move to backend
    sendMoveToServer(source, target);
}

function sendMoveToServer(from, to) {
    const formData = new URLSearchParams();
    formData.append('from', from);
    formData.append('to', to);

    fetch('/move', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: formData
    }).then(res => {
        if (!res.ok) {
            alert('Move rejected by server');
            game.undo();
            board.position(game.fen());
        }
    });
}

function initPlayBoard(isDraggable = true) {
    const boardElement = document.getElementById('board');
    if (!boardElement) return;

    if (typeof Chessboard === 'undefined') {
        console.error('Chessboard.js not loaded');
        return;
    }

    board = Chessboard('board', {
        draggable: isDraggable,
        position: 'start',
        onDrop: isDraggable ? onDrop : undefined,
        pieceTheme: '/img/chesspieces/wikipedia/{piece}.png'
    });
}

function initSSE() {
    evtSource = new EventSource('/moves/stream');

    evtSource.onopen = () => {
        console.log('SSE connection established');
        refreshOnlineUsers();
    };

    evtSource.onerror = (err) => {
        console.error('SSE error:', err);
    };

    // Listen for move events
    evtSource.addEventListener('move', (event) => {
        if (board) {
            const moveStr = event.data;
            // Handle move like "e2-e4"
            const parts = moveStr.split('-');
            if (parts.length === 2) {
                game.move({ from: parts[0], to: parts[1], promotion: 'q' });
                board.position(game.fen());
            } else {
                // Fallback for other formats
                board.move(moveStr);
                game.move(moveStr, { sloppy: true });
            }
        }
    });

    evtSource.addEventListener('challenge', (event) => {
        const challenger = event.data;
        showChallengeNotification(challenger);
    });

    evtSource.addEventListener('game_started', (event) => {
        const orientation = event.data;
        startPvPGame(orientation);
    });
}

function showChallengeNotification(challenger) {
    const container = document.getElementById('notification-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerHTML = `
        <div class="toast-header">New Challenge!</div>
        <div class="toast-body"><strong>${challenger}</strong> has challenged you to a game.</div>
        <div class="toast-actions">
            <button class="btn-accept" onclick="acceptChallenge('${challenger}', this)">Accept</button>
            <button class="btn-decline" onclick="this.parentElement.parentElement.remove()">Decline</button>
        </div>
    `;
    container.appendChild(toast);

    // Auto-remove after 20 seconds
    setTimeout(() => {
        if (toast.parentElement) toast.remove();
    }, 20000);
}

function acceptChallenge(challenger, buttonEl) {
    const formData = new URLSearchParams();
    formData.append('opponent', challenger);
    
    fetch('/challenge/accept', {
        method: 'POST',
        body: formData
    }).then(res => {
        if (!res.ok) {
            alert("Failed to accept challenge. Maybe it expired?");
        }
        // Remove the toast
        buttonEl.closest('.toast').remove();
    });
}


/* ===== INITIALIZATION ===== */

window.addEventListener('load', () => {
    // Initialize play board if present
    if (document.getElementById('board')) {
        initPlayBoard();
        initSSE();
        updateUIState(false);
    }
});

function refreshOnlineUsers() {
    fetch('/users/online')
        .then(res => res.json())
        .then(users => {
            const container = document.getElementById('online-users');
            if (!container) return;
            container.innerHTML = '';
            if (users.length === 0) {
                container.innerHTML = '<li>No other users online</li>';
            } else {
                users.forEach(user => {
                    const li = document.createElement('li');
                    li.style.marginBottom = '5px';
                    li.innerHTML = `${user} <button onclick="challenge('${user}')" style="padding:2px 5px; font-size:12px;">Challenge</button>`;
                    container.appendChild(li);
                });
            }
        });
}

function challenge(user) {
    const formData = new URLSearchParams();
    formData.append('opponent', user);
    fetch('/challenge', {
        method: 'POST',
        body: formData
    }).then(res => res.text()).then(data => {
        const output = document.getElementById('output');
        if (output) output.innerText = "Challenge sent to " + user + "... waiting for response.";
    });
}

function startPvPGame(orientation) {
    updateUIState(true);
    game.reset();
    if (board) {
        board.destroy();
    }
    
    board = Chessboard('board', {
        draggable: true,
        position: 'start',
        orientation: orientation,
        onDrop: onDrop,
        pieceTheme: '/img/chesspieces/wikipedia/{piece}.png'
    });
    
    const output = document.getElementById('output');
    if (output) output.innerText = "PvP Game started! You are " + orientation;
}

function askAI() {
    const output = document.getElementById('output');
    if (output) output.innerText = "AI is thinking...";

    fetch('/ask-ai', {
        method: 'POST'
    })
    .then(res => res.text())
    .then(data => {
        if (output) output.innerText = data;
    })
    .catch(err => {
        if (output) output.innerText = "AI Error: " + err;
    });
}
