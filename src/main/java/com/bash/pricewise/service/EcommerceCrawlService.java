package com.bash.pricewise.service;

import com.bash.pricewise.model.Product;
import com.bash.pricewise.repository.ProductRepository;
import com.bash.pricewise.service.crawler.WebsiteCrawler;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class EcommerceCrawlService {
    private final ProductRepository productRepository;
    private final List<WebsiteCrawler> websiteCrawlers;

    public void crawlAndSaveProducts(String searchTerm) {
        // Clear old data for the given search term to get fresh results
        productRepository.deleteAll();

        System.out.println("Starting web crawl for: " + searchTerm);

        for (WebsiteCrawler crawler : websiteCrawlers) {
            System.out.println("Crawling " + crawler.getBaseUrl() + " for: " + searchTerm);
            crawler.crawl(searchTerm);
        }
        System.out.println("Web crawl finished.");
    }


}
