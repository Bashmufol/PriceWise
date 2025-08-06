package com.bash.pricewise.service.crawler;

import org.springframework.stereotype.Component;

// Blueprint for crawling each website
@Component
public interface WebsiteCrawler {
    void crawl(String searchTerm);
    String getBaseUrl();
}
