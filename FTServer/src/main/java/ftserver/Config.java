package ftserver;

//Default for 4GB Setting
public final class Config {

    public static long mb(long len) {
        return 1024L * 1024L * len;
    }
    public static final long lowReadonlyCache = Config.mb(8);

    public static final long DSize = 1L;

    public static long SwitchToReadonlyIndexLength = mb(500L * 1L) / DSize;

    public static long Readonly_CacheLength = mb(32);

    //Set 1400 MB Readonly Index Cache
    public static long Readonly_MaxDBCount = mb(1400) / mb(32) / DSize;

    //HTML Page Cache, this should set bigger, if have more memory. 
    public static long ItemConfig_CacheLength = mb(256);
    public static int ItemConfig_SwapFileBuffer = (int) mb(20);

    //this should less than 2/3 MaxMemory
    public static long minCache() {
        return SwitchToReadonlyIndexLength + Readonly_CacheLength * Readonly_MaxDBCount + ItemConfig_CacheLength
                + ItemConfig_SwapFileBuffer * 2;
    }

}
