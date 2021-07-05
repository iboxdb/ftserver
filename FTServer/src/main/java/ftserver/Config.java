package ftserver;

public final class Config {

    public static long mb(long len) {
        return 1024L * 1024L * len;
    }
    public static final long lowReadonlyCache = Config.mb(2);

    public static final long DSize = 1L;

    public static long SwitchToReadonlyIndexLength = mb(500L * 1L) / DSize;

    public static long Readonly_CacheLength = mb(32);

    public static long Readonly_MaxDBCount = mb(2000) / mb(32) / DSize;

    public static long ItemConfig_CacheLength = mb(256);
    public static int ItemConfig_SwapFileBuffer = (int) mb(20);

}
