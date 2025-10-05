package com.moonshot.buzz.buzz3.tokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.Iterator;


/**
 * Tokenizer for Chinese text, based on the {@link SmartChineseAnalyzer}
 *
 * @author bing
 */
public class ChineseTokenizer {
    private static final ChineseSimplifier simplifier = new ChineseSimplifier();

    public Iterator<String> tokens(String string) {
        string = simplifier.convert(string);
        Analyzer analyzer = new SmartChineseAnalyzer(true);
        final TokenStream ts;
        try {
            ts = analyzer.tokenStream("contents", string);
            ts.reset();
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
        return new Iterator<String>() {

            private String getNextToken() {
                try {
                    boolean hasNext = ts.incrementToken();
                    return hasNext ? ts.getAttribute(CharTermAttribute.class).toString() : null;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

            String nextToken = getNextToken();

            @Override
            public boolean hasNext() {
                if (nextToken == null) {
                    analyzer.close();
                    return false;
                }
                return true;
            }

            @Override
            public String next() {
                String token = nextToken;
                nextToken = getNextToken();
                return token;
            }

            @Override
            public void remove() {
                throw new Error("Remove not implemented in " + getClass());
            }
        };
    }
}
