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

@Service
@RequiredArgsConstructor
public class JijiCrawler implements WebsiteCrawler {
    private final ProductRepository productRepository;
    final String baseUrl = "https://jiji.ng/search?query=";

    @Override
    public void crawl(String searchTerm) {
        String url = baseUrl + searchTerm.replace(" ", "+");

        System.out.println("Crawling Jiji: " + url);

        sleep();

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36")
                    .timeout(5000)
                    .get();

            // Extracting product from the target website
            Elements products = doc.select(".masonry-item");

            for (Element productElement : products) {
                Product product = new Product();
                product.setSource("Jiji");

                // Extracting name of product
                String name = productElement.select("div.b-advert-title-inner.qa-advert-title.b-advert-title-inner--div").text();
                product.setName(name);

                // Extracting price of product
                String priceText = productElement.select("div.qa-advert-price")
                        .text()
                        .replace("₦", "")
                        .replace(",", "").trim();
                try {
                    product.setPrice(Double.parseDouble(priceText));
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse price for Kara product: " + name);
                    continue;
                }

                // Extracting image URL
                String imageUrl = productElement.select("picture > img").attr("src");
                product.setImageUrl(imageUrl);

                // Extracting product URL
                String productUrl = "https://jiji.ng" + productElement.select("a.b-list-advert-base.b-list-advert-base--gallery.qa-advert-list-item").attr("href");
                product.setProductUrl(productUrl);

                productRepository.save(product);
            }
        } catch (IOException e) {
            System.err.println("Error crawling Jiji: " + e.getMessage());
        }
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    // Helper method to simulate delay between requests for ethical scraping
    private void sleep() {
        try {
            Thread.sleep(4000); // Wait for 4 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
