package project.service;

import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.controllers.OrdersController;
import com.paypal.sdk.exceptions.ApiException;
import com.paypal.sdk.http.response.ApiResponse;
import com.paypal.sdk.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@Component
public class PayPalService {

    @Autowired
    private final PaypalServerSdkClient client;

    public PayPalService(PaypalServerSdkClient client) {
        this.client = client;
    }

    public Order createOrder(Double amount) throws IOException, ApiException {
        CreateOrderInput createOrderInput = new CreateOrderInput.Builder(
                null,
                new OrderRequest.Builder(
                        CheckoutPaymentIntent.fromString("CAPTURE"),
                        Arrays.asList(
                                new PurchaseUnitRequest.Builder(
                                        new AmountWithBreakdown.Builder(
                                                "EUR",
                                                String.valueOf(amount)
                                        )
                                                .breakdown(
                                                        new AmountBreakdown.Builder()
                                                                .itemTotal(
                                                                        new Money(
                                                                                "EUR",
                                                                                String.valueOf(amount)
                                                                        )
                                                                ).build()
                                                )
                                                .build()
                                )
                                        .items(
                                                // lookup item details in `cart` from database
                                                Collections.singletonList(
                                                        new Item.Builder(
                                                                "Wallet",
                                                                new Money.Builder("EUR", String.valueOf(amount)).build(),
                                                                "1"
                                                        )
                                                                .description("Wallet money")
                                                                .sku("001")
                                                                .category(ItemCategory.DIGITAL_GOODS)
                                                                .build()
                                                )
                                        )
                                        /*
                                        .shipping(new ShippingDetails.Builder()
                                                .emailAddress("buyer_shipping_email@example.com")
                                                .phoneNumber(new PhoneNumberWithCountryCode.Builder(
                                                        "1",
                                                        "4081111111"
                                                ).build())
                                                .build())*/
                                        .build()
                        )
                )
                        .paymentSource(
                                new PaymentSource.Builder()
                                        .paypal(
                                                new PaypalWallet.Builder()
                                                        .experienceContext(
                                                                new PaypalWalletExperienceContext.Builder()
                                                                        .userAction(PaypalExperienceUserAction.PAY_NOW)
                                                                        .shippingPreference(PaypalWalletContextShippingPreference.NO_SHIPPING)
                                                                        .paymentMethodPreference(PayeePaymentMethodPreference.IMMEDIATE_PAYMENT_REQUIRED)
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )

                        .build()
        ).build();
        OrdersController ordersController = client.getOrdersController();
        ApiResponse<Order> apiResponse = ordersController.createOrder(createOrderInput);
        return apiResponse.getResult();
    }

    public Order captureOrders(String orderID) throws IOException, ApiException {
        CaptureOrderInput ordersCaptureInput = new CaptureOrderInput.Builder(
                orderID,
                null)
                .build();
        OrdersController ordersController = client.getOrdersController();
        ApiResponse<Order> apiResponse = ordersController.captureOrder(ordersCaptureInput);
        return apiResponse.getResult();
    }
}
