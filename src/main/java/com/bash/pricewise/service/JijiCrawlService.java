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

@Service
@RequiredArgsConstructor
public class JijiCrawlService implements EcomCrawl {
    private final ProductRepository productRepository;
    @Override
    public void crawl(String searchTerm) {
        final String jijiBaseUrl = "https://jiji.ng/search?query=";
        String url = jijiBaseUrl + searchTerm.replace(" ", "+");

        System.out.println("Crawling Jiji: " + url);

//        sleep();

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
