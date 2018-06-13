## Lightweight Full Text Search Server for Java

### Setup

```
Use Netbeans to build or download fts.zip(WAR) from Release
Deploy to tomcat/jetty
```


![](https://github.com/iboxdb/ftserver/raw/master/FTServer/web/css/fts2.png)

### Dependencies
[iBoxDB](http://www.iboxdb.com)

[FTS Engine](https://github.com/iboxdb/full-text-search)

[Semantic-UI](http://semantic-ui.com/)

[Jodd](http://jodd.org/)


#### Update to Private Server
Modify 
```java
public Page Page.get(String url);
```
Set your private WebSite text
```java
Page page = new Page();
page.url = url;
page.title = "..."
page.description = "..."
page.content = "..."
return page;
```

#### More
[The results order details & C# ASP.NET version](https://github.com/iboxdb/ftserver-cs#the-results-order)



