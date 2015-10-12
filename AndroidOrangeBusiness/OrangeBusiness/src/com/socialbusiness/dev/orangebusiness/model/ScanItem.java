package com.socialbusiness.dev.orangebusiness.model;

import java.io.Serializable;

/**
 * Created by zkboos on 2015/4/23.
 */
public class ScanItem implements Serializable{
    public Product product;
    public boolean isOneHeader = false;
    public boolean isSecondHeader = false;
    public int boxIndex;//箱子索引
    public int bagIndex;//盒索引
    public int boxSize;//箱个数
    public int bagSize;//盒个数
    public boolean isBox = false;//箱子item
    public boolean isBag = false;//盒子item
    public String code="";//二维码
//    public int boxScan;//已经扫完箱
//    public int bagScan;//已经扫完盒

//    @Override
//    public String toString() {
//        return "ScanItem{" +
//                "product=" + product +
//                ", isOneHeader=" + isOneHeader +
//                ", isSecondHeader=" + isSecondHeader +
//                ", boxIndex=" + boxIndex +
//                ", bagIndex=" + bagIndex +
//                ", boxSize=" + boxSize +
//                ", bagSize=" + bagSize +
//                ", isBox=" + isBox +
//                ", isBag=" + isBag +
//                ", code='" + code + '\'' +
//                '}';
//    }
}
