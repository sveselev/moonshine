package com.moonshot.buzz.emotion;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * csullivan - progenitor, jamdor - implementatorer, sungerer - jflexer
 */
@AllArgsConstructor
@Slf4j
public class FeatureExtractor {
    private final Set<String> modelFeatures;

    public List<String> extractFeatures(String contents) {
        try {
            if (StringUtils.isBlank(contents)) {
                return Collections.emptyList();
            }
            final ArrayList<String> tokens = new ArrayList<>();
            try (TokenStream s = EmotionAnalyzer.INSTANCE.tokenStream(null, contents);) {
                final CharTermAttribute text = s.getAttribute(CharTermAttribute.class);
                s.reset();
                while (s.incrementToken()) {
                    tokens.add(text.toString());
                }
            }
            return filter(tokens);
        } catch (Exception e) {
            log.warn("Failed to tokenize for emotions", e);
            return Collections.emptyList();
        }
    }
    
    private static List<String> getBigrams(List<String> tokens) {
        ArrayList<String> bigrams = new ArrayList<>();

        for (int i = 0; i < tokens.size() - 1; i++) {
            List<String> words = Arrays.asList(tokens.get(i), tokens.get(i + 1));
            String bigram = String.join(" ", words);
            bigrams.add(bigram);
        }

        return bigrams;
    }

    private static List<String> getTrigrams(List<String> tokens) {
        ArrayList<String> trigrams = new ArrayList<>();

        for (int i = 0; i < tokens.size() - 2; i++) {
            List<String> words = Arrays.asList(tokens.get(i), tokens.get(i + 1), tokens.get(i + 2));
            String trigram = String.join(" ", words);
            trigrams.add(trigram);
        }

        return trigrams;
    }

    private List<String> filter(List<String> tokens) {
        List<String> ngrams = new ArrayList<>(tokens);
        ngrams.addAll(getBigrams(tokens));
        ngrams.addAll(getTrigrams(tokens));

        ArrayList<String> features = new ArrayList<>();

        // check candidate unigrams, bigrams, trigrams if in model.
        for (String ngram : ngrams) {
            if (this.modelFeatures.contains(ngram)) {
                features.add(ngram);
            }
        }
        return features;
    }

}
