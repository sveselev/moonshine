package com.moonshot.buzz.buzz3.tokenizer;

import com.moonshot.buzz.buzz3.tokenizer.jflex.TurkishAnalyzer;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Created with IntelliJ IDEA. User: aykut Date: 12/2/13 Time: 6:50 PM To change this template use File | Settings | File Templates.
 */
public class BuzzTurkishTokenizer extends BuzzTokenizer {
    private static final Set<String> notWords = ImmutableSet.of("degil", "deil", "diil", "değil");
    static BuzzTurkishStemmer turkishStemmer = new BuzzTurkishStemmer();

    private IntList negateSpellStem(String[] tokens) {
        int i = 0;

        IntArrayList transformedList = new IntArrayList();
        while (i < tokens.length) {
            if (i == tokens.length - 1) {
                String s1 = tokens[i];
                String sMap = turkishStemmer.stem(s1);
                if (getVocabulary().containsKey(sMap)) {
                    transformedList.add(getVocabulary().get(sMap));
                }
                break;
            }
            String s1 = tokens[i];
            String s2 = tokens[i + 1];
            if (notWords.contains(s2)) {
                String sMap = turkishStemmer.stem(s1);
                if (sMap.contains("_not")) {
                    sMap = sMap.replaceAll("_not", ""); // double negation
                } else {
                    sMap += "_not";
                }
                if (getVocabulary().containsKey(sMap)) {
                    transformedList.add(getVocabulary().getInt(sMap));
                }
                i += 2;

            } else {
                String sMap = turkishStemmer.stem(s1);
                if (getVocabulary().containsKey(sMap)) {
                    transformedList.add(getVocabulary().getInt(sMap));
                }
                i += 1;
            }
        }
        return transformedList;
    }

    public static List<String> tokenize(String sentence) {
        return tokenize(sentence, TurkishAnalyzer.INSTANCE);
    }

    @Override
    public IntList tokenIndices(String sentence) {
        return tokenIndices(sentence, BuzzTurkishTokenizer::tokenize);
    }

    @Override
    public IntList tokenIndices(String string, Function<String, List<String>> tokenizer) {
        return negateSpellStem(tokenizer.apply(string).toArray(new String[0]));
    }
}
