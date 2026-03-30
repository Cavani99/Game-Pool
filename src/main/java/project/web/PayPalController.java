package project.web;

import com.paypal.sdk.PaypalServerSdkClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import com.paypal.sdk.controllers.OrdersController;
import com.paypal.sdk.exceptions.ApiException;
import com.paypal.sdk.http.response.ApiResponse;
import com.paypal.sdk.models.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import project.config.PayPalConfig;
import project.service.MessageService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/paypal")
@RequiredArgsConstructor
public class PayPalController {

    private final PaypalServerSdkClient client;
    private final PayPalConfig paypalConfig;
    private final MessageService messageService;

    @GetMapping("/api/config")
    @ResponseBody
    public Map<String, String> paypalConfig() {
        return Map.of("clientId", paypalConfig.getId());
    }

    @PostMapping("/api/orders")
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody Map<String, Object> request, Locale locale) {
        String message = messageService.getLocalizedMessage("paypal_valid_pay_amount", locale);
        try {
            String transactionAmount = String.valueOf(request.get("amount"));

            if (transactionAmount == null || transactionAmount.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", message));
            }

            double amount;
            try {
                amount = Double.parseDouble(transactionAmount);
                if (amount <= 0) {
                    return ResponseEntity
                            .badRequest()
                            .body(Map.of("error", message));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", message));
            }

            Order order = createOrder(amount);

            return ResponseEntity.ok(Map.of("id", order.getId()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server error. Please try again."));
        }
    }

    private Order createOrder(Double amount) throws IOException, ApiException {
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

    @PostMapping("/api/orders/{orderID}/capture")
    public ResponseEntity<Order> captureOrder(@PathVariable String orderID) {
        try {
            Order response = captureOrders(orderID);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Order captureOrders(String orderID) throws IOException, ApiException {
        CaptureOrderInput ordersCaptureInput = new CaptureOrderInput.Builder(
                orderID,
                null)
                .build();
        OrdersController ordersController = client.getOrdersController();
        ApiResponse<Order> apiResponse = ordersController.captureOrder(ordersCaptureInput);
        return apiResponse.getResult();
    }
}
