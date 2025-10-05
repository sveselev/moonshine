package com.moonshot.buzz.buzz3.tokenizer;

import com.google.common.collect.ImmutableSet;
import com.twitter.penguin.korean.TwitterKoreanProcessorJava;
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * created by csullivan@moonshot 12/1/2016
 */
public class BuzzKoreanTokenizer extends BuzzTokenizer {
    private static final String hahaRegEx = "[muah][hau]+";
    private static final String omgRegEx = "o+m+f*g+";
    private static final String wowRegEx = "w{2,}o+w+|w+o{2,}w+|w+o+w{2,}";
    private static final String damnRegEx = "d{2,}a+m+n+|d+a{2,}m+n+|d+a+m{2,}n+|d+a+m+n{2,}";
    private static final String oopsRegEx = "o{2,}p+s+";
    private static final String noRegEx = "n{2,}o+|n+o{2,}";
    private static final String lolRegEx = "l{2,}o+l+|l+o{2,}l+|l+o+l{2,}";
    private static final String fckRegEx = "f{2,}u+c+k+|f+u{2,}+c+k+|f+u+c{2,}k+|f+u+c+k{2,}";
    private static final String happyRegEx = "h{2,}a+p{2,}y+|h+a{2,}+p{2,}y+|h+a+p{3,}y+|h+a+p{2,}y{2,}";
    private static final String loveRegEx = "l{2,}o+v+e+s*|l+o{2,}+v+e+s*|l+o+v{2,}e+s*|l+o+v+e{2,}s*|l+o+v+e+s{2,}";
    private static final String yesRegEx = "y{2,}e+s+|y+e{2,}s+|y+e+s{2,}";
    private static final String emojiNegative = "💔|😫|😒|😩|😭|😞|😡|😠|💢|💩|😔|😕|😢|😤|😣|👎|😖|😰|😓|😨|😱|😪";
    private static final String hashTagRegEx = "\\#+([\\w_]+[\\w'_\\-]*[\\w_]*)";

    private static final Pattern emojiNegativePattern = Pattern.compile(emojiNegative);
    private static final Pattern hashTagRegExPattern = Pattern.compile("(?:^|\\s)" + hashTagRegEx);
    private static final Pattern hahaRegExPattern = Pattern.compile("(^|\\s)" + hahaRegEx + "(\\b|\\s)");
    private static final Pattern omgRegExPattern = Pattern.compile("(^|\\s)" + omgRegEx + "(\\b|\\s)");
    private static final Pattern wowRegExPattern = Pattern.compile("(^|\\s)" + wowRegEx + "(\\b|\\s)");
    private static final Pattern damnRegExPattern = Pattern.compile("(^|\\s)" + damnRegEx + "(\\b|\\s)");
    private static final Pattern oopsRegExPattern = Pattern.compile("(^|\\s)" + oopsRegEx + "(\\b|\\s)");
    private static final Pattern noRegExPattern = Pattern.compile("(^|\\s)" + noRegEx + "(\\b|\\s)");
    private static final Pattern lolRegExPattern = Pattern.compile("(^|\\s)" + lolRegEx + "(\\b|\\s)");
    private static final Pattern fckRegExPattern = Pattern.compile("(^|\\s)" + fckRegEx + "(\\b|\\s)");
    private static final Pattern happyRegExPattern = Pattern.compile("(^|\\s)" + happyRegEx + "(\\b|\\s)");
    private static final Pattern loveRegExPattern = Pattern.compile("(^|\\s)" + loveRegEx + "(\\b|\\s)");
    private static final Pattern yesRegExPattern = Pattern.compile("(^|\\s)" + yesRegEx + "(\\b|\\s)");

    private static final String easternEmoticonsPositive = "\\([\\^\\~\\-]_[\\^\\~\\-]\\)|｡◕‿◕｡|0-0|☻|o-o|◕‿◕|\\([\\^\\~\\-]▼[\\^\\~\\-]\\)|\\([\\^\\~\\-]o[\\^\\~\\-]\\)|>=\\}|T\\-T|:A|b\\([\\^\\~\\-]_[\\^\\~\\-]\\)d|\\\\\\([\\^\\~\\-]\\_[\\^\\~\\-]\\)/|\\\\õ/";
    private static final String easternEmoticonsNegative = "\\(>\\.<\\)|\\(;\\_;\\)|\\(T\\_T\\)|\\(;w;\\)|\\(TT\\_TT\\)|\\(;A;\\)|\\(T\\^T\\)|\\(QAQ\\)|\\(Q\\_Q\\)|\\(Ç\\.Ç\\)|\\(Ç\\_Ç\\)|\\(v\\_v\\)|\\(v00v\\)|\\(O//o\\)|\\(o//O\\)|\\(>//<\\)|\\(\\-\\_\\-;\\),|\\(\\-\\_\\-[U'#]\\)|\\(\\-\\.\\-'?\\)|\\(=\\.=\\)|\\(\\-\\_\\_\\_\\-\\)";
    private static final Pattern easternEmoticonsPositivePattern = Pattern.compile(easternEmoticonsPositive);
    private static final Pattern easternEmoticonsNegativePattern = Pattern.compile(easternEmoticonsNegative);

    private static final String not_words = "안";
    private static final String not_suffixes = "지";
    private static final Set<String> not_prefixes = ImmutableSet.of("않","못");

    private static String transformSentenceBeforeTokenization(String sentence) {
        sentence = sentence.toLowerCase();

        sentence = doReplace(doubleQuoteRegExPattern, sentence, " $2 ");
        sentence = doReplace(emphasisRegExPattern, sentence, " $2 ");

        if (sentence.contains("http") || sentence.contains("ftp://") || sentence.contains("www.")) {
            sentence = doReplace(uriRegExPattern, sentence, " URL ");
        }
        if (sentence.contains("<3")) {
            sentence = doReplace(heartPlusRegExPattern, sentence, " chheart ");
        }

        sentence = doReplace(emojiHeartsRegExPattern, sentence, " chheart ");

        if (sentence.indexOf('#') != -1) {
            sentence = doReplace(hashTagRegExPattern, sentence, " $1 ");
        }

        sentence = doReplace(positiveSmileyRegExPattern, sentence, "  chpositiveemoji ");
        sentence = doReplace(negativeSmileyRegExPattern, sentence, " chnegativeemoji ");

        sentence = doReplace(emojiPositivePattern, sentence, " chpositiveemoji ");
        sentence = doReplace(emojiNegativePattern, sentence, " chnegativeemoji ");

        sentence = doReplace(easternEmoticonsPositivePattern, sentence, " chpositiveemoji ");
        sentence = doReplace(easternEmoticonsNegativePattern, sentence, " chnegativeemoji ");

        if (sentence.indexOf('@') != -1) {
            sentence = doReplace(twitterUserRegExPattern, sentence, " user ");
        }
        if (sentence.indexOf('!') != -1) {
            sentence = doReplace(exclamationRegExPattern, sentence, " chexclamation ");
        }
        if (sentence.indexOf('?') != -1) {
            sentence = doReplace(questionRegExPattern, sentence, " chquestion ");
        }
        if (sentence.indexOf('#') != -1) {
            sentence = doReplace(hashTagRegExPatternPadded, sentence, " ");
        }

        sentence = doReplace(hahaRegExPattern, sentence, " haha ");

        if (sentence.contains("om") && (sentence.contains("mg") || sentence.contains("fg"))) {
            sentence = doReplace(omgRegExPattern, sentence, " omg ");
        }
        if (sentence.contains("wo") && sentence.contains("ow")) {
            sentence = doReplace(wowRegExPattern, sentence, " wow ");
        }
        if (sentence.contains("mn") && sentence.contains("da")) {
            sentence = doReplace(damnRegExPattern, sentence, " damn ");
        }
        if (sentence.contains("oop")) {
            sentence = doReplace(oopsRegExPattern, sentence, " oops ");
        }
        if (sentence.contains("no")) {
            sentence = doReplace(noRegExPattern, sentence, " no ");
        }
        if (sentence.contains("lo") && sentence.contains("ol")) {
            sentence = doReplace(lolRegExPattern, sentence, " lol ");
        }
        if (sentence.contains("fu") && sentence.contains("ck")) {
            sentence = doReplace(fckRegExPattern, sentence, " fuck ");
        }
        if (sentence.contains("ppy") && sentence.contains("ha")) {
            sentence = doReplace(happyRegExPattern, sentence, " happy ");
        }
        if (sentence.contains("ye")) {
            if (sentence.contains("es")) {
                sentence = doReplace(yesRegExPattern, sentence, " yes ");
            }
            if (sentence.contains("ah")) {
                sentence = doReplace(yeahRegExPattern, sentence, " yes ");
            }
        }
        if (sentence.contains("ar") && (sentence.contains("rg") || sentence.contains("rh"))) {
            sentence = doReplace(arghRegExPattern, sentence, " argh ");
        }
        if (sentence.contains("ov") && sentence.contains("lo")) {
            sentence = doReplace(loveRegExPattern, sentence, " love ");
        }
        if (sentence.contains("lm") && sentence.contains("ao")) {
            sentence = doReplace(lmaRegExPattern, sentence, " lol ");
        }
        if (sentence.indexOf('\'') != -1 || sentence.indexOf('´') != -1) {
            sentence = doReplace(singleQuoteRegEx1Pattern, sentence, "$1$2 ");
            sentence = doReplace(singleQuoteRegEx2Pattern, sentence, "$1ntve ");
            sentence = doReplace(singleQuoteRegEx3Pattern, sentence, "$1$2  ");
            sentence = doReplace(singleQuoteRegEx4Pattern, sentence, "$1ntve  ");
        }

        sentence = sentence.replaceAll(remainingPunctuationRegEx, "  ");

        if (sentence.indexOf('-') != -1 || sentence.indexOf('_') != -1) {
            sentence = doReplace(dashUnderscoreRegExPattern, sentence, " ");
        }

        return sentence;
    }


    // this negation method looks both forward and backward
    private IntList negateSpellStem(List<String> tokens) {

        IntArrayList transformedList = new IntArrayList();
        List<String> toks = new ArrayList<>();
        try {
            for (int i = 0; i < tokens.size(); i++) {
                String token = tokens.get(i);
                String negToken = tokens.get(i) + "_not";
                if (!getVocabulary().containsKey(tokens.get(i)) && !getVocabulary().containsKey(tokens.get(i) + "_not")) {
                    continue;
                }
                boolean tok = getVocabulary().containsKey(tokens.get(i));
                boolean neg_tok = getVocabulary().containsKey(tokens.get(i) + "_not");
                int maxForwardWindow = Math.min(tokens.size() - 1, i + 2);
                int maxBackwardWindow = Math.max(0, i - 2);

                boolean negated_word = false;
                boolean negated_prefix = false;
                boolean negated_suffix = false;

                for (int j = i + 1; j <= maxForwardWindow; j++) {
                    int token_length = tokens.get(j).length();
                    if (j + 1 < tokens.size()) {
                        if (not_suffixes.equalsIgnoreCase(tokens.get(j).substring(token_length - 1, token_length)) && not_prefixes.contains(tokens.get(j + 1).substring(0, 1))) {
                            negated_suffix = true;
                            break;
                        }
                    }
                }
                for (int j = i - 1; j >= maxBackwardWindow; j--) {

                    if (not_words.equalsIgnoreCase(tokens.get(j))) {
                        negated_word = true;
                        break;
                    }
                    if (not_prefixes.contains(tokens.get(j).substring(0, 1))) {

                        negated_prefix = true;
                        break;
                    }
                }

                if (negated_word) {
                    if (neg_tok) {
                        transformedList.add(getVocabulary().getInt(negToken));
                        toks.add(negToken);
                    }

                } else if (negated_suffix) {
                    if (neg_tok) {
                        transformedList.add(getVocabulary().getInt(negToken));
                        toks.add(negToken);
                    }
                } else if (negated_prefix) {
                    if (neg_tok) {
                        transformedList.add(getVocabulary().getInt(negToken));
                        toks.add(negToken);
                    }
                } else {
                    if (tok) {
                        transformedList.add(getVocabulary().getInt(token));
                        toks.add(token);
                    }
                }

            }

        } catch (IndexOutOfBoundsException e) {

            e.printStackTrace();
        }
        return transformedList;
    }

    @Override
    public IntList tokenIndices(String sentence, Function<String, List<String>> tokenizer) {
        return negateSpellStem(tokenizer.apply(sentence).stream()
            .map(BuzzTokenizer::replaceRepeatedCharacters)
            .collect(Collectors.toList()));
    }

    @Override
    public IntList tokenIndices(String sentence) {
        return tokenIndices(sentence, BuzzKoreanTokenizer::tokenize);
    }

    public static List<String> tokenize(String sentence) {
        sentence = transformSentenceBeforeTokenization(sentence);
        CharSequence normalized = TwitterKoreanProcessorJava.normalize(sentence);
        Seq<KoreanTokenizer.KoreanToken> koreanTokens = TwitterKoreanProcessorJava.tokenize(normalized);
        List<String> tokens = TwitterKoreanProcessorJava.tokensToJavaStringList(koreanTokens);
        return tokens;
    }
}
