package ftserver;

import iBoxDB.LocalServer.*;
import iBoxDB.LocalServer.IO.*;

import static ftserver.App.*;
import ftserver.fts.Engine;

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
            long tm = java.lang.Runtime.getRuntime().maxMemory();

            CacheLength = tm / 3;
            //if update the metadata, set low cache
            //cfg.CacheLength = cfg.mb(128);

            FileIncSize = (int) mb(4);
            SwapFileBuffer = (int) mb(4);
            log("DB Cache=" + CacheLength / 1024 / 1024 + "MB"
                    + " AppMEM=" + tm / 1024 / 1024 + "MB");
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

    private static class IndexStream implements IBStream {

        private IBStream s;

        public IndexStream(IBStream iBStream) {
            this.s = iBStream;
        }

        @Override
        public void BeginWrite(long appID, int maxLen) {
            DelayService.delay();
            s.BeginWrite(appID, maxLen);
        }

        @Override
        public void Write(long position, byte[] buffer, int offset, int count) {
            s.Write(position, buffer, offset, count);
        }

        @Override
        public int Read(long position, byte[] buffer, int offset, int count) {
            return s.Read(position, buffer, offset, count);
        }

        @Override
        public long Length() {
            return s.Length();
        }

        @Override
        public void Dispose() {
            if (s != null) {
                s.Dispose();
                s = null;
            }
        }

        @Override
        public void EndWrite() {
            s.EndWrite();
        }

        @Override
        public void Flush() {
            s.Flush();
        }

        @Override
        public void SetLength(long value) {
            s.SetLength(value);
        }
    }

}
