/**
 * Created by dnwiebe on 6/6/16.
 */

var Vestibule = function () {
    var self = {};

    var websocket = Utils.websocket ('/vestibule/socket', {
        events: {
            'waitList': function (players) {
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
            },
            'acceptPlayer': function (player) {
                self.player = player;
                $('#player-name').text (player.name);
                $('#enter-name-panel').hide ();
                $('#player-name-panel').show ();
            },
            'challenge': function (player) {
                if (window.confirm (player.name + ' has challenged you to a game of Convolution.')) {
                    self.navigate('/games/start/' + self.player.id + '/' + player.id);
                }
            }
        }
    });

    $('#enter-name-panel').show ();
    $('#player-name-panel').hide ();
    $('#wait-list-panel').hide ();

    $('#enter-name-submit').click (function (eventObject) {
        var name = $('#name-field').val ();
        websocket.send ("enterVestibule", {'name': name});
    });

    self.navigate = function (url) {
        window.location = url;
    };

    return self;
};
