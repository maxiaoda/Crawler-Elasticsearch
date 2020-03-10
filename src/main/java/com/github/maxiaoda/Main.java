package com.github.maxiaoda;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "root";

    @SuppressFBWarnings
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:C:\\Users\\abc\\IdeaProjects\\Crawler-Elasticsearch\\news", USER_NAME, PASSWORD);
        while (true) {

            List<String> linkPool = loadUrlsFromDatabase(connection, "select LINK from LINKS_TO_BE_PROCESSED");

            if (linkPool.isEmpty()) {
                break;
            }

            //remove 删除并返回值，ArrayList从尾部删除更有效。
            String link = linkPool.remove(linkPool.size() - 1);
            insertLinkIntoDatabase(connection, link, "delete from LINKS_TO_BE_PROCESSED where link = ?");

            if (isLinkProcessed(connection, link) || link.contains("https:\\/\\/")) {
                //符合条件的的链接，返回
                continue;
            }

            if (isNewsLink(link)) {
                Document doc = httpGetAndParseHtml(link);
                parseUrlsFromPageAndStoreIntoDatabase(connection, doc);
                storeIntoDataOfNewsPage(link, doc);
                insertLinkIntoDatabase(connection, link, "insert into LINKS_ALREADY_PROCESSED(LINK) values (?)");
            }
        }
    }

    //将<a>里的<href>加入到待处理的链接池（linkPool）
    private static void parseUrlsFromPageAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        for (Element aTagLink : doc.select("a")) {
            String href = aTagLink.attr("href");
            insertLinkIntoDatabase(connection, href, "insert into LINKS_TO_BE_PROCESSED(LINK) values (?)");
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select LINK from LINKS_ALREADY_PROCESSED where LINK = ?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }

    //每次处理后更新数据库,从待处理池中取一个；处理完后从池中删除，包括数据库。
    private static void insertLinkIntoDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    //待处理的链接池,从数据库加载即将处理的链接
    private static List<String> loadUrlsFromDatabase(Connection connection, String sql) throws SQLException {
        List<String> results = new ArrayList<>();
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                results.add(resultSet.getString(1));
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return results;
    }

    //储存新闻页面的数据，如果不是就什么也不做
    private static void storeIntoDataOfNewsPage(String link, Document doc) {
        ArrayList<Element> articleTags = doc.select("article");

        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                System.out.println(link);
                System.out.println(title);


//                //文章内容
//               articleTags.get(0).child(5).select("p");
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //处理符合条件的
        if (link.startsWith("//")) {
            link = "https://" + link;
        }

        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");

        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {

            //System.out.println(response1.getStatusLine());
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
        return "https://sina.cn".equals(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLoginPassport(String link) {
        return !link.contains("passport.sina.cn");
    }
}
