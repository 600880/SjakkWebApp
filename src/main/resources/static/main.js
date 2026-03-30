// ===== TAB SWITCHING =====
function switchTab(tabName) {
    document.querySelectorAll('.tab-content').forEach(tc => tc.style.display = 'none');
    const target = document.getElementById(tabName);
    if (target) target.style.display = 'block';

    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    const activeTab = Array.from(document.querySelectorAll('.tab'))
        .find(t => t.getAttribute('onclick')?.includes(tabName));
    if (activeTab) activeTab.classList.add('active');

    if (tabName === 'archive') loadArchive();
}

// ===== SIMULATION =====
function runProgram() {
	board.position('start');
    fetch('/spill')
        .then(res => {
            if (res.status === 401) {
                window.location.href = '/index';
                return null;
            }
            return res.text();
        })
        .then(data => {
            if (data !== null) {
                const output = document.getElementById('output');
                if (output) output.innerText = data;
            }
        })
        .catch(err => {
            const output = document.getElementById('output');
            if (output) output.innerText = 'Error: ' + err;
        });
}

// ===== ARCHIVE & REPLAY =====
let currentGame = null;
let replayChess = null;
let replayBoard = null;
let currentMoveIndex = 0;
let moves = [];

function loadArchive() {
    fetch('/minePartier')
        .then(res => {
            if (res.status === 401) {
                window.location.href = '/index';
                return null;
            }
            return res.json();
        })
        .then(data => {
            if (!data) return;
            const tbody = document.getElementById('archive-list');
            if (!tbody) return;
            tbody.innerHTML = '';

            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="3">No saved games.</td></tr>';
            } else {
                data.forEach(game => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td>${game.hvit}</td>
                        <td>${game.svart}</td>
                        <td><button onclick="loadGameReplay(${game.id})">Replay</button></td>
                    `;
                    tbody.appendChild(tr);
                });
            }
        })
        .catch(err => {
            console.error('Error fetching archive:', err);
            const tbody = document.getElementById('archive-list');
            if (tbody)
                tbody.innerHTML = '<tr><td colspan="3">Error loading archive</td></tr>';
        });
}

function loadGameReplay(gameId) {
    fetch(`/partier/${gameId}`)
        .then(res => {
            if (!res.ok) throw new Error('Game not found');
            return res.json();
        })
        .then(game => {
            currentGame = game;
            document.getElementById('gameListView').style.display = 'none';
            document.getElementById('replayView').style.display = 'block';
            
            // Initialize board first
            initReplayBoard();
            
            // Then parse and display the game
            parsePGN(game.pgn);
            document.getElementById('replayPlayers').textContent = `${game.hvit} vs ${game.svart}`;
            
            // Use setTimeout to ensure DOM is ready
            setTimeout(() => {
                firstMove();
            }, 100);
        })
        .catch(err => {
            console.error('Error loading game:', err);
            alert('Failed to load game');
        });
}

function backToGameList() {
    document.getElementById('replayView').style.display = 'none';
    document.getElementById('gameListView').style.display = 'block';
}

function initReplayBoard() {
    const replayBoardEl = document.getElementById('replayBoard');
    if (!replayBoardEl) {
        console.error('replayBoard element not found');
        return;
    }
    
    replayChess = new Chess();
    
    // Ensure element has proper size before creating board
    replayBoardEl.style.width = '400px';
    replayBoardEl.style.height = '400px';
    
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

function parsePGN(pgn) {
    moves = [];
    if (!pgn) return;
    
    // Split PGN by spaces
    const tokens = pgn.trim().split(/\s+/);
    for (let token of tokens) {
        // Skip move numbers (end with ".")
        if (token.endsWith('.')) {
            continue;
        }
        // Stop when we hit game result (starts with digit but doesn't end with ".", like "1/2" or "1-0")
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
        console.error('replayChess or replayBoard not initialized');
        return;
    }
    
    replayChess.reset();
    const errorMsg = document.getElementById('errorMsg');
    errorMsg.textContent = '';
    
    // Replay moves up to currentMoveIndex
    for (let i = 0; i < currentMoveIndex && i < moves.length; i++) {
        try {
            const moveNotation = moves[i].trim();
            
            // Try sloppy parsing first (most forgiving)
            let moveObj = replayChess.move(moveNotation, { sloppy: true });
            
            // If it fails and looks like piece notation (Kg2, Nf3, etc), try extracting just the destination
            if (!moveObj && /^[KQRBN][a-h][1-8]$/.test(moveNotation)) {
                const destination = moveNotation.substring(1); // "Kg2" → "g2"
                console.log('Move ' + (i+1) + ': First attempt failed with "' + moveNotation + '", trying coordinate "' + destination + '"');
                moveObj = replayChess.move(destination, { sloppy: true });
                
                // If still failing, try as a capture
                if (!moveObj) {
                    const captureMove = moveNotation[0] + 'x' + destination; // "Kg7" → "Kxg7"
                    console.log('Move ' + (i+1) + ': Coordinate failed, trying capture "' + captureMove + '"');
                    moveObj = replayChess.move(captureMove, { sloppy: true });
                }
            }
            
            if (moveObj) {
                console.log('Move ' + (i+1) + ': ' + moveNotation + ' → ' + moveObj.san);
            } else {
                console.error('Failed to parse move ' + (i+1) + ': ' + moveNotation);
                console.log('Valid moves at this position:', replayChess.moves({ verbose: true }).map(m => m.san).join(', '));
                errorMsg.textContent = 'Error at move ' + (i+1) + ': Invalid move notation "' + moves[i] + '"';
                break;
            }
        } catch (e) {
            console.error('Exception applying move ' + (i+1) + ' (' + moves[i] + '):', e);
            errorMsg.textContent = 'Error at move ' + (i+1) + ': ' + e.message;
            break;
        }
    }
    
    // Update board position
    replayBoard.position(replayChess.fen());
    
    // Update display
    const moveNum = currentMoveIndex;
    document.getElementById('currentMoveNum').textContent = `${moveNum}/${moves.length}`;
    
    // Format PGN with move numbers and two moves per line, clickable
    let pgnHtml = '';
    for (let i = 0; i < moves.length; i += 2) {
        const moveNumber = Math.floor(i / 2) + 1;
        const whiteMove = moves[i];
        const blackMove = moves[i + 1];
        
        let whiteStyle = 'cursor:pointer; color:black;';
        let blackStyle = 'cursor:pointer; color:black;';
        
        // Color code based on current position
        if (i < currentMoveIndex - 1) {
            whiteStyle += ' color:green;';
        } else if (i === currentMoveIndex - 1) {
            whiteStyle += ' color:red; font-weight:bold;';
        } else {
            whiteStyle += ' color:gray;';
        }
        
        if (blackMove) {
            if (i + 1 < currentMoveIndex - 1) {
                blackStyle += ' color:green;';
            } else if (i + 1 === currentMoveIndex - 1) {
                blackStyle += ' color:red; font-weight:bold;';
            } else {
                blackStyle += ' color:gray;';
            }
        }
        
        pgnHtml += '<div style="margin-bottom:5px;">';
        pgnHtml += '<span style="font-weight:bold; margin-right:5px;">' + moveNumber + '.</span>';
        pgnHtml += '<span style="' + whiteStyle + '" onclick="jumpToMove(' + (i+1) + ')">' + whiteMove + '</span>';
        
        if (blackMove) {
            pgnHtml += ' <span style="' + blackStyle + '" onclick="jumpToMove(' + (i+2) + ')">' + blackMove + '</span>';
        }
        
        pgnHtml += '</div>';
    }
    
    document.getElementById('pgnDisplay').innerHTML = pgnHtml;
}

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


let board;

function initChessBoard() {
    if (typeof Chessboard === 'undefined') {
        console.error('Chessboard.js not loaded.');
        return;
    }
    const boardElement = document.getElementById('board');
    if (!boardElement) return;

    board = Chessboard('board', {
        draggable: false,
        position: 'start',
        pieceTheme: '/img/chesspieces/wikipedia/{piece}.png',
        onDrop: handleMove
    });
}

function handleMove(move) {
    //board.move(move);
}

// ===== INITIALIZE =====
window.addEventListener('load', () => {
    // Initialize chessboard if it's present
    if (document.getElementById('board')) {
        initChessBoard();
    }
});


// ===== SSE =====
const evtSource = new EventSource("/moves/stream");
evtSource.onopen = () => console.log("SSE connected");
evtSource.onerror = (err) => console.error("SSE error:", err);

// Listen for named event "move"
evtSource.addEventListener("move", (event) => {
    board.move(event.data); // e.g., "e2-e4"
});
