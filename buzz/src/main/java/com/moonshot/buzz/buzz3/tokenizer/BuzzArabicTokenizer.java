package com.moonshot.buzz.buzz3.tokenizer;

import com.moonshot.buzz.buzz3.tokenizer.jflex.ArabicAnalyzer;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BuzzArabicTokenizer extends BuzzTokenizer {
    private static final Set<String> notWords = ImmutableSet.of("\u0644\u0627", "\u0644\u064A\u0633", "\u0644\u0645", "\u0644\u0646");

    private IntList negateSpellStem(List<String> tokens) {
        int i = 0;
        IntArrayList transformedList = new IntArrayList();

        while (i < tokens.size()) {
            String s1 = tokens.get(i);
            if (notWords.contains(s1)) {
                if (i != tokens.size() - 1) {
                    String s2 = tokens.get(i + 1) + "_not";
                    if (getVocabulary().containsKey(s2)) {
                        transformedList.add(getVocabulary().getInt(s2));
                    }
                }
                i += 2;
            } else {
                if (getVocabulary().containsKey(s1)) {
                    transformedList.add(getVocabulary().getInt(s1));
                }
                i += 1;
            }

        }
        return transformedList;
    }

    protected static List<String> tokenize(String sentence) {
        return tokenize(sentence, ArabicAnalyzer.INSTANCE);
    }

    @Override
    public IntList tokenIndices(String sentence) {
        return tokenIndices(sentence, BuzzArabicTokenizer::tokenize);
    }

    @Override
    public IntList tokenIndices(String sentence, Function<String, List<String>> tokenizer) {
        return negateSpellStem(tokenizer.apply(sentence).stream()
            .map(ArabicStemmer.INSTANCE::stemWord)
            .collect(Collectors.toList())
        );
    }
}
