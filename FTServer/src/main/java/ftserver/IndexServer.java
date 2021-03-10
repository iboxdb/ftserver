package ftserver;

import iboxdb.localserver.*;
import iboxdb.localserver.io.*;

import static ftserver.App.*;
import ftserver.fts.Engine;
import java.io.File;

public class IndexServer extends LocalDatabaseServer {

    @Override
    protected DatabaseConfig BuildDatabaseConfig(long address) {
        if (address == 1) {
            return new IndexConfig();
        }
        if (address == 2) {
            return new ItemConfig();
        }
        return null;
    }

    private static class ItemConfig extends BoxFileStreamConfig {

        public ItemConfig() {
            CacheLength = mb(64);
            ensureTable(PageSearchTerm.class, "/PageSearchTerm", "time", "keywords(" + PageSearchTerm.MAX_TERM_LENGTH + ")", "uid");
            ensureTable(Page.class, "/PageBegin", "textOrder", "url(" + Page.MAX_URL_LENGTH + ")");
        }
    }

    //this IndexConfig will use IndexStream() to delay index-write, 
    //other application's Tables, should place into ItemConfig 
    private static class IndexConfig extends BoxFileStreamConfig {

        public IndexConfig() {
            //-Xmx4G
            long tm = java.lang.Runtime.getRuntime().maxMemory();
            File mvnConfig = new File(".mvn/jvm.config");
            if (mvnConfig.exists()) {
                log("maven -Xmx setting " + mvnConfig.getAbsolutePath());
            }
            

            CacheLength = tm / 3;
            IndexAPI.HuggersMemory = tm / 8;
            if (IndexAPI.HuggersMemory > mb(550)) {
                IndexAPI.HuggersMemory = mb(550);
            }

            FileIncSize = (int) mb(16);
            SwapFileBuffer = (int) mb(16);

            log("-Xmx = " + (tm / 1024 / 1024) + " MB");
            log("DB Cache=" + CacheLength / 1024 / 1024 + "MB"
                    + " AppMEM=" + tm / 1024 / 1024 + "MB");
            log("Huggers Cache = " + (IndexAPI.HuggersMemory / 1024 / 1024) + " MB");

            new Engine().Config(this);

            ensureTable(Page.class, "Page", "url(" + Page.MAX_URL_LENGTH + ")");
            ensureIndex(Page.class, "Page", true, "textOrder");

            ensureTable(PageText.class, "PageText", "id");
            ensureIndex(PageText.class, "PageText", false, "textOrder");
        }

        @Override
        public IBStream CreateStream(String path, StreamAccess access) {
            IBStream s = super.CreateStream(path, access);
            if (access == StreamAccess.ReadWrite) {
                return new IndexStream(s);
            }
            return s;
        }

    }

    private static class IndexStream extends IBStreamWrapper {

        public IndexStream(IBStream iBStream) {
            super(iBStream);
        }

        @Override
        public void BeginWrite(long appID, int maxLen) {
            DelayService.delay();
            super.BeginWrite(appID, maxLen);
        }

    }

}
