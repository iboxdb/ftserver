package ftserver;

import iboxdb.localserver.*;
import iboxdb.localserver.io.*;
import java.io.*;

import static ftserver.App.*;

public class ReadonlyIndexServer extends LocalDatabaseServer {
    
    private static long lowReadonlyCache = Config.mb(1);
    
    public static AutoBox TryReadonly(AutoBox auto, long resetCache) {
        if (Config.Readonly_CacheLength < lowReadonlyCache) {
            if (auto.getDatabase().getConfig() instanceof ReadonlyConfig) {
                ReadonlyIndexServer server = new ReadonlyIndexServer();
                server.resetCacheLength = resetCache;
                //App.log("Reset Readonly Cache to " + resetCache);
                return server.getInstance(auto.getDatabase().localAddress()).get();
            }
        }
        return auto;
    }
    
    private long resetCacheLength = -1;
    
    @Override
    protected DatabaseConfig
            BuildDatabaseConfig(long address) {
        ReadonlyConfig cfg = new ReadonlyConfig(address);
        if (resetCacheLength > 0) {
            cfg.CacheLength = resetCacheLength;
        }
        return cfg;
    }
    
    private static class ReadonlyConfig
            extends ReadonlyStreamConfig {
        
        private long address;
        
        public ReadonlyConfig(long address) {
            super(GetStreamsImpl(address));
            this.address = address;
            this.CacheLength = Config.Readonly_CacheLength;
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
