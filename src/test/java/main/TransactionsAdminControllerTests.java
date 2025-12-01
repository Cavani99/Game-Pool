package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import project.model.Transaction;
import project.service.TransactionService;
import project.web.admin.TransactionsAdminController;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionsAdminControllerTests {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionsAdminController controller;

    @Test
    void getTransactions_ShouldReturnCorrectModelAndView() {
        List<Transaction> mockTransactions = List.of(new Transaction(), new Transaction());
        when(transactionService.findAll()).thenReturn(mockTransactions);

        ModelAndView mav = controller.getTransactions();

        assertEquals("admin/transactions", mav.getViewName());
        assertEquals(mockTransactions, mav.getModel().get("transactions"));
        assertEquals("transactions", mav.getModel().get("page"));
        assertEquals("Transactions", mav.getModel().get("title"));

        verify(transactionService).findAll();
    }
}