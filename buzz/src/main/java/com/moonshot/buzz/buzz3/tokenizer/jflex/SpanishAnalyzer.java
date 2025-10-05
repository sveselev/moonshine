package com.moonshot.buzz.buzz3.tokenizer.jflex;

import com.moonshot.buzz.buzz3.tokenizer.jflex.lang.JflexBuzzSpanishLexer;
import com.moonshot.buzz.buzz3.tokenizer.jflex.lang.JflexLexer;

import org.apache.lucene.analysis.TokenFilter;

import java.io.Reader;
import java.util.function.Function;

public class SpanishAnalyzer extends BuzzAnalyzer {
    public static final SpanishAnalyzer INSTANCE = new SpanishAnalyzer();

    @Override
    TokenFilter applyFilters(TokenFilter filter) {
        return filter;
    }

    @Override
    Function<Reader, JflexLexer> createScanner() {
        return r -> new JflexBuzzSpanishLexer(r);
    }

}
