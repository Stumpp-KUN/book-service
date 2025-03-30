package org.order.bookservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.order.bookservice.dto.OrderResponse;
import org.order.bookservice.dto.ProductRequest;
import org.order.bookservice.dto.ProductResponse;
import org.order.bookservice.entity.Category;
import org.order.bookservice.entity.Order;
import org.order.bookservice.entity.Product;
import org.order.bookservice.mapper.ProductMapper;
import org.order.bookservice.repository.OrderRepository;
import org.order.bookservice.repository.ProductRepository;
import org.order.bookservice.service.CurrencyService;
import org.order.bookservice.service.ProductService;
import org.order.bookservice.utils.CurrencyConverter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Mock
    private CurrencyConverter currencyConverter;

    @Mock
    private CurrencyService currencyService;

    private ProductService productService;

    private Product productUSD;

    private Product productEUR;

    @Mock
    private OrderRepository orderRepository;

    private Order order;

    private ProductResponse productResponse;

    private final static String RUB_CURRENCY = "RUB";

    private final static BigDecimal EXPECTED_CONVERTED_PRICE = new BigDecimal("7500.00");

    @BeforeEach
    void setUp() {
        productUSD = new Product(1L, "Test Product", new BigDecimal("100.00"), "USD", new Category(1L, "Books"), new HashSet<>());
        productEUR = new Product(2L, "Test Product2", new BigDecimal("50.00"), "EUR", new Category(1L, "Books"), new HashSet<>());
        productResponse = new ProductResponse(1L, "Test Product", new BigDecimal("100.00"), "USD", "Books", "Fiction");
        productService = new ProductService(productRepository, orderRepository, productMapper, currencyConverter);

        order = new Order();
        order.setId(1L);
        order.setCurrency(RUB_CURRENCY);
        order.setOrderProducts(new HashSet<>());
    }

    @Test
    void testFindAllProductsRUB() {
        // given
        List<Product> productList = List.of(productUSD);
        Specification<Product> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        Pageable pageable = Pageable.unpaged();
        when(productRepository.findAll(specification, pageable)).thenReturn(productList);
        when(currencyConverter.convert(new BigDecimal("100.00"), "USD", RUB_CURRENCY))
                .thenReturn(EXPECTED_CONVERTED_PRICE);

        // when
        List<ProductResponse> results = productService.findAllProducts(specification, pageable, RUB_CURRENCY);

        // then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(EXPECTED_CONVERTED_PRICE, results.get(0).price());
        assertEquals(RUB_CURRENCY, results.get(0).currency());
        verify(productRepository, times(1)).findAll(specification, pageable);
        verify(currencyConverter, times(1)).convert(new BigDecimal("100.00"), "USD", RUB_CURRENCY);
    }

    @ParameterizedTest
    @CsvSource({
            "7500.00, 5000.00, 12500.00",
            "7600.00, 4900.00, 12500.00",
            "7400.00, 5100.00, 12500.00"
    })
    void testMakeOrderWithDifferentExchangeRates(String usdRate, String eurRate, String expectedTotal) {
        // given
        ProductRequest request = new ProductRequest(List.of(1L, 2L), "RUB");
        when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(productUSD, productEUR));
        when(currencyConverter.convert(new BigDecimal("100.00"), "USD", "RUB"))
                .thenReturn(new BigDecimal(usdRate));
        when(currencyConverter.convert(new BigDecimal("50.00"), "EUR", "RUB"))
                .thenReturn(new BigDecimal(eurRate));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // when
        OrderResponse response = productService.makeOrder(request);

        // then
        assertNotNull(response);
        assertEquals(RUB_CURRENCY, response.currency());
        assertEquals(new BigDecimal(expectedTotal), response.totalPrice());
        verify(currencyConverter, times(1)).convert(new BigDecimal("100.00"), "USD", "RUB");
        verify(currencyConverter, times(1)).convert(new BigDecimal("50.00"), "EUR", "RUB");
        verify(orderRepository, times(1)).save(any(Order.class));
    }

}
