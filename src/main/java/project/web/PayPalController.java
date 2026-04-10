package project.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.paypal.sdk.models.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import project.config.PayPalConfig;
import project.service.MessageService;
import project.service.PayPalService;

import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/paypal")
@RequiredArgsConstructor
public class PayPalController {

    private final PayPalService payPalService;
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

            if (transactionAmount.equals("null") || transactionAmount.isBlank()) {
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

            Order order = payPalService.createOrder(amount);

            return ResponseEntity.ok(Map.of("id", order.getId()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server error. Please try again."));
        }
    }


    @PostMapping("/api/orders/{orderID}/capture")
    public ResponseEntity<Order> captureOrder(@PathVariable String orderID) {
        try {
            Order response = payPalService.captureOrders(orderID);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
