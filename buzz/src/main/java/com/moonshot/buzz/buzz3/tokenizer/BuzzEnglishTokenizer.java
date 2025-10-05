package com.moonshot.buzz.buzz3.tokenizer;

import com.moonshot.buzz.buzz3.tokenizer.jflex.EnglishAnalyzer;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class BuzzEnglishTokenizer extends BuzzTokenizer {
    private static final String stemSpellFile = "/com/moonshot/buzz/sentiment/tokenizer/englishStemSpell.map";

    private static final Set<String> notWords = ImmutableSet.of("aint", "arenot", "arent", "arnt", "caint", "cannot", "cant", "cantve", "couldnot", "couldnt", "couldntve",
        "ddnt", "didnot", "didnt", "doesnot", "doesnt", "donot", "dont", "hadnot", "hadnt", "hadntve", "haint", "hasnot", "hasnt", "havenot", "havent",
        "havnt", "icannot", "icant", "isnot", "isnt", "maynot", "maynt", "mightnot", "mightnt", "mightntve", "mustnot", "mustnt", "mustntve", "neednot",
        "needntve", "never", "no", "not", "nt", "oughntve", "oughtnot", "oughtnt", "shallnot", "shallnt", "shallntve", "shant", "shantve", "shouldnot",
        "shouldnt", "shouldntve", "wasnot", "wasnt", "werenot", "werent", "willnot", "wnt", "wont", "wouldnot", "wouldnt", "wouldntve");
    private static final Set<String> skipWords = ImmutableSet.of("the", "a", "an", "this", "that", "these", "those", "to");
    private final Object2IntOpenHashMap<String> stemSpellMap = new Object2IntOpenHashMap<>();

    private static final Set<String> conjunctiveWords = ImmutableSet.of("chmodelpunctuation", "chmodelquestion", "chmodelexclamation", "also", "anyway", "but", "because",
        "besides", "hence", "moreover", "nevertheless", "therefore", "thus");

    @Override
    protected void initializeResources() throws Exception {
        loadMap(this.getClass().getResourceAsStream(stemSpellFile), stemSpellMap);
    }

    private IntList negateSpellStem(String[] tokens) {
        int i = 0;

        IntList transformedList = new IntArrayList();
        while (i < tokens.length) {
            String s = tokens[i];
            int j = 1;

            if (!s.isEmpty() && !s.equals("chmodelpunctuation")) {
                if (notWords.contains(s)) {
                    while (true) {

                        if (i + j >= tokens.length || conjunctiveWords.contains(tokens[i + j]) || j > 3) {
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

    protected static List<String> tokenize(String contents) {
        return tokenize(contents, EnglishAnalyzer.INSTANCE);
    }

    @Override
    public IntList tokenIndices(String string) {
        return tokenIndices(string, BuzzEnglishTokenizer::tokenize);
    }

    @Override
    public IntList tokenIndices(String sentence, Function<String, List<String>> tokenizer) {
        if (StringUtils.isBlank(sentence)) {
            return IntLists.EMPTY_LIST;
        }
        return negateSpellStem(tokenizer.apply(sentence).toArray(new String[0]));
    }
}
