
package com.distributor.retailer.service;

import com.distributor.retailer.entity.Retailer;
import com.distributor.retailer.repository.RetailerRepository;
import com.distributor.retailer.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetailerServiceTest {

    @Mock
    private RetailerRepository retailerRepository;

    @Mock
    private OrderRepository orderRepository;

    private RetailerService retailerService;

    @BeforeEach
    void setUp() {
        retailerService = new RetailerService(
                retailerRepository,
                orderRepository
        );
    }

    private Retailer createRetailer(Long id) {
        Retailer retailer = new Retailer();

        ReflectionTestUtils.setField(retailer, "id", id);

        retailer.setName("ABC Stores");
        retailer.setOwnerName("Rahul Sharma");
        retailer.setPhone("9876543210");
        retailer.setEmail("abc@gmail.com");
        retailer.setAddress("Indore");

        return retailer;
    }

    @Test
    void createRetailerSuccessfully() {
        Retailer retailer = createRetailer(null);

        when(retailerRepository.findByPhone("9876543210"))
                .thenReturn(Optional.empty());

        when(retailerRepository.save(retailer))
                .thenReturn(retailer);

        Retailer result = retailerService.createRetailer(retailer);

        assertEquals("ABC Stores", result.getName());
        verify(retailerRepository).save(retailer);
    }

    @Test
    void createRetailerWithDuplicatePhoneThrowsException() {
        Retailer retailer = createRetailer(null);

        when(retailerRepository.findByPhone("9876543210"))
                .thenReturn(Optional.of(createRetailer(1L)));

        assertThrows(
                IllegalArgumentException.class,
                () -> retailerService.createRetailer(retailer)
        );

        verify(retailerRepository, never()).save(any(Retailer.class));
    }

    @Test
    void getAllRetailersReturnsRetailerList() {
        List<Retailer> retailers = List.of(
                createRetailer(1L),
                createRetailer(2L)
        );

        when(retailerRepository.findAll()).thenReturn(retailers);

        assertEquals(2, retailerService.getAllRetailers().size());
    }

    @Test
    void searchRetailersByNameReturnsMatchingRetailers() {
        List<Retailer> retailers =
                List.of(createRetailer(1L));

        when(retailerRepository.findByNameContainingIgnoreCase("ABC"))
                .thenReturn(retailers);

        assertEquals(
                1,
                retailerService.searchRetailersByName(" ABC ").size()
        );

        verify(retailerRepository)
                .findByNameContainingIgnoreCase("ABC");
    }

    @Test
    void deleteRetailerWithExistingOrdersThrowsException() {
        Retailer retailer = createRetailer(1L);

        when(retailerRepository.findById(1L))
                .thenReturn(Optional.of(retailer));

        when(orderRepository.findByRetailerId(1L))
                .thenReturn(List.of(mock(
                        com.distributor.retailer.entity.Order.class
                )));

        assertThrows(
                IllegalArgumentException.class,
                () -> retailerService.deleteRetailer(1L)
        );

        verify(retailerRepository, never()).delete(any(Retailer.class));
    }

    @Test
    void deleteRetailerWithoutOrdersSuccessfully() {
        Retailer retailer = createRetailer(1L);

        when(retailerRepository.findById(1L))
                .thenReturn(Optional.of(retailer));

        when(orderRepository.findByRetailerId(1L))
                .thenReturn(List.of());

        retailerService.deleteRetailer(1L);

        verify(retailerRepository).delete(retailer);
    }
    @Test
void getRetailerByIdReturnsRetailerSuccessfully() {
    Retailer retailer = createRetailer(1L);

    when(retailerRepository.findById(1L))
            .thenReturn(Optional.of(retailer));

    Retailer result = retailerService.getRetailerById(1L);

    assertEquals(1L, result.getId());
    assertEquals("ABC Stores", result.getName());
}

@Test
void getRetailerByIdThrowsExceptionWhenNotFound() {
    when(retailerRepository.findById(99L))
            .thenReturn(Optional.empty());

    assertThrows(
            org.springframework.web.server.ResponseStatusException.class,
            () -> retailerService.getRetailerById(99L)
    );
}

@Test
void updateRetailerSuccessfully() {
    Retailer existingRetailer = createRetailer(1L);
    Retailer updatedRetailer = createRetailer(null);

    updatedRetailer.setName("New ABC Stores");
    updatedRetailer.setPhone("9123456780");

    when(retailerRepository.findById(1L))
            .thenReturn(Optional.of(existingRetailer));

    when(retailerRepository.findByPhone("9123456780"))
            .thenReturn(Optional.empty());

    when(retailerRepository.save(existingRetailer))
            .thenReturn(existingRetailer);

    Retailer result = retailerService.updateRetailer(
            1L, updatedRetailer
    );

    assertEquals("New ABC Stores", result.getName());
    assertEquals("9123456780", result.getPhone());

    verify(retailerRepository).save(existingRetailer);
}

@Test
void updateRetailerWithDuplicatePhoneThrowsException() {
    Retailer existingRetailer = createRetailer(1L);
    Retailer updatedRetailer = createRetailer(null);

    updatedRetailer.setPhone("9123456780");

    when(retailerRepository.findById(1L))
            .thenReturn(Optional.of(existingRetailer));

    when(retailerRepository.findByPhone("9123456780"))
            .thenReturn(Optional.of(createRetailer(2L)));

    assertThrows(
            IllegalArgumentException.class,
            () -> retailerService.updateRetailer(
                    1L, updatedRetailer
            )
    );

    verify(retailerRepository, never()).save(any(Retailer.class));
}
}
