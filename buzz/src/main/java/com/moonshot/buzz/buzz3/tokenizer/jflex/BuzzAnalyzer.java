package com.moonshot.buzz.buzz3.tokenizer.jflex;

import com.moonshot.buzz.buzz3.tokenizer.jflex.lang.JflexBuzzTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.jflex.lang.JflexLexer;

import com.google.common.collect.ImmutableMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.compound.CompoundWordTokenFilterBase;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.FilteringTokenFilter;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.function.Function;

/**
 * {@link Analyzer} for {@link TokenStream} emotion tokenization. Designed for english only.<br>
 * Handles lowercasing, model substitutions, apostrophe and hashtag stripping, and repetition truncation 
 *
 * @author Steve Ungerer
 */
abstract class BuzzAnalyzer extends Analyzer {

    BuzzAnalyzer() {
        super(PER_FIELD_REUSE_STRATEGY);
    }

    /**
     * @return
     */
    abstract Function<Reader, JflexLexer> createScanner();
    
    abstract TokenFilter applyFilters(TokenFilter filter);
    
    // override for non-standard models
    Map<String, String> typeReplacementMap() {
        return new ImmutableMap.Builder<String, String>()
            .put(JflexBuzzTokenizer.URL_STR, "chmodellink")
            .put(JflexBuzzTokenizer.USER_STR, "chmodeltwitteruser")
            .put(JflexBuzzTokenizer.POSITIVE_STR, "chmodelpositivesmiley")
            .put(JflexBuzzTokenizer.HEARTS_STR, "chmodelheart")
            .put(JflexBuzzTokenizer.NEGATIVE_STR, "chmodelnegativesmiley")
            .put(JflexBuzzTokenizer.HAHA_STR, "chmodelhaha")
            .put(JflexBuzzTokenizer.OMG_STR, "chmodelomg")
            .put(JflexBuzzTokenizer.WOW_STR, "chmodelwow")
            .put(JflexBuzzTokenizer.DAMN_STR, "chmodeldamn")
            .put(JflexBuzzTokenizer.OOPS_STR, "chmodeloop")
            .put(JflexBuzzTokenizer.NO_STR, "chmodelno")
            .put(JflexBuzzTokenizer.LOL_STR, "chmodellol")
            .put(JflexBuzzTokenizer.FUCK_STR, "chmodelfuck")
            .put(JflexBuzzTokenizer.HAPPY_STR, "chmodelhappy")
            .put(JflexBuzzTokenizer.LOVE_STR, "chmodellove")
            .put(JflexBuzzTokenizer.YES_STR, "chmodelyeah")
            .put(JflexBuzzTokenizer.ARGH_STR, "argh")
            .put(JflexBuzzTokenizer.QUESTION_STR, "chmodelquestion")
            .put(JflexBuzzTokenizer.EXCLAMATION_STR, "chmodelexclamation")
            .put(JflexBuzzTokenizer.PUNCTUATION_STR, "chmodelpunctuation")
            .put(JflexBuzzTokenizer.HEHE_STR, "chmodelhehe")
            .put(JflexBuzzTokenizer.KISSES_STR, "chmodelkisses")
            .put(JflexBuzzTokenizer.OK_STR, "chmodelokay")
            .build();
    }
    
    @SuppressWarnings("resource")
    @Override
    protected final TokenStreamComponents createComponents(String field) {
        JflexBuzzTokenizer tokenizer = new JflexBuzzTokenizer(createScanner());
        Map<String, String> typeReplacementMap = typeReplacementMap();
        TokenFilter filter = new LowerCaseFilter(tokenizer);
        filter = new BuzzTokenFilter(filter, typeReplacementMap);
        filter = new HashtagTokenFilter(filter);
        filter = applyFilters(filter);
        return new TokenStreamComponents(tokenizer, filter);
    }

    /**
     * Replaces token types with model strings and replaces 3 or more repeated (consecutive) characters with 2 
     */
    protected static class BuzzTokenFilter extends TokenFilter {
        private Map<String, String> typeReplacementMap;
        private CharTermAttribute termAtt;
        private TypeAttribute typeAtt;

        protected BuzzTokenFilter(TokenStream input, Map<String, String> typeReplacementMap) {
            super(input);
            this.typeReplacementMap = typeReplacementMap;
            termAtt = addAttribute(CharTermAttribute.class);
            typeAtt = addAttribute(TypeAttribute.class);
        }

        @Override
        public final boolean incrementToken() throws IOException {
            // Increment the wrapped stream
            if (!input.incrementToken()) {
                return false;
            }
            
            final String type = typeAtt.type();
            boolean rpt = true;
            if (typeReplacementMap.containsKey(type)) {
                termAtt.setEmpty().append(typeReplacementMap.get(type));
                rpt = false;
            }
            if (rpt) {
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
    
    /**
     * Strip apostrophes from NT and NTVE contractions. Can be used for either or both
     */
    protected static class ApostropheNtFilter extends TokenFilter {
        private CharTermAttribute termAtt;
        private TypeAttribute typeAtt;

        protected ApostropheNtFilter(TokenStream input) {
            super(input);
            termAtt = addAttribute(CharTermAttribute.class);
            typeAtt = addAttribute(TypeAttribute.class);
        }

        @Override
        public final boolean incrementToken() throws IOException {
            if (!input.incrementToken()) {
                return false;
            }
            
            final String type = typeAtt.type();
            if (JflexBuzzTokenizer.APOSTROPHE_NT_STR.equals(type) || JflexBuzzTokenizer.APOSTROPHE_NTVE_STR.equals(type)) {
                final char[] buffer = termAtt.buffer();
                final int length = termAtt.length();
                final int idx = indexOfNt(buffer, length);
                termAtt.setEmpty();
                for (int i = 0; i < idx; i++) {
                    termAtt.append(buffer[i]);
                }
                if (JflexBuzzTokenizer.APOSTROPHE_NTVE_STR.equals(type)) {
                    termAtt.append("ntve");
                } else if (JflexBuzzTokenizer.APOSTROPHE_NT_STR.equals(type)) {
                    termAtt.append("nt");
                } else {
                    throw new UnsupportedOperationException("Looks like someone forgot to implement a case");
                }
            } else if (JflexBuzzTokenizer.APOSTROPHE_STR.equals(type)) {
                final char[] buffer = termAtt.buffer();
                final int length = termAtt.length();
                // throw out everything after the apostrophe
                termAtt.setEmpty();
                for (int i = 0; i < length; i++) {
                    if (buffer[i] == '\'' || buffer[i] == '’' || buffer[i] == '´') {
                        break;
                    }
                    termAtt.append(buffer[i]);
                }
            }
            return true;
        }

        // find the occurrence of 'nt or n't (with repetition allowed)
        protected static final int indexOfNt(final char[] buffer, final int length) {
            // find the first apostrophe
            for (int i = 0; i < length; i++) {
                if (buffer[i] == '\'' || buffer[i] == '’' || buffer[i] == '´') {
                    if (buffer[i + 1] == 't') {
                        // backtrack until we're out of Ns
                        while (i > 0 && buffer[i - 1] == 'n') {
                            i--;
                        }
                        return i;
                    } else {
                        // we matched before an N, return the index
                        return i;
                    }
                }
            }
            return -1;
        }
    }
    
    /**
     * Transform a hashtag into 2 tokens: the hashtag without the leading # and a hashtag model.
     * Uses {@link CompoundWordTokenFilterBase} with no dictionary to simplify token addition
     */
    protected static class HashtagTokenFilter extends CompoundWordTokenFilterBase {
        public static final String HASHTAG_MODEL = "chmodelhashtag";
        private TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
        
        protected HashtagTokenFilter(TokenStream input) {
            super(input, null, 1, 1, 255, true); // dictionary is unused
        }

        @Override
        protected void decompose() {
            if (JflexBuzzTokenizer.HASHTAG_STR.equals(typeAtt.type())) {
                // add the token without leading #
                tokens.add(new CompoundToken(1, termAtt.length() - 1));
                
                // change the current term to the hashtag model
                termAtt.setEmpty();
                termAtt.append(HASHTAG_MODEL);
            }
        }
    }
    
    /**
     * Collapses consecutive user mentions into 1 token
     */
    protected static class UserMentionCollapsingFilter extends FilteringTokenFilter {
        private String previousType = null;
        private TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

        public UserMentionCollapsingFilter(TokenStream input) {
            super(input);
        }
        
        @Override
        protected boolean accept() throws IOException {
            if (JflexBuzzTokenizer.USER_STR.equals(typeAtt.type()) && typeAtt.type().equals(previousType)) {
                return false;
            } else {
                previousType = typeAtt.type();
                return true;
            }
        }
    }
    
}
