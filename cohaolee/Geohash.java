package com.lz.framework.map.geohash;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liqiang on 2017/3/22.
 * 原理介绍：https://en.wikipedia.org/wiki/Geohash; http://www.cnblogs.com/LBSer/p/3310455.html
 * 秒银翻译过来
 */
public class Geohash {
    /**
     * 方向
     */
    public enum Direction {
        Top(0), Right(1), Bottom(2), Left(3);

        private int value;

        Direction(int value) {
            this.value = value;
        }
    }

    private static final String Base32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private static final Map<Character, Integer> Base32Map = new HashMap<Character, Integer>();
    private static final int Bits[] = {16, 8, 4, 2, 1};

    private static final String[][] Neighbors = {
            {
                    "p0r21436x8zb9dcf5h7kjnmqesgutwvy", // Top
                    "bc01fg45238967deuvhjyznpkmstqrwx", // Right
                    "14365h7k9dcfesgujnmqp0r2twvyx8zb", // Bottom
                    "238967debc01fg45kmstqrwxuvhjyznp", // Left
            },
            {
                    "bc01fg45238967deuvhjyznpkmstqrwx", // Top
                    "p0r21436x8zb9dcf5h7kjnmqesgutwvy", // Right
                    "238967debc01fg45kmstqrwxuvhjyznp", // Bottom
                    "14365h7k9dcfesgujnmqp0r2twvyx8zb", // Left
            }
    };

    private static final String[][] Borders = {
            {"prxz", "bcfguvyz", "028b", "0145hjnp"},
            {"bcfguvyz", "prxz", "0145hjnp", "028b"}
    };

    static {
        for (int i = 0; i < 32; i++) {
            Base32Map.put(Base32.charAt(i), i);
        }
    }

    private static void refineInterval(final double[] interval, int cd, int mask) {
        if ((cd & mask) > 0) {
            interval[0] = (interval[0] + interval[1]) / 2.0;
        } else {
            interval[1] = (interval[0] + interval[1]) / 2.0;
        }
    }

    public static String calculateAdjacent(String srcHash, Direction direction) {
        srcHash = srcHash.toLowerCase();

        char lastChr = srcHash.charAt(srcHash.length() - 1);
        int type = srcHash.length() % 2; //0或1，最后一位是维度还是经度
        int dir = direction.value;
        String nHash = srcHash.substring(0, srcHash.length() - 1); //获取除最后一位的所有编码

        //是否包含最后一个geohash字符
        if (Borders[type][dir].indexOf(lastChr) != -1) {
            nHash = calculateAdjacent(nHash, direction);
        }
        return nHash + Base32.charAt(Neighbors[type][dir].indexOf(lastChr));
    }

    /**
     * 获取geohash 相邻8个节点
     *
     * @param geohash
     * @return 0 本身
     * 1 Top
     * 2 Bottom
     * 3 Right
     * 4 Left
     * 5 TopLeft
     * 6 TopRight
     * 7 BottomRight
     * 8 BottomLeft
     */
    public static String[] getGeoHashExpand(String geohash) {
        try {
            String geohashTop = calculateAdjacent(geohash, Direction.Top);
            String geohashBottom = calculateAdjacent(geohash, Direction.Bottom);
            String geohashRight = calculateAdjacent(geohash, Direction.Right);
            String geohashLeft = calculateAdjacent(geohash, Direction.Left);

            String geohashTopLeft = calculateAdjacent(geohashLeft, Direction.Top);
            String geohashTopRight = calculateAdjacent(geohashRight, Direction.Top);
            String geohashBottomRight = calculateAdjacent(geohashRight, Direction.Bottom);
            String geohashBottomLeft = calculateAdjacent(geohashLeft, Direction.Bottom);

            String[] expand = {geohash, geohashTop, geohashBottom, geohashRight, geohashLeft, geohashTopLeft,
                    geohashTopRight, geohashBottomRight, geohashBottomLeft};
            return expand;
        } catch (Exception e) {
            //logger.error("GeoHash Error",e);
            return null;
        }
    }



    public static double[] decode(String geohash) {
        boolean even = true;
        double[] lat = {-90.0, 90.0};
        double[] lon = {-180.0, 180.0};

        for (int i = 0; i < geohash.length(); i++) {
            char c = geohash.charAt(i);
            int cd = Base32.indexOf(c);//Base32Map.get(c);// 测试结果没有优化必要（包装花费的时间在该场景中所占比例较大）
            for (int j = 0; j < 5; j++) {
                int mask = Bits[j];
                if (even) {
                    refineInterval(lon, cd, mask);
                } else {
                    refineInterval(lat, cd, mask);
                }
                even = !even;
            }
        }
        return new double[]{(lat[0] + lat[1]) / 2, (lon[0] + lon[1]) / 2};
    }

    public static String encode(double latitude, double longitude) {
        return encode(latitude, longitude, 12);
    }

    public static String encode(double latitude, double longitude, int precision) {
        boolean even = true;
        int bit = 0;
        int ch = 0;

        double[] lat = {-90.0, 90.0};
        double[] lon = {-180.0, 180.0};

        if (precision < 1 || precision > 20) precision = 12;
        StringBuilder geohash = new StringBuilder(precision);

        while (geohash.length() < precision) {
            double mid;

            if (even) {
                mid = (lon[0] + lon[1]) / 2;
                if (longitude > mid) {
                    ch |= Bits[bit];
                    lon[0] = mid;
                } else
                    lon[1] = mid;
            } else {
                mid = (lat[0] + lat[1]) / 2;
                if (latitude > mid) {
                    ch |= Bits[bit];
                    lat[0] = mid;
                } else
                    lat[1] = mid;
            }

            even = !even;
            if (bit < 4)
                bit++;
            else {
                geohash.append( Base32.charAt(ch));
                bit = 0;
                ch = 0;
            }
        }
        return geohash.toString();
    }
}
