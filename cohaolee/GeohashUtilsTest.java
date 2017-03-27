package com.lz.framework.map.geohash;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by cohaolee on 2017/3/24.
 */
public class GeohashUtilsTest {
    @Test
    public void encode() throws Exception {
        String encode = Geohash.encode(29.52508228572224, 106.50849558413029);
        String encode1 = GeohashUtils.encode(106.50849558413029, 29.52508228572224);
        Assert.assertEquals("wm5xzwvh5f62", encode);
        Assert.assertEquals(encode, encode1);
    }

    @Test
    public void decode() throws Exception {
        double[] var1 = Geohash.decode("wm5xzwvh5f62");
        double[] var2 = GeohashUtils.decode("wm5xzwvh5f62");
        Assert.assertEquals(var1[0], var2[0], 0.00000001d);
        Assert.assertEquals(var1[1], var2[1], 0.00000001d);
        System.out.println(var1[0]);
        System.out.println(var2[0]);
        System.out.println(var1[1]);
        System.out.println(var2[1]);
    }

    @Test
    public void GetDistance() throws Exception {
        //数据来自百度地图测距工具和坐标拾取
        //106.52819,29.548102
        //106.526461,29.548648
        //176
        double v = GeohashUtils.GetDistance(106.52819, 29.548102, 106.526461, 29.548648);
        Assert.assertEquals(176, v, 1d);
    }

    @Test
    public void calculateAdjacent() throws Exception {
//        String s = GeohashUtils.Borders[0][3];
//        int[] BASE_32_IDX = GeohashUtils.BASE_32_IDX;
//        char[] BASE_32 = GeohashUtils.BASE_32;
//
//        System.out.println(s);
//        for (int i = 0; i < s.length(); i++) {
//            char c = s.charAt(i);
//
//            //获取base32位字符索引位置
//            final int cd = BASE_32_IDX[c - BASE_32[0]];
//
//            System.out.println(cd);
//        }

//        String k8 = Geohash.calculateAdjacent("wx4g0b", Geohash.Direction.Left);
//        String s = Geohash.calculateAdjacent("wx4g0b", Geohash.Direction.Right);
//        String s1 = Geohash.calculateAdjacent("wx4g0b", Geohash.Direction.Top);
//        String s2 = Geohash.calculateAdjacent("wx4g0b", Geohash.Direction.Bottom);
//        System.out.println("左 " + k8);
//        System.out.println("右 " + s);
//        System.out.println("上 " + s1);
//        System.out.println("下 " + s2);


        String k8 = GeohashUtils.calculateAdjacent("3", GeohashUtils.Direction.Left);
        String s = GeohashUtils.calculateAdjacent("0f", GeohashUtils.Direction.Right);
        String s1 = GeohashUtils.calculateAdjacent("0f", GeohashUtils.Direction.Top);
        String s2 = GeohashUtils.calculateAdjacent("0f", GeohashUtils.Direction.Bottom);
        System.out.println("左 " + k8);
        System.out.println("右 " + s);
        System.out.println("上 " + s1);
        System.out.println("下 " + s2);
//        Assert.assertEquals("hx", k8);
    }

    @Test
    public void getSudokuTest() throws Exception{
        //http://geohash.gofreerange.com/ 查看空间编码，奇数编码，偶数编码
        //peano曲线走向不同（下标偶数按经度x轴方向，下标奇数按维度y轴方向）

        String[][] sudoku = GeohashUtils.getSudoku("00bh2");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < sudoku.length ; i++) {
            for (int j = 0; j < sudoku[0].length; j++) {
                sb.append(sudoku[i][j]);
                sb.append(" ");
            }
            sb.append("\n");
        }
        System.out.println(sb);
    }

    @Test
    public void lookupDegreesSizeForHashLen() throws Exception {

    }

    @Test
    public void lookupHashLenForWidthHeight() throws Exception {

    }


    /**
     * 打印Peano曲线矩阵
     *
     * @throws Exception
     */
    @Test
    public void printPeano() throws Exception {
        String[][] codeArr = {
                {"01", "11"},  //第二象限  第一象限
                {"00", "10"}   //第三象限  第四象限
        };

        //peano曲线走向不同（下标偶数按经度x轴方向，下标奇数按维度y轴方向）
        // 或者说（奇数按经度x轴方向已实现，偶数按维度y轴方向未实现 why？）

        String[][] quadrant = Quadrant(codeArr, codeArr, 15);
//        distance2Top(quadrant);
//        distance2Right(quadrant);

//        StringBuilder stringBuilder = new StringBuilder();
//        for (int i = 0; i < quadrant.length; i++) {
//            for (int j = 0; j < quadrant[0].length; j++) {
//                stringBuilder.append(quadrant[i][j]);
//                stringBuilder.append(" ");
//            }
//            System.out.println(stringBuilder.toString());
//        }

    }

    /**
     * 象限编码二进制编码
     *
     * @param codeArr
     * @param baseArr
     * @return
     */
    public String[][] Quadrant(String[][] codeArr, String[][] baseArr, int len) {
        if (codeArr[0][0].length() >= len) {
            return codeArr;
        }

        String[][] quadrant = new String[codeArr.length * 2][codeArr[0].length * 2];
        for (int i = 0; i < codeArr.length * 2; i++) {
            Arrays.fill(quadrant[i], "-1");
        }

        //三-->四-->二-->一
        for (int row = codeArr.length - 1; row >= 0; row--) {
            for (int col = 0; col < codeArr[0].length; col++) {
                String base = codeArr[row][col];
                int baseRow = row * 2;
                int baseCol = col * 2;

                quadrant[baseRow + 1][baseCol + 1] = base + baseArr[1][1]; //第一象限
                quadrant[baseRow][baseCol + 1] = base + baseArr[0][1]; //第二象限
                quadrant[baseRow][baseCol] = base + baseArr[0][0]; //第三象限
                quadrant[baseRow + 1][baseCol] = base + baseArr[1][0]; //第四象限

//                System.out.println(quadrant[baseRow][baseCol] +" "+ quadrant[baseRow][baseCol + 1] );
//                System.out.println(quadrant[baseRow + 1][baseCol] +" "+quadrant[baseRow + 1][baseCol + 1]);
            }
        }

        for (int i = 0; i < quadrant.length; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < quadrant[0].length; j++) {
                stringBuilder.append(GetBase32(quadrant[i][j]));
                stringBuilder.append(" ");
            }
            System.out.println(stringBuilder.toString());
        }
        System.out.println("---------------------");
        return Quadrant(quadrant, baseArr, len);
    }

    public void distance2Top(String[][] quadrant) {
        int[][] distance = new int[quadrant.length - 1][quadrant[0].length];
        for (int i = 0; i < quadrant.length; i++) {
            for (int j = 0; j < quadrant[0].length; j++) {
                if (i - 1 < 0) {
                    continue;
                }

                int base = Integer.parseInt(quadrant[i][j], 2);
                int top = Integer.parseInt(quadrant[i - 1][j], 2);
                distance[i - 1][j] = base - top;
            }
        }

        for (int i = 0; i < distance.length; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < distance[0].length; j++) {
                stringBuilder.append(distance[i][j]);
                stringBuilder.append(" ");
            }
            System.out.println("distance2Top：" + stringBuilder.toString());
        }
        System.out.println("---------------------");
    }

    public void distance2Right(String[][] quadrant) {
        int[][] distance = new int[quadrant.length][quadrant[0].length - 1];
        for (int i = 0; i < quadrant.length; i++) {
            for (int j = 0; j < quadrant[0].length; j++) {
                if (j + 1 == quadrant[0].length) {
                    continue;
                }

                int base = Integer.parseInt(quadrant[i][j], 2);
                int right = Integer.parseInt(quadrant[i][j + 1], 2);
                distance[i][j] = base - right;
            }
        }

        for (int i = 0; i < distance.length; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < distance[0].length; j++) {
                stringBuilder.append(distance[i][j]);
                stringBuilder.append(" ");
            }
            System.out.println("distance2Top：" + stringBuilder.toString());
        }
        System.out.println("---------------------");
    }

    public String GetBase32(String bitStr) {
        if (null == bitStr || "".equals(bitStr)) {
            return "";
        }
        int remainder = bitStr.length() % 5; //余数
        int digit = bitStr.length() / 5; //位数

        if (remainder > 0) digit++;

        String base32Str = "";
        for (int i = 0; i < digit; i++) {
            String bitSplit = "";
            if (i == 0 && remainder > 0) {
                bitSplit = bitStr.substring(0, remainder);
            } else {
                int start = i * 5;
                if (remainder > 0) {
                    start -= (5 - remainder);
                }

                int end = start + 5;
                try {
                    bitSplit = bitStr.substring(start, end);
                } catch (Exception ex) {
                    System.out.println(bitStr);
                    System.out.println(start);
                    System.out.println(end);
                }
            }

            base32Str += GeohashUtils.BASE_32[Integer.parseInt(bitSplit, 2)];
        }
        return base32Str;
    }


}