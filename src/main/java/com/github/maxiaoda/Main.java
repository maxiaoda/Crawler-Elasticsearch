package com.github.maxiaoda;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {

        //待处理的链接池
        List<String> linkPool = new ArrayList<>();

        //已经处理的链接池
        Set<String> processLinks = new HashSet<>();
        linkPool.add("https://sina.cn");

        while (!linkPool.isEmpty()) {

            //remove 删除并返回值，ArrayList从尾部删除更有效。
            String link = linkPool.remove(linkPool.size() - 1);
            if (processLinks.contains(link) || link.contains("https:\\/\\/")) {

                //符合条件的的链接，返回
                continue;
            }

            //只要新闻有关的，排除其它页面
            if (isNewsLink(link)) {
                Document doc = httpGetAndParseHtml(link);

                //将<a>里的<href>加入到待处理的链接池（linkPool）
                addHrefOfTheATagIntoLinkPool(linkPool, doc);

                //储存新闻页面的数据，如果不是就什么也不做
                storeIntoDataOfNewsPage(link, doc);

                //添加到processLinks
                processLinks.add(link);
            }
        }//不感兴趣不处理，返回
    }

    private static void addHrefOfTheATagIntoLinkPool(List<String> linkPool, Document doc) {
        ArrayList<Element> aTagLinksElement = doc.select("a");

        for (Element aTagLink : aTagLinksElement) {
            linkPool.add(aTagLink.attr("href"));
        }
    }

    private static void storeIntoDataOfNewsPage(String link, Document doc) {
        ArrayList<Element> articleTags = doc.select("article");

        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                System.out.println(link);
                System.out.println(title);

//                //文章内容
//                System.out.println(articleTags.get(0).child(5).select("p"));
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        //处理符合条件的
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");

        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();

            String html = EntityUtils.toString(entity1, "UTF-8");
            return Jsoup.parse(html);
        }
    }

    private static boolean isNewsLink(String link) {
        return isNotLoginPassport(link) && isNewsPage(link) || isSinaLink(link);

    }

    private static boolean isSinaLink(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLoginPassport(String link) {
        return !link.contains("passport.sina.cn");
    }
}
