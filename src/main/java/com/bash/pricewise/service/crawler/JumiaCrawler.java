package com.bash.pricewise.service.crawler;

import com.bash.pricewise.model.Product;
import com.bash.pricewise.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class JumiaCrawler implements WebsiteCrawler {
    private final ProductRepository  productRepository;
    final String baseUrl = "https://www.jumia.com.ng/catalog/?q=";

    @Override
    public void crawl(String searchTerm) {
        String url = baseUrl + searchTerm.replace(" ", "+");

        System.out.println("Crawling Jumia: " + url);

        sleep();

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("PriceComparisonApp/1.0 (Nigeria)")
                    .timeout(5000)
                    .get();

            // === IMPORTANT: These selectors are examples. You must inspect the actual Jumia page to find correct ones. ===
            Elements products = doc.select(".core");

            for (Element productElement : products) {
                Product product = new Product();
                product.setSource("Jumia");

                // Extracting name
                String name = productElement.select("h3.name").text();
                product.setName(name);

                // Extracting price
                String priceText = productElement.select(".prc").text()
                        .replace("₦", "")
                        .replace(",", "")
                        .trim();
                try {
                    product.setPrice(Double.parseDouble(priceText));
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse price for Jumia product: " + name);
                    continue; // Skip this product if price cannot be parsed
                }

                // Extracting image URL
                String imageUrl = productElement.select("img.img")
                        .attr("data-src");
                product.setImageUrl(imageUrl);

                // Extracting product URL
                String productUrl = "https://www.jumia.com.ng" + productElement.select("a.core").attr("href");
                product.setProductUrl(productUrl);

                // Rating field has been removed as per the user's request.

                productRepository.save(product);
            }
        } catch (IOException e) {
            System.err.println("Error crawling Jumia: " + e.getMessage());
        }
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    // Helper method to simulate a random delay between requests for ethical scraping
    private void sleep() {
        try {
            Thread.sleep(4000); // Wait for 4 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
