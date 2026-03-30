/* ========================================
   CHESS SIMULATOR - MAIN APPLICATION
   ======================================== */

/* ===== STATE VARIABLES ===== */
let board = null;
let currentGame = null;
let replayChess = null;
let replayBoard = null;
let currentMoveIndex = 0;
let moves = [];
let evtSource = null;


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

function runProgram() {
    // Reset board to starting position
    if (board) {
        board.position('start');
    }

    // Fetch simulation result from server
    fetch('/spill')
        .then(handleSimulationResponse)
        .then(displaySimulationOutput)
        .catch(displaySimulationError);
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

function initPlayBoard() {
    const boardElement = document.getElementById('board');
    if (!boardElement) return;

    if (typeof Chessboard === 'undefined') {
        console.error('Chessboard.js not loaded');
        return;
    }

    board = Chessboard('board', {
        draggable: false,
        position: 'start',
        pieceTheme: '/img/chesspieces/wikipedia/{piece}.png'
    });
}

function initSSE() {
    evtSource = new EventSource('/moves/stream');

    evtSource.onopen = () => {
        console.log('SSE connection established');
    };

    evtSource.onerror = (err) => {
        console.error('SSE error:', err);
    };

    // Listen for move events
    evtSource.addEventListener('move', (event) => {
        if (board) {
            board.move(event.data);
        }
    });
}


/* ===== INITIALIZATION ===== */

window.addEventListener('load', () => {
    // Initialize play board if present
    if (document.getElementById('board')) {
        initPlayBoard();
        initSSE();
    }
});
