package main.service;

import main.repository.DiscountRepository;
import org.springframework.stereotype.Service;

@Service
public class DiscountService {
    private final DiscountRepository discountRepository;

    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }
}
