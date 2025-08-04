package com.bash.pricewise.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Product {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @Column(name = "product_name")
        private String name;
        @Column(name = "ecommerce_site")
        private String source; // e.g., Jumia, Konga, Jiji
        @Column(name = "product_price")
        private double price;
        @Column(name = "product_image_url")
        private String imageUrl;
        @Lob // Use @Lob for potentially large text fields like the URL
        @Column(name = "product_link")
        private String productUrl;
}
