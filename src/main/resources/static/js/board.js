import { state } from './state.js';
import { sendMoveToServer } from './api.js';

export function initPlayBoard(isDraggable = true) {
    const boardElement = document.getElementById('board');
    if (!boardElement) return;

    state.board = Chessboard('board', {
        draggable: isDraggable,
        position: 'start',
        onDrop: isDraggable ? onDrop : undefined,
        pieceTheme: '/img/chesspieces/wikipedia/{piece}.png'
    });
}

export function initReplayBoard() {
    const replayElement = document.getElementById('replayBoard');
    if (!replayElement) return;

    state.replayChess = new Chess();
    state.replayBoard = Chessboard('replayBoard', {
        draggable: false,
        position: 'start',
        pieceTheme: '/img/chesspieces/wikipedia/{piece}.png'
    });
}

function onDrop(source, target) {
    let move = state.game.move({
        from: source,
        to: target,
        promotion: 'q'
    });

    if (move === null) return 'snapback';

    sendMoveToServer(source, target, state.game, state.board);
}

export function startPvPBoard(orientation) {
    if (state.board) {
        state.board.destroy();
    }
    
    state.board = Chessboard('board', {
        draggable: true,
        position: 'start',
        orientation: orientation,
        onDrop: onDrop,
        pieceTheme: '/img/chesspieces/wikipedia/{piece}.png'
    });
}
