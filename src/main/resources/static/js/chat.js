$(document).ready(function () {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    $(document).on('click', '.chat-btn', function (e) {
           e.preventDefault();
           const url = $(this).attr('href');

           $.ajax({
               url: url,
               type: 'POST',
               contentType: 'application/json',
               beforeSend: function(xhr) {
                   xhr.setRequestHeader(header, token);
               },
               success: function(response) {
                   let chat = $('.chat-wrapper');

                   chat.css('display', 'block');
                   chat.stop(true, true)
                         .fadeIn(300);
               },
               error: function(xhr) {
                   console.error("Error opening chat:", xhr);
               }
           });
    });

    $(document).on('click', '.close-chat-btn', function (e) {
               e.preventDefault();

               let chat = $('.chat-wrapper');

               chat.css('display', 'block');
               chat.stop(true, true)
                  .fadeOut(300);
    });
    //insert functions here
});