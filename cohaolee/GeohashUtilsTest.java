package com.lz.framework.map.geohash;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Queue;

import static org.junit.Assert.*;

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
        String s = GeohashUtils.Borders[0][3];
        int[] BASE_32_IDX = GeohashUtils.BASE_32_IDX;
        char[] BASE_32 = GeohashUtils.BASE_32;

        System.out.println(s);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            //获取base32位字符索引位置
            final int cd = BASE_32_IDX[c - BASE_32[0]];

            System.out.println(cd);
        }


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
                {"10", "11"},  //第二象限  第一象限
                {"00", "01"}   //第三象限  第四象限
        };

        String[][] quadrant = Quadrant(codeArr, codeArr, 8);
        distance2Top(quadrant);
        distance2Right(quadrant);

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
        for (int row = codeArr.length - 1; row >= 0; row--) {
            for (int col = 0; col < codeArr[0].length; col++) {
                String base = codeArr[row][col];
                int baseRow = row * 2;
                int baseCol = col * 2;

                quadrant[baseRow][baseCol + 1] = base + baseArr[0][1]; //第一象限
                quadrant[baseRow][baseCol] = base + baseArr[0][0]; //第二象限
                quadrant[baseRow + 1][baseCol] = base + baseArr[1][0]; //第三象限
                quadrant[baseRow + 1][baseCol + 1] = base + baseArr[1][1]; //第四象限

//                System.out.println(quadrant[baseRow][baseCol] +" "+ quadrant[baseRow][baseCol + 1] );
//                System.out.println(quadrant[baseRow + 1][baseCol] +" "+quadrant[baseRow + 1][baseCol + 1]);
            }
        }

        for (int i = 0; i < quadrant.length; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < quadrant[0].length; j++) {
                stringBuilder.append(quadrant[i][j]);
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


}