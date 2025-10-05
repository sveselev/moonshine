package com.moonshot.buzz.buzz3.tokenizer.jflex;

import com.moonshot.buzz.buzz3.tokenizer.jflex.lang.JflexBuzzSwedishLexer;
import com.moonshot.buzz.buzz3.tokenizer.jflex.lang.JflexBuzzTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.jflex.lang.JflexLexer;

import com.google.common.collect.ImmutableMap;
import org.apache.lucene.analysis.TokenFilter;

import java.io.Reader;
import java.util.Map;
import java.util.function.Function;

public class SwedishAnalyzer extends BuzzAnalyzer {
    public static final SwedishAnalyzer INSTANCE = new SwedishAnalyzer();

    @Override
    Map<String, String> typeReplacementMap() {
        return new ImmutableMap.Builder<String, String>()
            .put(JflexBuzzTokenizer.URL_STR, "url")
            .put(JflexBuzzTokenizer.USER_STR, "user")
            .put(JflexBuzzTokenizer.POSITIVE_STR, "chpositiveemoji")
            .put(JflexBuzzTokenizer.HEARTS_STR, "chheart")
            .put(JflexBuzzTokenizer.NEGATIVE_STR, "chnegativeemoji")
            .put(JflexBuzzTokenizer.HAHA_STR, "haha")
            .put(JflexBuzzTokenizer.OMG_STR, "omg")
            .put(JflexBuzzTokenizer.WOW_STR, "wow")
            .put(JflexBuzzTokenizer.DAMN_STR, "damn")
            .put(JflexBuzzTokenizer.OOPS_STR, "oop")
            .put(JflexBuzzTokenizer.NO_STR, "no")
            .put(JflexBuzzTokenizer.LOL_STR, "lol")
            .put(JflexBuzzTokenizer.FUCK_STR, "fuck")
            .put(JflexBuzzTokenizer.HAPPY_STR, "happy")
            .put(JflexBuzzTokenizer.LOVE_STR, "love")
            .put(JflexBuzzTokenizer.YES_STR, "yes")
            .put(JflexBuzzTokenizer.ARGH_STR, "argh")
            .put(JflexBuzzTokenizer.QUESTION_STR, "chquestion")
            .put(JflexBuzzTokenizer.EXCLAMATION_STR, "chexclamation")
            .put(JflexBuzzTokenizer.PUNCTUATION_STR, "chmodelpunctuation")
            .put(JflexBuzzTokenizer.OK_STR, "okay")
            .build();
    }
    
    @Override
    TokenFilter applyFilters(TokenFilter filter) {
        filter = new ApostropheNtFilter(filter);
        return new UserMentionCollapsingFilter(filter);
    }

    @Override
    Function<Reader, JflexLexer> createScanner() {
        return JflexBuzzSwedishLexer::new;
    }

}
