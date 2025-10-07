<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Chess Simulator</title>
<style>
/* ===== General Styling ===== */
body {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    background-color: #f0f5f0;
    margin: 0;
    padding: 0;
    color: #333;
}

/* ===== Header ===== */
header {
    background-color: #2c6e49;
    color: #fff;
    padding: 20px 30px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

header h1 {
    margin: 0;
    font-size: 28px;
}

header button {
    padding: 10px 20px;
    border: none;
    background-color: #3aa76d;
    color: white;
    border-radius: 5px;
    cursor: pointer;
    font-size: 16px;
    transition: 0.2s;
}

header button:hover {
    background-color: #2c6e49;
}

/* ===== Tabs ===== */
.tabs {
    display: flex;
    background-color: #fff;
    border-bottom: 2px solid #cce6cc;
}

.tab {
    flex: 1;
    text-align: center;
    padding: 15px;
    cursor: pointer;
    font-weight: bold;
    border-bottom: 3px solid transparent;
    transition: 0.2s;
    font-size: 16px;
}

.tab.active {
    border-bottom: 3px solid #3aa76d;
    background-color: #e6f2e6;
}

/* ===== Tab Content ===== */
.tab-content {
    padding: 20px 30px;
    background-color: #fff;
}

/* ===== Simulate Button ===== */
.simulate-button {
    padding: 12px 25px;
    font-size: 16px;
    background-color: #3aa76d;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    margin-bottom: 20px;
    transition: 0.2s;
}

.simulate-button:hover {
    background-color: #2c6e49;
}

/* ===== Output Box ===== */
#output {
    background-color: #f4faf4;
    border: 1px solid #cce6cc;
    padding: 15px;
    border-radius: 6px;
    min-height: 60px;
    white-space: pre-wrap;
}

/* ===== Archive Table ===== */
table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 15px;
}

th, td {
    padding: 10px;
    border: 1px solid #cce6cc;
    text-align: left;
    vertical-align: top;
}

th {
    background-color: #3aa76d;
    color: white;
}

td {
    background-color: #f4faf4;
    word-wrap: break-word;
}

/* Scrollable table if PGN is long */
#archive-list-wrapper {
    max-height: 400px;
    overflow-y: auto;
    border: 1px solid #cce6cc;
    border-radius: 6px;
}
</style>
</head>
<body>

<header>
    <h1>Chess Simulator</h1>
    <form id="logoutForm" action="/logout" method="post" style="display:none;"></form>
	<button onclick="document.getElementById('logoutForm').submit()">Log Out</button>
</header>

<div class="tabs">
    <div class="tab active" onclick="switchTab('play')">Play</div>
    <div class="tab" onclick="switchTab('archive')">Archive</div>
</div>

<div id="play" class="tab-content">
    <button class="simulate-button" onclick="runProgram()">Simulate</button>
    <div id="output"></div>
</div>

<div id="archive" class="tab-content" style="display:none;">
    <h3>Your saved games:</h3>
    <div id="archive-list-wrapper">
        <table>
            <thead>
                <tr>
                    <th>White</th>
                    <th>Black</th>
                    <th>PGN</th>
                </tr>
            </thead>
            <tbody id="archive-list">
                <!-- Games will be dynamically loaded here -->
            </tbody>
        </table>
    </div>
</div>

<script>
// Tab switching
function switchTab(tabName) {
    document.querySelectorAll('.tab-content').forEach(tc => tc.style.display = 'none');
    document.getElementById(tabName).style.display = 'block';

    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    const activeTab = Array.from(document.querySelectorAll('.tab'))
        .find(t => t.getAttribute('onclick')?.includes(tabName));
    if (activeTab) activeTab.classList.add('active');

    if(tabName === 'archive') loadArchive();
}

// Fetch simulation result
function runProgram() {
    fetch('/spill', { method: 'GET' })
        .then(response => response.text())
        .then(data => {
            document.getElementById('output').innerText = data;
        })
        .catch(err => {
            document.getElementById('output').innerText = 'Error: ' + err;
        });
}

// Load user's saved games
function loadArchive() {
    fetch('/minePartier', { method: 'GET' })
        .then(response => response.json())
        .then(data => {
            const tbody = document.getElementById('archive-list');
            tbody.innerHTML = '';

            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="3">No saved games.</td></tr>';
            } else {
                data.forEach(game => {
                    const tr = document.createElement('tr');

                    const tdHvit = document.createElement('td');
                    tdHvit.innerText = game.hvit;

                    const tdSvart = document.createElement('td');
                    tdSvart.innerText = game.svart;

                    const tdPgn = document.createElement('td');
                    tdPgn.innerText = game.pgn;

                    tr.appendChild(tdHvit);
                    tr.appendChild(tdSvart);
                    tr.appendChild(tdPgn);

                    tbody.appendChild(tr);
                });
            }
        })
        .catch(err => {
            const tbody = document.getElementById('archive-list');
            tbody.innerHTML = '<tr><td colspan="3">Error loading archive</td></tr>';
            console.error('Error fetching archive:', err);
        });
}
</script>

</body>
</html>