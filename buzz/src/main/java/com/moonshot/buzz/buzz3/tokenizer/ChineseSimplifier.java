package com.moonshot.buzz.buzz3.tokenizer;

import com.github.houbb.opencc4j.util.ZhConverterUtil;

/**
 * Created with IntelliJ IDEA. User: frankie Date: 12/9/13 Time: 12:46 PM To change this template use File | Settings | File Templates.
 */
public class ChineseSimplifier {


    public ChineseSimplifier() { }

    public String convert(String s) {
        return ZhConverterUtil.toSimple(s);
    }


    public static void main(String[] args) {
        ChineseSimplifier ch = new ChineseSimplifier();
        String orig = "國家";
        System.out.println(orig);
        System.out.println(ch.convert(orig));
    }
}
