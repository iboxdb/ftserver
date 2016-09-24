//Free
package iBoxDB.fulltext;

import iBoxDB.LocalServer.*;
import java.util.*;

public class Engine {

    final Util util = new Util();
    final StringUtil sUtil = new StringUtil();

    public long maxSearchTime = Long.MAX_VALUE;

    public void Config(DatabaseConfig config) {
        KeyWord.config(config);
    }

    public long indexText(Box box, long id, String str, boolean isRemove) {
        if (id == -1) {
            // -1 is internal default value as NULL
            return -1;
        }
        long itCount = 0;
        char[] cs = sUtil.clear(str);
        ArrayList<KeyWord> map = util.fromString(id, cs, true);

        HashSet<String> words = new HashSet<String>();
        for (KeyWord kw : map) {
            insertToBox(box, kw, words, isRemove);
            itCount++;
        }

        return itCount;
    }

    public long indexTextNoTran(AutoBox auto, final int commitCount, long id, String str, boolean isRemove) {
        if (id == -1) {
            // -1 is internal default value as NULL
            return -1;
        }
        long itCount = 0;
        char[] cs = sUtil.clear(str);
        ArrayList<KeyWord> map = util.fromString(id, cs, true);

        HashSet<String> words = new HashSet<String>();
        Box box = null;
        int ccount = 0;
        for (KeyWord kw : map) {
            if (box == null) {
                box = auto.cube();
                ccount = commitCount;
            }
            insertToBox(box, kw, words, isRemove);
            itCount++;
            if (--ccount < 1) {
                box.commit().Assert();
                box = null;
            }
        }
        if (box != null) {
            box.commit().Assert();
        }
        return itCount;
    }

    private void insertToBox(Box box, KeyWord kw, HashSet<String> insertedWords, boolean isRemove) {
        Binder binder;
        if (kw instanceof KeyWordE) {
            if (insertedWords.contains(kw.getKeyWord().toString())) {
                return;
            }
            insertedWords.add(kw.getKeyWord().toString());
            binder = box.d("/E", kw.getKeyWord(), kw.getID(), kw.getPosition());
        } else {
            binder = box.d("/N", kw.getKeyWord(), kw.getID(), kw.getPosition());
        }
        if (isRemove) {
            binder.delete();
        } else {
            binder.insert(kw);
        }
    }

    public LinkedHashSet<String> discover(final Box box,
            char efrom, char eto, int elength,
            char nfrom, char nto, int nlength) {
        LinkedHashSet<String> list = new LinkedHashSet<String>();
        Random ran = new Random();
        if (elength > 0) {
            int len = ran.nextInt(KeyWord.MAX_WORD_LENGTH) + 1;
            char[] cs = new char[len];
            for (int i = 0; i < cs.length; i++) {
                cs[i] = (char) (ran.nextInt(eto - efrom) + efrom);
            }
            KeyWordE kw = new KeyWordE();
            kw.setKeyWord(new String(cs));
            for (KeyWord tkw : lessMatch(box, kw)) {
                String str = tkw.getKeyWord().toString();
                if (str.length() < 3 || sUtil.mvends.contains(str)) {
                    continue;
                }
                int c = list.size();
                list.add(str);
                if (list.size() > c) {
                    elength--;
                    if (elength <= 0) {
                        break;
                    }
                }
            }
        }
        if (nlength > 0) {
            char[] cs = new char[2];
            for (int i = 0; i < cs.length; i++) {
                cs[i] = (char) (ran.nextInt(nto - nfrom) + nfrom);
            }
            KeyWordN kw = new KeyWordN();
            kw.longKeyWord(cs[0], cs[1], (char) 0);
            for (KeyWord tkw : lessMatch(box, kw)) {
                int c = list.size();
                list.add(((KeyWordN) tkw).toKString());
                if (list.size() > c) {
                    nlength--;
                    if (nlength <= 0) {
                        break;
                    }
                }
            }
        }
        return list;
    }

    public String getDesc(String str, KeyWord kw, int length) {
        return sUtil.getDesc(str, kw, length);
    }

    public Iterable<KeyWord> searchDistinct(final Box box, String str) {
        return searchDistinct(box, str, Long.MAX_VALUE, Long.MAX_VALUE);
    }

    // startId -> descending order
    public Iterable<KeyWord> searchDistinct(final Box box, final String str,
            final long startId, final long length) {
        final Iterator<KeyWord> it = search(box, str, startId).iterator();
        return new Iterable<KeyWord>() {
            private long len = length;

            @Override
            public Iterator<KeyWord> iterator() {
                return new EngineIterator<KeyWord>() {
                    long c_id = -1;
                    KeyWord current;

                    @Override
                    public boolean hasNext() {
                        if (len < 1) {
                            return false;
                        }
                        while (it.hasNext()) {
                            current = it.next();
                            if (current.getID() == c_id) {
                                continue;
                            }
                            c_id = current.getID();
                            len--;
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public KeyWord next() {
                        return current;
                    }

                };
            }
        };
    }

    public Iterable<KeyWord> search(final Box box, String str) {
        return search(box, str, Long.MAX_VALUE);
    }

    public Iterable<KeyWord> search(final Box box, String str, long startId) {
        if (startId < 0) {
            return new ArrayList();
        }
        char[] cs = sUtil.clear(str);
        ArrayList<KeyWord> map = util.fromString(-1, cs, false);
        sUtil.correctInput(map);

        if (map.size() > KeyWord.MAX_WORD_LENGTH || map.isEmpty()) {
            return new ArrayList();
        }
        ArrayList<KeyWord> kws = new ArrayList<KeyWord>();

        for (int i = 0; i < map.size(); i++) {
            KeyWord kw = map.get(i);
            if (kw instanceof KeyWordE) {
                String s = kw.getKeyWord().toString();
                if ((s.length() > 2) && (!sUtil.mvends.contains(s))) {
                    kws.add(kw);
                    map.set(i, null);
                }
            } else {
                KeyWordN kwn = (KeyWordN) kw;
                if (kwn.size() >= 2) {
                    kws.add(kw);
                    map.set(i, null);
                } else if (kws.size() > 0) {
                    KeyWord p = kws.get(kws.size() - 1);
                    if (p instanceof KeyWordN) {
                        if (kwn.getPosition() == (p.getPosition() + ((KeyWordN) p).size())) {
                            kws.add(kw);
                            map.set(i, null);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < map.size(); i++) {
            KeyWord kw = map.get(i);
            if (kw != null) {
                kws.add(kw);
            }
        }
        final MaxID maxId = new MaxID(this.maxSearchTime);
        maxId.id = startId;
        return search(box, kws.toArray(new KeyWord[0]), maxId);
    }

    private Iterable<KeyWord> search(final Box box, final KeyWord[] kws, MaxID maxId) {

        if (kws.length == 1) {
            return search(box, kws[0], (KeyWord) null, false, maxId);
        }

        boolean asWord = true;
        KeyWord kwa = kws[kws.length - 2];
        KeyWord kwb = kws[kws.length - 1];
        if ((kwa instanceof KeyWordN) && (kwb instanceof KeyWordN)) {
            asWord = kwb.getPosition() != (kwa.getPosition() + ((KeyWordN) kwa).size());
        }

        return search(box, kws[kws.length - 1],
                search(box, Arrays.copyOf(kws, kws.length - 1), maxId),
                asWord, maxId);
    }

    private Iterable<KeyWord> search(final Box box, final KeyWord nw,
            Iterable<KeyWord> condition, final boolean isWord, final MaxID maxId) {
        final Iterator<KeyWord> cd = condition.iterator();
        return new Iterable<KeyWord>() {

            @Override
            public Iterator<KeyWord> iterator() {
                return new EngineIterator<KeyWord>() {
                    Iterator<KeyWord> r1 = null;
                    KeyWord r1_con = null;
                    long r1_id = -1;

                    @Override
                    public boolean hasNext() {
                        if (r1 != null && r1.hasNext()) {
                            return true;
                        }
                        while (cd.hasNext()) {
                            r1_con = cd.next();
                            if (isWord) {
                                if (r1_id == r1_con.getID()) {
                                    continue;
                                }
                            }
                            r1_id = r1_con.getID();
                            r1 = search(box, nw, r1_con, isWord, maxId).iterator();
                            if (r1.hasNext()) {
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public KeyWord next() {
                        KeyWord k = r1.next();
                        k.previous = r1_con;
                        return k;
                    }
                };

            }
        };

    }

    private static final Iterable<KeyWord> emptySearch = new ArrayList();

    private static Iterable<KeyWord> search(Box box, KeyWord kw, KeyWord con, boolean asWord, MaxID maxId) {

        if (kw instanceof KeyWordE) {
            asWord = true;
            return new Index2KeyWordEIterable(box.select(Object.class, "from /E where K==? & I<=?",
                    kw.getKeyWord(), maxId.id), box, kw, con, asWord, maxId);
        } else {

            if (con instanceof KeyWordE) {
                asWord = true;
            }
            if (con == null || asWord) {
                asWord = true;
                return new Index2KeyWordNIterable(box.select(Object.class, "from /N where K==? & I<=?",
                        kw.getKeyWord(), maxId.id), box, kw, con, asWord, maxId);
            } else {
                Object[] os = (Object[]) box.d("/N", kw.getKeyWord(),
                        con.getID(), (con.getPosition() + ((KeyWordN) con).size()))
                        .select(Object.class);
                if (os != null) {
                    KeyWordN cache = new KeyWordN();
                    cache.setKeyWord(os[0]);
                    cache.I = (Long) os[1];
                    cache.P = (Integer) os[2];
                    ArrayList<KeyWord> r = new ArrayList<KeyWord>(1);
                    r.add(cache);
                    return r;
                } else {
                    return emptySearch;
                }
            }
        }
    }

    private static Iterable<KeyWord> lessMatch(Box box, KeyWord kw) {
        if (kw instanceof KeyWordE) {
            return new Index2KeyWordEIterable(box.select(Object.class, "from /E where K<=? limit 0, 50", kw.getKeyWord()), null, null, null, true, new MaxID(Long.MAX_VALUE));
        } else {
            return new Index2KeyWordNIterable(box.select(Object.class, "from /N where K<=? limit 0, 50", kw.getKeyWord()), null, null, null, true, new MaxID(Long.MAX_VALUE));
        }
    }

    private static final class MaxID {

        public MaxID(long maxSearchTime) {
            maxTime = maxSearchTime;
        }
        protected long id = Long.MAX_VALUE;
        public long maxTime;
    }

    private static final class Index2KeyWordEIterable extends Index2KeyWordIterable {

        public Index2KeyWordEIterable(Iterable<Object> findex,
                Box box, KeyWord kw, KeyWord con, boolean asWord, MaxID maxId) {
            super(findex, box, kw, con, asWord, maxId);
        }

        @Override
        protected KeyWord create() {
            return new KeyWordE();
        }
    }

    private static final class Index2KeyWordNIterable extends Index2KeyWordIterable {

        public Index2KeyWordNIterable(Iterable<Object> findex,
                Box box, KeyWord kw, KeyWord con, boolean asWord, MaxID maxId) {
            super(findex, box, kw, con, asWord, maxId);
        }

        @Override
        protected KeyWord create() {
            return new KeyWordN();
        }

    }

    private static abstract class Index2KeyWordIterable
            implements Iterable<KeyWord> {

        final Iterator<KeyWord> iterator;
        Iterator<Object[]> index;

        protected Index2KeyWordIterable(final Iterable<Object> findex,
                final Box box, final KeyWord kw, final KeyWord con, final boolean asWord, final MaxID maxId) {
            this.index = (Iterator<Object[]>) (Object) findex.iterator();
            this.iterator = new EngineIterator<KeyWord>() {
                long currentMaxId = maxId.id;
                KeyWord cache;

                @Override
                public boolean hasNext() {
                    if (maxId.id == -1) {
                        return false;
                    }
                    if (con != null) {
                        if (con.I != maxId.id) {
                            return false;
                        }
                    }

                    if (currentMaxId > (maxId.id + 1) && currentMaxId != Long.MAX_VALUE) {
                        currentMaxId = maxId.id;

                        Iterable<KeyWord> tmp = search(box, kw, con, asWord, maxId);
                        if (tmp instanceof Index2KeyWordIterable) {
                            index = ((Index2KeyWordIterable) tmp).index;
                        }
                    }

                    if (index.hasNext()) {
                        if (--maxId.maxTime < 0) {
                            maxId.id = -1;
                            return false;
                        }
                        Object[] os = index.next();

                        long osid = (Long) os[1];
                        maxId.id = osid;
                        currentMaxId = maxId.id;

                        if (con != null) {
                            if (con.I != maxId.id) {
                                return false;
                            }
                        }

                        cache = create();
                        cache.setKeyWord(os[0]);
                        cache.I = (Long) os[1];
                        cache.P = (Integer) os[2];

                        return true;
                    }
                    maxId.id = -1;
                    return false;
                }

                @Override
                public KeyWord next() {
                    return cache;
                }
            };
        }

        @Override
        public Iterator<KeyWord> iterator() {
            return iterator;
        }

        protected abstract KeyWord create();

    }

    private static abstract class EngineIterator<E> implements java.util.Iterator<E> {

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

    }
}
