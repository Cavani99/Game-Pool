$(document).ready(function () {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    $(document).on('click', '.open-chat-btn', function (e) {
           e.preventDefault();

           const url = $(this).attr('href');
           const userId = $(this).data('user-id');

           $('#userId').val(userId);

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

     $(document).on('click', '.send-message-btn', function (e) {
                e.preventDefault();

                const userId = $('#userId').val();
                const url = `/dashboard/notifications/sent/${userId}`;
                const message = $('.message-field').val().trim();

                $.ajax({
                     url: url,
                     type: 'POST',
                     contentType: 'application/json',
                     data: JSON.stringify({ message: message }),
                     beforeSend: function(xhr) {
                         xhr.setRequestHeader(header, token);
                     },
                     success: function(response) {
                        let messageInput = $('.message-field');
                        messageInput.val('');
                     },
                     error: function(xhr) {
                         console.error("Error sending message:", xhr);
                     }
                });
     });
    //insert functions here
});