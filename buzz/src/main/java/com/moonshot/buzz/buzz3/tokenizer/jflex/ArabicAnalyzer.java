package com.moonshot.buzz.buzz3.tokenizer.jflex;

import com.moonshot.buzz.buzz3.tokenizer.jflex.lang.JflexBuzzArabicLexer;
import com.moonshot.buzz.buzz3.tokenizer.jflex.lang.JflexLexer;

import org.apache.lucene.analysis.TokenFilter;

import java.io.Reader;
import java.util.function.Function;

public class ArabicAnalyzer extends BuzzAnalyzer {
    public static final ArabicAnalyzer INSTANCE = new ArabicAnalyzer();

    @Override
    TokenFilter applyFilters(TokenFilter filter) {
        return new ApostropheNtFilter(filter);
    }

    @Override
    Function<Reader, JflexLexer> createScanner() {
        return r -> new JflexBuzzArabicLexer(r);
    }

}
