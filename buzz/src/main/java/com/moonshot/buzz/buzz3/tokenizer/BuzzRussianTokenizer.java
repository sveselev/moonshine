package com.moonshot.buzz.buzz3.tokenizer;

import com.moonshot.buzz.buzz3.tokenizer.jflex.RussianAnalyzer;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.List;
import java.util.Set;
import java.util.function.Function;


public class BuzzRussianTokenizer extends BuzzTokenizer {
    private static final String stemSpellFile = "/com/moonshot/buzz/sentiment/tokenizer/russianStemSpell.map";
    private static final Set<String> notWords = ImmutableSet.of("не", "ни");

    private Object2IntOpenHashMap<String> stemSpellMap = new Object2IntOpenHashMap<>();

    @Override
    protected void initializeResources() throws Exception {
        loadMap(this.getClass().getResourceAsStream(stemSpellFile), stemSpellMap);
    }

    private IntList negateSpellStem(String[] tokens) {
        int i = 0;
        IntArrayList transformedList = new IntArrayList();
        while (i < tokens.length) {
            String s1 = tokens[i];

            if (notWords.contains(s1)) {
                if (i != tokens.length - 1) {
                    if (stemSpellMap.containsKey(tokens[i + 1] + "_not")) {
                        transformedList.add(stemSpellMap.getInt(tokens[i + 1] + "_not"));
                    }
                }
                i += 2;
            } else {
                if (stemSpellMap.containsKey(s1)) {
                    transformedList.add(stemSpellMap.getInt(s1));
                }
                i += 1;
            }

        }
        return transformedList;
    }

    public static List<String> tokenize(String sentence) {
        return tokenize(sentence, RussianAnalyzer.INSTANCE);
    }

    @Override
    public IntList tokenIndices(String sentence) {
        return tokenIndices(sentence, BuzzRussianTokenizer::tokenize);
    }

    @Override
    public IntList tokenIndices(String string, Function<String, List<String>> tokenizer) {
        return negateSpellStem(tokenizer.apply(string).toArray(new String[0]));
    }
}
