package org.order.bookservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.order.bookservice.dto.OrderResponse;
import org.order.bookservice.dto.ProductRequest;
import org.order.bookservice.dto.ProductResponse;
import org.order.bookservice.entity.Category;
import org.order.bookservice.entity.Product;
import org.order.bookservice.mapper.ProductMapper;
import org.order.bookservice.repository.ProductRepository;
import org.order.bookservice.service.ProductService;
import org.order.bookservice.utils.CurrencyConverter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CurrencyConverter currencyConverter;

    @InjectMocks
    private ProductService productService;

    private Product productUSD;

    private Product productEUR;

    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productUSD = new Product(1L, "Test Product", new BigDecimal("100.00"), "USD", new Category(1L, "Books"), new HashSet<>());
        productEUR = new Product(2L, "Test Product2", new BigDecimal("50.00"), "EUR", new Category(1L, "Books"), new HashSet<>());
        productResponse = new ProductResponse(1L, "Test Product", new BigDecimal("100.00"), "USD", "Books", "Fiction");
    }

    @Test
    void testFindAllProductsRUB() {
        List<Product> productList = List.of(productUSD);
        Specification<Product> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        Pageable pageable = Pageable.unpaged();

        when(productRepository.findAll(specification, pageable)).thenReturn(productList);
        when(productMapper.toDTO(productUSD)).thenReturn(productResponse);
        when(currencyConverter.convert(new BigDecimal("100.00"), "USD", "RUB"))
                .thenReturn(new BigDecimal("7500.00"));

        List<ProductResponse> results = productService.findAllProducts(specification, pageable, "RUB");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(new BigDecimal("7500.00"), results.getFirst().price());
        assertEquals("RUB", results.getFirst().currency());

        verify(productRepository, times(1)).findAll(specification, pageable);
        verify(currencyConverter, times(1)).convert(new BigDecimal("100.00"), "USD", "RUB");
    }

    @Test
    void testMakeOrderWithUSDAndEURO() {
        ProductRequest request = new ProductRequest(List.of(1L, 2L), "RUB");
        when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(productUSD, productEUR));
        when(currencyConverter.convert(new BigDecimal("100.00"), "USD", "RUB"))
                .thenReturn(new BigDecimal("7500.00"));
        when(currencyConverter.convert(new BigDecimal("50.00"), "EUR", "RUB"))
                .thenReturn(new BigDecimal("5000.00"));

        OrderResponse response = productService.makeOrder(request);

        assertNotNull(response);
        assertEquals("RUB", response.currency());
        assertEquals(new BigDecimal("12500.00"), response.totalPrice());

        verify(currencyConverter, times(1)).convert(new BigDecimal("100.00"), "USD", "RUB");
        verify(currencyConverter, times(1)).convert(new BigDecimal("50.00"), "EUR", "RUB");
    }

    @Test
    void testMakeOrderWhenCurrencyConversionFails() {
        ProductRequest request = new ProductRequest(List.of(1L, 2L), "RUB");
        when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(productUSD, productEUR));
        when(currencyConverter.convert(any(), any(), any())).thenThrow(new IllegalArgumentException("Currency conversion service unavailable"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> productService.makeOrder(request));

        assertEquals("Currency conversion service unavailable", exception.getMessage());
        verify(currencyConverter, times(1)).convert(any(), any(), any());
    }
}
