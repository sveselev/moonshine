package com.moonshot.buzz.buzz3.tokenizer;

import com.moonshot.buzz.buzz3.tokenizer.jflex.FrenchAnalyzer;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * User: frankie
 */
public class BuzzFrenchTokenizer extends BuzzTokenizer {
    private static final String stemSpellFile = "/com/moonshot/buzz/sentiment/tokenizer/frenchStemSpell.map";

    private static final Set<String> notWords = ImmutableSet.of("jamais", "aucun", "aucune", "ni");
    private static final Set<String> conjunctiveWords = ImmutableSet.of("chmodelpunctuation", "chmodelquestion", "chmodelexclamation", "chmodellink");

    private final Object2IntOpenHashMap<String>  stemSpellMap = new Object2IntOpenHashMap<>();

    @Override
    protected void initializeResources() throws Exception {
        loadMap(this.getClass().getResourceAsStream(stemSpellFile), stemSpellMap);
    }

    protected static void loadMap(InputStream mapStream, Map<String, Integer> map) throws IOException {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(mapStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                map.put(tokens[0], Integer.parseInt(tokens[1]));
            }
        }
    }

    private IntList negateSpellStem(String[] tokens) {
        int i = 0;
        IntArrayList transformedList = new IntArrayList();
        while (i < tokens.length) {
            if (i == tokens.length - 1) {
                if (stemSpellMap.containsKey(tokens[i])) {
                    transformedList.add(stemSpellMap.getInt(tokens[i]));
                }
                break;
            }
            String s1 = tokens[i];
            String s2 = tokens[i + 1];
            if (s2.equals("pas")) {
                if (stemSpellMap.containsKey(s1 + "_not")) {
                    transformedList.add(stemSpellMap.getInt(s1 + "_not"));
                }
                i += 2;
            } else if (notWords.contains(s1)) {
                int j = 0;
                while (true) {
                    if (i + j >= tokens.length || conjunctiveWords.contains(tokens[i + j]) || j > 3) {
                        break;
                    }
                    if (stemSpellMap.containsKey(tokens[i + j] + "_not")) {
                        transformedList.add(stemSpellMap.getInt(tokens[i + j] + "_not"));
                    }
                    j++;
                }
                i += j;
            } else {
                if (stemSpellMap.containsKey(s1)) {
                    transformedList.add(stemSpellMap.getInt(s1));
                }
                i += 1;
            }
        }
        return transformedList;
    }

    protected static List<String> tokenize(String sentence) {
        return tokenize(sentence, FrenchAnalyzer.INSTANCE);
    }

    @Override
    public IntList tokenIndices(String sentence) {
        return tokenIndices(sentence, BuzzFrenchTokenizer::tokenize);
    }

    @Override
    public IntList tokenIndices(String string, Function<String, List<String>> tokenizer) {
        return negateSpellStem(tokenizer.apply(string).toArray(new String[0]));
    }

}
