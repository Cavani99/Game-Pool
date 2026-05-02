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
                   let messagesList = $('.messages-list');
                   const userId = response.userId;

                   chat.css('display', 'block');
                   chat.stop(true, true)
                         .fadeIn(300);

                   messagesList.html('');
                   response.chat.forEach(msg => {
                       let isSender = msg.sender === userId;
                       let formattedTime = formatDate(msg.createdOn);

                       let messageHtml = `
                           <div class="message ${isSender ? 'sent' : 'received'}">
                               <div class="message-content" title="${formattedTime}">
                                   <p>${msg.message}</p>
                               </div>
                           </div>
                       `;

                       messagesList.append(messageHtml);
                   });
                   messagesList.scrollTop(messagesList[0].scrollHeight);
               },
               error: function(xhr) {
                   console.error("Error opening chat:", xhr);
               }
           });
    });

    function formatDate(dateString) {
        const date = new Date(dateString);

        const pad = n => n.toString().padStart(2, '0');

        return `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())} `
             + `${pad(date.getDate())}/${pad(date.getMonth() + 1)}/${date.getFullYear()}`;
    }

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