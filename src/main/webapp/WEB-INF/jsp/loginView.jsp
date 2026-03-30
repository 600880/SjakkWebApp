<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Chess Simulator</title>
	<link rel="stylesheet" href="${pageContext.request.contextPath}/main.css">	
	<!-- Chessboard.js -->
	<link rel="stylesheet" href="https://unpkg.com/@chrisoakman/chessboardjs@1.0.0/dist/chessboard-1.0.0.min.css" integrity="sha384-q94+BZtLrkL1/ohfjR8c6L+A6qzNH9R2hBLwyoAfu3i/WCvQjzL2RQJ3uNHDISdU" crossorigin="anonymous">
	<script src="https://code.jquery.com/jquery-3.5.1.min.js" integrity="sha384-ZvpUoO/+PpLXR1lu4jmpXWu80pZlYUAfxl5NsBMWOEPSjUn/6Z/hRTt8+pR6L4N2" crossorigin="anonymous"></script>
	<script src="https://unpkg.com/@chrisoakman/chessboardjs@1.0.0/dist/chessboard-1.0.0.min.js" integrity="sha384-8Vi8VHwn3vjQ9eUHUxex3JSN/NFqUg3QbPyX8kWyb93+8AC/pPWTzj+nHtbC5bxD" crossorigin="anonymous"></script>
	<!-- Chess.js for move validation and board state -->
	<script src="https://cdnjs.cloudflare.com/ajax/libs/chess.js/0.10.3/chess.min.js"></script>
	<!-- JavaScript -->
	<script src="${pageContext.request.contextPath}/main.js" defer></script>
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
    <div id="board"></div>
</div>

<div id="archive" class="tab-content" style="display:none;">
    <h3>Your saved games:</h3>
    
    <!-- Game List View -->
    <div id="gameListView">
        <div id="archive-list-wrapper">
            <table>
                <thead>
                    <tr>
                        <th>White</th>
                        <th>Black</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody id="archive-list"></tbody>
            </table>
        </div>
    </div>
    
    <!-- Game Replay View -->
    <div id="replayView" style="display:none;">
        <button onclick="backToGameList()">← Back to Games</button>
        <h3><span id="replayPlayers"></span></h3>
        
        <div style="display:flex; gap:30px; align-items:flex-start;">
            <!-- Board on the left -->
            <div>
                <div id="replayBoard"></div>
                <div style="margin-top:15px;">
                    <button onclick="firstMove()">⏮ Start</button>
                    <button onclick="prevMove()">◀ Previous</button>
                    <button onclick="nextMove()">Next ▶</button>
                    <button onclick="lastMove()">End ⏭</button>
                </div>
                <div style="margin-top:10px; font-size:12px; color:#666;">
                    <strong>Move: </strong><span id="currentMoveNum">0/0</span>
                </div>
            </div>
            
            <!-- PGN on the right -->
            <div style="flex:1; display:flex; flex-direction:column;">
                <div id="pgnDisplay" style="font-family:monospace; line-height:1.6; padding:10px; background:#f5f5f5; border-radius:4px; max-width:400px; max-height:450px; overflow-y:auto; overflow-x:hidden; border:1px solid #ccc; flex:1;"></div>
                <div id="errorMsg" style="color:red; margin-top:10px;"></div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
