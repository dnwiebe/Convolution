/**
 * Created by dnwiebe on 6/6/16.
 */

var Vestibule = function (webSocketUrl) {
    var self = {};

    var handleWaitList = function (players) {
        $('#enter-name-panel').hide ();
        $('#player-name-panel').show ();
        if (players.length > 1) {
            players.sort(function (a, b) {
                return a.timestamp - b.timestamp;
            });
            var list = $("#wait-list");
            list.empty();
            players.filter(function (player) {
                return (player.id != self.player.id);
            }).forEach(function (player) {
                var html =
                    '<li id="wait-list-' + player.id + '" class="wait-list-element">\n' +
                    '    <a href="/games/start/' + self.player.id + '/' + player.id + '">\n' +
                    '        ' + player.name + '\n' +
                    '    </a>\n' +
                    '</li>\n';
                list.append(html);
            });
            $('#wait-list-panel').show();
        }
        else {
            $('#wait-list-panel').hide();
        }
    };

    var acceptPlayer = function (player) {
        self.player = player;
        $('#player-name').text (player.name);
        $('#enter-name-panel').hide ();
        $('#player-name-panel').show ();
    };

    var challenge = function (player) {
        if (window.confirm (player.name + ' has challenged you to a game of Convolution.')) {
            self.navigate('/games/start/' + self.player.id + '/' + player.id);
        }
    };

    var makeWebSocket = function (opcode, data) {
        console.log ("Connecting WebSocket at " + webSocketUrl);
        var webSocket = Utils.websocket (webSocketUrl, {
            events: {
                'waitList': handleWaitList,
                'acceptPlayer': acceptPlayer,
                'challenge': challenge
            },
            open: function () {
                webSocket.send (opcode, data);
            }
        });
        return webSocket;
    };

    $('#enter-name-panel').show ();
    $('#player-name-panel').hide ();
    $('#wait-list-panel').hide ();

    $('#enter-name-submit').click (function (eventObject) {
        var name = $('#name-field').val ();
        self.websocket = makeWebSocket ("enterVestibule", {'name': name});
    });

    self.webSocket = null;

    self.navigate = function (url) {
        window.location = url;
    };

    return self;
};
