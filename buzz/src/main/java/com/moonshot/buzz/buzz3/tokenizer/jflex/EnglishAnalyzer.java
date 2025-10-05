package com.moonshot.buzz.buzz3.tokenizer.jflex;

import com.moonshot.buzz.buzz3.tokenizer.jflex.lang.JflexBuzzEnglishLexer;
import com.moonshot.buzz.buzz3.tokenizer.jflex.lang.JflexLexer;

import org.apache.lucene.analysis.TokenFilter;

import java.io.Reader;
import java.util.function.Function;

public class EnglishAnalyzer extends BuzzAnalyzer {
    public static final EnglishAnalyzer INSTANCE = new EnglishAnalyzer();

    @Override
    TokenFilter applyFilters(TokenFilter filter) {
        return new ApostropheNtFilter(filter);
    }

    @Override
    Function<Reader, JflexLexer> createScanner() {
        return r -> new JflexBuzzEnglishLexer(r);
    }

}
