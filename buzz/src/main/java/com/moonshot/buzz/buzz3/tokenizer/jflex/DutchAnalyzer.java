package com.moonshot.buzz.buzz3.tokenizer.jflex;

import com.moonshot.buzz.buzz3.tokenizer.jflex.lang.JflexBuzzDutchLexer;
import com.moonshot.buzz.buzz3.tokenizer.jflex.lang.JflexLexer;

import org.apache.lucene.analysis.TokenFilter;

import java.io.Reader;
import java.util.function.Function;

public class DutchAnalyzer extends BuzzAnalyzer {
    public static final DutchAnalyzer INSTANCE = new DutchAnalyzer();

    @Override
    TokenFilter applyFilters(TokenFilter filter) {
        return filter;
    }

    @Override
    Function<Reader, JflexLexer> createScanner() {
        return JflexBuzzDutchLexer::new;
    }

}
