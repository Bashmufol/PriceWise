package com.bash.pricewise.service.crawler;

import com.bash.pricewise.model.Product;
import com.bash.pricewise.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class KaraCrawler implements WebsiteCrawler {
    private final ProductRepository productRepository;
    private final String baseUrl = "https://kara.com.ng/catalogsearch/result/?q=";

    @Override
    public void crawl(String searchTerm) {
        String url = baseUrl + searchTerm.replace(" ", "+");

        System.out.println("Crawling Kara: " + url);

        sleep();

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("PriceComparisonApp/1.0 (Nigeria)")
                    .timeout(5000)
                    .get();

            // === IMPORTANT: These selectors are examples. You must inspect the actual Konga page to find correct ones. ===
            Elements products = doc.select("li.item.product.product-item");

            for (Element productElement : products) {
                Product product = new Product();
                product.setSource("Kara");

                // Extracting name
                String name = productElement.select("h2.product.name.product-item-name")
                        .attr("title");
                product.setName(name);

                // Extracting price
                String priceText = productElement.select("span.price")
                        .text().replace("₦", "").
                        replace(",", "")
                        .trim();
                try {
                    product.setPrice(Double.parseDouble(priceText));
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse price for Kara product: " + name);
                    continue;
                }

                // Extracting image URL
                String imageUrl = productElement.select("img.product-image-photo.lazy")
                        .attr("src");
                product.setImageUrl(imageUrl);

                // Extracting product URL
                String productUrl = productElement.select("div.product_image > a").attr("href");
                product.setProductUrl(productUrl);

                // Rating field has been removed as per the user's request.

                productRepository.save(product);
            }
        } catch (IOException e) {
            System.err.println("Error crawling Kara: " + e.getMessage());
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
