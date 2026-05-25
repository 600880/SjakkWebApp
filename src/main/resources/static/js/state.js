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
    gameRunning: false,
    userColor: 'w', // Default to white for PvE
    whitePlayer: 'User',
    blackPlayer: 'CPU',
    currentUser: null,
    lastOpponent: null,
    timers: {
        w: 300,
        b: 300
    },
    timerIncrement: 0,
    timerInterval: null,
    specialEffectsEnabled: false,
    preferredColor: 'white'
};
