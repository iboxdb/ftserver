//Free
package iBoxDB.fulltext;

import iBoxDB.LocalServer.*;
import java.util.*;

public class Engine {

    final StringUtil sUtil = new StringUtil();

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
        ArrayList<KeyWord> map = sUtil.fromString(id, cs, true);

        for (KeyWord kw : map) {
            insertToBox(box, kw, isRemove);
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
        ArrayList<KeyWord> map = sUtil.fromString(id, cs, true);

        Box box = null;
        int ccount = 0;
        for (KeyWord kw : map) {
            if (box == null) {
                box = auto.cube();
                ccount = commitCount;
            }
            insertToBox(box, kw, isRemove);
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

    private void insertToBox(Box box, KeyWord kw, boolean isRemove) {
        Binder binder;
        if (kw instanceof KeyWordE) {
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
                if (str.length() < 3) {
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
        ArrayList<KeyWord> map = sUtil.fromString(-1, cs, false);

        if (map.size() > KeyWord.MAX_WORD_LENGTH || map.isEmpty()) {
            return new ArrayList();
        }

        final MaxID maxId = new MaxID();
        maxId.id = startId;
        return search(box, map.toArray(new KeyWord[0]), maxId);
    }

    private Iterable<KeyWord> search(final Box box, final KeyWord[] kws, MaxID maxId) {

        if (kws.length == 1) {
            return search(box, kws[0], (KeyWord) null, maxId);
        }

        return search(box, kws[kws.length - 1],
                search(box, Arrays.copyOf(kws, kws.length - 1), maxId),
                maxId);
    }

    private Iterable<KeyWord> search(final Box box, final KeyWord nw,
            Iterable<KeyWord> condition, final MaxID maxId) {
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

                            if (r1_id == r1_con.getID()) {
                                continue;
                            }
                            if (!nw.isLinked) {
                                r1_id = r1_con.getID();
                            }

                            r1 = search(box, nw, r1_con, maxId).iterator();
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

    private static Iterable<KeyWord> search(final Box box,
            final KeyWord kw, final KeyWord con, final Engine.MaxID maxId) {

        final String ql = kw instanceof KeyWordE
                ? "from /E where K==? & I<=?"
                : "from /N where K==? & I<=?";

        final Class rclass = kw instanceof KeyWordE ? KeyWordE.class : KeyWordN.class;

        final int linkPos = kw.isLinked ? (con.getPosition() + con.size()
                + (kw instanceof KeyWordE ? 1 : 0)) : -1;

        return new Iterable<KeyWord>() {
            @Override
            public Iterator<KeyWord> iterator() {

                return new EngineIterator<KeyWord>() {
                    long currentMaxId = Long.MAX_VALUE;
                    KeyWord cache = null;
                    Iterator<KeyWord> iter = null;
                    boolean isLinkEndMet = false;

                    @Override
                    public boolean hasNext() {
                        if (maxId.id == -1) {
                            return false;
                        }

                        if (currentMaxId > (maxId.id + 1)) {
                            currentMaxId = maxId.id;
                            iter = box.select(rclass, ql, kw.getKeyWord(), maxId.id).iterator();
                        }

                        while (iter.hasNext()) {

                            cache = iter.next();

                            maxId.id = cache.getID();
                            currentMaxId = maxId.id;
                            if (con != null && con.I != maxId.id) {
                                return false;
                            }

                            if (isLinkEndMet) {
                                continue;
                            }

                            if (linkPos == -1) {
                                return true;
                            }

                            int cpos = cache.getPosition();
                            if (cpos > linkPos) {
                                continue;
                            }
                            if (cpos == linkPos) {
                                if (kw.isLinkedEnd) {
                                    isLinkEndMet = true;
                                }
                                return true;
                            }
                            return false;
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

        };

    }

    private static abstract class EngineIterator<E> implements java.util.Iterator<E> {

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

    }

    private static Iterable<KeyWord> lessMatch(Box box, KeyWord kw) {
        if (kw instanceof KeyWordE) {
            return (Iterable<KeyWord>) (Object) box.select(KeyWordE.class, "from /E where K<=? limit 0, 50", kw.getKeyWord());

        } else {
            return (Iterable<KeyWord>) (Object) box.select(KeyWordN.class, "from /N where K<=? limit 0, 50", kw.getKeyWord());
        }
    }

    static final class MaxID {

        protected long id = Long.MAX_VALUE;

    }

}
