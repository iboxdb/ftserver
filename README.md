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


####[the Results Order details](https://github.com/iboxdb/ftserver-cs#the-results-order)


### Known Issues
doesn't support the URL includes '&,?', need an URLEncode() operation. [Hotfix](https://github.com/iboxdb/ftserver/commit/f0f06e90683f04d5ede21753d6706f3ea62e75aa)
