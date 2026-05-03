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
                        renderMessage(msg, response.userId);
                    });
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
                $('#userId').val('');

               chat.css('display', 'block');
               chat.stop(true, true)
                  .fadeOut(300);
    });

    $(document).on('click', '.send-message-btn', function (e) {
        e.preventDefault();

        const receiverId = $('#userId').val();
        const messageInput = $('.message-field');
        const message = messageInput.val().trim();

        if (!message) return;

        stompClient.send("/app/chat", {}, JSON.stringify({
            senderId: currentUserId,
            receiverId: receiverId,
            message: message
        }));

        messageInput.val('');
    });

    function renderMessage(msg, currentUserId) {
       const messagesList = $('.messages-list');

       let isSender = msg.sender === currentUserId;
       let formattedTime = formatDate(msg.createdOn);

       let messageHtml = `
           <div class="message ${isSender ? 'sent' : 'received'}">
               <div class="message-content" title="${formattedTime}">
                   <p>${msg.message}</p>
               </div>
           </div>
       `;

       messagesList.append(messageHtml);
       messagesList.scrollTop(messagesList[0].scrollHeight);
    }

     let socket = new SockJS('/chat');
     let stompClient = Stomp.over(socket);

     stompClient.connect({}, function () {

        const userId = $('#currentUserId').val();
        stompClient.subscribe('/queue/messages/' + userId, function (message) {
             let msg = JSON.parse(message.body);

             renderMessage(msg, userId);
        });

     });
    //insert functions here
});