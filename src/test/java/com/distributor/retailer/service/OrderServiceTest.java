
package com.distributor.retailer.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.distributor.retailer.dto.OrderResponse;
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
import org.springframework.test.util.ReflectionTestUtils;

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
    }@Test
void getOrdersByRetailerReturnsOrdersLatestFirst() {
    Order order = createOrder(10L, Order.OrderStatus.PENDING);

    when(retailerRepository.existsById(1L))
            .thenReturn(true);

    when(orderRepository.findByRetailerIdOrderByOrderDateDesc(1L))
            .thenReturn(List.of(order));

    when(orderItemRepository.findByOrderId(10L))
            .thenReturn(List.of());

    List<OrderResponse> responses =
            orderService.getOrdersByRetailer(1L);

    assertEquals(1, responses.size());
    assertEquals(
            Order.OrderStatus.PENDING,
            responses.get(0).getStatus()
    );

    verify(orderRepository)
            .findByRetailerIdOrderByOrderDateDesc(1L);
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
        when(orderRepository.findByIdForUpdate(999L))
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

        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.updateOrderStatus(
                1L, Order.OrderStatus.DELIVERED
        );

        assertEquals(Order.OrderStatus.DELIVERED, response.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void confirmedOrderCanBeShippedDirectly() {
        Order order = createOrder(2L, Order.OrderStatus.CONFIRMED);

        when(orderRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.updateOrderStatus(
                2L, Order.OrderStatus.SHIPPED
        );

        assertEquals(Order.OrderStatus.SHIPPED, response.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void deliveredOrderCannotBeChangedAgain() {
        Order order = createOrder(3L, Order.OrderStatus.DELIVERED);

        when(orderRepository.findByIdForUpdate(3L))
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

        when(orderRepository.findByIdForUpdate(4L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(
                        4L, Order.OrderStatus.CONFIRMED
                )
        );

        verify(orderRepository, never()).save(any(Order.class));
        verify(productRepository, never())
        .findByIdForUpdate(anyLong());

        verify(productRepository, never())
        .save(any(Product.class));
    }

    @Test
    void pendingOrderCannotBeShippedDirectly() {
        Order order = createOrder(5L, Order.OrderStatus.PENDING);

        when(orderRepository.findByIdForUpdate(5L))
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
void cancellingPendingOrderDoesNotRestoreStock() {
    Order order = createOrder(6L, Order.OrderStatus.PENDING);
    when(orderRepository.findByIdForUpdate(6L))
            .thenReturn(Optional.of(order));

    when(orderItemRepository.findByOrderId(6L))
            .thenReturn(List.of());

    when(orderRepository.save(any(Order.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    var response = orderService.updateOrderStatus(
            6L, Order.OrderStatus.CANCELLED
    );

    assertEquals(Order.OrderStatus.CANCELLED, response.getStatus());

    // Pending orders should not restore stock.
    verify(productRepository, never())
            .findByIdForUpdate(anyLong());

    verify(productRepository, never())
            .save(any(Product.class));

    verify(orderRepository).save(order);
}
    @Test
    void confirmingPendingOrderDeductsProductStock() {
        Order order = createOrder(7L, Order.OrderStatus.PENDING);
        Product product = createProduct(10L, 10);

        OrderItem item = createOrderItem(order, product, 3);

        when(orderRepository.findByIdForUpdate(7L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrderId(7L))
                .thenReturn(List.of(item));

        when(productRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.updateOrderStatus(
                7L, Order.OrderStatus.CONFIRMED
        );

        assertEquals(Order.OrderStatus.CONFIRMED, response.getStatus());
        assertEquals(7, product.getStockQuantity());

        verify(productRepository).findByIdForUpdate(10L);
        verify(orderRepository).save(order);
        verify(productRepository).save(product);
    }

    @Test
    void confirmingOrderWithInsufficientStockThrowsException() {
        Order order = createOrder(8L, Order.OrderStatus.PENDING);
        Product product = createProduct(11L, 2);

        OrderItem item = createOrderItem(order, product, 3);

        when(orderRepository.findByIdForUpdate(8L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrderId(8L))
                .thenReturn(List.of(item));

        when(productRepository.findByIdForUpdate(11L))
                .thenReturn(Optional.of(product));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(
                        8L, Order.OrderStatus.CONFIRMED
                )
        );

        assertEquals(2, product.getStockQuantity());
        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancellingConfirmedOrderRestoresProductStock() {
        Order order = createOrder(9L, Order.OrderStatus.CONFIRMED);
        Product product = createProduct(12L, 7);

        OrderItem item = createOrderItem(order, product, 3);

        when(orderRepository.findByIdForUpdate(9L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrderId(9L))
                .thenReturn(List.of(item));

        when(productRepository.findByIdForUpdate(12L))
                .thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.updateOrderStatus(
                9L, Order.OrderStatus.CANCELLED
        );

        assertEquals(Order.OrderStatus.CANCELLED, response.getStatus());
        assertEquals(10, product.getStockQuantity());

        verify(productRepository).findByIdForUpdate(12L);
        verify(orderRepository).save(order);
        verify(productRepository).save(product);
    }

    private Order createOrder(long id, Order.OrderStatus initialStatus) {
        Order order = mock(Order.class);
        Retailer retailer = mock(Retailer.class);

        AtomicReference<Order.OrderStatus> status =
                new AtomicReference<>(initialStatus);

        lenient().when(retailer.getId()).thenReturn(1L);
        lenient().when(order.getId()).thenReturn(id);
        lenient().when(order.getRetailer()).thenReturn(retailer);
        lenient().when(order.getStatus()).thenAnswer(
                invocation -> status.get()
        );

        lenient().doAnswer(invocation -> {
            status.set(invocation.getArgument(0));
            return null;
        }).when(order).setStatus(any(Order.OrderStatus.class));

        return order;
    }
private Product createProduct(long id, int stockQuantity) {
    Product product = new Product();

    ReflectionTestUtils.setField(product, "id", id);

    product.setName("Test Rice");
    product.setSku("RICE-" + id);
    product.setPrice(new BigDecimal("100.00"));
    product.setStockQuantity(stockQuantity);

    return product;
}

    private OrderItem createOrderItem(
            Order order,
            Product product,
            int quantity
    ) {
        OrderItem item = new OrderItem();

        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal("100.00"));
        item.setSubtotal(
                new BigDecimal("100.00").multiply(BigDecimal.valueOf(quantity))
        );

        return item;
    }
}
