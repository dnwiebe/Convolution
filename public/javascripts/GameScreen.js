/**
 * Created by dnwiebe on 6/23/16.
 */

var GameScreen = function (playerId, opponentId, webSocketUrl) {
    var self = {};

    $('#wait-panel').show ();
    $('#game-panel').hide ();

    var makeWebSocket = function (opcode, data) {
        console.log ("Connecting WebSocket at " + webSocketUrl);
        var webSocket = Utils.websocket (webSocketUrl, {
            events: {
            },
            open: function () {
                webSocket.send (opcode, data);
            }
        });
        return webSocket;
    };

    self.playerId = playerId;
    self.opponent = opponentId;
    self.websocket = makeWebSocket ("gamePrepare", {'playerId': self.playerId, opponentId: self.opponentId});

    return self;
};