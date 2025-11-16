$(document).ready(function () {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    $('.wishlist-btn').on('click', function(e) {
        e.preventDefault();

        const gameId = $(this).data('game-id');

        $.ajax({
            url: '/dashboard/games/wishlist',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ id: gameId }),
            beforeSend: function(xhr) {
               xhr.setRequestHeader(header, token);
            },
            success: function (response) {
               if (response.wishlisted) {
                   $('.wishlist-add[data-game-id="' + gameId + '"]').addClass('hidden');
                   $('.wishlist-remove[data-game-id="' + gameId + '"]').removeClass('hidden');
               } else {
                   $('.wishlist-remove[data-game-id="' + gameId + '"]').addClass('hidden');
                   $('.wishlist-add[data-game-id="' + gameId + '"]').removeClass('hidden');
               }
            },
            error: function(xhr) {
                 console.log("There was a problem with adding game to wishlist", xhr);
            }
        });
    });

    $('#buy-btn').on('click', function(e) {
        e.preventDefault();

        const gameId = $(this).data('game-id');

        $.ajax({
            url: '/dashboard/games/buy',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ id: gameId }),
            beforeSend: function(xhr) {
               xhr.setRequestHeader(header, token);
            },
            success: function (response) {
                if(response.status == 'success') {
                    window.location.href = '/dashboard/games';
                } else {
                    let msgBox = $('.event-message');

                    $('html, body').animate({ scrollTop: 0 }, 400);
                    msgBox.css('display', 'block');
                    msgBox.stop(true, true)
                        .text(response.message)
                        .fadeIn(300)
                        .delay(3000)
                        .fadeOut(400);
                }
            },
            error: function(xhr) {
                 console.log("There was a problem buying the game", xhr);
            }
        });
    });

});