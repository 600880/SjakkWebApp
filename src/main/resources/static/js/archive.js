import { state } from './state.js';
import { fetchJson } from './api.js';
import { initReplayBoard } from './board.js';

export function loadArchive() {
    fetchJson('/minePartier')
        .then(populateGameList)
        .catch(err => console.error('Error loading archive:', err));
}

function populateGameList(games) {
    if (!games) return;
    const tbody = document.getElementById('archive-list');
    if (!tbody) return;

    tbody.innerHTML = '';
    if (games.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3">No saved games.</td></tr>';
    } else {
        games.sort((a, b) => Number(b.id) - Number(a.id));
        games.forEach(game => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${game.hvit}</td>
                <td>${game.svart}</td>
                <td><button class="replay-btn" data-id="${game.id}">Replay</button></td>
            `;
            tbody.appendChild(row);
        });
        
        // Add listeners to buttons instead of onclick
        document.querySelectorAll('.replay-btn').forEach(btn => {
            btn.addEventListener('click', () => loadGameReplay(btn.dataset.id));
        });
    }
}

export function loadGameReplay(gameId) {
    fetchJson(`/partier/${gameId}`)
        .then(game => {
            state.currentGame = game;
            showReplayView(game);
        })
        .catch(err => alert('Failed to load game'));
}

function showReplayView(game) {
    document.getElementById('gameListView').style.display = 'none';
    document.getElementById('replayView').style.display = 'block';

    initReplayBoard();
    parsePGN(game.pgn);
    document.getElementById('replayPlayers').textContent = `${game.hvit} vs ${game.svart}`;

    setTimeout(() => firstMove(), 100);
}

export function backToGameList() {
    document.getElementById('replayView').style.display = 'none';
    document.getElementById('gameListView').style.display = 'block';
}

function parsePGN(pgn) {
    state.moves = [];
    if (!pgn) return;
    const tokens = pgn.trim().split(/\s+/);
    for (let token of tokens) {
        if (token.endsWith('.')) continue;
        if (/^\d/.test(token) && !token.endsWith('.')) break;
        if (token !== '') state.moves.push(token);
    }
}

export function updateReplayBoard() {
    if (!state.replayChess || !state.replayBoard) return;
    state.replayChess.reset();
    document.getElementById('errorMsg').textContent = '';

    for (let i = 0; i < state.currentMoveIndex && i < state.moves.length; i++) {
        const move = state.moves[i].trim();
        const moveObj = state.replayChess.move(move, { sloppy: true }) || tryAlternateFormats(move);
        if (!moveObj) {
            document.getElementById('errorMsg').textContent = `Error at move ${i + 1}: ${move}`;
            break;
        }
    }
    state.replayBoard.position(state.replayChess.fen());
    document.getElementById('currentMoveNum').textContent = `${state.currentMoveIndex}/${state.moves.length}`;
    renderPGNDisplay();
}

function tryAlternateFormats(notation) {
    if (/^[KQRBN][a-h][1-8]$/.test(notation)) {
        const destination = notation.substring(1);
        return state.replayChess.move(destination, { sloppy: true }) || 
               state.replayChess.move(notation[0] + 'x' + destination, { sloppy: true });
    }
    return null;
}

function renderPGNDisplay() {
    const pgnContainer = document.getElementById('pgnDisplay');
    let html = '';
    for (let i = 0; i < state.moves.length; i += 2) {
        const moveNumber = Math.floor(i / 2) + 1;
        html += `<div><span style="font-weight:bold; margin-right:4px;">${moveNumber}.</span>`;
        html += buildMoveSpan(i, state.moves[i]);
        if (state.moves[i + 1]) html += ' ' + buildMoveSpan(i + 1, state.moves[i + 1]);
        html += '</div>';
    }
    pgnContainer.innerHTML = html;
    
    // Add event listeners for jump-to-move
    pgnContainer.querySelectorAll('.move-link').forEach(link => {
        link.addEventListener('click', () => {
            state.currentMoveIndex = parseInt(link.dataset.index) + 1;
            updateReplayBoard();
        });
    });
}

function buildMoveSpan(index, move) {
    const isPlayed = index < state.currentMoveIndex - 1;
    const isCurrent = index === state.currentMoveIndex - 1;
    let color = isPlayed ? '#666' : (isCurrent ? '#d32f2f' : '#999');
    let weight = isCurrent ? 'bold' : 'normal';
    return `<span class="move-link" data-index="${index}" style="cursor:pointer; color:${color}; font-weight:${weight}">${move}</span>`;
}

export function firstMove() { state.currentMoveIndex = 0; updateReplayBoard(); }
export function prevMove() { if (state.currentMoveIndex > 0) { state.currentMoveIndex--; updateReplayBoard(); } }
export function nextMove() { if (state.currentMoveIndex < state.moves.length) { state.currentMoveIndex++; updateReplayBoard(); } }
export function lastMove() { state.currentMoveIndex = state.moves.length; updateReplayBoard(); }
