/**
 * Created by dnwiebe on 6/23/16.
 */

describe ('Given a fake HTML page', function () {

    beforeEach (function () {
        $("body").append (
            '<div id="fake-page">\n' +
            '    <div id="game-panel">\n' +
            '        <div id="winner-panel">\n' +
            '        <span id="winner-message"></span>\n' +
            '        </div>\n' +
            '        <div id="scores">\n' +
            '            <span id="horizontal-name" class="player-name"></span>\n' +
            '            <span id="horizontal-score" class="score"></span>\n' +
            '            <span id="vertical-name" class="player-name"></span>\n' +
            '            <span id="vertical-score" class="score"></span>\n' +
            '        </div>\n' +
            '        <table>\n' +
            '            <tbody id="game-board">\n' +
            '            </tbody>\n' +
            '        </table>\n' +
            '    </div>\n' +
            '    <div id="wait-panel" class="instructions">\n' +
            '        Waiting for opponent @himPlayer.name\n' +
            '    </div>\n' +
            '</div>\n'
        );
    });

    afterEach (function () {
        $('#fake-page').remove ();
    });

    describe ('A GameScreen with a mocked websocket', function () {
        var websocket;
        var subject;
        var args;

        beforeEach(function () {
            websocket = jasmine.createSpyObj('websocket', ['send', 'replaceEvents']);
            spyOn(Utils, 'websocket').and.returnValue(websocket);
            subject = GameScreen(123, 234, 'ws://nowhere.com');
            args = Utils.websocket.calls.argsFor (0);
        });

        it ('the subject has the correct IDs and names', function () {
            checkSubject (subject, {
                playerId: 123,
                playerName: null,
                opponentId: 234,
                opponentName: null
            });
        });

        describe ('the screen', function () {
            checkScreen ({
                waitPanel: {},
                gamePanel: null,
                winnerPanel: null
            });
        });

        it ('uses the correct URL to create the websocket', function () {
            expect (args[0]).toBe ('ws://nowhere.com')
        });

        describe ('among the event handlers provided', function () {
            var handlers;

            beforeEach(function () {
                handlers = args[1].events;
            });

            describe('the rejectGame handler when called', function () {

                beforeEach (function () {
                    spyOn (window, 'alert');
                    spyOn (Utils, 'navigateTo');
                    handlers['rejectGame'] ({player: {id: 234, name: 'Emily', timestamp: 1234}});
                });

                it ('displays the proper rejection message', function () {
                    expect (window.alert).toHaveBeenCalledWith ('Emily is no longer waiting to play Convolution with you');
                });

                it ('navigates back to the vestibule', function () {
                    expect (Utils.navigateTo).toHaveBeenCalledWith ('/vestibule/index');
                });
            });

            describe ('the startGame handler when called for the horizontal player', function () {

                var cellAt = function (column, row) {
                    return $('#cell-' + column + '-' + row);
                };

                beforeEach (function () {
                    handlers['startGame'] ({
                        board: {
                            'order': 2,
                            'horizontalScore': 321,
                            'verticalScore': 432,
                            'horizontalIsWinner': false,
                            'horizontalIsNext': true,
                            'isGameOver': false,
                            'fixedCoordinate': 0,
                            'data': [
                                [1, 2],
                                [3, null]
                            ]
                        },
                        isHorizontal: true,
                        player: {id: 123, name: 'Fernando', timestmap: 999},
                        opponent: {id: 234, name: 'Emily', timestamp: 1234}
                    });
                });

                it ('the subject has the correct IDs and names', function () {
                    checkSubject (subject, {
                        playerId: 123,
                        playerName: 'Fernando',
                        opponentId: 234,
                        opponentName: 'Emily'
                    });
                });

                describe ('the screen', function () {
                    checkScreen ({
                        waitPanel: null,
                        gamePanel: {
                            horizontalName: 'Fernando',
                            horizontalScore: '321',
                            verticalName: 'Emily',
                            verticalScore: '432',
                            board: [
                                [
                                    {value: '1', click: {playerId: 123, coordinate: 0}},
                                    {value: '2', click: {playerId: 123, coordinate: 1}}
                                ],
                                [
                                    {value: '3', click: null},
                                    {value: null, click: null}
                                ]
                            ]
                        },
                        winnerPanel: null
                    });
                });

                describe ('The turn handler', function () {
                    describe ('given a regular board', function () {

                        beforeEach(function () {
                            handlers['turn']({
                                board: {
                                    'order': 2,
                                    'horizontalScore': 543,
                                    'verticalScore': 654,
                                    'horizontalIsWinner': false,
                                    'horizontalIsNext': false,
                                    'isGameOver': false,
                                    'fixedCoordinate': 1,
                                    'data': [
                                        [32, null],
                                        [23, 54]
                                    ]
                                },
                                yourTurn: false
                            });
                        });

                        it ('the subject has the correct IDs and names', function () {
                            checkSubject (subject, {
                                playerId: 123,
                                playerName: 'Fernando',
                                opponentId: 234,
                                opponentName: 'Emily'
                            });
                        });

                        describe ('the screen', function () {
                            checkScreen ({
                                waitPanel: null,
                                gamePanel: {
                                    horizontalName: 'Fernando',
                                    horizontalScore: '543',
                                    verticalName: 'Emily',
                                    verticalScore: '654',
                                    board: [
                                        [
                                            {value: '32', click: null},
                                            {value: null, click: null}
                                        ],
                                        [
                                            {value: '23', click: null},
                                            {value: '54', click: null}
                                        ]
                                    ]
                                },
                                winnerPanel: null
                            });
                        });
                    });
                    describe ('given a horizontal win', function () {

                        beforeEach(function () {
                            handlers['turn']({
                                board: {
                                    'order': 2,
                                    'horizontalScore': 543,
                                    'verticalScore': 654,
                                    'horizontalIsWinner': true,
                                    'horizontalIsNext': null,
                                    'isGameOver': true,
                                    'fixedCoordinate': null,
                                    'data': [
                                        [1, 2],
                                        [3, 4]
                                    ]
                                },
                                yourTurn: true
                            });
                        });

                        it ('the subject has the correct IDs and names', function () {
                            checkSubject (subject, {
                                playerId: 123,
                                playerName: 'Fernando',
                                opponentId: 234,
                                opponentName: 'Emily'
                            });
                        });

                        describe ('the screen', function () {
                            checkScreen ({
                                waitPanel: null,
                                gamePanel: {
                                    horizontalName: 'Fernando',
                                    horizontalScore: '543',
                                    verticalName: 'Emily',
                                    verticalScore: '654',
                                    board: [
                                        [
                                            {value: '1', click: null},
                                            {value: '2', click: null}
                                        ],
                                        [
                                            {value: '3', click: null},
                                            {value: '4', click: null}
                                        ]
                                    ]
                                },
                                winnerPanel: {winnerMessage: 'Fernando Wins!'}
                            });
                        });
                    });

                    describe ('given a vertical win', function () {

                        beforeEach(function () {
                            handlers['turn']({
                                board: {
                                    'order': 2,
                                    'horizontalScore': 543,
                                    'verticalScore': 654,
                                    'horizontalIsWinner': false,
                                    'horizontalIsNext': null,
                                    'isGameOver': true,
                                    'fixedCoordinate': null,
                                    'data': [
                                        [1, 2],
                                        [3, 4]
                                    ]
                                },
                                yourTurn: false
                            });
                        });

                        it ('the subject has the correct IDs and names', function () {
                            checkSubject (subject, {
                                playerId: 123,
                                playerName: 'Fernando',
                                opponentId: 234,
                                opponentName: 'Emily'
                            });
                        });

                        describe ('the screen', function () {
                            checkScreen ({
                                waitPanel: null,
                                gamePanel: {
                                    horizontalName: 'Fernando',
                                    horizontalScore: '543',
                                    verticalName: 'Emily',
                                    verticalScore: '654',
                                    board: [
                                        [
                                            {value: '1', click: null},
                                            {value: '2', click: null}
                                        ],
                                        [
                                            {value: '3', click: null},
                                            {value: '4', click: null}
                                        ]
                                    ]
                                },
                                winnerPanel: {winnerMessage: 'Emily Wins!'}
                            });
                        });
                    });

                    describe ('given a tie', function () {

                        beforeEach(function () {
                            handlers['turn']({
                                board: {
                                    'order': 2,
                                    'horizontalScore': 543,
                                    'verticalScore': 654,
                                    'horizontalIsWinner': null,
                                    'horizontalIsNext': null,
                                    'isGameOver': true,
                                    'fixedCoordinate': null,
                                    'data': [
                                        [1, 2],
                                        [3, 4]
                                    ]
                                },
                                yourTurn: true
                            });
                        });

                        it ('the subject has the correct IDs and names', function () {
                            checkSubject (subject, {
                                playerId: 123,
                                playerName: 'Fernando',
                                opponentId: 234,
                                opponentName: 'Emily'
                            });
                        });

                        describe ('the screen', function () {
                            checkScreen ({
                                waitPanel: null,
                                gamePanel: {
                                    horizontalName: 'Fernando',
                                    horizontalScore: '543',
                                    verticalName: 'Emily',
                                    verticalScore: '654',
                                    board: [
                                        [
                                            {value: '1', click: null},
                                            {value: '2', click: null}
                                        ],
                                        [
                                            {value: '3', click: null},
                                            {value: '4', click: null}
                                        ]
                                    ]
                                },
                                winnerPanel: {winnerMessage: 'Fernando and Emily Tie!'}
                            });
                        });
                    });
                });
            });

            describe ('the startGame handler when called for the vertical player', function () {

                beforeEach (function () {
                    handlers['startGame'] ({
                        board: {
                            'order': 2,
                            'horizontalScore': 321,
                            'verticalScore': 432,
                            'horizontalIsWinner': false,
                            'horizontalIsNext': true,
                            'isGameOver': false,
                            'fixedCoordinate': 0,
                            'data': [
                                [1, 2],
                                [3, null]
                            ]
                        },
                        isHorizontal: false,
                        opponent: {id: 123, name: 'Fernando', timestmap: 999},
                        player: {id: 234, name: 'Emily', timestamp: 1234}
                    });
                });

                it ('the subject has the correct IDs and names', function () {
                    checkSubject (subject, {
                        playerId: 234,
                        playerName: 'Emily',
                        opponentId: 123,
                        opponentName: 'Fernando'
                    });
                });

                describe ('the screen', function () {
                    checkScreen ({
                        waitPanel: null,
                        gamePanel: {
                            horizontalName: 'Fernando',
                            horizontalScore: '321',
                            verticalName: 'Emily',
                            verticalScore: '432',
                            board: [
                                [
                                    {value: '1', click: null},
                                    {value: '2', click: null}
                                ],
                                [
                                    {value: '3', click: null},
                                    {value: null, click: null}
                                ]
                            ]
                        },
                        winnerPanel: null
                    });
                });

                describe ('the turn handler, given a regular board', function () {

                    beforeEach (function () {
                        handlers['turn'] ({
                            board: {
                                'order': 2,
                                'horizontalScore': 543,
                                'verticalScore': 654,
                                'horizontalIsWinner': false,
                                'horizontalIsNext': false,
                                'isGameOver': false,
                                'fixedCoordinate': 1,
                                'data': [
                                    [32, null],
                                    [23, 54]
                                ]
                            },
                            yourTurn: true
                        });
                    });

                    it ('the subject has the correct IDs and names', function () {
                        checkSubject (subject, {
                            playerId: 234,
                            playerName: 'Emily',
                            opponentId: 123,
                            opponentName: 'Fernando'
                        });
                    });

                    describe ('the screen', function () {
                        checkScreen ({
                            waitPanel: null,
                            gamePanel: {
                                horizontalName: 'Fernando',
                                horizontalScore: '543',
                                verticalName: 'Emily',
                                verticalScore: '654',
                                board: [
                                    [
                                        {value: '32', click: null},
                                        {value: null, click: {playerId: 234, coordinate: 0}}
                                    ],
                                    [
                                        {value: '23', click: null},
                                        {value: '54', click: {playerId: 234, coordinate: 1}}
                                    ]
                                ]
                            },
                            winnerPanel: null
                        });
                    });
                });
            });
        });
    });

    function checkSubject (subject, expected) {
        checkIdAndName (subject, 'player', expected);
        checkIdAndName (subject, 'opponent', expected);
    }

    function checkScreen (expected) {
        checkWaitingPanel (expected.waitPanel);
        checkGamePanel (expected.gamePanel);
        checkWinnerPanel (expected.winnerPanel);
    }

    function checkIdAndName (subject, character, expected) {
        expect (subject[character + 'Id']).toBe (expected[character + 'Id']);
        expect (subject[character + 'Name']).toBe (expected[character + 'Name']);
    }

    function checkWaitingPanel (expectedPanel) {
        if (expectedPanel == null) {
            it('does not display the wait-panel', function () {
                expect($('#wait-panel').is(':visible')).toBe(false);
            });
        }
        else {
            it('displays the wait-panel', function () {
                expect($('#wait-panel').is(':visible')).toBe(true);
            });
        }
    }

    function checkGamePanel (expectedPanel) {
        if (expectedPanel == null) {
            it('does not display the game-panel', function () {
                expect($('#game-panel').is(':visible')).toBe(false);
            });
        }
        else {
            it('displays the game-panel', function () {
                expect($('#game-panel').is(':visible')).toBe(true);
            });

            var checkNameAndScore = function (direction) {
                it ('sets the ' + direction + ' name properly', function () {
                    expect ($('#' + direction + '-name').html ()).toBe (expectedPanel[direction + 'Name']);
                });

                it ('sets the ' + direction + ' score properly', function () {
                    expect ($('#' + direction + '-score').html ()).toBe (expectedPanel[direction + 'Score']);
                });
            };

            checkNameAndScore ('horizontal');
            checkNameAndScore ('vertical');
            checkBoard (expectedPanel.board);
        }
    }

    function checkBoard (board) {
        var cellAt = function (column, row) {
            return $('#cell-' + column + '-' + row);
        };

        for (var row = 0; row < board.length; row++) {
            for (var column = 0; column < row.length; column++) {
                checkCellValue (board[row][column].value, cellAt (column, row), column, row);
                checkClickHandler (board[row][column].click, cellAt (column, row), column, row);
            }
        }
    }

    function checkCellValue(expectedValue, cell, column, row) {
        if (expectedValue == null) {
            it ('cell at (' + column + ', ' + row + ') should be empty', function () {
                expect(cell.prop('tagName')).toBe('IMG');
            });
        }
        else {
            it ('cell at (' + column + ', ' + row + ') should contain ' + expectedValue, function () {
                expect(cell.html()).toBe(expectedValue);
            });
        }
    }

    function checkClickHandler (expectedHandler, cell, column, row) {
        describe ('cell at (' + column + ', ' + row + ')', function () {

            beforeEach (function () {
                cell.click ();
            });

            if (expectedHandler == null) {
                it('should not respond to clicks', function () {
                    expect(subject.websocket.send).not.toHaveBeenCalled();
                });
            }
            else {
                it('should send playerId ' + expectedHandler.playerId + ', coordinate ' + expectedHandler.coordinate +
                    ' when clicked', function () {
                    expect (subject.websocket.send).toHaveBeenCalledWith ('acceptMove',
                        {playerId: expectedHandler.playerId, coordinate: expectedHandler.coordinate});
                });
            }
        });
    }

    function checkWinnerPanel (expectedPanel) {
        if (expectedPanel == null) {
            it('does not display the winner-panel', function () {
                expect($('#winner-panel').is(':visible')).toBe(false);
            });
        }
        else {
            it ('declares the game end: "' + expectedPanel.winnerMessage + '"', function () {
                expect ($('#winner-message').html ()).toBe (expectedPanel.winnerMessage);
            });
        }
    }
});
