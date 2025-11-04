$(document).ready(function () {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    // Trigger AJAX when any checkbox changes
    $('.filter-box input[type="checkbox"]').on('change', function() {
        filterGames();
    });

    function filterGames() {
        // Collect selected values
        const categories = $('input[name="categories"]:checked')
           .map(function() { return $(this).val(); })
           .get();

        const companies = $('input[name="companies"]:checked')
           .map(function() { return $(this).val(); })
           .get();

        const filterData = {
            categories: categories,
            companies: companies
        };

        $.ajax({
           url: '/dashboard/games/filter',
           type: 'POST',
           contentType: 'application/json',
           data: JSON.stringify(filterData),
           beforeSend: function(xhr) {
               xhr.setRequestHeader(header, token);
           },
           success: function(response) {
               $('.list-box').html(response);
           },
           error: function(xhr) {
               console.error("Error loading filtered games:", xhr);
                   }
        });
    }
});