package com.moonshot.buzz.buzz3.tokenizer;

import com.moonshot.buzz.buzz3.tokenizer.jflex.GermanAnalyzer;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Created with IntelliJ IDEA. User: aykut Date: 12/6/13 Time: 11:06 AM To change this template use File | Settings | File Templates.
 */
public class BuzzGermanTokenizer extends BuzzTokenizer {
    private static final String stemSpellFile = "/com/moonshot/buzz/sentiment/tokenizer/germanStemSpell.map";
    private static final Set<String> notWords = ImmutableSet.of("nicht", "kein", "keine", "nie", "nichts");
    private static final Set<String> conjunctiveWords = ImmutableSet.of("chmodelpunctuation", "chmodelquestion", "chmodelexclamation");

    private final Object2IntOpenHashMap<String> stemSpellMap = new Object2IntOpenHashMap<>();

    @Override
    protected void initializeResources() throws Exception {
        loadMap(this.getClass().getResourceAsStream(stemSpellFile), stemSpellMap);
    }

    private IntList negateSpellStem(String[] tokens) {
        IntArrayList transformedList = new IntArrayList();

        for (int i = 0; i < tokens.length; i++) {
            boolean added = false;
            boolean stem = stemSpellMap.containsKey(tokens[i]);
            int sMap = stemSpellMap.getInt(tokens[i]);
            boolean neg = stemSpellMap.containsKey(tokens[i] + "_not");
            int sMapNeg = stemSpellMap.getInt(tokens[i] + "_not");
            if (!stem && !neg) {
                continue;
            }
            int maxForwardWindow = Math.min(tokens.length - 1, i + 3);
            int maxBackwardWindow = Math.max(0, i - 3);
            for (int j = i + 1; j <= maxForwardWindow; j++) {
                if (conjunctiveWords.contains(tokens[j])) {
                    if (stem) {
                        transformedList.add(sMap);
                    }
                    added = true;
                    break;
                }
                if (notWords.contains(tokens[j])) {
                    if (neg) {
                        transformedList.add(sMapNeg);
                    }
                    added = true;
                    break;
                }
            }
            for (int j = i - 1; j >= maxBackwardWindow; j--) {
                if (conjunctiveWords.contains(tokens[j])) {
                    if (stem) {
                        transformedList.add(sMap);
                    }
                    added = true;
                    break;
                }
                if (notWords.contains(tokens[j])) {
                    if (neg) {
                        transformedList.add(sMapNeg);
                    }
                    added = true;
                    break;
                }
            }
            if (!added && stem) {
                transformedList.add(sMap);
            }
        }


        return transformedList;
    }

    protected static List<String> tokenize(String sentence) {
        return tokenize(sentence, GermanAnalyzer.INSTANCE);
    }

    @Override
    public IntList tokenIndices(String sentence) {
        return tokenIndices(sentence, BuzzGermanTokenizer::tokenize);
    }

    @Override
    public IntList tokenIndices(String string, Function<String, List<String>> tokenizer) {
        return negateSpellStem(tokenizer.apply(string).toArray(new String[0]));
    }
}
