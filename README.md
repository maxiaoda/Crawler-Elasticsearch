# Crawler-Elasticsearch:多线程爬虫与ES数据分析
jdbc:h2:file:C:\Users\abc\IdeaProjects\Crawler-Elasticsearch\news

create table LINKS_TO_BE_PROCESSED(
                 link varchar(2000)
             )
             
create table LINKS_ALREADY_PROCESSED(
                 link varchar(2000)
             )
             
create table NEWS
             (
                 ID    bigint primary key auto_increment,
                 TITLE text,
                 CONTENT text,
                 URL varchar(2000),
                 CREATED_AT timestamp,
                 MODIFIED_AT timestamp
             )