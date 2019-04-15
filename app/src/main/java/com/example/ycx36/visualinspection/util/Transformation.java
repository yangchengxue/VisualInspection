package com.example.ycx36.visualinspection.util;

public class Transformation {
    /**
     * 要进行DCT变换的图片的宽或高
     */
    public static final int N = 256;

    /**
     * 傅里叶变换
     * @retur
     */
    public int[] FFT() {

        return null;
    }

    /**
     * 离散余弦变换
     * @param pix 原图像的数据矩阵
     * @param x,y 原图像(n*n)的高或宽
     * @return 变换后的矩阵数组
     */
    public double[] DCT(double[] pix, int x, int y) {
        double[][] iMatrix = new double[x][y];
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                iMatrix[i][j] = (double)(pix[i*y + j]);
            }
        }
        double[][] quotient = coefficient(x,y);	//求系数矩阵
        double[][] quotientT = transposingMatrix(quotient, x,y);	//转置系数矩阵

        double[][] temp = new double[x][y];
        temp = matrixMultiply(quotient, iMatrix, x,y);
        iMatrix =  matrixMultiply(temp, quotientT, x,y);

        double newpix[] = new double[x*y];
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                newpix[i*y + j] = iMatrix[i][j];
            }
        }
        return newpix;
    }


    public double[] IDCT(double[] pix, int x,int y) {
        double[][] iMatrix = new double[x][y];
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                iMatrix[i][j] = (double)(pix[i*y + j]);
            }
        }
        double[][] quotient = coefficient(x,y);	//求系数矩阵
        double[][] quotientT = transposingMatrix(quotient, x,y);	//转置系数矩阵

        double[][] temp = new double[x][y];
        temp = matrixMultiply(quotientT, iMatrix, x,y);
        iMatrix =  matrixMultiply(temp, quotient, x,y);

        double newpix[] = new double[x*y];
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                newpix[i*y + j] = iMatrix[i][j];
            }
        }
        return newpix;
    }

    /**
     * 矩阵转置
     * @param matrix 原矩阵
     * @param x,y 矩阵(n*n)的高或宽
     * @return 转置后的矩阵
     */
    private double[][]  transposingMatrix(double[][] matrix, int y, int x) {
        double nMatrix[][] = new double[x][y];
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                nMatrix[i][j] = matrix[j][i];
            }
        }
        return nMatrix;
    }
    /**
     * 求离散余弦变换的系数矩阵
     * @param x,y n*n矩阵的大小
     * @return 系数矩阵
     */
    private double[][] coefficient(int x,int y) {
        double[][] coeff = new double[x][y];
        double sqrt = 2.0/Math.sqrt(x*y);
        for(int i=0; i<y; i++) {
            coeff[0][i] = sqrt;
        }
        for(int i=1; i<x; i++) {
            for(int j=0; j<y; j++) {
                coeff[i][j] = Math.sqrt(2.0/x*y) * Math.cos(i*Math.PI*(j+0.5)/(double)(x*y));
            }
        }
        return coeff;
    }
    /**
     * 矩阵相乘
     * @param A 矩阵A
     * @param B 矩阵B
     * @param x,y 矩阵的大小n*n
     * @return 结果矩阵
     */
    private double[][] matrixMultiply(double[][] A, double[][] B, int x, int y) {
        double nMatrix[][] = new double[x][y];
        double t = 0.0;
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                t = 0;
                for(int k=0; k<y; k++) {
                    t += A[i][k]*B[k][j];
                }
                nMatrix[i][j] = t;
            }
        }
        return nMatrix;
    }

}
