package com.moonshot.buzz.buzz3.tokenizer;

import com.moonshot.buzz.buzz3.tokenizer.jflex.PortugueseAnalyzer;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * User: frankie
 */
public class BuzzPortugueseTokenizer extends BuzzTokenizer {
    private static final String stemSpellFile = "/com/moonshot/buzz/sentiment/tokenizer/portugueseStemSpell.map";
    private static final String stopwordsFile = "/com/moonshot/buzz/sentiment/tokenizer/portugueseStopwords.txt";

    private static final Set<String> notWords = ImmutableSet.of("não", "n", "ñ", "nao", "jamais", "nem", "num", "naum");
    private static final Set<String> conjunctiveWords = ImmutableSet.of("chmodelpunctuation", "chmodelquestion", "chmodelexclamation");

    private Set<String> skipWords;
    private Object2IntOpenHashMap<String> stemSpellMap = new Object2IntOpenHashMap<>();

    @Override
    protected void initializeResources() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(stopwordsFile)));) {
            String line = null;
            Set<String> skip = new HashSet<>();
            while ((line = reader.readLine()) != null) {
                skip.add(line);
            }
            this.skipWords = ImmutableSet.copyOf(skip);
        }
        loadMap(this.getClass().getResourceAsStream(stemSpellFile), stemSpellMap);
    }

    private IntList negateSpellStem(String[] tokens) {
        int i = 0;

        IntArrayList transformedList = new IntArrayList();
        while (i < tokens.length) {
            String s = tokens[i];
            s = s.replaceAll("\\s+", "");
            int j = 1;

            if (!s.equals("") && !s.equals("chmodelpunctuation")) {
                if (notWords.contains(s)) {
                    while (true) {

                        if (i + j >= tokens.length || conjunctiveWords.contains(tokens[i + j]) || j > 4) {
                            break;
                        }

                        if (!skipWords.contains(tokens[i + j])) {
                            if (stemSpellMap.containsKey(tokens[i + j] + "_not")) {
                                transformedList.add(stemSpellMap.getInt(tokens[i + j] + "_not"));
                            }
                        }
                        j++;
                    }
                } else {
                    if (stemSpellMap.containsKey(s)) {
                        transformedList.add(stemSpellMap.getInt(s));
                    }
                }
            }
            i += j;
        }
        return transformedList;
    }

    public static List<String> tokenize(String sentence) {
        return tokenize(sentence, PortugueseAnalyzer.INSTANCE);
    }

    @Override
    public IntList tokenIndices(String sentence) {
        return tokenIndices(sentence, BuzzPortugueseTokenizer::tokenize);
    }

    @Override
    public IntList tokenIndices(String string, Function<String, List<String>> tokenizer) {
        return negateSpellStem(tokenizer.apply(string).toArray(new String[0]));
    }

}
