package project.web.admin;

import project.model.Transaction;
import project.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/admin/transactions")
public class TransactionsAdminController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionsAdminController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView getTransactions() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/transactions");

        List<Transaction> transactions = transactionService.findAll();

        modelAndView.addObject("transactions", transactions);
        modelAndView.addObject("page", "transactions");
        modelAndView.addObject("title", "Transactions");

        return modelAndView;
    }

}
