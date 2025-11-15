$(document).ready(function () {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    // Trigger AJAX when any checkbox changes
    $('#search').on('input', function () {
        searchUsers();
    });

    $(document).on('click', '.send-btn', function (e) {
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
               let msgBox = $('.event-message');

               msgBox.css('display', 'block');
               msgBox.stop(true, true)
                     .text(response.message)
                     .fadeIn(300)
                     .delay(3000)
                     .fadeOut(400);

               $('#search').val('');
               searchUsers();
           },
           error: function(xhr) {
               console.error("Error sending friend request:", xhr);
           }
       });
    });

    function searchUsers() {
       const searchText = $('#search').val().trim();

       if (searchText.length >= 2) {
            $.ajax({
               url: '/dashboard/friends/search_users',
               type: 'POST',
               contentType: 'application/json',
               data: JSON.stringify({ search: searchText }),
               beforeSend: function(xhr) {
                   xhr.setRequestHeader(header, token);
               },
               success: function(response) {
                   $('.result-search').html(response);
               },
               error: function(xhr) {
                   console.error("Error in searching for users:", xhr);
               }
            });
       } else {
            $('.result-search').html('');
       }
    }
});