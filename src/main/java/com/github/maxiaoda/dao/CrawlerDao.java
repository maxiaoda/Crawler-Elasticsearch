package com.github.maxiaoda.dao;

import java.sql.SQLException;

public interface CrawlerDao {

    String getNextLinkThenDelete() throws SQLException;

    void insertNewsIntoDatabase(String url, String title, String content) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

    void insertToBeProcessedLink(String href);

    void insertProcessedLink(String link);
}
