package ftserver.zdemo.recommendingSystem;

import iboxdb.localserver.*;
import ftserver.fts.*;
import java.util.*;

public class DemoMainClass {

    public static void main(String[] args) throws Exception {

        DB.root("/tmp/");
        Calendar today = java.util.Calendar.getInstance();
        Engine engine = Engine.Instance;

        //================== Daily Favor Database ================ 
        /*
        if user_1 favored "f100, f200, f300".
        when user_2 favored "f100, f200".
        recommend user_2 the f300 by user_1's record.
         */
        long dailyTrackDB01 = Long.parseLong(
                today.get(Calendar.YEAR) + "" + (today.get(Calendar.MONTH) + 1)
                + "00" + today.get(Calendar.DAY_OF_MONTH));
        //System.out.println(dailyTrackDB01);

        BoxSystem.DBDebug.DeleteDBFiles(dailyTrackDB01);
        DB dailyDB = new DB(dailyTrackDB01);

        engine.Config(dailyDB.getConfig());
        dailyDB.getConfig().ensureTable(Favor.class, "/Favor", "userId", "itemId");
        AutoBox autoDaily = dailyDB.open();

        //add testing data, users with items
        for (long userId = 1; userId < 100; userId++) {
            for (long itemId = 1000 + userId; itemId < 1000 + userId + 10; itemId++) {
                // User-->FavorItems(1000,1001,1002....)
                FavorWhenStay10Sec(engine, autoDaily, userId, itemId);
            }
        }

        //Search FavorItems(1000,1002) --> UserFavorItems(1000,1001,1002....) --> Recommend(1001)
        RealTimeRecommendPrint(engine, autoDaily, new long[]{1055, 1058});

        //=====Another Example, Monthly Database ============================
        long monthlyTrackDB01 = Long.parseLong(
                today.get(Calendar.YEAR) + "" + (today.get(Calendar.MONTH) + 1)
                + "00" + "32");
        //System.out.println(monthlyTrackDB01);

        BoxSystem.DBDebug.DeleteDBFiles(monthlyTrackDB01);
        DB monthlyDB = new DB(monthlyTrackDB01);
        engine.Config(monthlyDB.getConfig());
        monthlyDB.getConfig().ensureTable(RelatedItems.class, "RelatedItems", "id");
        AutoBox autoMonthly = monthlyDB.open();

        BackgroundAnalysis(engine, autoDaily, autoMonthly);
        MonthlyRecommendPrint(engine, autoMonthly, new long[]{1085, 1088});

        autoDaily.getDatabase().close();
        autoMonthly.getDatabase().close();

    }

    private static void FavorWhenStay10Sec(Engine dailyEngine, AutoBox autoDaily, long userId, long itemId) {

        try (Box box = autoDaily.cube()) {
            box.d("/Favor").insert(new Favor(userId, itemId));
            // add item as Text
            dailyEngine.indexText(box, userId, Long.toString(itemId), false);
            box.commit();
        }
    }

    //Do full text search
    private static void RealTimeRecommendPrint(Engine dailyEngine, AutoBox autoDaily, long... relatedItemId) {
        // combin items as Text, for search
        StringBuilder sb = new StringBuilder();
        HashSet<Long> itemSet = new HashSet<Long>(relatedItemId.length + 1);
        for (long l : relatedItemId) {
            sb.append(l).append(" ");
            //itemSet.add(l);
        }

        final long count = Long.MAX_VALUE;
        long startId = Long.MAX_VALUE;

        System.out.println("Daily Recommend");
        try (Box box = autoDaily.cube()) {

            // get the userId he has similar items
            // and use this user's items to recommend current user.
            for (KeyWord kw
                    : dailyEngine.searchDistinct(box, sb.toString(), startId, count)) {

                startId = kw.I - 1;
                long userId = kw.I;
                System.out.print("\r\n" + userId + " -> ");
                for (Favor favor : box.select(Favor.class, "from /Favor where userId == ?", userId)) {
                    if (itemSet.contains(favor.itemId)) {
                        continue;
                    }
                    itemSet.add(favor.itemId);
                    System.out.print(favor.itemId + ",");
                }
            }

        }
        System.out.println("\r\nALL:" + itemSet.toString());

    }

    // Above is all, following is another example.
    //===============================================================================//
    private static void BackgroundAnalysis(Engine engine, AutoBox autoDaily, AutoBox autoMonthly) {
        //this Demo only get the Top 10, not do analysis
        HashMap<Long, Integer> map = new HashMap<Long, Integer>();
        try (Box box = autoDaily.cube()) {
            for (Favor favor : box.select(Favor.class, "from /Favor")) {
                Integer c = map.get(favor.itemId);
                if (c == null) {
                    c = 0;
                }
                c = c + 1;
                map.put(favor.itemId, c);
            }
        }
        Map.Entry<Long, Integer>[] es = map.entrySet().toArray(new Map.Entry[0]);
        Arrays.sort(es, new Comparator<Map.Entry<Long, Integer>>() {
            @Override
            public int compare(Map.Entry<Long, Integer> o1, Map.Entry<Long, Integer> o2) {
                int c = o2.getValue() - o1.getValue();
                if (c == 0) {
                    c = o2.getKey().compareTo(o1.getKey());
                }
                return c;
            }
        });

        ArrayList<RelatedItems> items = new ArrayList<RelatedItems>();
        for (int t = 0; t < 2; t++) {
            RelatedItems item = new RelatedItems();
            for (int i = t * 10; i < t * 10 + 10; i++) {
                item.text = item.text + (es[i].getKey() + " ");
                item.items = Arrays.copyOf(item.items, item.items.length + 1);
                item.items[item.items.length - 1] = es[i].getKey();
            }
            items.add(item);
        }
        try (Box box = autoMonthly.cube()) {
            for (RelatedItems item : items) {
                item.id = box.newId();
                box.d("RelatedItems").insert(item);
                engine.indexText(box, item.id, item.text, false);
                //System.out.println(item.text);
            }
            box.commit();
        }

    }

    private static void MonthlyRecommendPrint(Engine monthlyEngine, AutoBox autoMonthly, long... relatedItemId) {
        StringBuilder sb = new StringBuilder();
        HashSet<Long> itemSet = new HashSet<Long>(relatedItemId.length + 1);
        for (long l : relatedItemId) {
            sb.append(l).append(" ");
            itemSet.add(l);
        }

        final long HowManyReference = Long.MAX_VALUE;
        long startId = Long.MAX_VALUE;
        try (Box box = autoMonthly.cube()) {
            System.out.print("\r\nMonthly Recommend\r\n");
            for (KeyWord kw
                    : monthlyEngine.searchDistinct(box, sb.toString(), startId, HowManyReference)) {
                RelatedItems item = box.d("RelatedItems", kw.I).select(RelatedItems.class);
                for (Object l : item.items) {
                    if (!itemSet.contains(l)) {
                        System.out.print(l + " ");
                    }
                }
            }
        }
    }

}
