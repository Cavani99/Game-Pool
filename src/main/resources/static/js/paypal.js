
  fetch('/paypal/api/config')
    .then(res => res.json())
    .then(config => {

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
                  const priceAmount = $('#amount').val().trim();

                  const response = await fetch("/paypal/api/orders", {
                      method: "POST",
                      headers: {
                          "Content-Type": "application/json",
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
                  const errorMessage = errorDetail
                      ? `${errorDetail.issue} ${errorDetail.description} (${orderData.debug_id})`
                      : JSON.stringify(orderData);

                  throw new Error(errorMessage);
              } catch (error) {
                  console.error(error);
                  // resultMessage(`Could not initiate PayPal Checkout...<br><br>${error}`);
              }
          },
         async onApprove(data, actions) {
              try {
                  const response = await fetch(
                      `/paypal/api/orders/${data.orderID}/capture`,
                      {
                          method: "POST",
                          headers: {
                              "Content-Type": "application/json",
                          },
                      }
                  );

                  const orderData = await response.json();
                  // Three cases to handle:
                  //   (1) Recoverable INSTRUMENT_DECLINED -> call actions.restart()
                  //   (2) Other non-recoverable errors -> Show a failure message
                  //   (3) Successful transaction -> Show confirmation or thank you message

                  const errorDetail = orderData?.details?.[0];

                  if (errorDetail?.issue === "INSTRUMENT_DECLINED") {
                      // (1) Recoverable INSTRUMENT_DECLINED -> call actions.restart()
                      // recoverable state, per
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
                      // Or go to another URL:  actions.redirect('thank_you.html');
                      const transaction =
                          orderData?.purchase_units?.[0]?.payments?.captures?.[0] ||
                          orderData?.purchase_units?.[0]?.payments
                              ?.authorizations?.[0];
                      resultMessage(
                          `Transaction ${transaction.status}: ${transaction.id}<br>
                <br>See console for all available details`
                      );
                      console.log(
                          "Capture result",
                          orderData,
                          JSON.stringify(orderData, null, 2)
                      );
                  }
              } catch (error) {
                  console.error(error);
                  resultMessage(
                      `Sorry, your transaction could not be processed...<br><br>${error}`
                  );
              }
          },

         onError: (err) => {
              // redirect to your specific error page
              window.location.assign("/your-error-page-here");
          },
         onCancel: (data) => {
              // Show a cancel page or return to cart
              window.location.assign("/your-error-page-here");
          },


      });
      paypalButtons.render("#paypal-button-container");
    }

      document.head.appendChild(script);
});


// Example function to show a result to the user. Your site's UI library can be used instead.
function resultMessage(message) {
    const container = document.querySelector("#result-message");
    container.innerHTML = message;
}