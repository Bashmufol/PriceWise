package com.bash.pricewise.controller;

import com.bash.pricewise.model.Product;
import com.bash.pricewise.repository.ProductRepository;
import com.bash.pricewise.service.EcommerceCrawlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final EcommerceCrawlService crawlService;

    @GetMapping("/")
    public String home(Model model) {
        // Display an empty page
        model.addAttribute("productsBySource", Map.of());
        return "home";
    }

    @PostMapping("/search")
    public String search(@RequestParam("searchTerm") String searchTerm, Model model) {
        // First, trigger the crawl to get the latest data
        crawlService.crawlAndSaveProducts(searchTerm);

        // Then, Fetch all results from the database
        List<Product> products = productRepository.findByNameContainingIgnoreCase(searchTerm);

        // Sort all products by price from lowest to highest
        products.sort(Comparator.comparing(Product::getPrice));

        // Add the sorted list of products directly to the model
        model.addAttribute("products", products);
        model.addAttribute("searchTerm", searchTerm);
        return "home";
    }

}