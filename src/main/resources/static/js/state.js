/* ===== STATE VARIABLES ===== */
export const state = {
    board: null,
    game: new Chess(),
    currentGame: null,
    replayChess: null,
    replayBoard: null,
    currentMoveIndex: 0,
    moves: [],
    evtSource: null,
    gameRunning: false
};
