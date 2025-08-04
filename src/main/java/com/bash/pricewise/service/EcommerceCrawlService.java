package com.bash.pricewise.service;

import com.bash.pricewise.model.Product;
import com.bash.pricewise.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EcommerceCrawlService {
    private final ProductRepository productRepository;
    private final Random random = new Random();


    public void crawlAndSaveProducts(String searchTerm) {
        // Clear old data for the given search term to get fresh results
        productRepository.deleteAll();

        System.out.println("Starting web crawl for: " + searchTerm);

        // Crawl all three sites
        crawlJumia(searchTerm);
        crawlKara(searchTerm);
        crawlJiji(searchTerm);

        System.out.println("Web crawl finished.");
    }

    // Helper method to simulate a random delay between requests for ethical scraping
    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(random.nextInt(5) + 2); // Wait between 2 and 7 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void crawlJumia(String searchTerm) {
        final String jumiaBaseUrl = "https://www.jumia.com.ng/catalog/?q=";
        String url = jumiaBaseUrl + searchTerm.replace(" ", "+");

        System.out.println("Crawling Jumia: " + url);

        sleep();

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("PriceComparisonApp/1.0 (Nigeria)")
                    .timeout(10000)
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
                String priceText = productElement.select(".prc").text().replace("₦", "").replace(",", "").trim();
                try {
                    product.setPrice(Double.parseDouble(priceText));
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse price for Jumia product: " + name);
                    continue; // Skip this product if price cannot be parsed
                }

                // Extracting image URL
                String imageUrl = productElement.select("img.img").attr("data-src");
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

    private void crawlKara(String searchTerm) {
        final String karaBaseUrl = "https://kara.com.ng/catalogsearch/result/?q=";
        String url = karaBaseUrl + searchTerm.replace(" ", "+");

        System.out.println("Crawling Kara: " + url);
        sleep();

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("PriceComparisonApp/1.0 (Nigeria)")
                    .timeout(10000)
                    .get();

            // === IMPORTANT: These selectors are examples. You must inspect the actual Konga page to find correct ones. ===
            Elements products = doc.select("li.item.product.product-item");

            for (Element productElement : products) {
                Product product = new Product();
                product.setSource("Kara");

                // Extracting name
                String name = productElement.select("h2.product.name.product-item-name").text();
                product.setName(name);

                // Extracting price
                String priceText = productElement.select("span.price")
                        .text()
                        .replace("NGN", "")
                        .replace(",", "")
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
                String productUrl = productElement.select("div.product_image > a")
                        .attr("href");
                product.setProductUrl(productUrl);

                // Rating field has been removed as per the user's request.

                productRepository.save(product);
            }
        } catch (IOException e) {
            System.err.println("Error crawling Kara: " + e.getMessage());
        }
    }

    private void crawlJiji(String searchTerm) {
        final String jijiBaseUrl = "https://jiji.ng/search?query=";
        String url = jijiBaseUrl + searchTerm.replace(" ", "+");

        System.out.println("Crawling Jiji: " + url);

        sleep();

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("PriceComparisonApp/1.0 (Nigeria)")
                    .timeout(10000)
                    .get();

            // === IMPORTANT: These selectors are examples. You must inspect the actual Jiji page to find correct ones. ===
            Elements products = doc.select(".masonry-item");

            for (Element productElement : products) {
                Product product = new Product();
                product.setSource("Jiji");

                // Extracting name
                String name = productElement.select("div.b-advert-title-inner.qa-advert-title.b-advert-title-inner--div").text();
                product.setName(name);

                // Extracting price, which may be in a different format
                String priceText = productElement.select("div.qa-advert-price").text().replace("₦", "").replace(",", "").trim();
                try {
                    product.setPrice(Double.parseDouble(priceText));
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse price for jiji product: " + name);
                    continue;
                }

                // Extract image URL
                String imageUrl = productElement.select("picture > img").attr("src");
                product.setImageUrl(imageUrl);

                // Extract product URL
                String productUrl = "https://jiji.ng" + productElement.select("a.b-list-advert-base.b-list-advert-base--gallery.qa-advert-list-item").attr("href");
                product.setProductUrl(productUrl);

                productRepository.save(product);
            }
        } catch (IOException e) {
            System.err.println("Error crawling Jiji: " + e.getMessage());
        }
    }
}
