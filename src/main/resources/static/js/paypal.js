
  fetch('/paypal/api/config')
    .then(res => res.json())
    .then(config => {
        const token = $("meta[name='_csrf']").attr("content");
        const header = $("meta[name='_csrf_header']").attr("content");

      const script = document.createElement('script');
      script.src = `https://www.paypal.com/sdk/js?client-id=${config.clientId}&currency=EUR`;

      script.onload = function () {

      const paypalButtons = window.paypal.Buttons({
         style: {
              shape: "pill",
              layout: "vertical",
              color: "blue",
              label: "pay",
          },
         message: {
              amount: 100,
         },
         async createOrder() {
              try {
                  const token = document.querySelector("meta[name='_csrf']").content;
                  const header = document.querySelector("meta[name='_csrf_header']").content;

                  const priceAmount = $('#amount').val().trim();
                  const response = await fetch("/paypal/api/orders", {
                      method: "POST",
                      headers: {
                          "Content-Type": "application/json",
                          [header]: token
                      },
                      // use the "body" param to optionally pass additional order information
                      // like product ids and quantities
                      body: JSON.stringify({
                          amount: priceAmount,
                      }),
                  });

                  const orderData = await response.json();

                  if (orderData.id) {
                      return orderData.id;
                  }
                  const errorDetail = orderData?.details?.[0];
                  const errorMessage = orderData.error || (
                      errorDetail
                          ? `${errorDetail.issue} ${errorDetail.description} (${orderData.debug_id})`
                          : "Unexpected payment error"
                  );

                   throw new Error(errorMessage);
              } catch (error) {
                resultMessage(
                  `Sorry, your transaction could not be processed...<br><br>${error}`
                );

                throw error
              }
         },
         async onApprove(data, actions) {
              try {
                  const token = document.querySelector("meta[name='_csrf']").content;
                  const header = document.querySelector("meta[name='_csrf_header']").content;

                  const response = await fetch(
                      `/paypal/api/orders/${data.orderID}/capture`,
                      {
                          method: "POST",
                          headers: {
                              "Content-Type": "application/json",
                              [header]: token
                          },
                      }
                  );

                  const orderData = await response.json();
                  const errorDetail = orderData?.details?.[0];

                  if (errorDetail?.issue === "INSTRUMENT_DECLINED") {
                      // (1) Recoverable INSTRUMENT_DECLINED -> call actions.restart()
                      // https://developer.paypal.com/docs/checkout/standard/customize/handle-funding-failures/
                      return actions.restart();
                  } else if (errorDetail) {
                      // (2) Other non-recoverable errors -> Show a failure message
                      throw new Error(
                          `${errorDetail.description} (${orderData.debug_id})`
                      );
                  } else if (!orderData.purchase_units) {
                      throw new Error(JSON.stringify(orderData));
                  } else {
                      // (3) Successful transaction -> Show confirmation or thank you message
                      const amount = orderData.purchase_units[0].payments.captures[0].amount.value;

                      fetch("/dashboard/wallet/add_ajax", {
                          method: "POST",
                          headers: {
                              "Content-Type": "application/json",
                              "X-CSRF-TOKEN": document.querySelector('meta[name="_csrf"]').content
                          },
                          body: JSON.stringify({
                              amount: amount
                          })
                      })
                      .then(res => {
                          if (!res.ok) throw new Error("Failed to add funds");
                          return res.text();
                      })
                      .then(() => {
                          window.location.href = "/dashboard/wallet";
                      })
                      .catch(err => {
                          console.error(err);
                          resultMessage("Payment captured but wallet update failed.");
                      });
                  }
              } catch (error) {
                  resultMessage(
                      `Sorry, your transaction could not be processed...<br><br>${error}`
                  );
              }
          },

         onError: (err) => {
              // redirect to your specific error page
              //window.location.assign("/your-error-page-here");
            console.error(err);
            resultMessage(
                `Sorry, your transaction could not be processed...<br><br>${err}`
            );
          },
         onCancel: (data) => {
              // Show a cancel page or return to cart
              //window.location.assign("/your-error-page-here");
              resultMessage(
                  `PayPal transaction cancelled`
              );
          },


      });
      paypalButtons.render("#paypal-button-container");
    }

      document.head.appendChild(script);
});

function resultMessage(message) {
    const element = $('#result-message');
    const readTime = Math.max(2000, message.length * 40);

    element.stop(true, true)
      .removeClass('d-none')
      .hide()
      .html(message)
      .fadeIn(400)
      .delay(readTime)
      .fadeOut(400, function () {
          $(this).addClass('d-none');
      });
}