package com.moonshot.buzz.buzz3.tokenizer;

import com.moonshot.buzz.buzz3.tokenizer.jflex.ItalianAnalyzer;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Created with IntelliJ IDEA. User: aykut Date: 11/9/13 Time: 6:48 PM To change this template use File | Settings | File Templates.
 */
public class BuzzItalianTokenizer extends BuzzTokenizer {
    private static final String stemSpellFile = "/com/moonshot/buzz/sentiment/tokenizer/italianStemSpell.map";
    private static final Set<String> notWords = ImmutableSet.of("non", "no", "mai");
    private static final Set<String> skipWords = ImmutableSet.of("potere", "tutto", "troppo", "appena", "essere", "avere", "dovere", "fare", "e'", "l'", "tu", "noi", "voi",
        "lei", "lui", "a", "al", "c", "ce", "che", "chi", "ci", "cio", "co", "col", "con", "così", "cosï¿½", "cè", "d", "da", "dal", "de", "del", "dell",
        "della", "di", "due", "e", "ecco", "ed", "fa", "g", "gli", "il", "in", "io", "l", "la", "loro", "ma", "me", "mi", "mio", "ms", "ne", "nel", "nella",
        "non", "nostro", "o", "ogni", "per", "perché", "perciò", "però", "più", "poi", "puo", "qual", "quale", "quel", "quella", "quelli", "quello", "questa",
        "questi", "s", "se", "si", "stare", "su", "sua", "sue", "sul", "sull", "suo", "t", "ti", "tra", "tuo", "un", "una", "ve", "vi", "vostro", "vs", "yoyo",
        "è", "é");
    private static final Set<String> conjunctiveWords = ImmutableSet.of("chmodelpunctuation", "chmodelquestion", "chmodelexclamation");

    private final Object2IntOpenHashMap<String> stemSpellMap = new Object2IntOpenHashMap<>();

    @Override
    protected void initializeResources() throws Exception {
        loadMap(this.getClass().getResourceAsStream(stemSpellFile), stemSpellMap);
    }

    private IntList negateSpellStem(String[] tokens) {
        int i = 0;

        IntArrayList transformedList = new IntArrayList();
        while (i < tokens.length) {
            String s = tokens[i];
            s = s.replaceAll("\\s+", "");
            int j = 1;

            if (!s.isEmpty() && !s.equals("chmodelpunctuation")) {
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
        return tokenize(sentence, ItalianAnalyzer.INSTANCE);
    }

    @Override
    public IntList tokenIndices(String sentence) {
        return tokenIndices(sentence, BuzzItalianTokenizer::tokenize);
    }

    @Override
    public IntList tokenIndices(String string, Function<String, List<String>> tokenizer) {
        return negateSpellStem(tokenizer.apply(string).toArray(new String[0]));
    }
}
