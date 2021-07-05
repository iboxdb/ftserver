package ftserver;

import iboxdb.localserver.*;
import iboxdb.localserver.io.*;
import java.io.*;

import static ftserver.App.*;

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
        boolean OutOfCache;

        public ReadonlyConfig(long address, boolean outOfCache) {
            super(GetStreamsImpl(address, outOfCache));
            this.address = address;
            this.OutOfCache = outOfCache;
            this.CacheLength = Config.Readonly_CacheLength;
            if (this.CacheLength < Config.lowReadonlyCache) {
                this.CacheLength = Config.SwitchToReadonlyIndexLength / 5 + 1;
            }
        }

        private static File[] GetStreamsImpl(long address, boolean outOfCache) {
            String pa = BoxFileStreamConfig.RootPath + ReadonlyStreamConfig.GetNameByAddrDefault(address);

            File[] os = new File[App.IsAndroid || outOfCache ? 1 : 2];
            for (int i = 0; i < os.length; i++) {
                os[i] = new File(pa);
            }
            return os;
        }

    }

}
