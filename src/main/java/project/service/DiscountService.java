package project.service;

import project.exception.UnknownElementException;
import project.model.Discount;
import project.model.DiscountType;
import project.repository.DiscountRepository;
import project.web.dto.CreateDiscountRequest;
import org.springframework.stereotype.Service;

@Service
public class DiscountService {
    private final DiscountRepository discountRepository;

    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    public Discount persist(Discount formerDiscount, CreateDiscountRequest createDiscountRequest) {
        Discount discount;
        if (formerDiscount.getId() != null) {
            discount = discountRepository.findById(formerDiscount.getId()).orElseThrow(() -> new UnknownElementException("Discount does not exist!"));
        } else {
            discount = new Discount();
        }

        discount.setAmount(createDiscountRequest.getAmount());
        discount.setType(createDiscountRequest.getType());
        discount.setStartDate(createDiscountRequest.getStartDate());
        discount.setEndDate(createDiscountRequest.getEndDate());

        return discountRepository.save(discount);
    }

    public void unsetDiscount(Discount discount) {
        discount.setAmount(0);
        discount.setType(DiscountType.FIXED);
        discount.setStartDate(null);
        discount.setEndDate(null);

        discountRepository.save(discount);
    }
}
