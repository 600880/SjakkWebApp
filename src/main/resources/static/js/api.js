export async function fetchWithAuth(url, options = {}) {
    const res = await fetch(url, options);
    if (res.status === 401) {
        window.location.href = '/index';
        return null;
    }
    return res;
}

export async function fetchText(url, options = {}) {
    const res = await fetchWithAuth(url, options);
    if (!res) return null;
    return res.text();
}

export async function fetchJson(url, options = {}) {
    const res = await fetchWithAuth(url, options);
    if (!res) return null;
    return res.json();
}

export function sendMoveToServer(from, to, game, board) {
    const formData = new URLSearchParams();
    formData.append('from', from);
    formData.append('to', to);

    return fetch('/move', {
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
            return false;
        }
        return true;
    });
}

export function abortGameRequest() {
    return fetch('/abort', { method: 'POST' });
}

export function sendChallengeRequest(user) {
    const formData = new URLSearchParams();
    formData.append('opponent', user);
    return fetch('/challenge', {
        method: 'POST',
        body: formData
    }).then(res => res.text());
}

export function acceptChallengeRequest(challenger) {
    const formData = new URLSearchParams();
    formData.append('opponent', challenger);
    
    return fetch('/challenge/accept', {
        method: 'POST',
        body: formData
    });
}
