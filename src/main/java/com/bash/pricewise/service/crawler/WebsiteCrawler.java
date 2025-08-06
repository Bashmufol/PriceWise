package com.bash.pricewise.service.crawler;

public interface WebsiteCrawler {
    void crawl(String searchTerm);
    String getBaseUrl();
}
