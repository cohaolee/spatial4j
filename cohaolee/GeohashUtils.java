/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// NOTE: we keep the header as it came from ASF; it did not originate in Spatial4j

// create by liqiang 20170317，基础代码来自于Spatial4j项目
// 原理介绍：https://en.wikipedia.org/wiki/Geohash; http://www.cnblogs.com/LBSer/p/3310455.html

package com.lz.framework.map.geohash;

import java.util.Arrays;

/**
 * Utilities for encoding and decoding <a href="http://en.wikipedia.org/wiki/Geohash">geohashes</a>.
 * <p>
 * This class isn't used by any other part of Spatial4j; it's included largely for convenience of
 * software using Spatial4j. There are other open-source libraries that have more comprehensive
 * geohash utilities but providing this one avoids an additional dependency for what's a small
 * amount of code.  <em>If you're using Spatial4j just for this class, consider alternatives.</em>
 * <p>
 * This code originally came from <a href="https://issues.apache.org/jira/browse/LUCENE-1512">
 * Apache Lucene, LUCENE-1512</a>.
 */
public class GeohashUtils {

    //偶数位放经度0，奇数位放纬度1
    public static final char[] BASE_32 = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};//note: this is sorted

    public static final int[] BASE_32_IDX;//sparse array of indexes from '0' to 'z'

    public static final int MAX_PRECISION = 24;//DWS: I forget what level results in needless more precision but it's about this
    public static final int[] BITS = {16, 8, 4, 2, 1}; //二进制 2^4(10000) 2^3 2^2 2^1 2^0  之和31（0-31）

    /**
     * 方位
     */
    public enum Direction {
        Top(0), Right(1), Bottom(2), Left(3);

        private int value;

        Direction(int value) {
            this.value = value;
        }
    }

    public static final String[][] Neighbors = {
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

    public static final String[][] Borders = {
            {"prxz", "bcfguvyz", "028b", "0145hjnp"}, // Top，Right，Bottom，Left
            {"bcfguvyz", "prxz", "0145hjnp", "028b"}  // Top，Right，Bottom，Left
    };

    static {
        BASE_32_IDX = new int[BASE_32[BASE_32.length - 1] - BASE_32[0] + 1];
        assert BASE_32_IDX.length < 100;//reasonable length
        Arrays.fill(BASE_32_IDX, -500);//ascii空白处填充 -500；
        for (int i = 0; i < BASE_32.length; i++) {
            BASE_32_IDX[BASE_32[i] - BASE_32[0]] = i;//ascii编码
        }
    }

    private GeohashUtils() {
    }

    /**
     * Encodes the given latitude and longitude into a geohash
     * 将经纬度编码为geohash
     *
     * @param longitude Longitude to encode 经度
     * @param latitude  Latitude to encode 纬度
     * @return Geohash encoding of the longitude and latitude
     */
    public static String encode(double longitude, double latitude) {
        return encode(longitude, latitude, 12);
    }

    public static String encode(double longitude, double latitude, int precision) {
        double[] latInterval = {-90.0, 90.0};
        double[] lngInterval = {-180.0, 180.0};

        final StringBuilder geohash = new StringBuilder(precision);
        boolean isEven = true;

        int bit = 0;
        int ch = 0;

        while (geohash.length() < precision) {
            double mid = 0.0;
            if (isEven) { //偶数 经度
                mid = (lngInterval[0] + lngInterval[1]) / 2D;
                if (longitude > mid) {
                    ch |= BITS[bit];
                    lngInterval[0] = mid;
                } else {
                    lngInterval[1] = mid;
                }
            } else {//奇数 维度
                mid = (latInterval[0] + latInterval[1]) / 2D;
                if (latitude > mid) {
                    ch |= BITS[bit];
                    latInterval[0] = mid;
                } else {
                    latInterval[1] = mid;
                }
            }

            isEven = !isEven;

            if (bit < 4) {//计算合并出了一位base32，5位2进制一位32编码
                bit++;
            } else {
                geohash.append(BASE_32[ch]);
                bit = 0;
                ch = 0;
            }
        }

        return geohash.toString();
    }

    /**
     * 计算geohash的经纬度，为矩形的中心
     * Returns {longitude, latitude}
     */
    public static double[] decode(String geohash) {
        double minY = -90, maxY = 90, minX = -180, maxX = 180;
        boolean isEven = true;

        for (int i = 0; i < geohash.length(); i++) {
            char c = geohash.charAt(i);

            //转换小写，避免使用 toLowerCase()
            if (c >= 'A' && c <= 'Z')
                c -= ('A' - 'a');

            //获取base32位字符索引位置
            final int cd = BASE_32_IDX[c - BASE_32[0]];

            for (int mask : BITS) {
                if (isEven) {//经度
                    if ((cd & mask) != 0) {
                        minX = (minX + maxX) / 2D;
                    } else {
                        maxX = (minX + maxX) / 2D;
                    }
                } else {//维度
                    if ((cd & mask) != 0) {
                        minY = (minY + maxY) / 2D;
                    } else {
                        maxY = (minY + maxY) / 2D;
                    }
                }
                isEven = !isEven;
            }

        }

        double longitude = (minX + maxX) / 2D;
        double latitude = (minY + maxY) / 2D;
        return new double[]{longitude, latitude};
    }

    /**
     * 计算相邻节点
     *
     * @param srcHash
     * @param direction 方位
     * @return 返回指定方位的相邻节点编码
     */
    public static String calculateAdjacent(String srcHash, Direction direction) {
        if (srcHash == null || "".equals(srcHash)) {
            return ""; //该方位没有相邻节点
        }

        char lastChr = 0;
        int isEven = 0; //0经度或1维度，最后一位是维度还是经度
        int dir = 0;
        String nHash = null; //获取除最后一位的所有编码

        srcHash = srcHash.toLowerCase();

        lastChr = srcHash.charAt(srcHash.length() - 1);
        isEven = srcHash.length() % 2; //（待确认Bug:算法在srcHash奇数编码时，反对角线换位），原因：多了一位经度导致，peano曲线构建问题？
        dir = direction.value;
        nHash = srcHash.substring(0, srcHash.length() - 1);

        //是否包含最后一个geohash字符
        if (Borders[isEven][dir].indexOf(lastChr) != -1) {
            nHash = calculateAdjacent(nHash, direction);//包含继续计算
            if ("".equals(nHash)) {
                return nHash;
            }
        }

        return nHash + BASE_32[Neighbors[isEven][dir].indexOf(lastChr)];
    }

    /**
     * 获取geohash 相邻节点的九宫格
     *
     * @param geohash
     * @return 返回九宫格
     * 0 TopLeft     Top        TopRight
     * 1 Left        本身        Right
     * 2 BottomLeft BottomRight  Bottom
     */
    public static String[][] getSudoku(String geohash) {
        String top = calculateAdjacent(geohash, Direction.Top);
        String bottom = calculateAdjacent(geohash, Direction.Bottom);
        String right = calculateAdjacent(geohash, Direction.Right);
        String left = calculateAdjacent(geohash, Direction.Left);

        String topLeft = calculateAdjacent(left, Direction.Top);
        String topRight = calculateAdjacent(right, Direction.Top);
        String bottomRight = calculateAdjacent(right, Direction.Bottom);
        String bottomLeft = calculateAdjacent(left, Direction.Bottom);

        String[][] sudoku = {
                {topLeft, top, topRight}
                , {left, geohash, right}
                , {bottomLeft, bottom, bottomRight}
        };
        return sudoku;
    }

    private static final double EARTH_RADIUS = 6371000;//赤道半径(单位m)

    /**
     * 转化为弧度(rad)
     */
    private final static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 基于googleMap中的算法得到两经纬度之间的距离,计算精度与谷歌地图的距离精度差不多，相差范围在0.2米以下
     *
     * @param lon1 第一点的经度
     * @param lat1 第一点的纬度
     * @param lon2 第二点的经度
     * @param lat2 第二点的纬度
     * @return 返回的距离，单位m
     */
    public static double GetDistance(double lon1, double lat1, double lon2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lon1) - rad(lon2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }


    public static double[] lookupDegreesSizeForHashLen(int hashLen) {
        return new double[]{hashLenToLatHeight[hashLen], hashLenToLonWidth[hashLen]};
    }

    /**
     * 获取
     * Return the shortest geohash length that will have a width &amp; height &gt;= specified arguments.
     */
    public static int lookupHashLenForWidthHeight(double lonErr, double latErr) {
        //loop through hash length arrays from beginning till we find one.
        for (int len = 1; len < MAX_PRECISION; len++) {
            double latHeight = hashLenToLatHeight[len];
            double lonWidth = hashLenToLonWidth[len];
            if (latHeight < latErr && lonWidth < lonErr)
                return len;
        }
        return MAX_PRECISION;
    }

    /**
     * See the table at http://en.wikipedia.org/wiki/Geohash
     */
    private static final double[] hashLenToLatHeight, hashLenToLonWidth;

    static {
        hashLenToLatHeight = new double[MAX_PRECISION + 1]; //25个元素
        hashLenToLonWidth = new double[MAX_PRECISION + 1];
        hashLenToLatHeight[0] = 90 * 2;
        hashLenToLonWidth[0] = 180 * 2;
        boolean even = false;
        for (int i = 1; i <= MAX_PRECISION; i++) {
            hashLenToLatHeight[i] = hashLenToLatHeight[i - 1] / (even ? 8 : 4);
            hashLenToLonWidth[i] = hashLenToLonWidth[i - 1] / (even ? 4 : 8);
            even = !even;
        }
    }

}
