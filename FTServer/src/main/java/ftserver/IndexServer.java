package ftserver;

import iboxdb.localserver.*;
import iboxdb.localserver.io.*;
import java.util.ArrayList;
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

            log("ItemConfig CacheLength = " + (CacheLength / 1024L / 1024L) + " MB");
            log("ItemConfig SwapFileBuffer = " + (SwapFileBuffer / 1024L / 1024L) + " MB");

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

            CacheLength = Config.SwitchToReadonlyIndexLength;
            SwapFileBuffer = Config.ItemConfig_SwapFileBuffer;
            if (SwapFileBuffer < FileIncSize) {
                FileIncSize = SwapFileBuffer;
            }

            //log("DB Cache = " + lenMB + " MB");
            log("DB Switch Length = " + (Config.SwitchToReadonlyIndexLength / 1024L / 1024L) + " MB");
            new Engine().Config(this);
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

                ArrayList<AutoBox> newIndices = new ArrayList<AutoBox>(App.Indices);
                newIndices.remove(newIndices.size() - 1);

                long addr = App.Index.getDatabase().localAddress();
                newIndices.add(new ReadonlyIndexServer().getInstance(addr).get());
                addr++;
                log("\r\nSwitch To DB (" + addr + ")");
                newIndices.add(new IndexServer().getInstance(addr).get());

                for (int i = 0; i < newIndices.size() - 2; i++) {
                    addr = newIndices.get(i).getDatabase().localAddress();
                    newIndices.set(i, new ReadonlyIndexServer().getInstance(addr).get());
                    if (App.IsAndroid) {
                        ReadonlyIndexServer.DeleteOldSwap(addr);
                    }
                }

                App.Indices = newIndices;
                App.Index = newIndices.get(newIndices.size() - 1);
                System.gc();
            }
        }

    }

}
