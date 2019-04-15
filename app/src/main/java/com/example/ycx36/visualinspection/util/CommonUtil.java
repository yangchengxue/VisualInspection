package com.example.ycx36.visualinspection.util;

public class CommonUtil {


    /**
     * 比较四个数最小值
     */
    public int GetMinimum(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {

                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
        return arr[0];
    }



}
