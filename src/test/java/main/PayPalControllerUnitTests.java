package main;

import com.paypal.sdk.exceptions.ApiException;
import com.paypal.sdk.models.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import project.config.PayPalConfig;
import project.service.MessageService;
import project.service.PayPalService;
import project.web.PayPalController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PayPalControllerUnitTests {

    @Mock
    private PayPalService payPalService;
    @Mock
    private PayPalConfig paypalConfig;
    @Mock
    private MessageService messageService;

    @InjectMocks
    private PayPalController payPalController;

    @Test
    void whenPayPalConfigCalled_GetClientId() {
        when(paypalConfig.getId()).thenReturn("clientId");

        Map<String, String> result = payPalController.paypalConfig();

        assertEquals("clientId", result.get("clientId"));
        verify(paypalConfig).getId();
    }

    @Test
    void shouldReturnOrderId_whenValidAmount() throws Exception {
        Order order = new Order();
        order.setId("123");

        when(payPalService.createOrder(10.0)).thenReturn(order);

        Map<String, Object> request = Map.of("amount", "10");

        ResponseEntity<Map<String, String>> response = payPalController.createOrder(request, Locale.ENGLISH);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("123", response.getBody().get("id"));
    }

    @Test
    void shouldReturnError_whenTransactionAmountMissing() {
        String message = "Please, write a valid payment amount!";

        when(messageService.getLocalizedMessage("paypal_valid_pay_amount", Locale.ENGLISH)).thenReturn(message);

        Map<String, Object> request = new HashMap<>();

        ResponseEntity<Map<String, String>> response = payPalController.createOrder(request, Locale.ENGLISH);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(message, response.getBody().get("error"));


        request = Map.of("amount", "");

        response = payPalController.createOrder(request, Locale.ENGLISH);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(message, response.getBody().get("error"));
    }

    @Test
    void shouldReturnError_whenTransactionAmountIsNotNumber() {
        String message = "Please, write a valid payment amount!";

        when(messageService.getLocalizedMessage("paypal_valid_pay_amount", Locale.ENGLISH)).thenReturn(message);

        Map<String, Object> request = Map.of("amount", "AB");

        ResponseEntity<Map<String, String>> response = payPalController.createOrder(request, Locale.ENGLISH);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(message, response.getBody().get("error"));
    }

    @Test
    void shouldReturnError_whenTransactionAmountIsLessOrEqualThanZero() {
        String message = "Please, write a valid payment amount!";

        when(messageService.getLocalizedMessage("paypal_valid_pay_amount", Locale.ENGLISH)).thenReturn(message);

        Map<String, Object> request = Map.of("amount", "0");

        ResponseEntity<Map<String, String>> response = payPalController.createOrder(request, Locale.ENGLISH);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(message, response.getBody().get("error"));

        request = Map.of("amount", "-2");

        response = payPalController.createOrder(request, Locale.ENGLISH);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(message, response.getBody().get("error"));
    }

    @Test
    void shouldServerError_whenCreateOrderThrowsException() throws IOException, ApiException {
        String message = "Server error. Please try again.";

        Map<String, Object> request = Map.of("amount", "10");

        when(payPalService.createOrder(10.00)).thenThrow(new RuntimeException("error"));

        ResponseEntity<Map<String, String>> response = payPalController.createOrder(request, Locale.ENGLISH);

        assertEquals(500, response.getStatusCode().value());
        assertEquals(message, response.getBody().get("error"));
    }

    @Test
    void shouldReturnOk_whenCaptureOrderSuccessful() throws IOException, ApiException {
        String orderId = "123";

        Order order = new Order();
        order.setId(orderId);

        when(payPalService.captureOrders(orderId)).thenReturn(order);

        ResponseEntity<Order> response = payPalController.captureOrder(orderId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(orderId, response.getBody().getId());
    }

    @Test
    void shouldServerError_whenCaptureOrderThrowsException() throws IOException, ApiException {
        String orderId = "123";

        when(payPalService.captureOrders(orderId)).thenThrow(new RuntimeException("error"));

        ResponseEntity<Order> response = payPalController.captureOrder(orderId);

        assertEquals(500, response.getStatusCode().value());
    }
}
