$(document).ready(function () {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    $(document).on('click', '.delete-images', function (e) {
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
               let deleteBtn = $('.delete-images');

               msgBox.css('display', 'block');
               deleteBtn.hide();

               msgBox.stop(true, true)
                     .text(response.message)
                     .fadeIn(300)
                     .delay(3000)
                     .fadeOut(400, function () {
                         deleteBtn.fadeIn(200);
                     });
           },
           error: function(xhr) {
               console.error("Error in deleting unused game images:", xhr);
           }
       });
    });
});