package com.moonshot.buzz.buzz3.tokenizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA. User: aykut Date: 12/6/13 Time: 4:47 PM To change this template use File | Settings | File Templates.
 */
public class BuzzChineseTokenizer extends BuzzTokenizer {
    private static final ChineseSimplifier simplifier = new ChineseSimplifier();
    private static final ChineseTokenizer chineseTokenizer = new ChineseTokenizer();

    static final String hahaRegEx = "[muah][hau]+";
    static final String omgRegEx = "o+m+f*g+";
    static final String wowRegEx = "w{2,}o+w+|w+o{2,}w+|w+o+w{2,}";
    static final String damnRegEx = "d{2,}a+m+n+|d+a{2,}m+n+|d+a+m{2,}n+|d+a+m+n{2,}";
    static final String oopsRegEx = "o{2,}p+s+";
    static final String noRegEx = "n{2,}o+|n+o{2,}";
    static final String lolRegEx = "l{2,}o+l+|l+o{2,}l+|l+o+l{2,}";
    static final String fckRegEx = "f{2,}u+c+k+|f+u{2,}+c+k+|f+u+c{2,}k+|f+u+c+k{2,}";
    static final String happyRegEx = "h{2,}a+p{2,}y+|h+a{2,}+p{2,}y+|h+a+p{3,}y+|h+a+p{2,}y{2,}";
    static final String loveRegEx = "l{2,}o+v+e+s*|l+o{2,}+v+e+s*|l+o+v{2,}e+s*|l+o+v+e{2,}s*|l+o+v+e+s{2,}";
    static final String yesRegEx = "y{2,}e+s+|y+e{2,}s+|y+e+s{2,}";

    static final Pattern hahaRegExPattern = Pattern.compile("(^|\\s)" + hahaRegEx + "(\\b|\\s)");
    static final Pattern omgRegExPattern = Pattern.compile("(^|\\s)" + omgRegEx + "(\\b|\\s)");
    static final Pattern wowRegExPattern = Pattern.compile("(^|\\s)" + wowRegEx + "(\\b|\\s)");
    static final Pattern damnRegExPattern = Pattern.compile("(^|\\s)" + damnRegEx + "(\\b|\\s)");
    static final Pattern oopsRegExPattern = Pattern.compile("(^|\\s)" + oopsRegEx + "(\\b|\\s)");
    static final Pattern noRegExPattern = Pattern.compile("(^|\\s)" + noRegEx + "(\\b|\\s)");
    static final Pattern lolRegExPattern = Pattern.compile("(^|\\s)" + lolRegEx + "(\\b|\\s)");
    static final Pattern fckRegExPattern = Pattern.compile("(^|\\s)" + fckRegEx + "(\\b|\\s)");
    static final Pattern happyRegExPattern = Pattern.compile("(^|\\s)" + happyRegEx + "(\\b|\\s)");
    static final Pattern loveRegExPattern = Pattern.compile("(^|\\s)" + loveRegEx + "(\\b|\\s)");
    static final Pattern yesRegExPattern = Pattern.compile("(^|\\s)" + yesRegEx + "(\\b|\\s)");

    private static final Set<String> notWords = ImmutableSet.of("不", "没有", "不会", "摆脱", "免去", "避免");
    private static final Set<String> conjunctiveWords = ImmutableSet.of("chmodelpunctu", "chmodelquest", "chmodelexclamat");

    private static String transformSentenceBeforeTokenization(String sentence) {
        sentence = simplifier.convert(sentence);

        sentence = doReplace(doubleQuoteRegExPattern, sentence, " $2 ");
        sentence = doReplace(emphasisRegExPattern, sentence, " $2 ");

        if (sentence.contains("http") || sentence.contains("ftp://") || sentence.contains("www.")) {
            sentence = doReplace(uriRegExPattern, sentence, " chmodellink ");
        }
        if (sentence.contains("<3")) {
            sentence = doReplace(heartPlusRegExPattern, sentence, " chmodelheart ");
        }

        sentence = doReplace(emojiHeartsRegExPattern, sentence, " chmodelheart ");

        if (sentence.indexOf('#') != -1) {
            sentence = doReplace(hashTagRegExPattern, sentence, " #$1 $1 ");
        }

        sentence = doReplace(positiveSmileyRegExPattern, sentence, " chmodelpositivesmiley ");
        sentence = doReplace(negativeSmileyRegExPattern, sentence, " chmodelnegativesmiley ");

        sentence = doReplace(emojiPositivePattern, sentence, " chmodelpositivesmiley ");
        sentence = doReplace(emojiNegativePattern, sentence, " chmodelnegativesmiley ");

        if (sentence.indexOf('@') != -1) {
            sentence = doReplace(twitterUserRegExPattern, sentence, " chmodeltwitteruser ");
        }
        if (sentence.indexOf('!') != -1) {
            sentence = doReplace(exclamationRegExPattern, sentence, " chmodelexclamation ");
        }
        if (sentence.indexOf('?') != -1) {
            sentence = doReplace(questionRegExPattern, sentence, " chmodelquestion ");
        }
        if (sentence.indexOf('#') != -1) {
            sentence = doReplace(hashTagRegExPatternPadded, sentence, " chmodelhashtag ");
        }

        sentence = doReplace(hahaRegExPattern, sentence, " chmodelhaha ");

        if (sentence.contains("om") && (sentence.contains("mg") || sentence.contains("fg"))) {
            sentence = doReplace(omgRegExPattern, sentence, " chmodelomg ");
        }
        if (sentence.contains("wo") && sentence.contains("ow")) {
            sentence = doReplace(wowRegExPattern, sentence, " chmodelwow ");
        }
        if (sentence.contains("mn") && sentence.contains("da")) {
            sentence = doReplace(damnRegExPattern, sentence, " chmodeldamn ");
        }
        if (sentence.contains("oop")) {
            sentence = doReplace(oopsRegExPattern, sentence, " chmodeloop ");
        }
        if (sentence.contains("no")) {
            sentence = doReplace(noRegExPattern, sentence, " chmodelno ");
        }
        if (sentence.contains("lo") && sentence.contains("ol")) {
            sentence = doReplace(lolRegExPattern, sentence, " chmodellol ");
        }
        if (sentence.contains("fu") && sentence.contains("ck")) {
            sentence = doReplace(fckRegExPattern, sentence, " chmodelfuck ");
        }
        if (sentence.contains("ppy") && sentence.contains("ha")) {
            sentence = doReplace(happyRegExPattern, sentence, " chmodelhappy ");
        }
        if (sentence.contains("ye")) {
            if (sentence.contains("es")) {
                sentence = doReplace(yesRegExPattern, sentence, " chmodelyeah ");
            }
            if (sentence.contains("ah")) {
                sentence = doReplace(yeahRegExPattern, sentence, " chmodelyeah ");
            }
        }
        if (sentence.contains("ar") && (sentence.contains("rg") || sentence.contains("rh"))) {
            sentence = doReplace(arghRegExPattern, sentence, " argh ");
        }
        if (sentence.contains("ov") && sentence.contains("lo")) {
            sentence = doReplace(loveRegExPattern, sentence, " chmodellove ");
        }
        if (sentence.contains("lm") && sentence.contains("ao")) {
            sentence = doReplace(lmaRegExPattern, sentence, " chmodellol ");
        }
        if (sentence.indexOf('\'') != -1 || sentence.indexOf('´') != -1) {
            sentence = doReplace(singleQuoteRegEx1Pattern, sentence, "$1$2 ");
            sentence = doReplace(singleQuoteRegEx2Pattern, sentence, "$1ntve ");
            sentence = doReplace(singleQuoteRegEx3Pattern, sentence, "$1$2 chmodelpunctuation ");
            sentence = doReplace(singleQuoteRegEx4Pattern, sentence, "$1ntve chmodelpunctuation ");
        }

        sentence = doReplace(remainingPunctuationRegExPattern, sentence, " chmodelpunctuation ");

        if (sentence.indexOf('-') != -1 || sentence.indexOf('_') != -1) {
            sentence = doReplace(dashUnderscoreRegExPattern, sentence, " ");
        }

        return sentence;
    }

    private IntList negateSpellStem(List<String> tokens) {
        int i = 0;

        IntArrayList transformedList = new IntArrayList();
        while (i < tokens.size()) {
            String s = tokens.get(i);
            s = s.replaceAll("\\s+", "");
            int j = 1;

            if (!s.isEmpty() && !s.equals("chmodelpunctu")) {
                if (notWords.contains(s)) {
                    while (true) {
                        if (i + j >= tokens.size() || conjunctiveWords.contains(tokens.get(i + j)) || j > 3) {
                            break;
                        }
                        if (getVocabulary().containsKey(tokens.get(i + j) + "not")) {
                            transformedList.add(getVocabulary().get(tokens.get(i + j) + "_not"));
                        }
                        j++;
                    }
                } else {
                    if (getVocabulary().containsKey(s)) {
                        transformedList.add(getVocabulary().get(s));
                    }
                }
            }
            i += j;
        }
        return transformedList;
    }

    public static List<String> tokenize(String sentence) {
        sentence = transformSentenceBeforeTokenization(sentence);
        List<String> tokens = ImmutableList.copyOf(chineseTokenizer.tokens(sentence));
        return tokens.stream()
            .map(BuzzTokenizer::replaceRepeatedCharacters)
            .collect(Collectors.toList());
    }

    @Override
    public IntList tokenIndices(String sentence, Function<String, List<String>> tokenizer) {
        return negateSpellStem(tokenizer.apply(sentence));
    }

    @Override
    public IntList tokenIndices(String sentence) {
        return tokenIndices(sentence, BuzzChineseTokenizer::tokenize);
    }
}
