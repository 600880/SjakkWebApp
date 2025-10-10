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

// ===== ARCHIVE =====
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
                        <td>${game.pgn}</td>
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

// ===== CHESS BOARD LOGIC =====
let board;

function initChessBoard() {
    if (typeof Chessboard === 'undefined') {
        console.error('Chessboard.js not loaded.');
        return;
    }
    const boardElement = document.getElementById('board');
    if (!boardElement) return;

    board = Chessboard('board', {
        draggable: true,
        position: 'start',
        onDrop: handleMove
    });
}

function handleMove(source, target) {
    const move = { from: source, to: target };

    fetch('/spill/move', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(move)
    })
        .then(response => {
            if (!response.ok) throw new Error('Move failed');
            return response.text();
        })
        .then(newFen => {
            board.position(newFen);
        })
        .catch(err => {
            console.error('Error sending move:', err);
            board.position(board.fen());
        });

    return 'snapback';
}

// ===== INITIALIZE =====
window.addEventListener('load', () => {
    // Initialize chessboard if it's present
    if (document.getElementById('board')) {
        initChessBoard();
    }
});
