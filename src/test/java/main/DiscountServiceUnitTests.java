package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.model.Discount;
import project.model.DiscountType;
import project.repository.DiscountRepository;
import project.service.DiscountService;
import project.web.dto.CreateDiscountRequest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DiscountServiceUnitTests {

    @Mock
    private DiscountRepository discountRepository;

    @InjectMocks
    private DiscountService discountService;

    @Test
    public void whenDiscountIsNew_thenNewDiscountIsCreatedAndSaved() {
        CreateDiscountRequest request = CreateDiscountRequest.builder()
                .amount(20)
                .type(DiscountType.PERCENT)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(5))
                .build();

        Discount createDiscount = new Discount();

        Discount saved = new Discount();
        saved.setAmount(request.getAmount());
        saved.setType(request.getType());
        saved.setStartDate(request.getStartDate());
        saved.setEndDate(request.getEndDate());

        when(discountRepository.save(any())).thenReturn(saved);

        Discount result = discountService.persist(createDiscount, request);

        assertNotNull(result);
        assertEquals(request.getAmount(), result.getAmount());
        assertEquals(request.getType(), result.getType());
        assertEquals(request.getStartDate(), result.getStartDate());
        assertEquals(request.getEndDate(), result.getEndDate());

        verify(discountRepository).save(any());
    }

    @Test
    public void whenDiscountExists_thenExistingDiscountIsUpdatedAndSaved() {
        UUID discountId = UUID.randomUUID();

        Discount former = new Discount();
        former.setId(discountId);

        Discount existing = new Discount();
        existing.setId(discountId);

        CreateDiscountRequest request = CreateDiscountRequest.builder()
                .amount(30)
                .type(DiscountType.FIXED)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(10))
                .build();

        when(discountRepository.findById(discountId)).thenReturn(Optional.of(existing));
        when(discountRepository.save(existing)).thenReturn(existing);


        Discount result = discountService.persist(former, request);

        assertEquals(30, result.getAmount());
        assertEquals(DiscountType.FIXED, result.getType());
        assertEquals(request.getStartDate(), result.getStartDate());
        assertEquals(request.getEndDate(), result.getEndDate());

        verify(discountRepository).findById(discountId);
        verify(discountRepository).save(existing);
    }

    @Test
    public void whenFormerDiscountIdExistsButNotFound_thenThrowException() {
        UUID id = UUID.randomUUID();

        Discount former = new Discount();
        former.setId(id);

        when(discountRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> discountService.persist(former, new CreateDiscountRequest()));

        assertEquals("Discount does not exist!", ex.getMessage());
    }

    @Test
    public void whenDiscountIsRemoved_thenReturnEmptyDiscount() {
        Discount discount = new Discount();
        discount.setAmount(10);
        discount.setType(DiscountType.PERCENT);
        discount.setStartDate(LocalDateTime.now());
        discount.setEndDate(LocalDateTime.now().plusDays(5));

        when(discountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        discountService.unsetDiscount(discount);

        assertEquals(0, discount.getAmount());
        assertEquals(DiscountType.FIXED, discount.getType());
        assertNull(discount.getStartDate());
        assertNull(discount.getEndDate());
        verify(discountRepository).save(discount);
    }

}
