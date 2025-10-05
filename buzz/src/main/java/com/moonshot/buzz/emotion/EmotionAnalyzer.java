package com.moonshot.buzz.emotion;

import com.google.common.collect.ImmutableMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.util.Map;

/**
 * {@link Analyzer} for {@link TokenStream} emotion tokenization. Designed for english only.<br>
 * Handles lowercasing, model substitutions, apostrophe and hashtag stripping, and repetition truncation 
 *
 * @author Steve Ungerer
 */
class EmotionAnalyzer extends Analyzer {

    public static final EmotionAnalyzer INSTANCE = new EmotionAnalyzer();

    public EmotionAnalyzer() {
        super(PER_FIELD_REUSE_STRATEGY);
    }

    @Override
    protected TokenStreamComponents createComponents(String field) {
        EmotionTokenizer tokenizer = new EmotionTokenizer();
        TokenFilter filter = new LowerCaseFilter(tokenizer);
        filter = new EmotionTokenFilter(filter);
        return new TokenStreamComponents(tokenizer, filter);
    }

    protected static class EmotionTokenFilter extends TokenFilter {

        private static final Map<String, String> typeReplacementMap = new ImmutableMap.Builder<String, String>()
            .put(EmotionTokenizer.URL_STR, "url")
            .put(EmotionTokenizer.USER_STR, "user")
            .put(EmotionTokenizer.POSITIVE_STR, "ch_positive_emoji")
            .put(EmotionTokenizer.ANGER_STR, "ch_anger_emoji")
            .put(EmotionTokenizer.SAD_STR, "ch_sad_emoji")
            .put(EmotionTokenizer.FEAR_STR, "ch_fear_emoji")
            .put(EmotionTokenizer.DISGUST_STR, "ch_disgust_emoji")
            .put(EmotionTokenizer.HEARTS_STR, "ch_heart")
            .put(EmotionTokenizer.NEGATIVE_STR, "ch_negative_emoji")
            .put(EmotionTokenizer.HAHA_STR, "haha")
            .put(EmotionTokenizer.OMG_STR, "omg")
            .put(EmotionTokenizer.WOW_STR, "wow")
            .put(EmotionTokenizer.REALLY_STR, "really")
            .put(EmotionTokenizer.SO_STR, "so")
            .put(EmotionTokenizer.DAMN_STR, "damn")
            .put(EmotionTokenizer.OOPS_STR, "oops")
            .put(EmotionTokenizer.NO_STR, "no")
            .put(EmotionTokenizer.LOL_STR, "lol")
            .put(EmotionTokenizer.FUCK_STR, "fuck")
            .put(EmotionTokenizer.SHIT_STR, "shit")
            .put(EmotionTokenizer.HAPPY_STR, "happy")
            .put(EmotionTokenizer.LOVE_STR, "love")
            .put(EmotionTokenizer.YES_STR, "yes")
            .put(EmotionTokenizer.ARGH_STR, "argh")
            .put(EmotionTokenizer.UGH_STR, "ugh")
            .put(EmotionTokenizer.LMAO_STR, "lmao")
            .build();

        private final CharTermAttribute termAtt;
        private final TypeAttribute typeAtt;


        protected EmotionTokenFilter(TokenStream input) {
            super(input);
            termAtt = addAttribute(CharTermAttribute.class);
            typeAtt = addAttribute(TypeAttribute.class);
        }

        @Override
        public final boolean incrementToken() throws IOException {
            // Increment the wrapped stream
            if (!input.incrementToken()) {
                return false;
            }
            String type = typeAtt.type();
            boolean repetition = true;
            if (typeReplacementMap.containsKey(type)) {
                termAtt.setEmpty().append(typeReplacementMap.get(type));
                repetition = false; // don't replace repeated chars here
            } else if (EmotionTokenizer.APOSTROPHE_STR.equals(type)) {
                // strip out apostrophe and concat both sides
                final char[] buffer = termAtt.buffer();
                final int length = termAtt.length();
                termAtt.setEmpty();
                for (int i = 0; i < length; i++) {
                    if (buffer[i] == '\'' || buffer[i] == '’' || buffer[i] == '´') {
                        continue;
                    }
                    termAtt.append(buffer[i]);
                }
            } else if (EmotionTokenizer.HASHTAG_STR.equals(type)) {
                // remove leading hashtag
                final char[] buffer = termAtt.buffer();
                final int length = termAtt.length();
                termAtt.setEmpty();
                for (int i = 1; i < length; i++) {
                    termAtt.append(buffer[i]);
                }
            }
            if (repetition) {
                // replace 3 or more repeated consecutive characters
                final char[] buffer = termAtt.buffer();
                final int length = termAtt.length();
                char p = Character.MIN_CODE_POINT;
                boolean repeat = false;
                termAtt.setEmpty();
                for (int i = 0; i < length; i++) {
                    if (buffer[i] == p) {
                        if (!repeat) {
                            termAtt.append(buffer[i]);
                            repeat = true;
                        }
                    } else {
                        p = buffer[i];
                        repeat = false;
                        termAtt.append(buffer[i]);
                    }
                }
            }
            
            return true;
        }
    }
}
