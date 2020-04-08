package com.github.maxiaoda.service;

import com.github.maxiaoda.dao.CrawlerDao;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Crawler extends Thread {
    private CrawlerDao dao;

    public Crawler(CrawlerDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {
        try {
            String link;

            //先从数据库里拿出来一个链接，（拿出来并从数据库中删除）
            while ((link = dao.getNextLinkThenDelete()) != null) {
                if (dao.isLinkProcessed(link) || link.contains("https:\\/\\/")) {
                    //符合条件的的链接，返回
                    continue;
                }
                if (isNewsLink(link)) {
                    Document doc = httpGetAndParseHtml(link);
                    parseUrlsFromPageAndStoreIntoDatabase(doc);
                    storeIntoDataOfNewsPage(link, doc);
                    dao.insertProcessedLink(link);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //将<a>里的<href>加入到待处理的链接池（linkPool）
    private void parseUrlsFromPageAndStoreIntoDatabase(Document doc) {
        for (Element aTagLink : doc.select("a")) {
            String href = aTagLink.attr("href");
            if (!href.toLowerCase().startsWith("javascript")) {
                dao.insertToBeProcessedLink(href);
            }
        }
    }

    //储存新闻页面的数据，如果不是就什么也不做
    private void storeIntoDataOfNewsPage(String link, Document doc) throws SQLException {
        ArrayList<Element> articleTags = doc.select("article");

        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                String content = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));

                System.out.println(link);
                System.out.println(title);
                dao.insertNewsIntoDatabase(link, title, content);
            }
        }
    }

    private Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //处理符合条件的
        if (link.startsWith("//")) {
            link = "https://" + link;
        }

        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");

        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            return Jsoup.parse(html);
        }
    }

    //只要新闻有关的，排除其它页面
    private static boolean isNewsLink(String link) {
        return isNotLoginPassport(link) && isNewsPage(link) || isSinaLink(link);
    }

    private static boolean isSinaLink(String link) {
        return "https://news.sina.cn".equals(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLoginPassport(String link) {
        return !link.contains("passport.sina.cn");
    }
}
