CREATE TABLE category (
      id BIGSERIAL PRIMARY KEY,
      name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE genre (
       id BIGSERIAL PRIMARY KEY,
       name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE product (
     id BIGSERIAL PRIMARY KEY,
     name VARCHAR(255) NOT NULL,
     price DECIMAL(10,2),
     currency VARCHAR(10),
     category_id BIGINT NOT NULL,
     CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE TABLE product_genre (
       product_id BIGINT NOT NULL,
       genre_id BIGINT NOT NULL,
       PRIMARY KEY (product_id, genre_id),
       CONSTRAINT fk_product_genre_product FOREIGN KEY (product_id) REFERENCES product(id),
       CONSTRAINT fk_product_genre_genre FOREIGN KEY (genre_id) REFERENCES genre(id)
);

CREATE TABLE "order" (
     id BIGSERIAL PRIMARY KEY,
     currency VARCHAR(10)
);

CREATE TABLE order_product (
       id BIGSERIAL PRIMARY KEY,
       order_id BIGINT NOT NULL,
       product_id BIGINT NOT NULL,
       converted_price DECIMAL(10,2),
       CONSTRAINT fk_order_product_order FOREIGN KEY (order_id) REFERENCES "order"(id),
       CONSTRAINT fk_order_product_product FOREIGN KEY (product_id) REFERENCES product(id)
);
