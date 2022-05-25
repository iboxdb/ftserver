## Full Text Search Engine Server for Java


### User Guide


#### Setup

1. [Install Java 11](https://jdk.java.net/)

2. [Install Maven 3](https://maven.apache.org/)

3. Download this Project.

4. Run

```sh
$ cd FTServer
$ mvn package cargo:run
```

5. Open [http://127.0.0.1:8080/](http://127.0.0.1:8080/)

6. Press [Ctrl-C] to stop the container


![](FTServer/src/main/webapp/css/fts.png)


Input a Full URL to index the Page, then search.


#### Search Format

[Word1 Word2 Word3] => text has **Word1** and **Word2** and **Word3**

["Word1 Word2 Word3"] => text has **"Word1 Word2 Word3"** as a whole

Search [https http] => get almost all pages



### Developer Guide


[Download Netbeans](https://netbeans.apache.org/)


#### Dependencies

[iBoxDB](http://www.iboxdb.com)

[Semantic-UI](http://semantic-ui.com/)

[Jsoup](http://jsoup.org/)


#### The Results Order
The results order based on the **id()** number in **class PageText**,  descending order.

A Page has many PageTexts. if don't need multiple Texts, modify **Html.getDefaultTexts(Page)**, returns only one PageText.

the Page.GetRandomContent() method is used to keep the Search-Page-Content always changing, doesn't affect the real PageText order.

Use the ID number to control the order instead of loading all pages to memory. 
Or load top 100 pages to memory then re-order it by favor. 


#### Search Method
search (... String keywords, long **startId**, long **count**)

**startId** => which ID(the id when you created PageText) to start, 
use (startId=Long.MaxValue) to read from the top, descending order

**count** => records to read,  **important parameter**, the search speed depends on this parameter, not how big the data is.

##### Next Page
set the startId as the last id from the results of search minus one

```java
startId = search( "keywords", startId, count);
nextpage_startId = startId - 1 // this 'minus one' has done inside search()
...
//read next page
search("keywords", nextpage_startId, count)
```

mostly, the nextpage_startId is posted from client browser when user reached the end of webpage, 
and set the default nextpage_startId=Long.MaxValue, 
in javascript the big number have to write as String ("'" + nextpage_startId + "'")



#### Private Server
Open 
```java
public Page Html.get(String url);
```
Set your private WebSite text
```java
Page page = new Page();
page.url = url;
page.title = title;
page.text = replace(doc.body().text());
page... = ...
return page;
```

#### Set Maximum Opened Files

```sh
[user@localhost ~]$ cat /proc/sys/fs/file-max
803882
[user@localhost ~]$ ulimit -a | grep files
open files                      (-n) 500000
[user@localhost ~]$  ulimit -Hn
500000
[user@localhost ~]$ ulimit -Sn
500000
[user@localhost ~]$ 


$ vi /etc/security/limits.conf
*         hard    nofile      500000
*         soft    nofile      500000
root      hard    nofile      500000
root      soft    nofile      500000


[user@localhost ~]$ firewall-cmd --add-port=8080/tcp --permanent
```


#### More

[C# ASP.NET Core Version](https://github.com/iboxdb/ftserver-cs)

[FTServer for Android with APK](https://sourceforge.net/p/ftserver-android/code/)


<br />

![Flag](https://s05.flagcounter.com/count2/Ep/bg_373737/txt_F2F2F2/border_373737/columns_3/maxflags_12/viewers_0/labels_0/pageviews_1/flags_0/percent_0/)



