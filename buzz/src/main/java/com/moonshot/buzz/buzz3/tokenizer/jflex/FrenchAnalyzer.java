package com.moonshot.buzz.buzz3.tokenizer.jflex;

import com.moonshot.buzz.buzz3.tokenizer.jflex.lang.JflexBuzzFrenchLexer;
import com.moonshot.buzz.buzz3.tokenizer.jflex.lang.JflexLexer;

import org.apache.lucene.analysis.TokenFilter;

import java.io.Reader;
import java.util.function.Function;

public class FrenchAnalyzer extends BuzzAnalyzer {
    public static final FrenchAnalyzer INSTANCE = new FrenchAnalyzer();

    @Override
    TokenFilter applyFilters(TokenFilter filter) {
        return filter;
    }

    @Override
    Function<Reader, JflexLexer> createScanner() {
        return JflexBuzzFrenchLexer::new;
    }

}
