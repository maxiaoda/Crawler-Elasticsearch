# Crawler-Elasticsearch:多线程爬虫与ES数据分析
使用 Java 编写爬虫，实现对新浪新闻站的 HTTP 请求、模拟登录、HTML 解析的功能。
筛选链接循环爬取新闻站内内容，连接 MySQL 数据库实现断点续传功能，使用 Elasticsearch 分析数据，并完成一个简单的搜索引擎。 

* 使用 Git 进行版本控制，小步提交 PR 至 Github 主分支，用 Maven 进行依赖包的管理，
CircleCI 进行自动化测试，在生命周期绑定 Checkstyle、SpotBugs 插件保证代码质量。 
* 使用 Flyway 自动迁移工具完成数据库初始化建表及添加原始数据工作，用 MyBatis实现数据与 Java 对象的关系映射，
对 MySQL 数据库进行索引优化，使百万级新闻内容的查找效率显著提升。
* 采用多线程完成爬虫任务，提高爬取效率 3 倍，使用 Elasticsearch 搜索引擎进行新闻内容的全文检索，实现了百万级文本内容的快速搜索功能。

## How to build

clone 项目至本地目录：

```shell
git clone https://github.com/maxiaoda/Crawler-Elasticsearch.git
```

从 Docker 启动 MySQL 数据库：

- [Docker 下载地址](https://www.docker.com/)
- 如果需要持久化数据需要配置 -v 磁盘文件映射
```shell
docker run --name crawler -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -d mysql
```
MySQL链接:
`jdbc:mysql://localhost:3306/crawler`

使用 IDEA 打开项目，刷新 Maven，再使用开源数据库迁移工具 Flyway 完成自动建表工作：

```shell
mvn flyway:migrate
```

项目测试：

```shell
mvn verify
```

运行项目：

- Run CrawlerController，即可运行该项目~