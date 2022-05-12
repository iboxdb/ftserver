package ftserver;

import iboxdb.localserver.*;
import iboxdb.localserver.io.*;
import ftserver.fts.Engine;

import static ftserver.App.*;

public class IndexServer extends LocalDatabaseServer {

    public static long ItemDB = 2L;
    public static long IndexDBStart = 10L;

    @Override
    protected DatabaseConfig BuildDatabaseConfig(long address) {
        if (address >= IndexDBStart) {
            return new IndexConfig();
        }
        if (address == ItemDB) {
            return new ItemConfig();
        }
        return null;
    }

    private static class ItemConfig extends BoxFileStreamConfig {

        public ItemConfig() {

            CacheLength = Config.ItemConfig_CacheLength;
            SwapFileBuffer = Config.ItemConfig_SwapFileBuffer;
            FileIncSize = Config.ItemConfig_SwapFileBuffer;
            if (App.IsAndroid) {
                ReadStreamCount = 1;
            }
            log("ItemConfig CacheLength = " + (CacheLength / 1024L / 1024L) + " MB");
            log("ItemConfig SwapFileBuffer = " + (SwapFileBuffer / 1024L / 1024L) + " MB");
            log("ItemConfig ReadStreamCount = " + ReadStreamCount);

            ensureTable(PageSearchTerm.class, "/PageSearchTerm", "time", "keywords(" + PageSearchTerm.MAX_TERM_LENGTH + ")", "uid");
            ensureTable(Page.class, "Page", "textOrder");
            //the 'textOrder' is used to control url's order
            ensureIndex(Page.class, "Page", "url(" + Page.MAX_URL_LENGTH + ")", "textOrder");
        }
    }

    //this IndexConfig will use IndexStream() to delay index-write, 
    //other application's Tables, should place into ItemConfig 
    private static class IndexConfig extends BoxFileStreamConfig {

        public IndexConfig() {

            CacheLength = Config.Index_CacheLength;
            SwapFileBuffer = Config.ItemConfig_SwapFileBuffer;
            //this size trigger "SWITCH" in Flush()
            FileIncSize = Config.ItemConfig_SwapFileBuffer;

            if (App.IsAndroid) {
                ReadStreamCount = 1;
            }

            //log("DB Cache = " + lenMB + " MB");
            log("DB Switch Length = " + (CacheLength / 1024L / 1024L) + " MB");
            Engine.Instance.Config(this);
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

        long length = 0;

        @Override
        public void SetLength(long value) {
            super.SetLength(value);
            length = value;
        }

        @Override
        public void Flush() {
            super.Flush();
            if (length > Config.SwitchToReadonlyIndexLength) {

                App.Indices.switchIndexToReadonly();
                long addr = App.Index.getDatabase().localAddress();
                addr++;
                log("\r\nSwitch To DB (" + addr + ")");
                App.Indices.add(addr, false);

                App.Index = App.Indices.get(App.Indices.length() - 1);

                System.gc();
            }
        }

    }

}
