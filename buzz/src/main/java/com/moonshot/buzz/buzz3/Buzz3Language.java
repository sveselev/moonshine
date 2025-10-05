package com.moonshot.buzz.buzz3;

import com.moonshot.buzz.buzz3.BaseBuzz3.Buzz3Algorithm;
import com.moonshot.buzz.buzz3.tokenizer.BuzzArabicTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.BuzzChineseTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.BuzzDutchTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.BuzzEnglishTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.BuzzFrenchTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.BuzzGermanTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.BuzzIndonesianTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.BuzzItalianTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.BuzzJapaneseTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.BuzzKoreanTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.BuzzPortugueseTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.BuzzRussianTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.BuzzSpanishTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.BuzzSwedishTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.BuzzTokenizer;
import com.moonshot.buzz.buzz3.tokenizer.BuzzTurkishTokenizer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines metadata around supported languages used in {@link BaseBuzz3} analysis
 *
 * @author Steve Ungerer
 */
public enum Buzz3Language {
    Arabic("ar", 3, 2, 1, 5, BuzzArabicTokenizer.class),
    Chinese("zh", 1, 1, 3, 1, BuzzChineseTokenizer.class),
    English("en", 1, 3, 2, 1, BuzzEnglishTokenizer.class),
    French("fr", 1, 3, 2, 1, BuzzFrenchTokenizer.class),
    German("de", 2, 3, 3, 1, BuzzGermanTokenizer.class),
    Italian("it", 1, 3, 2, 1, BuzzItalianTokenizer.class),
    Japanese("ja", 6, 1, 3, 3, BuzzJapaneseTokenizer.class),
    Portuguese("pt", 2, 1, 2, 2, BuzzPortugueseTokenizer.class),
    Russian("ru", 4, 2, 3, 2, BuzzRussianTokenizer.class),
    Spanish("es", 1, 3, 2, 1, BuzzSpanishTokenizer.class),
    Turkish("tr", 1, 3, 3, 1, BuzzTurkishTokenizer.class),
    Dutch("nl", 2, 3, 3, 1, BuzzDutchTokenizer.class),
    Korean("ko", 0, 1, 0, 1, BuzzKoreanTokenizer.class),
    Indonesian("id",0,1,0,1,BuzzIndonesianTokenizer.class),
    Malaysian("ms",0, 1, 0, 1, BuzzIndonesianTokenizer.class),
    Swedish("sv",0,1,0,1,BuzzSwedishTokenizer .class);

    private static final String NAIVE_BAYES_MODEL_FILE = "/com/moonshot/buzz/buzz3/models/%1$s/%1$sNaiveBayes.model";
    private static final String VOCAB_FILE = "/com/moonshot/buzz/buzz3/models/%1$s/%1$s.vocab";
    private static final String LINEAR_MODEL_FILE = "/com/moonshot/buzz/buzz3/models/%1$s/%1$sLibLinear.model";
    private static final String VW_MODEL_FILE = "/com/moonshot/buzz/buzz3/models/%1$s/%1$sVW.model";
    private static final String BOOSTER_MODEL_NAMES = "/com/moonshot/buzz/buzz3/models/%1$s/%1$sBooster.names";
    private static final String BOOSTER_MODEL_SHYP = "/com/moonshot/buzz/buzz3/models/%1$s/%1$sBooster.shyp";

    private final String languageCode;
    private final Class<? extends BuzzTokenizer> tokenizerClass;
    private final Map<Buzz3Algorithm, Double> weights;

    Buzz3Language(
        String languageCode,
        double decisionTreeBoostingWeight,
        double naiveBayesWeight,
        double vowpalWabbitWeight,
        double labLinearWeight,
        Class<? extends BuzzTokenizer> tokenizerClass) {
        this.languageCode = languageCode;
        Map<Buzz3Algorithm, Double> m = new HashMap<>();
        m.put(Buzz3Algorithm.DecisionTreeBoosting, decisionTreeBoostingWeight);
        m.put(Buzz3Algorithm.NaiveBayes, naiveBayesWeight);
        m.put(Buzz3Algorithm.VowpalWabbit, vowpalWabbitWeight);
        m.put(Buzz3Algorithm.LabLinear, labLinearWeight);
        this.weights = Collections.unmodifiableMap(m);
        this.tokenizerClass = tokenizerClass;
    }

    public Map<Buzz3Algorithm, Double> getWeights() {
        return weights;
    }

    public String getNaiveBayesModelFileUri() {
        return String.format(NAIVE_BAYES_MODEL_FILE, name().toLowerCase());
    }

    public String getVocabFileUri() {
        return String.format(VOCAB_FILE, name().toLowerCase());
    }

    public String getLinearModelFileUri() {
        return String.format(LINEAR_MODEL_FILE, name().toLowerCase());
    }

    public String getVWModelFileUri() {
        return String.format(VW_MODEL_FILE, name().toLowerCase());
    }

    public String getBoosterModelNamesUri() {
        return String.format(BOOSTER_MODEL_NAMES, name().toLowerCase());
    }

    public String getBoosterModelShypUri() {
        return String.format(BOOSTER_MODEL_SHYP, name().toLowerCase());
    }

    public Class<? extends BuzzTokenizer> getTokenizerClass() {
        return tokenizerClass;
    }

    public String getLanguageCode() {
        return languageCode;
    }
}
