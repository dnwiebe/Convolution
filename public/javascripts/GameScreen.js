/**
 * Created by dnwiebe on 6/23/16.
 */

var GameScreen = function (playerId, opponentId, webSocketUrl) {
    var self = {};

    $('#wait-panel').show ();
    $('#game-panel').hide ();

    var rejectGame = function (data) {
        window.alert (data.player.name + ' is no longer waiting to play Convolution with you');
        Utils.navigateTo ('/vestibule/index');
    };
    
    var startGame = function (data) {

        $('#game-panel').show ();
        $('#winner-panel').hide ();
        $('#wait-panel').hide ();
        self.playerId = data.player.id;
        self.playerName = data.player.name;
        self.opponentId = data.opponent.id;
        self.opponentName = data.opponent.name;
        self.isHorizontal = data.isHorizontal;
        var horizontalName, verticalName;
        if (data.isHorizontal) {
            horizontalName = data.player.name;
            verticalName = data.opponent.name;
        }
        else {
            horizontalName = data.opponent.name;
            verticalName = data.player.name;
        }
        $('#horizontal-name').html (horizontalName);
        $('#vertical-name').html (verticalName);
        var tbody = $('#game-board');
        var html = '';
        for (var row = 0; row < data.board.order; row++) {
            html += '<tr>\n';
            for (var column = 0; column < data.board.order; column++) {
                html += '<td id="' + wrapId (column, row) + '"></td>';
            }
            html += '</tr>\n';
        }
        tbody.html (html);
        populateBoard (data.board, self.isHorizontal);
    };

    var turn = function (data) {
        var yourTurn = data.yourTurn && !data.board.isGameOver
        populateBoard (data.board, yourTurn);
        if (data.board.isGameOver) {declareWinner (data.board.horizontalIsWinner);}
    };
    
    var populateBoard = function (board, yourTurn) {
        var cellId = function (column, row) {
            return 'cell-' + column + '-' + row;
        };
        var cellClass = function (column, row) {
            if ((board.horizontalIsNext == true) && (row == board.fixedCoordinate)) {return 'hfixed';}
            if ((board.horizontalIsNext == false) && (column == board.fixedCoordinate)) {return 'vfixed';}
            return 'unselected';
        };

        $('#horizontal-score').html (board.horizontalScore);
        $('#vertical-score').html (board.verticalScore);
        for (var row = 0; row < board.order; row++) {
            for (var column = 0; column < board.order; column++) {
                var screenElement = $('#' + wrapId (column, row));
                screenElement.html ('');
                var boardElement = board.data[row][column];
                if (boardElement == null) {
                    screenElement.append ('<img id="' + cellId (column, row) + '" class="empty ' + cellClass (column, row) +
                        '" src="/assets/images/emptycell.png" height="30px" width="30px" alt="Empty cell">');
                }
                else {
                    screenElement.append ('<div id="' + cellId (column, row) + '" class="' + cellClass (column, row) +
                        '">' + boardElement + '</div>');
                }
                $('#' + cellId (column, row)).click (function () {})
            }
        }
        if (yourTurn) {
            for (var coord = 0; coord < board.order; coord++) {
                var column = (board.horizontalIsNext) ? coord : board.fixedCoordinate;
                var row = (board.horizontalIsNext) ? board.fixedCoordinate : coord;
                let constCoord = coord;
                $('#' + cellId(column, row)).click(function () {
                    cellClicked(constCoord);
                })
            }
        }
    };

    var declareWinner = function (horizontalIsWinner) {
        var horizontalName = self.isHorizontal ? self.playerName : self.opponentName;
        var verticalName = self.isHorizontal ? self.opponentName : self.playerName;
        $('#winner-panel').show ();
        if (horizontalIsWinner == null) {
            $('#winner-message').html (horizontalName + ' and ' + verticalName + ' Tie!');
        }
        else {
            var winnerName = horizontalIsWinner ? horizontalName : verticalName;
            $('#winner-message').html (winnerName + ' Wins!');
        }
    };

    var wrapId = function (column, row) {
        return 'wrap-' + column + '-' + row;
    };

    var cellClicked = function (coord) {
        self.websocket.send ('acceptMove', {playerId: self.playerId, coordinate: coord})
    };

    var makeWebSocket = function () {
        console.log ("Connecting WebSocket at " + webSocketUrl);
        return Utils.websocket (webSocketUrl, {
            events: {
                'rejectGame': rejectGame,
                'startGame': startGame,
                'turn': turn
            }
        });
    };

    self.playerId = playerId;
    self.playerName = null;
    self.opponentId = opponentId;
    self.opponentName = null;
    self.isHorizontal = null;
    self.websocket = makeWebSocket ();

    return self;
};
