import { state } from './state.js';
import { refreshOnlineUsers, handleChallenge, startPvPGame, checkGameOver, applyIncrement } from './game.js';
import { appendChatMessage } from './ui.js';
import { playMoveSound } from './effects.js';

export function initSSE() {
    state.evtSource = new EventSource('/moves/stream');

    state.evtSource.onopen = () => {
        console.log('SSE connection established');
        refreshOnlineUsers();
    };

    state.evtSource.onerror = (err) => {
        console.error('SSE error:', err);
    };

    state.evtSource.addEventListener('move', (event) => {
        if (state.board) {
            const moveStr = event.data;
            const parts = moveStr.split('-');
            let move = null;
            if (parts.length === 2) {
                move = state.game.move({ from: parts[0], to: parts[1], promotion: 'q' });
                state.board.position(state.game.fen());
            } else {
                state.board.move(moveStr);
                move = state.game.move(moveStr, { sloppy: true });
            }
            if (move) {
                playMoveSound(!!move.captured);
            }
            if (!checkGameOver()) {
                applyIncrement();
            }
        }
    });

    state.evtSource.addEventListener('challenge', (event) => {
        handleChallenge(event.data);
    });

    state.evtSource.addEventListener('game_started', (event) => {
        startPvPGame(event.data);
    });

    state.evtSource.addEventListener('chat', (event) => {
        appendChatMessage(event.data);
    });
}
