package com.moonshot.buzz.buzz3.tokenizer;

import com.moonshot.buzz.buzz3.tokenizer.jflex.SwedishAnalyzer;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class BuzzSwedishTokenizer extends BuzzTokenizer {
    private static final Set<String> notWords = ImmutableSet.of("ingen", "inte", "ej", "inga", "utan", "inget", "aldrig", "icke");

    private IntList negateSpellStem(String[] tokens) {
        int i = 0;
        IntArrayList transformedList = new IntArrayList();
        while (i < tokens.length) {
            String s = tokens[i];
            s = s.replaceAll("\\s+", "");
            int j = 1;

            if (!s.equals("") && !s.equals("chmodelpunctuation")) {
                if (notWords.contains(s)) {
                    transformedList.add(getVocabulary().getInt(s));
                    while (true) {
                        if (i + j >= tokens.length || j > 3) {
                            break;
                        }

                        if (getVocabulary().containsKey(tokens[i + j] + "_not")) {
                            transformedList.add(getVocabulary().getInt(tokens[i + j] + "_not"));
                        }
                        j++;
                    }
                } else {
                    if (getVocabulary().containsKey(s)) {
                        transformedList.add(getVocabulary().getInt(s));
                    }
                }
            }
            i += j;
        }
        return transformedList;
    }


    public static List<String> tokenize(String sentence) {
        return tokenize(sentence, SwedishAnalyzer.INSTANCE);
    }

    @Override
    public IntList tokenIndices(String sentence) {
        return tokenIndices(sentence, BuzzSwedishTokenizer::tokenize);
    }

    @Override
    public IntList tokenIndices(String string, Function<String, List<String>> tokenizer) {
        return negateSpellStem(tokenizer.apply(string).toArray(new String[0]));
    }
}
