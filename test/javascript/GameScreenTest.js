/**
 * Created by dnwiebe on 6/23/16.
 */

describe ('Given a fake HTML page', function () {

    beforeEach (function () {
        $("body").append (
            '<div id="fake-page">\n' +
            '    <div id="game-panel">\n' +
            '        <div id="scores">\n' +
            '            <span id="horizontal-score" class="score"></span>\n' +
            '            <span id="vertical-score" class="score"></span>\n' +
            '        </div>\n' +
            '        <table>\n' +
            '            <tbody>\n' +
            '            </tbody>\n' +
            '        </table>\n' +
            '    </div>\n' +
            '    <div id="waiting-panel" class="instructions">\n' +
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

        it ('has the correct player ID', function () {
            expect (subject.playerId).toBe (123);
        });

        it ('has no opponent ID', function () {
            expect (subject.opponentId).toBeUndefined ();
        });

        it ('displays the waiting-panel', function () {
            expect ($('#waiting-panel').is (':visible')).toBe (true);
        });

        it ('does not display the game-panel', function () {
            expect ($('#game-panel').is (':visible')).toBe (false);
        });

        it ('the correct URL is used to create the websocket', function () {
            expect (args[0]).toBe ('ws://nowhere.com')
        });

        describe ('among the event handlers provided', function () {
            var handlers;

            beforeEach(function () {
                handlers = args[1].events;
            });

            describe('the something handler', function () {

            });
        });
    });
});
