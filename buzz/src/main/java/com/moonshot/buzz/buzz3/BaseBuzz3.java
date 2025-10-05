package com.moonshot.buzz.buzz3;

import com.moonshot.buzz.buzz3.algorithms.AdditiveLinearAlgorithm;
import com.moonshot.buzz.buzz3.algorithms.DecisionTreeBoosterAlgorithm;
import com.moonshot.buzz.buzz3.algorithms.GenericBuzzAlgorithm;
import com.moonshot.buzz.buzz3.tokenizer.BuzzTokenizer;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jblas.DoubleMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Buzz3: performs classification across multiple languages
 *
 * @author aykut
 * @author Steve Ungerer
 */
public abstract class BaseBuzz3 {
    private static final Logger log = LoggerFactory.getLogger(BaseBuzz3.class);

    private static final String CLD_CHINESE = "zh";
    private static final String CLD_CHINESE_T = "zh-Hant";

    /**
     * Algorithms run by buzz 3
     */
    public enum Buzz3Algorithm {
        DecisionTreeBoosting, NaiveBayes, VowpalWabbit, LabLinear
    }

    private Map<String, Buzz3Language> languageCodeMap;
    private Map<Buzz3Language, BuzzTokenizer> tokenizerMap;
    private Map<Buzz3Language, List<GenericBuzzAlgorithm>> algorithmMap;

    /**
     * @return the list of languages supported by this version of Buzz3
     */
    protected Buzz3Language[] getSupportedLanguages() {
        return Buzz3Language.values(); // all languages
    }

    protected BaseBuzz3() {
        try {
            this.languageCodeMap = buildLanguageCodeMap();

            // construct tokenizer map
            this.tokenizerMap = buildTokenizerMap();

            // construct algorithm map
            this.algorithmMap = buildAlgorithmMap(tokenizerMap);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    protected Map<String, Buzz3Language> buildLanguageCodeMap() {
        Map<String, Buzz3Language> ret = new HashMap<>();
        for (Buzz3Language lang : getSupportedLanguages()) {
            // for chinese, support both chinese and chinese traditional
            if (CLD_CHINESE.equals(lang.getLanguageCode())) {
                ret.put(CLD_CHINESE_T, lang);
            }
            ret.put(lang.getLanguageCode(), lang);
        }
        return Collections.unmodifiableMap(ret);
    }

    protected Map<Buzz3Language, BuzzTokenizer> buildTokenizerMap() throws Exception {
        Map<Buzz3Language, BuzzTokenizer> ret = new HashMap<>();
        for (Buzz3Language lang : getSupportedLanguages()) {
            Object2IntMap<String> vocabulary = loadVocabulary(this.getClass().getResourceAsStream(lang.getVocabFileUri()));
            BuzzTokenizer tokenizer = lang.getTokenizerClass().newInstance();
            tokenizer.initialize(vocabulary);
            ret.put(lang, tokenizer);
        }
        return Collections.unmodifiableMap(ret);
    }

    protected Map<Buzz3Language, List<GenericBuzzAlgorithm>> buildAlgorithmMap(Map<Buzz3Language, BuzzTokenizer> tokenizerMap) throws Exception {
        Map<Buzz3Language, List<GenericBuzzAlgorithm>> ret = new HashMap<>();
        for (Buzz3Language lang : getSupportedLanguages()) {
            Object2IntMap<String> vocabulary = tokenizerMap.get(lang).getVocabulary();
            List<GenericBuzzAlgorithm> algorithms = new LinkedList<>();
            if (lang == Buzz3Language.Korean || lang == Buzz3Language.Indonesian || lang == Buzz3Language.Malaysian || lang == Buzz3Language.Swedish) {

                // Use only single Linear Model for now.
                // TODO train VW and Decision Tree
                algorithms.add(new AdditiveLinearAlgorithm(this.getClass().getResourceAsStream(lang.getLinearModelFileUri()), Buzz3Algorithm.LabLinear, vocabulary.size()));
                algorithms.add(new AdditiveLinearAlgorithm(this.getClass().getResourceAsStream(lang.getNaiveBayesModelFileUri()), Buzz3Algorithm.NaiveBayes, vocabulary.size()));
                algorithms.add(new AdditiveLinearAlgorithm(this.getClass().getResourceAsStream(lang.getLinearModelFileUri()), Buzz3Algorithm.LabLinear, vocabulary.size()));
                algorithms.add(new AdditiveLinearAlgorithm(this.getClass().getResourceAsStream(lang.getLinearModelFileUri()), Buzz3Algorithm.LabLinear, vocabulary.size()));
            } else {
                algorithms.add(new AdditiveLinearAlgorithm(this.getClass().getResourceAsStream(lang.getNaiveBayesModelFileUri()), Buzz3Algorithm.NaiveBayes, vocabulary.size()));
                algorithms.add(new AdditiveLinearAlgorithm(this.getClass().getResourceAsStream(lang.getLinearModelFileUri()), Buzz3Algorithm.LabLinear, vocabulary.size()));
                algorithms.add(new AdditiveLinearAlgorithm(this.getClass().getResourceAsStream(lang.getVWModelFileUri()), Buzz3Algorithm.VowpalWabbit, vocabulary.size()));
                algorithms.add(new DecisionTreeBoosterAlgorithm(
                    this.getClass().getResourceAsStream(lang.getBoosterModelNamesUri()),
                    this.getClass().getResourceAsStream(lang.getBoosterModelShypUri()),
                    Buzz3Algorithm.DecisionTreeBoosting)
                );
            }
            ret.put(lang, algorithms);
        }
        return Collections.unmodifiableMap(ret);
    }

    protected static Object2IntAVLTreeMap<String> loadVocabulary(InputStream vocabStream) throws IOException {
        Object2IntAVLTreeMap<String> vocabulary = new Object2IntAVLTreeMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(vocabStream, StandardCharsets.UTF_8))) {
            String vocab;
            int i = 0;
            while ((vocab = br.readLine()) != null) {
                vocabulary.put(vocab, i++);
            }
        }
        return vocabulary;
    }

    protected static DoubleMatrix combineModels(Buzz3Language language, Map<Buzz3Algorithm, DoubleMatrix> modelProbabilities) {
        DoubleMatrix combinedProbabilities = null;
        for (Map.Entry<Buzz3Algorithm, DoubleMatrix> entry : modelProbabilities.entrySet()) {
            if (combinedProbabilities == null) {
                combinedProbabilities = entry.getValue().muli(language.getWeights().get(entry.getKey()));
            } else {
                combinedProbabilities.addi(entry.getValue().muli(language.getWeights().get(entry.getKey())));
            }
        }
        if (combinedProbabilities == null) {
            throw new RuntimeException("Null combinedProbablities - no languages?");
        }
        combinedProbabilities.diviColumnVector(combinedProbabilities.rowSums());
        return combinedProbabilities;
    }

    protected static int[] convertProbabilitiesToAssignments(DoubleMatrix probabilities) {
        int[] assignments = probabilities.rowArgmaxs();
        DoubleMatrix maxValues = probabilities.rowMaxs();
        for (int i = 0; i < maxValues.length; i++) {
            if (maxValues.get(i) < 0.40) {
                assignments[i] = 1;
            }
        }
        return assignments;
    }

    public DoubleMatrix getModelProbabilities(Buzz3Language language, List<IntList> documentTermIndices) {
        return getModelProbabilities(language, algorithmMap.get(language), documentTermIndices);
    }

    protected static DoubleMatrix getModelProbabilities(Buzz3Language language, List<GenericBuzzAlgorithm> algorithms, List<IntList> documentTermIndices) {
        Map<Buzz3Algorithm, DoubleMatrix> modelProbabilities = new HashMap<>();
        for (GenericBuzzAlgorithm algorithm : algorithms) {
            log.trace("running algo: {}", algorithm.getAlgorithm().name());
            modelProbabilities.put(algorithm.getAlgorithm(), algorithm.calculateListOfDocumentProbabilities(documentTermIndices));
        }
        return combineModels(language, modelProbabilities);
    }

    public Buzz3Language getLanguage(String code) {
        return languageCodeMap.get(code);
    }

    public Map<String, Buzz3Language> getLanguageCodeMap() {
        return languageCodeMap;
    }

    public Map<Buzz3Language, BuzzTokenizer> getTokenizerMap() {
        return tokenizerMap;
    }

    public Map<Buzz3Language, List<GenericBuzzAlgorithm>> getAlgorithmMap() {
        return algorithmMap;
    }
}
