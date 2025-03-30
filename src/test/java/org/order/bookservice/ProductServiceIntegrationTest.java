package org.order.bookservice;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.order.bookservice.dto.OrderResponse;
import org.order.bookservice.dto.ProductRequest;
import org.order.bookservice.dto.ProductResponse;
import org.order.bookservice.entity.Category;
import org.order.bookservice.entity.Product;
import org.order.bookservice.repository.CategoryRepository;
import org.order.bookservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class ProductServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("shop_db")
            .withUsername("admin")
            .withPassword("admin");

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().port(8091))
            .build();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = new Category();
        category.setName("Test Category");
        categoryRepository.save(category);

        wireMockServer.stubFor(get(urlEqualTo("/exrates/rates?periodicity=0"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("nbrb-response.json")));
    }

    @Test
    void shouldFetchProductsWithConvertedCurrency() {
        Category category = categoryRepository.findAll().get(0);

        Product product = new Product();
        product.setName("Book 1");
        product.setPrice(new BigDecimal("50.00"));
        product.setCurrency("USD");
        product.setCategory(category);
        productRepository.save(product);

        String url = "http://localhost:" + port + "/api/v1/products?currency=EUR";
        ResponseEntity<List<ProductResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ProductResponse>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody().get(0).price()).isEqualByComparingTo("41.67");
    }

    @Test
    void shouldCreateOrderWithCurrencyConversion() {
        Category category = categoryRepository.findAll().get(0);

        Product product = new Product();
        product.setName("Book 1");
        product.setPrice(new BigDecimal("100.00"));
        product.setCurrency("USD");
        product.setCategory(category);
        product = productRepository.save(product);

        ProductRequest request = new ProductRequest(List.of(product.getId()), "EUR");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        String url = "http://localhost:" + port + "/api/v1/products";
        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                OrderResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().totalPrice())
                .usingComparator(BigDecimal::compareTo)
                .isEqualTo(new BigDecimal("83.33"));
        assertThat(response.getBody().currency()).isEqualTo("EUR");
    }
}