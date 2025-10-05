package com.moonshot.buzz.buzz3.tokenizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArabicStemmer {
    private static final String pathToStemmerFiles = "/com/moonshot/buzz/buzz3/tokenizer/arabic/stemmer/";

    public static final ArabicStemmer INSTANCE = new ArabicStemmer();

    private ArabicStemmer() {
        try {
            definiteArticles = readTokens(pathToStemmerFiles + "definite_article.txt" + ".utf8");
            duplicate = readTokensSet(pathToStemmerFiles + "duplicate.txt" + ".utf8");
            firstWaw = readTokensSet(pathToStemmerFiles + "first_waw.txt" + ".utf8");
            firstYah = readTokensSet(pathToStemmerFiles + "first_yah.txt" + ".utf8");
            lastAlif = readTokensSet(pathToStemmerFiles + "last_alif.txt" + ".utf8");
            lastHamza = readTokensSet(pathToStemmerFiles + "last_hamza.txt" + ".utf8");
            lastMaksoura = readTokensSet(pathToStemmerFiles + "last_maksoura.txt" + ".utf8");
            lastYah = readTokensSet(pathToStemmerFiles + "last_yah.txt" + ".utf8");
            midWaw = readTokensSet(pathToStemmerFiles + "mid_waw.txt" + ".utf8");
            midYah = readTokensSet(pathToStemmerFiles + "mid_yah.txt" + ".utf8");
            prefixes = readTokens(pathToStemmerFiles + "prefixes.txt" + ".utf8");
            quadRoots = readTokensSet(pathToStemmerFiles + "quad_roots.txt" + ".utf8");
            stopWords = readTokensSet(pathToStemmerFiles + "stopwords.txt" + ".utf8");
            suffixes = readTokens(pathToStemmerFiles + "suffixes.txt" + ".utf8");
            triPatt = readTokens(pathToStemmerFiles + "tri_patt.txt" + ".utf8");
            triRoots = readTokensSet(pathToStemmerFiles + "tri_roots.txt" + ".utf8");
            strange = readTokensSet(pathToStemmerFiles + "strange.txt" + ".utf8");
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    static class Status {
        public boolean rootFound = false;
        public boolean stopWordFound = false;
        public boolean fromSuffixes = false;
    }

    private final List<String> definiteArticles; // 0
    private final Set<String> duplicate; // 1
    private final Set<String> firstWaw; // 2
    private final Set<String> firstYah; // 3
    private final Set<String> lastAlif; // 4
    private final Set<String> lastHamza; // 5
    private final Set<String> lastMaksoura; // 6
    private final Set<String> lastYah; // 7
    private final Set<String> midWaw; // 8
    private final Set<String> midYah; // 9
    private final List<String> prefixes; // 10
    private final Set<String> quadRoots; // 12
    private final Set<String> stopWords; // 13
    private final List<String> suffixes; // 14
    private final List<String> triPatt; // 15
    private final Set<String> triRoots; // 16
    private final Set<String> strange; // 18


    private Set<String> readTokensSet(String fileName) throws Exception {
        return new HashSet<>(readTokens(fileName));
    }

    private List<String> readTokens(String fileName) throws Exception {
        List<String> tokenList = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(fileName)));
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            Collections.addAll(tokenList, tokens);
        }
        bufferedReader.close();

        return tokenList;
    }

    public String stemWord(String word) {

        if (word.isEmpty()) {
            return word;
        }
        Status status = new Status();
        // String word = s.replaceAll("\\P{L}","");
        // check if the word consists of two letters
        // and find it's root
        if (word.length() == 2) {
            word = stemTwoLetters(word, status);
        }

        // if the word consists of three letters
        if (word.length() == 3 && !status.rootFound) {
            // check if it's a root
            word = stemThreeLetters(word, status);
        }

        // if the word consists of four letters
        if (word.length() == 4) {
            // check if it's a root
            stemFourLetters(word, status);
        }

        // if the root hasn't yet been found
        if (!status.rootFound) {
            // check if the word is a pattern
            word = checkPatterns(word, status);
        }

        // if the root still hasn't been found
        if (!status.rootFound) {
            // check for a definite article, and remove it
            word = checkDefiniteArticle(word, status);
        }

        // if the root still hasn't been found
        if (!status.rootFound && !status.stopWordFound) {
            // check for the prefix waw
            word = checkPrefixWaw(word, status);
        }

        // if the root STILL hasnt' been found
        if (!status.rootFound && !status.stopWordFound) {
            // check for suffixes
            word = checkSuffixes(word, status);
        }

        // if the root STILL hasn't been found
        if (!status.rootFound && !status.stopWordFound) {
            // check for prefixes
            word = checkPrefixes(word, status);
        }
        return word;
    }

    private void stemFile(String inputFile, String outputFile) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inputFile)), StandardCharsets.UTF_8));
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputFile)), StandardCharsets.UTF_8));
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            // System.out.println(count++);
            String[] tokens = line.split("\\s");
            for (String s : tokens) {
                s = s.replaceAll("\\P{L}", "");
                if (s.isEmpty()) {
                    continue;
                }
                if (!strange.contains(s) && !stopWords.contains(s)) {
                    bufferedWriter.write(stemWord(s) + " ");
                } else {
                    bufferedWriter.write(s + " ");
                }
            }
            bufferedWriter.write("\n");
        }


        bufferedReader.close();
        bufferedWriter.close();
    }

    private String checkPrefixes(String word, Status status) {

        String modifiedWord = word;

        for (String prefix : prefixes) {

            if (prefix.regionMatches(0, modifiedWord, 0, prefix.length())) {
                modifiedWord = modifiedWord.substring(prefix.length());

                // check to see if the word is a stopword
                if (checkStopwords(modifiedWord, status)) {
                    return modifiedWord;
                }

                // check to see if the word is a root of three or four letters
                // if the word has only two letters, test to see if one was removed
                if (modifiedWord.length() == 2) {
                    modifiedWord = stemTwoLetters(modifiedWord, status);
                } else if (modifiedWord.length() == 3 && !status.rootFound) {
                    modifiedWord = stemThreeLetters(modifiedWord, status);
                } else if (modifiedWord.length() == 4) {
                    stemFourLetters(modifiedWord, status);
                }

                // if the root hasn't been found, check for patterns
                if (!status.rootFound && modifiedWord.length() > 2) {
                    modifiedWord = checkPatterns(modifiedWord, status);
                }

                // if the root STILL hasn't been found
                if (!status.rootFound && !status.stopWordFound && !status.fromSuffixes) {
                    // check for suffixes
                    modifiedWord = checkSuffixes(modifiedWord, status);
                }

                if (status.stopWordFound) {
                    return modifiedWord;
                }

                // if the root was found, return the modified word
                if (status.rootFound && !status.stopWordFound) {
                    return modifiedWord;
                }
            }
        }
        return word;
    }


    private String checkSuffixes(String word, Status status) {

        String modifiedWord = word;

        status.fromSuffixes = true;

        for (String suffix : suffixes) {

            if (suffix.regionMatches(0, modifiedWord, modifiedWord.length() - suffix.length(), suffix.length())) {
                modifiedWord = modifiedWord.substring(0, modifiedWord.length() - suffix.length());

                if (checkStopwords(modifiedWord, status)) {
                    status.fromSuffixes = false;
                    return modifiedWord;
                }

                if (modifiedWord.length() == 2) {
                    modifiedWord = stemTwoLetters(modifiedWord, status);
                } else if (modifiedWord.length() == 3) {
                    modifiedWord = stemThreeLetters(modifiedWord, status);
                } else if (modifiedWord.length() == 4) {
                    stemFourLetters(modifiedWord, status);
                }

                if (!status.rootFound && modifiedWord.length() > 2) {
                    modifiedWord = checkPatterns(modifiedWord, status);
                }

                if (status.stopWordFound) {
                    status.fromSuffixes = false;
                    return modifiedWord;
                }

                if (status.rootFound) {
                    status.fromSuffixes = false;
                    return modifiedWord;
                }
            }
        }
        status.fromSuffixes = false;
        return word;
    }


    private String checkPrefixWaw(String word, Status status) {
        String modifiedWord;

        if (word.length() > 3 && word.charAt(0) == '\u0648') {
            modifiedWord = word.substring(1);


            if (checkStopwords(modifiedWord, status)) {
                return modifiedWord;
            }

            if (modifiedWord.length() == 2) {
                modifiedWord = stemTwoLetters(modifiedWord, status);
            } else if (modifiedWord.length() == 3 && !status.rootFound) {
                modifiedWord = stemThreeLetters(modifiedWord, status);
            } else if (modifiedWord.length() == 4) {
                stemFourLetters(modifiedWord, status);
            }

            // if the root hasn't been found, check for patterns
            if (!status.rootFound && modifiedWord.length() > 2) {
                modifiedWord = checkPatterns(modifiedWord, status);
            }

            // if the root STILL hasnt' been found
            if (!status.rootFound && !status.stopWordFound) {
                // check for suffixes
                modifiedWord = checkSuffixes(modifiedWord, status);
            }

            // iIf the root STILL hasn't been found
            if (!status.rootFound && !status.stopWordFound) {
                // check for prefixes
                modifiedWord = checkPrefixes(modifiedWord, status);
            }

            if (status.stopWordFound) {
                return modifiedWord;
            }

            if (status.rootFound && !status.stopWordFound) {
                return modifiedWord;
            }
        }
        return word;
    }

    private String lastWeak(String word, Status status) {
        if (lastAlif.contains(word)) {
            status.rootFound = true;
            word += "\u0627";
        } else if (lastHamza.contains(word)) {
            status.rootFound = true;
            word += "\u0623";
        } else if (lastMaksoura.contains(word)) {
            status.rootFound = true;
            word += "\u0649";
        } else if (lastYah.contains(word)) {
            status.rootFound = true;
            word += "\u064a";
        }
        return word;
    }

    private String firstWeak(String word, Status status) {
        if (firstWaw.contains(word)) {
            status.rootFound = true;
            word = "\u0648" + word;
        } else if (firstYah.contains(word)) {
            status.rootFound = true;
            word = "\u064a" + word;
        }
        return word;
    }

    private String middleWeak(String word, Status status) {
        if (midWaw.contains(word)) {
            status.rootFound = true;
            word = word.substring(0, 1) + "\u064a" + word.substring(1);
        } else if (midYah.contains(word)) {
            status.rootFound = true;
            word = word.substring(0, 1) + "\u0648" + word.substring(1);
        }
        return word;
    }


    private String stemTwoLetters(String word, Status status) {
        if (duplicate.contains(word)) {
            status.rootFound = true;
            word += word.substring(1);
        }
        if (!status.rootFound) {
            word = lastWeak(word, status);
        }
        if (!status.rootFound) {
            word = firstWeak(word, status);
        }
        if (!status.rootFound) {
            word = middleWeak(word, status);
        }
        return word;
    }

    private String stemThreeLetters(String word, Status status) {

        String root = "";

        if (word.length() > 0) {
            if (word.charAt(0) == '\u0627' || word.charAt(0) == '\u0624' || word.charAt(0) == '\u0626') {
                root = '\u0623' + word.substring(1);
            }

            if (word.charAt(2) == '\u0648' || word.charAt(2) == '\u064a' || word.charAt(2) == '\u0627' ||
                word.charAt(2) == '\u0649' || word.charAt(2) == '\u0621' || word.charAt(2) == '\u0626') {
                root = word.substring(0, 2);
                root = lastWeak(root, status);
                if (status.rootFound) {
                    return root;
                }

            }

            if (word.charAt(1) == '\u0648' || word.charAt(1) == '\u064a' || word.charAt(1) == '\u0627' || word.charAt(1) == '\u0626') {
                root = word.substring(0, 1);
                root = root + word.substring(2);


                root = middleWeak(root, status);
                if (status.rootFound) {
                    return root;
                }
            }

            if (word.charAt(1) == '\u0624' || word.charAt(1) == '\u0626') {
                if (word.charAt(2) == '\u0645' || word.charAt(2) == '\u0632' || word.charAt(2) == '\u0631') {
                    root = word.substring(0, 1);
                    root = root + '\u0627';
                    root = root + word.substring(2);
                } else {
                    root = word.substring(0, 1);
                    root = root + '\u0623';
                    root = root + word.substring(2);
                }
            }

            if (word.charAt(2) == '\u0651') {
                root = word.substring(0, 1);
                root = root + word.charAt(1);
            }
        }


        if (root.isEmpty()) {
            if (triRoots.contains(word)) {
                status.rootFound = true;
                return word;
            }
        } else if (triRoots.contains(root)) {
            status.rootFound = true;
            return root;
        }

        return word;
    }

    private void stemFourLetters(String word, Status status) {
        if (quadRoots.contains(word)) {
            status.rootFound = true;
        }

    }

    private boolean checkStopwords(String currentWord, Status status) {
        status.stopWordFound = stopWords.contains(currentWord);
        return status.stopWordFound;
    }



    private String checkDefiniteArticle(String word, Status status) {


        String modifiedWord = "";

        for (String definiteArticle : definiteArticles) {
            if (definiteArticle.regionMatches(0, word, 0, definiteArticle.length())) {

                modifiedWord = word.substring(definiteArticle.length());
                if (checkStopwords(modifiedWord, status)) {
                    return modifiedWord;
                }

                if (modifiedWord.length() == 2) {
                    modifiedWord = stemTwoLetters(modifiedWord, status);
                } else if (modifiedWord.length() == 3 && !status.rootFound) {
                    modifiedWord = stemThreeLetters(modifiedWord, status);
                } else if (modifiedWord.length() == 4) {
                    stemFourLetters(modifiedWord, status);
                }

                // if the root hasn't been found, check for patterns
                if (!status.rootFound && modifiedWord.length() > 2) {
                    modifiedWord = checkPatterns(modifiedWord, status);
                }

                // if the root STILL hasnt' been found
                if (!status.rootFound && !status.stopWordFound) {
                    // check for suffixes
                    modifiedWord = checkSuffixes(modifiedWord, status);
                }

                // if the root STILL hasn't been found
                if (!status.rootFound && !status.stopWordFound) {
                    // check for prefixes
                    modifiedWord = checkPrefixes(modifiedWord, status);
                }


                if (status.stopWordFound) {
                    return modifiedWord;
                }


                // if the root was found, return the modified word
                if (status.rootFound && !status.stopWordFound) {
                    return modifiedWord;
                }
            }
        }
        if (modifiedWord.length() > 3) {
            return modifiedWord;
        }
        return word;
    }

    private String checkPatterns(String word, Status status) {
        StringBuilder root = new StringBuilder();

        if (!word.isEmpty()) {
            if (word.charAt(0) == '\u0623' || word.charAt(0) == '\u0625' || word.charAt(0) == '\u0622') {
                root.append("j");
                root.setCharAt(0, '\u0627');
                root.append(word.substring(1));
                word = root.toString();
            }
        }



        int numberSameLetters;

        String modifiedWord;


        for (String pattern : triPatt) {

            root.setLength(0);

            if (pattern.length() == word.length()) {
                numberSameLetters = 0;

                for (int j = 0; j < word.length(); j++) {
                    if (pattern.charAt(j) == word.charAt(j) &&
                        pattern.charAt(j) != '\u0641' &&
                        pattern.charAt(j) != '\u0639' &&
                        pattern.charAt(j) != '\u0644') {
                        numberSameLetters++;
                    }
                }


                if (word.length() == 6 && word.charAt(3) == word.charAt(5) && numberSameLetters == 2) {
                    root.append(word.charAt(1));
                    root.append(word.charAt(2));
                    root.append(word.charAt(3));
                    modifiedWord = root.toString();
                    modifiedWord = stemThreeLetters(modifiedWord, status);
                    if (status.rootFound) {
                        return modifiedWord;
                    } else {
                        root.setLength(0);
                    }
                }

                if (word.length() - 3 <= numberSameLetters) {

                    for (int j = 0; j < word.length(); j++) {
                        if (pattern.charAt(j) == '\u0641' ||
                            pattern.charAt(j) == '\u0639' ||
                            pattern.charAt(j) == '\u0644') {
                            root.append(word.charAt(j));
                        }
                    }

                    modifiedWord = root.toString();
                    modifiedWord = stemThreeLetters(modifiedWord, status);

                    if (status.rootFound) {
                        word = modifiedWord;
                        return word;
                    }
                }
            }
        }
        return word;
    }
}
