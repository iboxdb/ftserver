package ftserver;

import iboxdb.localserver.*;
import iboxdb.localserver.io.*;
import java.io.*;

public class ReadonlyIndexServer extends LocalDatabaseServer {

    public boolean OutOfCache = false;

    @Override
    protected DatabaseConfig
            BuildDatabaseConfig(long address) {
        ReadonlyConfig cfg = new ReadonlyConfig(address, OutOfCache);
        return cfg;
    }

    public static class ReadonlyConfig
            extends ReadonlyStreamConfig {

        private long address;
        public boolean OutOfCache;

        public ReadonlyConfig(long address, boolean outOfCache) {
            super(GetStreamsImpl(address, outOfCache));
            this.address = address;
            this.OutOfCache = outOfCache;
            this.CacheLength = Config.Readonly_CacheLength;
            if (outOfCache) {
                this.CacheLength = Config.ShortCacheLength;
            }
            if (this.CacheLength < Config.lowReadonlyCache) {
                this.CacheLength = Config.lowReadonlyCache;
            }
        }

        private static File[] GetStreamsImpl(long address, boolean outOfCache) {
            String pa = DatabaseConfig.getFileName(address, null);

            File[] os = new File[App.IsAndroid || outOfCache ? 1 : 2];
            for (int i = 0; i < os.length; i++) {
                os[i] = new File(pa);
            }
            return os;
        }

    }

}
