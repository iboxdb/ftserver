package ftserver;

import iboxdb.localserver.*;
import iboxdb.localserver.io.*;
import java.io.*;

import static ftserver.App.*;

public class ReadonlyIndexServer extends LocalDatabaseServer {

    @Override
    protected DatabaseConfig
            BuildDatabaseConfig(long address) {
        return new ReadonlyConfig(address);
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
