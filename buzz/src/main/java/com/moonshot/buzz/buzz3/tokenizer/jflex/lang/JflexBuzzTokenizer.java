package com.moonshot.buzz.buzz3.tokenizer.jflex.lang;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizerImpl;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Function;

/**
 * Min token length of 2, max of 255
 */
public class JflexBuzzTokenizer extends Tokenizer {

    public static final int ALPHANUM = 0;
    public static final int APOSTROPHE = 1;
    public static final int APOSTROPHE_NT = 2;
    public static final int APOSTROPHE_NTVE = 3;
    public static final int URL = 4;
    public static final int HEARTS = 5;
    public static final int POSITIVE = 6;
    public static final int NEGATIVE = 7;
    public static final int EXCLAMATION = 8;
    public static final int QUESTION = 9;
    public static final int HAHA = 10;
    public static final int OMG = 11;
    public static final int WOW = 12;
    public static final int DAMN = 13;
    public static final int OOPS = 14;
    public static final int NO = 15;
    public static final int LOL = 16;
    public static final int FUCK = 17;
    public static final int HAPPY = 18;
    public static final int LOVE = 19;
    public static final int YES = 20;
    public static final int ARGH = 21;
    public static final int USER = 22;
    public static final int HASHTAG = 23;
    public static final int HEHE = 24;
    public static final int KISSES = 25;
    public static final int OK = 26;
    public static final int PUNCTUATION = 27;

    // -- strings --

    public static final String ALPHANUM_STR = "<ALPHANUM>";
    public static final String APOSTROPHE_STR = "<APOSTROPHE>";
    public static final String APOSTROPHE_NT_STR = "<APOSTROPHE_NT>";
    public static final String APOSTROPHE_NTVE_STR = "<APOSTROPHE_NTVE>";

    public static final String URL_STR = "<URL>";
    public static final String HEARTS_STR = "<HEARTS>";
    public static final String POSITIVE_STR = "<POSITIVE>";
    public static final String NEGATIVE_STR = "<NEGATIVE>";
    public static final String EXCLAMATION_STR = "<EXCLAMATION>";
    public static final String QUESTION_STR = "<QUESTION>";
    public static final String HAHA_STR = "<HAHA>";
    public static final String OMG_STR = "<OMG>";
    public static final String WOW_STR = "<WOW>";
    public static final String DAMN_STR = "<DAMN>";
    public static final String OOPS_STR = "<OOPS>";
    public static final String NO_STR = "<NO>";
    public static final String LOL_STR = "<LOL>";
    public static final String FUCK_STR = "<FUCK>";
    public static final String HAPPY_STR = "<HAPPY>";
    public static final String LOVE_STR = "<LOVE>";
    public static final String YES_STR = "<YES>";
    public static final String ARGH_STR = "<ARGH>";
    public static final String USER_STR = "<USER>";
    public static final String HASHTAG_STR = "<HASHTAG>";
    public static final String HEHE_STR = "<HEHE>";
    public static final String KISSES_STR = "<KISSES>";
    public static final String OK_STR = "<OK>";
    public static final String PUNCTUATION_STR = "<PUNCTUATION>";

    /** String token types that correspond to token type int constants */
    public static final String[] TOKEN_TYPES = new String[] {
        ALPHANUM_STR,
        APOSTROPHE_STR,
        APOSTROPHE_NT_STR,
        APOSTROPHE_NTVE_STR,
        URL_STR,
        HEARTS_STR,
        POSITIVE_STR,
        NEGATIVE_STR,
        EXCLAMATION_STR,
        QUESTION_STR,
        HAHA_STR,
        OMG_STR,
        WOW_STR,
        DAMN_STR,
        OOPS_STR,
        NO_STR,
        LOL_STR,
        FUCK_STR,
        HAPPY_STR,
        LOVE_STR,
        YES_STR,
        ARGH_STR,
        USER_STR,
        HASHTAG_STR,
        HEHE_STR,
        KISSES_STR,
        OK_STR,
        PUNCTUATION_STR
    };
    
    private JflexLexer scanner;
    private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

    // this tokenizer generates three attributes:
    // offset, positionIncrement and type
    private CharTermAttribute termAtt;
    private OffsetAttribute offsetAtt;
    private PositionIncrementAttribute posIncrAtt;
    private TypeAttribute typeAtt;

    private int skippedPositions;

    /** @see #setMaxTokenLength */
    public int getMaxTokenLength() {
        return maxTokenLength;
    }

    public JflexBuzzTokenizer(Function<Reader, JflexLexer> scannerFunc) {
        this.scanner = scannerFunc.apply(input);
        termAtt = addAttribute(CharTermAttribute.class);
        offsetAtt = addAttribute(OffsetAttribute.class);
        posIncrAtt = addAttribute(PositionIncrementAttribute.class);
        typeAtt = addAttribute(TypeAttribute.class);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();
        skippedPositions = 0;

        while (true) {
            int tokenType = scanner.getNextToken();

            if (tokenType == StandardTokenizerImpl.YYEOF) {
                return false;
            }

            if (scanner.yylength() <= maxTokenLength ) {
                posIncrAtt.setPositionIncrement(skippedPositions + 1);
                scanner.getText(termAtt);
                final int start = scanner.yychar();
                offsetAtt.setOffset(correctOffset(start), correctOffset(start + termAtt.length()));
                typeAtt.setType(TOKEN_TYPES[tokenType]);
                return true;
            } else {
                // When we skip a too-long term, we still increment the
                // position increment
                skippedPositions++;
            }
        }
    }

    @Override
    public final void end() throws IOException {
        super.end();
        // set final offset
        int finalOffset = correctOffset(scanner.yychar() + scanner.yylength());
        offsetAtt.setOffset(finalOffset, finalOffset);
        // adjust any skipped tokens
        posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + skippedPositions);
    }

    @Override
    public void close() throws IOException {
        super.close();
        scanner.yyreset(input);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        scanner.yyreset(input);
        skippedPositions = 0;
    }
}
