'use strict';
let app = angular.module('poker', ['ngStomp']);
app.controller('controller', function ($scope, $stomp, $log, $http) {
    const $ctrl = this;
    $ctrl.getCardUrl = getCardUrl;
    $scope.displayWinner = false;

// Получение параметра 'username' из URL
    const urlParams = new URLSearchParams(window.location.search);
    $scope.username = urlParams.get('username'); // Это вернет значение параметра 'username'
    $scope.player = urlParams.get('username'); // Это вернет значение параметра 'username'

// Теперь можно использовать $scope.username в вашем контроллере
    console.log($scope.username); // Например, вывести в консоль



    // Функция для подключения WebSocket
    const connectToWebSocket = function () {
        $stomp.connect( 'http://localhost:8080/socket')
            .then(function (frame) {
                var url = '/poker/' + $scope.player;
                var subscription = $stomp.subscribe(url, function (payload, headers, res) {
                    $scope.currentGameData = payload;
                    $scope.currentGameData.personalCards = $scope.currentGameData.personalCards.filter(x => x !== null);
                    refreshBet($scope.currentGameData.turnNotification);
                    console.log("Player:", $scope.player);
                    console.log("Pot:", $scope.currentGameData?.pot);

                    if ($scope.currentGameData.winner != null && $scope.displayWinner === false) {
                        $scope.displayWinner = true;
                        // show winner
                        $('#winnerText').text($scope.currentGameData.winnerInfo);
                        $('#winnerModal').modal('show');
                    } else {
                        $scope.displayWinner = false;
                    }
                    $scope.$apply();
                });

                var errorSubscription = $stomp.subscribe(url + "/error", function (payload, headers, res) {
                    $('#errorText').text(payload);
                    $('#errorModal').modal('show');
                });
            })
            .catch(function (error) {
                // Обработка ошибок при подключении
                console.error('WebSocket connection failed: ', error);
            });
    };

    // Вызываем подключение WebSocket сразу при загрузке страницы
    connectToWebSocket();

    const refreshBet = function (tn) {
        if (tn == undefined){
            return;
        }
        console.log($ctrl.raiseBet + " " + $ctrl.bet);
        if (tn.requiredBet > 0) {
            $ctrl.bet = tn.requiredBet;
        } else {
            $ctrl.bet = tn.minimumBet;
        }
        if ($ctrl.raiseBet < $ctrl.bet || $ctrl.raiseBet == undefined) {
            $ctrl.raiseBet = $ctrl.bet;
        }
    };

    $scope.submitTurn = function (action) {
        let bet = 0;
        if (action === "CALL") {
            bet=$ctrl.bet
        } else if (action === "BET" || action === "RAISE") {
            bet=Number($ctrl.raiseBet)
        }
        $stomp.send('/app/sendTurn', {
            message: {
                "action": action,
                "betAmount": bet,
                "player": $scope.player
            }
        });
        $ctrl.raiseBet = undefined;
    };

    $stomp.setDebug(function (args) {
        $log.debug(args)
    });
    $('#loginWarning').hide();
    $('#usernameSelect').modal('show');
});

function loadCSS() {
    let head = document.getElementsByTagName("HEAD")[0];
    let cssURL = URL.createObjectURL(document.getElementById("css-file").files[0]);
    let link = document.createElement("link");
    link.rel = "stylesheet";
    link.type = "text/css";
    link.href = cssURL;
    head.appendChild(link);
}

function getCardUrl(rank, suit) {
    var rankMap = new Map();
    rankMap.set("two", 2);
    rankMap.set("three", 3);
    rankMap.set("four", 4);
    rankMap.set("five", 5);
    rankMap.set("six", 6);
    rankMap.set("seven", 7);
    rankMap.set("eight", 8);
    rankMap.set("nine", 9);
    rankMap.set("ten", 10);
    rankMap.set("jack", "J");
    rankMap.set("queen", "Q");
    rankMap.set("king", "K");
    rankMap.set("ace", "A");

    var suitMap = new Map();
    suitMap.set("hearts", "h");
    suitMap.set("clubs", "c");
    suitMap.set("spades", "s");
    suitMap.set("diamonds", "d");

    return "resources/cards/" + rankMap.get(rank) + suitMap.get(suit) + ".png";
};
