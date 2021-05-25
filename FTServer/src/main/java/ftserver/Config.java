package ftserver;

public final class Config {

    public static long mb(long len) {
        return 1024L * 1024L * len;
    }

    public static long SwitchToReadonlyIndexLength = mb(500L * 1L);

    public static long Readonly_CacheLength = mb(32);

    public static long ItemConfig_CacheLength = mb(256);
    public static int ItemConfig_SwapFileBuffer = (int) mb(20);

}
