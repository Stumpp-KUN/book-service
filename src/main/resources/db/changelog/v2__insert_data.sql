INSERT INTO category (name) VALUES ('Magazines'), ('Books'), ('Clothing');

INSERT INTO genre (name) VALUES ('Sci-Fi'), ('Romance'), ('Technology');

INSERT INTO product (name, price, currency, category_id) VALUES ('Magazine Name', 199.99, 'USD', 1);
INSERT INTO product (name, price, currency, category_id) VALUES ('Test Book', 29.99, 'USD', 2);
INSERT INTO product (name, price, currency, category_id) VALUES ('Clothing Test', 49.99, 'USD', 3);

INSERT INTO product_genre (product_id, genre_id) VALUES (1, 3);
INSERT INTO product_genre (product_id, genre_id) VALUES (2, 1);
INSERT INTO product_genre (product_id, genre_id) VALUES (3, 2);
