
package com.distributor.retailer.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.distributor.retailer.entity.Order;
import com.distributor.retailer.entity.OrderItem;
import com.distributor.retailer.entity.Product;
import com.distributor.retailer.entity.Retailer;
import com.distributor.retailer.repository.OrderItemRepository;
import com.distributor.retailer.repository.OrderRepository;
import com.distributor.retailer.repository.ProductRepository;
import com.distributor.retailer.repository.RetailerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RetailerRepository retailerRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                orderItemRepository,
                productRepository,
                retailerRepository
        );
    }

    @Test
    void getOrderByIdWhenOrderDoesNotExistThrowsException() {
        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getOrderById(999L)
        );

        assertTrue(exception.getMessage().contains("Order not found"));
    }

    @Test
    void getOrdersByRetailerWhenRetailerDoesNotExistThrowsException() {
        when(retailerRepository.existsById(999L))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getOrdersByRetailer(999L)
        );

        verify(orderRepository, never()).findByRetailerId(anyLong());
    }

    @Test
    void getAllOrdersWhenNoOrdersExistReturnsEmptyList() {
        when(orderRepository.findAll()).thenReturn(List.of());

        assertTrue(orderService.getAllOrders().isEmpty());
    }

    @Test
    void getOrderSummaryWhenNoOrdersExistReturnsZeroTotals() {
        when(orderRepository.findAll()).thenReturn(List.of());

        var summary = orderService.getOrderSummary();

        assertEquals(0L, summary.getTotalOrders());
        assertEquals(0L, summary.getPendingOrders());
        assertEquals(0L, summary.getCancelledOrders());
        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(summary.getTotalSales())
        );
    }

    @Test
    void updateOrderStatusWhenOrderDoesNotExistThrowsException() {
        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(
                        999L, Order.OrderStatus.CONFIRMED
                )
        );
    }

    @Test
    void shippedOrderCanBeMarkedDelivered() {
        Order order = createOrder(1L, Order.OrderStatus.SHIPPED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.findByOrderId(1L))
                .thenReturn(List.of());

        var response = orderService.updateOrderStatus(
                1L, Order.OrderStatus.DELIVERED
        );

        assertEquals(Order.OrderStatus.DELIVERED, response.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void confirmedOrderCanBeShippedDirectly() {
        Order order = createOrder(2L, Order.OrderStatus.CONFIRMED);

        when(orderRepository.findById(2L))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.findByOrderId(2L))
                .thenReturn(List.of());

        var response = orderService.updateOrderStatus(
                2L, Order.OrderStatus.SHIPPED
        );

        assertEquals(Order.OrderStatus.SHIPPED, response.getStatus());
    }

    @Test
    void deliveredOrderCannotBeChangedAgain() {
        Order order = createOrder(3L, Order.OrderStatus.DELIVERED);

        when(orderRepository.findById(3L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(
                        3L, Order.OrderStatus.SHIPPED
                )
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelledOrderCannotBeChangedAgain() {
        Order order = createOrder(4L, Order.OrderStatus.CANCELLED);

        when(orderRepository.findById(4L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(
                        4L, Order.OrderStatus.CONFIRMED
                )
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void pendingOrderCannotBeShippedDirectly() {
        Order order = createOrder(5L, Order.OrderStatus.PENDING);

        when(orderRepository.findById(5L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(
                        5L, Order.OrderStatus.SHIPPED
                )
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancellingOrderRestoresProductStock() {
        Order order = createOrder(6L, Order.OrderStatus.PENDING);

        Product product = new Product();
        product.setName("Test Rice");
        product.setSku("RICE-TEST");
        product.setPrice(new BigDecimal("100.00"));
        product.setStockQuantity(7);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(3);
        item.setUnitPrice(new BigDecimal("100.00"));
                        item.setSubtotal(new BigDecimal("300.00"));

        when(orderRepository.findById(6L))
                .thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(6L))
                .thenReturn(List.of(item));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.updateOrderStatus(
                6L, Order.OrderStatus.CANCELLED
        );

        assertEquals(Order.OrderStatus.CANCELLED, response.getStatus());
        assertEquals(10, product.getStockQuantity());
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }
private Order createOrder(long id, Order.OrderStatus initialStatus) {
    Order order = mock(Order.class);
    Retailer retailer = mock(Retailer.class);

    java.util.concurrent.atomic.AtomicReference<Order.OrderStatus> status =
            new java.util.concurrent.atomic.AtomicReference<>(initialStatus);

    lenient().when(retailer.getId()).thenReturn(1L);
    lenient().when(order.getId()).thenReturn(id);
    lenient().when(order.getRetailer()).thenReturn(retailer);
    lenient().when(order.getStatus()).thenAnswer(invocation -> status.get());

    lenient().doAnswer(invocation -> {
    status.set(invocation.getArgument(0));
    return null;
}).when(order).setStatus(any(Order.OrderStatus.class));

    return order;
}
}
