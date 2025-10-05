package com.moonshot.buzz.buzz3.tokenizer;

import com.moonshot.buzz.buzz3.tokenizer.jflex.IndonesianAnalyzer;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class BuzzIndonesianTokenizer extends BuzzTokenizer {

    private static final Set<String> notWords = ImmutableSet.of("tidak", "tak", "tdk", "tanpa", "bukan", "kurang", "jangan", "belum");

    private IntList negateSpellStem(String[] tokens) {
        int i = 0;
        IntArrayList transformedList = new IntArrayList();
        while (i < tokens.length) {
            String s = tokens[i];
            int j = 1;

            if (!s.isEmpty() && !s.equals("chmodelpunctuation")) {
                if (notWords.contains(s)) {
                    transformedList.add(getVocabulary().getInt(s));
                    while (true) {
                        if (i + j >= tokens.length || j > 2) {
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

    public static String[] filterUserMentions(String[] tokens) {
        List<String> toks = new ArrayList<>();

        int user_counter = 0;
        int index = 0;
        while (index < tokens.length) {
            String tok = tokens[index];
            if (!tok.equalsIgnoreCase("user")) {
                toks.add(tok);
                index += 1;
                user_counter = 0;
            } else if (user_counter > 0) {
                index += 1;
            } else {
                toks.add(tok);
                user_counter += 1;
                index += 1;
            }
        }

        return String.join(" ", toks).split(" ");

    }

    protected static List<String> tokenize(String sentence) {
        return tokenize(sentence, IndonesianAnalyzer.INSTANCE);
    }

    @Override
    public IntList tokenIndices(String sentence) {
        return tokenIndices(sentence, BuzzIndonesianTokenizer::tokenize);
    }

    @Override
    public IntList tokenIndices(String string, Function<String, List<String>> tokenizer) {
        return negateSpellStem(tokenizer.apply(string).toArray(new String[0]));
    }
}
