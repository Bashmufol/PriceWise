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
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final EcommerceCrawlService crawlService;

    @GetMapping("/")
    public String home(Model model) {
        // Display an empty page or some initial products
        // No longer using pagination for the home page, just an empty list
        model.addAttribute("productsBySource", Map.of());
        return "search";
    }

    @PostMapping("/search")
    public String search(@RequestParam("searchTerm") String searchTerm, Model model) {
        // First, trigger the crawl to get the latest data
        crawlService.crawlAndSaveProducts(searchTerm);

        // Then, fetch all results from the database
        List<Product> products = productRepository.findByNameContainingIgnoreCase(searchTerm);

        // Group products by source and sort each group by price
        Map<String, List<Product>> productsBySource = products.stream()
                .collect(Collectors.groupingBy(Product::getSource,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    list.sort(Comparator.comparing(Product::getPrice));
                                    return list;
                                })));

        model.addAttribute("productsBySource", productsBySource);
        model.addAttribute("searchTerm", searchTerm);
        return "search";
    }

}