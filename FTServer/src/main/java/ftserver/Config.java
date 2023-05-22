package ftserver;

//Default for 4GB Setting
public final class Config {

    public static long mb(long len) {
        return 1024L * 1024L * len;
    }
    public static final long lowReadonlyCache = Config.mb(8);

    public static final long DSize = 1L;

    //only index description, not full text, faster indexing    
    //if wanting more Pages, dotn't care the content, set it to True
    public static boolean DescriptionOnly = false;

    public static long Index_CacheLength = mb(800L) / DSize;

    //this should set bigger than 500MB. 
    //for DescriptionOnly, it can set bigger, because it might load Only 10% to Memory 
    public static long SwitchToReadonlyIndexLength = mb(DescriptionOnly ? 1024L * 2L : 750L) / DSize;

    //Readonly Cache after Switch One Database To Readonly
    public static long Readonly_CacheLength = SwitchToReadonlyIndexLength / 23L;

    //How Many Readonly Databases Having long Cache
    //Set 1000 MB Readonly Index Cache
    public static long Readonly_MaxDBCount = mb(1000) / Readonly_CacheLength / DSize;

    public static long ShortCacheLength = mb(32L * (DescriptionOnly ? 2 : 1));

    //HTML Page Cache, this should set bigger, if having more memory. 
    public static long ItemConfig_CacheLength = mb(256);
    public static int ItemConfig_SwapFileBuffer = (int) mb(20);

    //this should less than 1/2 MaxMemory
    public static long minCache() {
        return Index_CacheLength + Readonly_CacheLength * Readonly_MaxDBCount + ItemConfig_CacheLength
                + ItemConfig_SwapFileBuffer * 2;
    }

}
