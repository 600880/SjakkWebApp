import { state } from './state.js';

export function switchTab(tabName) {
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
    
    // Find tab by data-tab instead of onclick
    const activeTabElement = document.querySelector(`.tab[data-tab="${tabName}"]`);
    if (activeTabElement) {
        activeTabElement.classList.add('active');
    }
}

export function updateUIState(running) {
    state.gameRunning = running;
    const playBtn = document.getElementById('playBtn');
    const simulateBtn = document.getElementById('simulateBtn');
    const resetBtn = document.getElementById('resetBtn');
    const cpuSelector = document.getElementById('cpuLevel');
    const askAiBtn = document.getElementById('askAiBtn');

    if (playBtn) playBtn.disabled = running;
    if (simulateBtn) simulateBtn.disabled = running;
    if (cpuSelector) cpuSelector.disabled = running;
    if (resetBtn) resetBtn.disabled = !running;
    if (askAiBtn) askAiBtn.disabled = !running;
}

export function displayOutput(msg, isError = false) {
    const output = document.getElementById('output');
    if (output) {
        output.innerText = (isError ? 'Error: ' : '') + msg;
    }
}

export function showChallengeNotification(challenger, onAccept) {
    const container = document.getElementById('notification-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerHTML = `
        <div class="toast-header">New Challenge!</div>
        <div class="toast-body"><strong>${challenger}</strong> has challenged you to a game.</div>
        <div class="toast-actions">
            <button class="btn-accept">Accept</button>
            <button class="btn-decline">Decline</button>
        </div>
    `;
    
    toast.querySelector('.btn-accept').addEventListener('click', () => {
        onAccept(challenger);
        toast.remove();
    });
    
    toast.querySelector('.btn-decline').addEventListener('click', () => {
        toast.remove();
    });

    container.appendChild(toast);

    // Auto-remove after 20 seconds
    setTimeout(() => {
        if (toast.parentElement) toast.remove();
    }, 20000);
}
