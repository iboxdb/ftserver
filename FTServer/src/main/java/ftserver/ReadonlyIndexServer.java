package ftserver;

import iboxdb.localserver.*;
import iboxdb.localserver.io.*;
import java.io.*;

import static ftserver.App.*;

public class ReadonlyIndexServer extends LocalDatabaseServer {

    private ReadonlyIndexServer() {
        super();
    }

    public static class AutoBoxHolder extends AutoBox {

        private final long address;

        public AutoBoxHolder(long addr) {
            super(null, addr);
            address = addr;
        }

        public long getLocalAddress() {
            return address;
        }
    }

    private static final long lowReadonlyCache = Config.mb(1);

    public static AutoBox TryReadonly(AutoBox auto) {
        if (auto instanceof AutoBoxHolder) {
            ReadonlyIndexServer server = new ReadonlyIndexServer();
            long resetCache = Config.SwitchToReadonlyIndexLength / 5;
            if (resetCache < lowReadonlyCache) {
                resetCache = lowReadonlyCache + 1;
            }
            server.resetCacheLength = resetCache;
            //App.log("Reset Readonly Cache to " + resetCache);
            return server.getInstance(auto.getDatabase().localAddress()).get();

        }
        return auto;
    }

    public static AutoBox GetReadonly(long addr) {
        if (Config.Readonly_CacheLength < lowReadonlyCache) {
            return new AutoBoxHolder(addr);
        }
        return new ReadonlyIndexServer().getInstance(addr).get();
    }

    public static AutoBox RenewReadonly(AutoBox auto) {
        if (auto instanceof AutoBoxHolder) {
            return auto;
        }
        long addr = auto.getDatabase().localAddress();
        return GetReadonly(addr);

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
            super(GetStreamsImpl(address));
            this.address = address;
            this.CacheLength = cache;
        }

        private static File[] GetStreamsImpl(long address) {
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
