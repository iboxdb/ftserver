package ftserver;

import iboxdb.localserver.*;
import java.util.Arrays;

public class ReadonlyList implements java.io.Closeable {

    public static class AutoBoxHolder {

        public final long address;
        public final AutoBox Auto;

        public AutoBoxHolder(AutoBox auto, long a) {
            Auto = auto;
            address = a;
        }
    }

    AutoBoxHolder[] list = new AutoBoxHolder[0];

    public int length() {
        return list.length;
    }

    public void switchIndexToReadonly() {
        App.log("Switch Readonly DB " + list.length);
        long addr = list[list.length - 1].address;
        AutoBox auto = CreateAutoBox(addr, true);
        list[list.length - 1] = new AutoBoxHolder(auto, addr);
    }

    public AutoBox get(int pos) {
        if (Config.Readonly_MaxDBCount < 2) {
            Config.Readonly_MaxDBCount = 2;
        }
        if (pos < (list.length - Config.Readonly_MaxDBCount)) {
            AutoBoxHolder o = list[pos];
            if (o.Auto != null) {
                App.log("Out of Cache " + (pos) + " / " + list.length + " , set Config.Readonly_MaxDBCount bigger");
                list[pos] = new AutoBoxHolder(null, o.address);
            }
        }
        AutoBoxHolder a = list[pos];
        if (a.Auto != null) {
            return a.Auto;
        }
        ReadonlyIndexServer server = new ReadonlyIndexServer();
        server.OutOfCache = true;
        if (Config.DSize > 1) {
            App.log("Use No Cache DB " + a.address);
        }
        return server.getInstance(a.address).get();
    }

    public void tryCloseOutOfCache(AutoBox auto) {
        DatabaseConfig cfg = auto.getDatabase().getConfig();
        if (cfg instanceof ReadonlyIndexServer.ReadonlyConfig) {
            if (((ReadonlyIndexServer.ReadonlyConfig) cfg).OutOfCache) {
                if (Config.DSize > 1) {
                    App.log("close No Cache DB " + auto.getDatabase().localAddress());
                }
                auto.getDatabase().close();
            }
        }
    }

    public void add(long addr, boolean isReadonly) {
        AutoBox auto = CreateAutoBox(addr, isReadonly);
        AutoBoxHolder[] t = Arrays.copyOf(list, list.length + 1);
        t[t.length - 1] = new AutoBoxHolder(auto, addr);
        list = t;
    }

    private AutoBox CreateAutoBox(long addr, boolean isReadonly) {
        AutoBox auto;
        if (isReadonly) {
            if (Config.Readonly_CacheLength < Config.lowReadonlyCache) {
                auto = null;
            } else {
                ReadonlyIndexServer server = new ReadonlyIndexServer();
                auto = server.getInstance(addr).get();

            }
        } else {
            auto = new IndexServer().getInstance(addr).get();
        }
        return auto;
    }

    @Override
    public void close() {
        if (list != null) {
            for (AutoBoxHolder a : list) {
                if (a.Auto != null) {
                    a.Auto.getDatabase().close();
                }
            }
        }
        list = null;
    }
}
