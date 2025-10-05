package com.moonshot.buzz.buzz3.tokenizer;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Subclasses must provide a default, empty constructor
 */
public abstract class BuzzTokenizer {
    protected static final Logger logger = LoggerFactory.getLogger(BuzzTokenizer.class);

    public static final String doubleQuoteRegEx = "(['\\\"-*])(\\w+)\\1";
    public static final String emphasisRegEx = "(['\\\"*])(\\w+)\\1"; // " # $ % & ' ( ) *
    public static final String singleQuoteRegEx1 = "(\\w+)['´]([nt]{0,2})\\s";
    public static final String singleQuoteRegEx2 = "(\\w+)[´']nt['´]ve\\s";
    public static final String singleQuoteRegEx3 = "(\\w+)['´]([nt]{0,2})\\p{Punct}";
    public static final String singleQuoteRegEx4 = "(\\w+)nt['´]ve\\p{Punct}";
    public static final String emojiHeartsRegEx = "❤|♥|💕|💙|💚|💛|💜|💖|💗|💘|💝|💞|💟|💓|💒|💑|💐|💏|💌";
    public static final String emojiNegative = "💔|😫|😒|😩|😭|😞|😡|😠|💢";
    public static final String emojiPositive = "😃|💋|😝|👏|😂|☺|😍|😘|👌|😊|😏|😁|😳|✌|👍|😉|😌|😜|😋|🙏|😄|🌹|🎈|😆";
    public static final String uriRegEx = "((https?|ftp):/{1,3}|www\\.)[^\\s/$.?#].[^\\s]*";
    public static final String twitterUserRegEx = "@[\\w_]+";
    public static final String hashTagRegEx = "\\#+([\\w_]+[\\w'_\\-]*[\\w_]+)";
    public static final String positiveSmileyRegEx = "[<>]?[:;=8][\\-o\\*\\']?[\\)\\]DPp]|[\\(\\[P][\\-o\\*\\']?[:;=8][<>]?";

    public static final String negativeSmileyRegEx = "[<>]?[:;=8][\\-o\\*\\']?[\\(\\[]|[\\)\\]][\\-o\\*\\']?[:;=8][<>]?";
    public static final String remainingPunctuationRegEx = "\\p{Punct}+";
    public static final String yeahRegEx = "y{2,}e+a+h+|y+e{2,}a+h+|y+e+a{2,}h+|y+e+a+h{2,}";
    public static final String arghRegEx = "a+r+[gh]+";
    public static final String lmaRegEx = "l+m+f*a+o+";
    public static final String heartPlusRegEx = "<3+";
    public static final String exclamationRegEx = "\\!+";
    public static final String questionRegEx = "\\?";
    public static final String dashUnderscoreRegEx = "[\\-_]";

    public static final String singleQuoteRegEx = "\\w+'\\w*";
    public static final Pattern singleQuoteRegExPattern = Pattern.compile(singleQuoteRegEx);

    public static final Pattern doubleQuoteRegExPattern = Pattern.compile(doubleQuoteRegEx);
    public static final Pattern emphasisRegExPattern = Pattern.compile(emphasisRegEx);
    public static final Pattern uriRegExPattern = Pattern.compile("(\\b|^|\\s)" + uriRegEx + "(\\b|\\s)");
    public static final Pattern heartPlusRegExPattern = Pattern.compile(heartPlusRegEx);
    public static final Pattern emojiHeartsRegExPattern = Pattern.compile(emojiHeartsRegEx);
    public static final Pattern hashTagRegExPattern = Pattern.compile("(?:^|\\s)" + hashTagRegEx);
    public static final Pattern positiveSmileyRegExPattern = Pattern.compile(positiveSmileyRegEx);
    public static final Pattern emojiPositivePattern = Pattern.compile(emojiPositive);
    public static final Pattern negativeSmileyRegExPattern = Pattern.compile(negativeSmileyRegEx);
    public static final Pattern emojiNegativePattern = Pattern.compile(emojiNegative);
    public static final Pattern twitterUserRegExPattern = Pattern.compile("(^|\\s|\\\")" + twitterUserRegEx + "(\\b|\\s)");
    public static final Pattern exclamationRegExPattern = Pattern.compile(exclamationRegEx);
    public static final Pattern questionRegExPattern = Pattern.compile(questionRegEx);
    public static final Pattern hashTagRegExPatternPadded = Pattern.compile("(^|\\s)" + hashTagRegEx + "(\\b|\\s)");

    public static final Pattern yeahRegExPattern = Pattern.compile("(^|\\s)" + yeahRegEx + "(\\b|\\s)");
    public static final Pattern arghRegExPattern = Pattern.compile("(^|\\s)" + arghRegEx + "(\\b|\\s)");
    public static final Pattern lmaRegExPattern = Pattern.compile("(^|\\s)" + lmaRegEx + "(\\b|\\s)");
    public static final Pattern singleQuoteRegEx1Pattern = Pattern.compile(singleQuoteRegEx1);
    public static final Pattern singleQuoteRegEx2Pattern = Pattern.compile(singleQuoteRegEx2);
    public static final Pattern singleQuoteRegEx3Pattern = Pattern.compile(singleQuoteRegEx3);
    public static final Pattern singleQuoteRegEx4Pattern = Pattern.compile(singleQuoteRegEx4);
    public static final Pattern remainingPunctuationRegExPattern = Pattern.compile(remainingPunctuationRegEx);
    public static final Pattern dashUnderscoreRegExPattern = Pattern.compile(dashUnderscoreRegEx);

    private Object2IntMap<String> vocabulary;

    public void initialize(Object2IntMap<String> vocabulary) throws Exception {
        this.vocabulary = vocabulary;
        initializeResources();
    }

    protected static void loadMap(InputStream mapStream, Object2IntOpenHashMap<String> map) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(mapStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                map.put(tokens[0], Integer.parseInt(tokens[1]));
            }
        }
    }


    public final Object2IntMap<String> getVocabulary() {
        return vocabulary;
    }

    public abstract IntList tokenIndices(String sentence);

    public abstract IntList tokenIndices(String sentence, Function<String, List<String>> tokenizer);

    public static String doReplace(Pattern pattern, String sentence, String replacement) {
        return pattern.matcher(sentence).replaceAll(replacement);
    }

    /**
     * Perform any needed resource initialization
     * @param resourceLoader
     */
    protected void initializeResources() throws Exception {
        // default noop; override for resource initialization
    }

    /**
     * Walks through a given string and shorten repeated sequences of characters.
     * The replaced string will contain up to two consecutive letters at any place.
     * For instance, "Succccessssss" will be turned into "Success"
     *
     * @param s the string to replace
     * @return the replaced string
     */
    public static String replaceRepeatedCharacters(String s) {
        if (s == null) {
            return s;
        }
        char p = Character.MIN_CODE_POINT;
        boolean repeat = false;
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == p) {
                if (!repeat) {
                    sb.append(c);
                    repeat = true;
                }
            } else {
                p = c;
                repeat = false;
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static List<String> tokenize(String contents, Analyzer a) {
        try {
            if (StringUtils.isBlank(contents)) {
                return Collections.emptyList();
            }
            final ArrayList<String> tokens = new ArrayList<>();
            try (TokenStream s = a.tokenStream(null, contents)) {
                final CharTermAttribute text = s.getAttribute(CharTermAttribute.class);
                s.reset();
                while (s.incrementToken()) {
                    tokens.add(text.toString());
                }
            }
            return tokens;
        } catch (Exception e) {
            //TODO log
            return Collections.emptyList();
        }
    }

}
