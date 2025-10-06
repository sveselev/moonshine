package com.moonshot.buzz.emotion;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizerImpl;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;


/**
 * Min token length of 2, max of 255
 */
public class EmotionTokenizer extends Tokenizer {

    public static final int ALPHANUM = 0;
    public static final int APOSTROPHE = 1;

    public static final int URL = 2;
    public static final int HEARTS = 3;
    public static final int POSITIVE = 4;
    public static final int NEGATIVE = 5;
    public static final int SAD = 6;
    public static final int ANGER = 7;
    public static final int FEAR = 8;
    public static final int DISGUST = 9;
    public static final int HAHA = 10;
    public static final int OMG = 11;
    public static final int WOW = 12;
    public static final int REALLY = 13;
    public static final int SO = 14;
    public static final int DAMN = 15;
    public static final int OOPS = 16;
    public static final int NO = 17;
    public static final int LOL = 18;
    public static final int FUCK = 19;
    public static final int SHIT = 20;
    public static final int HAPPY = 21;
    public static final int LOVE = 22;
    public static final int YES = 23;
    public static final int ARGH = 24;
    public static final int UGH = 25;
    public static final int LMAO = 26;
    public static final int USER = 27;
    public static final int HASHTAG = 28;

    // -- strings --

    public static final String ALPHANUM_STR = "<ALPHANUM>";
    public static final String APOSTROPHE_STR = "<APOSTROPHE>";

    public static final String URL_STR = "<URL>";
    public static final String HEARTS_STR = "<HEARTS>";
    public static final String POSITIVE_STR = "<POSITIVE>";
    public static final String NEGATIVE_STR = "<NEGATIVE>";
    public static final String SAD_STR = "<SAD>";
    public static final String ANGER_STR = "<ANGER>";
    public static final String FEAR_STR = "<FEAR>";
    public static final String DISGUST_STR = "<DISGUST>";
    public static final String HAHA_STR = "<HAHA>";
    public static final String OMG_STR = "<OMG>";
    public static final String WOW_STR = "<WOW>";
    public static final String REALLY_STR = "<REALLY>";
    public static final String SO_STR = "<SO>";
    public static final String DAMN_STR = "<DAMN>";
    public static final String OOPS_STR = "<OOPS>";
    public static final String NO_STR = "<NO>";
    public static final String LOL_STR = "<LOL>";
    public static final String FUCK_STR = "<FUCK>";
    public static final String SHIT_STR = "<SHIT>";
    public static final String HAPPY_STR = "<HAPPY>";
    public static final String LOVE_STR = "<LOVE>";
    public static final String YES_STR = "<YES>";
    public static final String ARGH_STR = "<ARGH>";
    public static final String UGH_STR = "<UGH>";
    public static final String LMAO_STR = "<LMAO>";
    public static final String USER_STR = "<USER>";
    public static final String HASHTAG_STR = "<HASHTAG>";

    /** String token types that correspond to token type int constants */
    public static final String[] TOKEN_TYPES = new String[] {
        ALPHANUM_STR,
        APOSTROPHE_STR,
        URL_STR,
        HEARTS_STR,
        POSITIVE_STR,
        NEGATIVE_STR,
        SAD_STR,
        ANGER_STR,
        FEAR_STR,
        DISGUST_STR,
        HAHA_STR,
        OMG_STR,
        WOW_STR,
        REALLY_STR,
        SO_STR,
        DAMN_STR,
        OOPS_STR,
        NO_STR,
        LOL_STR,
        FUCK_STR,
        SHIT_STR,
        HAPPY_STR,
        LOVE_STR,
        YES_STR,
        ARGH_STR,
        UGH_STR,
        LMAO_STR,
        USER_STR,
        HASHTAG_STR
    };

    private final EmotionTokenizerImpl scanner;
    private int minTokenLength = 2;
    private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

    // this tokenizer generates three attributes:
    // offset, positionIncrement and type
    private final CharTermAttribute termAtt;
    private final OffsetAttribute offsetAtt;
    private final PositionIncrementAttribute posIncrAtt;
    private final TypeAttribute typeAtt;

    private int skippedPositions;


    /**
     * Set the max allowed token length. Any token longer
     * than this is skipped.
     */
    public void setMaxTokenLength(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("maxTokenLength must be greater than zero");
        }
        this.maxTokenLength = length;
        scanner.setBufferSize(Math.min(length, 1024 * 1024)); // limit buffer size to 1M chars
    }

    /** @see #setMaxTokenLength */
    public int getMaxTokenLength() {
        return maxTokenLength;
    }

    public EmotionTokenizer() {
        this.scanner = makeTokenizerImpl();
        termAtt = addAttribute(CharTermAttribute.class);
        offsetAtt = addAttribute(OffsetAttribute.class);
        posIncrAtt = addAttribute(PositionIncrementAttribute.class);
        typeAtt = addAttribute(TypeAttribute.class);
    }

    protected EmotionTokenizerImpl makeTokenizerImpl() {
        return new EmotionTokenizerImpl(input);
    }

    public void setMinTokenLength(int minTokenLength) {
        this.minTokenLength = minTokenLength;
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

            if (scanner.yylength() <= maxTokenLength && (tokenType != EmotionTokenizerImpl.ALPHANUM || scanner.yylength() >= minTokenLength)) {
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
