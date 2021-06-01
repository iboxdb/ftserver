package ftserver;

import iboxdb.localserver.*;
import iboxdb.localserver.io.*;
import java.io.*;

import static ftserver.App.*;

public class ReadonlyIndexServer extends LocalDatabaseServer {

    private static long lowReadonlyCache = Config.mb(1);

    public static AutoBox TryReadonly(AutoBox auto) {
        if (Config.Readonly_CacheLength < lowReadonlyCache) {
            if (auto.getDatabase().getConfig() instanceof ReadonlyConfig) {
                ReadonlyIndexServer server = new ReadonlyIndexServer();
                long resetCache = Config.SwitchToReadonlyIndexLength / 5;
                if (resetCache < lowReadonlyCache) {
                    resetCache = lowReadonlyCache + 1;
                }
                server.resetCacheLength = resetCache;
                //App.log("Reset Readonly Cache to " + resetCache);
                return server.getInstance(auto.getDatabase().localAddress()).get();
            }
        }
        return auto;
    }

    private long resetCacheLength = Config.Readonly_CacheLength;

    @Override
    protected DatabaseConfig
            BuildDatabaseConfig(long address) {
        ReadonlyConfig cfg = new ReadonlyConfig(address, resetCacheLength);
        return cfg;
    }

    private static class ReadonlyConfig
            extends ReadonlyStreamConfig {

        private long address;

        public ReadonlyConfig(long address, long cache) {
            super(GetStreamsImpl(address, cache));
            this.address = address;
            this.CacheLength = cache;
        }

        private static File[] GetStreamsImpl(long address, long cache) {
            if (cache < lowReadonlyCache) {
                //this Config will be replaced in TryReadonly().
                //Cache too low can't work.
                return new File[0];
            }
            String pa = BoxFileStreamConfig.RootPath + ReadonlyStreamConfig.GetNameByAddrDefault(address);
            if (App.IsAndroid) {
                return new File[]{new File(pa)};
            }
            return new File[]{new File(pa), new File(pa)};
        }

    }

    public static void DeleteOldSwap(long address) {
        String pa = BoxFileStreamConfig.RootPath + ReadonlyStreamConfig.GetNameByAddrDefault(address);
        pa += ".swp";
        try {
            new File(pa).delete();
        } catch (Throwable ex) {
            log("Can't Delete " + pa);
        }
    }
}
