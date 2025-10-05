package com.moonshot.buzz.buzz3.tokenizer;

import com.moonshot.buzz.buzz3.tokenizer.jflex.DutchAnalyzer;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Created by csullivan on 3/19/15.
 */
public class BuzzDutchTokenizer extends BuzzTokenizer {
    private static final String stemSpellFile = "/com/moonshot/buzz/sentiment/tokenizer/dutchStemSpell.map";
    private static final Set<String> notWords = ImmutableSet.of("niet", "nooit", "niets", "geen");
    private static final Set<String> skipWords = ImmutableSet.of("de", "en", "van", "ik", "te", "dat", "die", "in", "een", "hij", "het", "zijn", "is", "was", "op", "aan", "met", "als",
        "voor", "had", "er", "maar", "om", "hem", "dan", "zou", "of", "wat", "mijn", "men", "dit", "zo", "door", "over",
        "ze", "zich", "bij", "ook", "tot", "je", "mij", "uit", "der", "daar", "haar", "naar", "heb", "hoe", "heeft",
        "hebben", "deze", "u", "want", "nog", "zal", "me", "zij", "nu", "ge", "omdat", "iets", "worden", "toch", "al",
        "waren", "veel", "meer", "doen", "toen", "moet", "ben", "zonder", "kan", "hun", "dus", "alles", "onder", "ja",
        "eens", "hier", "wie", "werd", "altijd", "doch", "wordt", "wezen", "kunnen", "ons", "zelf", "tegen", "na",
        "reeds", "wil", "kon", "uw", "iemand", "geweest", "andere");
    private static final Set<String> conjunctiveWords = ImmutableSet.of("chmodelpunctuation", "chmodelquestion", "chmodelexclamation");

    private final Object2IntOpenHashMap<String> stemSpellMap = new Object2IntOpenHashMap<>();


    @Override
    protected void initializeResources() throws Exception {
        loadMap(this.getClass().getResourceAsStream(stemSpellFile), stemSpellMap);
    }

    // this negation method looks both forward and backward
    private IntList negateSpellStem(String[] tokens) {
        IntArrayList transformedList = new IntArrayList();
        for (int i = 0; i < tokens.length; i++) {
            String s = tokens[i];
            final boolean stem = stemSpellMap.containsKey(s);
            final boolean neg = stemSpellMap.containsKey(s + "_not");
            if (!stem && !neg) {
                continue;
            }
            int maxForwardWindow = Math.min(tokens.length - 1, i + 2);
            int maxBackwardWindow = Math.max(0, i - 2);

            boolean negated = false;

            for (int j = i + 1; j <= maxForwardWindow; j++) {
                if (conjunctiveWords.contains(tokens[j])) {
                    break;
                }
                if (notWords.contains(tokens[j])) {
                    negated = true;
                    break;
                }
            }
            for (int j = i - 1; j >= maxBackwardWindow; j--) {
                if (conjunctiveWords.contains(tokens[j])) {
                    break;
                }
                if (notWords.contains(tokens[j])) {
                    negated = true;
                    break;
                }
            }

            if (negated) {
                if (neg && !conjunctiveWords.contains(s) && !skipWords.contains(s)) {
                    transformedList.add(stemSpellMap.getInt(s + "_not"));
                }

            } else {
                if (stem && !conjunctiveWords.contains(s) && !skipWords.contains(s)) {
                    transformedList.add(stemSpellMap.getInt(s));
                }
            }
        }

        return transformedList;
    }

    protected static List<String> tokenize(String sentence) {
        return tokenize(sentence, DutchAnalyzer.INSTANCE);
    }

    @Override
    public IntList tokenIndices(String string, Function<String, List<String>> tokenizer) {
        return negateSpellStem(tokenizer.apply(string).toArray(new String[0]));
    }

    @Override
    public IntList tokenIndices(String sentence) {
        return tokenIndices(sentence, BuzzDutchTokenizer::tokenize);

    }
}
