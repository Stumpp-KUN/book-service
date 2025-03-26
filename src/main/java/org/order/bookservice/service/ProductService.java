package org.order.bookservice.service;

import lombok.RequiredArgsConstructor;
import org.order.bookservice.dto.OrderResponse;
import org.order.bookservice.dto.ProductRequest;
import org.order.bookservice.dto.ProductResponse;
import org.order.bookservice.entity.Order;
import org.order.bookservice.entity.OrderProduct;
import org.order.bookservice.entity.Product;
import org.order.bookservice.mapper.ProductMapper;
import org.order.bookservice.repository.OrderRepository;
import org.order.bookservice.repository.ProductRepository;
import org.order.bookservice.utils.CurrencyConverter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ProductMapper productMapper;
    private final CurrencyConverter currencyConverter;

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ProductResponse> findAllProducts(Specification specification, Pageable pageable, String currency) {
        var products = productRepository.findAll(specification, pageable);
        return products.stream()
                .map(productMapper::toDTO)
                .map(product -> {
                    BigDecimal finalPrice = currencyConverter.convert(product.price(), product.currency(), currency);
                    return new ProductResponse(product.id(), product.name(), finalPrice, currency, product.category(), product.genre());
                })
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderResponse makeOrder(ProductRequest request) {
        var products = productRepository.findAllById(request.productIds());

        Order order = new Order();
        order.setCurrency(request.currency());

        BigDecimal totalPrice = BigDecimal.ZERO;
        Set<OrderProduct> orderProducts = new HashSet<>();
        for (Product product : products) {
            BigDecimal convertedPrice = currencyConverter.convert(product.getPrice(), product.getCurrency(), request.currency());

            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrder(order);
            orderProduct.setProduct(product);
            orderProduct.setConvertedPrice(convertedPrice);

            orderProducts.add(orderProduct);
            totalPrice = totalPrice.add(convertedPrice);
        }
        order.setOrderProducts(orderProducts);
        order = orderRepository.save(order);

        return new OrderResponse(order.getId(), request.currency(), totalPrice.setScale(2, BigDecimal.ROUND_HALF_UP));
    }
}
