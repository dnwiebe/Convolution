/**
 * Created by dnwiebe on 6/6/16.
 */

describe ('Given a fake HTML page', function () {

    beforeEach (function () {
        $("body").append (
            '<div id="fake-page">\n' +
            '    <div id="enter-name-panel">\n' +
            '        <div><h3>enter-name-panel</h3></div>\n' +
            '        <input id="name-field" name="name-field" type="text"/>\n' +
            '        <input id="enter-name-submit" type="button"/>' +
            '    </div>\n' +
            '    <div id="player-name-panel">\n' +
            '        <div><h3>player-name-panel</h3></div>\n' +
            '        <span id="player-name"></span>\n' +
            '    </div>\n' +
            '    <div id="wait-list-panel">\n' +
            '        <div><h3>wait-list-panel</h3></div>\n' +
            '        <ul id="wait-list">\n' +
            '        </ul>\n' +
            '    </div>\n' +
            '</div>\n'
        );
    });

    afterEach (function () {
        $('#fake-page').remove ();
    });

    describe ('A Vestibule with a mocked websocket', function () {
        var websocket;
        var subject;

        beforeEach(function () {
            websocket = jasmine.createSpyObj('websocket', ['send', 'replaceEvents']);
            spyOn (Utils, 'websocket').and.returnValue (websocket);
            subject = Vestibule ();
        });

        it ('displays the enter-name-panel', function () {
            expect ($('#enter-name-panel').is (':visible')).toBe (true);
        });

        it ('does not display the player-name-panel', function () {
            expect ($('#player-name-panel').is (':visible')).toBe (false);
        });

        it ('does not display the wait-list-panel', function () {
            expect ($('#wait-list-panel').is (':visible')).toBe (false);
        });

        describe ('when a name is filled in', function () {

            beforeEach (function () {
                $('#name-field').val('Billy');
            });

            describe ('and the submit button is clicked', function () {

                beforeEach (function () {
                    $('#enter-name-submit').click ();
                });

                describe ('and the websocket initialization call is analyzed', function () {

                    var args;

                    beforeEach (function () {
                        args = Utils.websocket.calls.argsFor (0);
                    });

                    it ('the correct URL is used', function () {
                        expect (args[0]).toBe ('/vestibule/socket')
                    });

                    describe ('among the event handlers provided', function () {
                        var handlers;

                        beforeEach (function () {
                            handlers = args[1].events;
                        });

                        describe ('the acceptPlayer handler', function () {

                            var acceptPlayer;

                            beforeEach (function () {
                                acceptPlayer = handlers['acceptPlayer'];
                            });

                            describe ('when a good message is received', function () {

                                beforeEach (function () {
                                    acceptPlayer ({id: 345, name: 'Buster', timestamp: 12345678901300});
                                });

                                it ('sets the player attribute properly', function () {
                                    expect (subject.player).toEqual ({id: 345, name: 'Buster', timestamp: 12345678901300});
                                });

                                it ('displays the player name properly', function () {
                                    expect ($('#player-name').text ()).toBe ('Buster');
                                });

                                it ('does not display the enter-name-panel', function () {
                                    expect ($('#enter-name-panel').is (':visible')).toBe (false);
                                });

                                it ('displays the player-name-panel', function () {
                                    expect ($('#player-name-panel').is (':visible')).toBe (true);
                                });

                                it ('does not display the wait-list-panel', function () {
                                    expect ($('#wait-list-panel').is (':visible')).toBe (false);
                                });
                            });
                        });

                        describe ('the waitList handler', function () {

                            var waitList;

                            beforeEach (function () {
                                subject.player = {id: 345};
                                waitList = handlers['waitList']
                            });

                            describe ('when a good multiple-player message is received', function () {

                                beforeEach (function () {
                                    waitList ([
                                        {id: 123, name: 'Tommy', timestamp: 12345678901234},
                                        {id: 234, name: 'Emily', timestamp: 12345678901233},
                                        {id: 345, name: 'Buster', timestamp: 12345678901300}
                                    ]);
                                });

                                var checkLink = function (element, name, id) {
                                    expect (element.tagName).toBe ('LI');
                                    expect (element.getAttribute ('class')).toBe ('wait-list-element');
                                    expect (element.getAttribute ('id')).toBe ('wait-list-' + id);
                                    var link = element.children[0];
                                    expect (link.tagName).toBe ('A');
                                    expect (link.getAttribute ('href')).toBe ('/games/start/345/' + id)
                                    expect (link.innerHTML).toContain (name);
                                };

                                it ('displays the wait list properly', function () {
                                    var waitListDiv = $('#wait-list');
                                    var children = waitListDiv.children ();
                                    checkLink (children.get (0), 'Emily', 234);
                                    checkLink (children.get (1), 'Tommy', 123);
                                    expect (children.length).toBe (2);
                                    expect (waitListDiv.attr ('style')).toBeUndefined ();
                                });

                                it ('does not display the enter-name-panel', function () {
                                    expect ($('#enter-name-panel').is (':visible')).toBe (false);
                                });

                                it ('displays the player-name-panel', function () {
                                    expect ($('#player-name-panel').is (':visible')).toBe (true);
                                });

                                it ('displays the wait-list-panel', function () {
                                    expect ($('#wait-list-panel').is (':visible')).toBe (true);
                                });
                            });

                            describe ('when a good single-player message is received', function () {

                                beforeEach (function () {
                                    waitList ([
                                        {id: 345, name: 'Buster', timestamp: 12345678901300}
                                    ]);
                                });

                                it ('does not display the enter-name-panel', function () {
                                    expect ($('#enter-name-panel').is (':visible')).toBe (false);
                                });

                                it ('displays the player-name-panel', function () {
                                    expect ($('#player-name-panel').is (':visible')).toBe (true);
                                });

                                it ('does not display the wait-list-panel', function () {
                                    expect ($('#wait-list-panel').is (':visible')).toBe (false);
                                });
                            });
                        });

                        describe ('the challenge handler', function () {

                            var challenge;

                            beforeEach (function () {
                                handlers['acceptPlayer'] ({id: 345, name: 'Buster', timestamp: 12345678901300});
                                challenge = handlers['challenge'];
                            });

                            describe ('when a good message is received and accepted', function () {

                                beforeEach (function () {
                                    spyOn (window, 'confirm').and.returnValue (true);
                                    spyOn (subject, 'navigate');
                                    challenge ({id: 123, name: 'Tommy', timestamp: 12345678901300})
                                });

                                it ('navigates away to game', function () {
                                    expect (subject.navigate).toHaveBeenCalledWith ('/games/start/345/123')
                                });
                            });

                            describe ('when a good message is received and accepted', function () {

                                beforeEach (function () {
                                    spyOn (window, 'confirm').and.returnValue (false);
                                    spyOn (subject, 'navigate');
                                    challenge ({id: 123, name: 'Tommy', timestamp: 12345678901300})
                                });

                                it ('does not navigate away to game', function () {
                                    expect (subject.navigate).not.toHaveBeenCalled ();
                                });
                            });
                        });
                    });
                });

                it ('an entry message is sent', function () {
                    var args = websocket['send'].calls.argsFor (0);
                    var type = args[0];
                    var data = args[1];

                    expect (type).toBe ('enterVestibule');
                    expect (data).toEqual ({'name': 'Billy'});
                });
            });
        });
    });
});
