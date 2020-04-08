package com.github.maxiaoda;

import com.github.maxiaoda.dao.CrawlerDao;
import com.github.maxiaoda.dao.MyBatisCrawlerDao;
import com.github.maxiaoda.service.Crawler;

public class CrawlerController {
    public static void main(String[] args) {
        CrawlerDao dao = new MyBatisCrawlerDao();

        for (int i = 0; i < 4; i++) {
            new Crawler(dao).start();
        }
    }
}
