package com.moonshot.buzz.buzz3;

import com.moonshot.buzz.buzz3.tokenizer.BuzzTokenizer;

import it.unimi.dsi.fastutil.ints.IntList;
import org.apache.commons.lang3.StringUtils;
import org.jblas.DoubleMatrix;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Computing sentiment scores directly from document text
 *
 * @author jtanza
 */
public class SimpleBuzz extends BaseBuzz3 implements SentimentClassifier {

    public static final SimpleBuzz INSTANCE = new SimpleBuzz();

    private SimpleBuzz() {
        // private
    }

    @Override
    public SentimentLabel computeSentiment(String contents, String language) {
        return computeSentiment(contents, language, null);
    }

    /**
     * Specific tokenizer for testing
     */
    public SentimentLabel computeSentiment(String contents, String language, Function<String, List<String>> tokenizer) {
        return computeSentimentWithScores(contents, language, tokenizer).getSentiment();
    }

    @Override
    public SentimentResult computeSentimentWithScores(String contents, String language) {
        return computeSentimentWithScores(contents, language, null);
    }

    /**
     * Specific tokenizer for testing
     */
    public SentimentResult computeSentimentWithScores(String contents, String language, Function<String, List<String>> tokenizer) {
        if (StringUtils.isBlank(contents)) {
            return SentimentResult.NEUTRAL_RESULT;
        }

        Optional<Buzz3Language> b3lang = getLanguage(contents, language);
        if (!b3lang.isPresent()) {
            // unsupported lang doc
            return SentimentResult.NEUTRAL_RESULT;
        }

        final DoubleMatrix probs = computeModelProbabilities(contents, b3lang.get(), tokenizer);
        final int solidAssignment = BaseBuzz3.convertProbabilitiesToAssignments(probs)[0];
        // 0 = negative, 1 = neutral, 2 = positive

        return SentimentResult.of(categoryFromAssignment(solidAssignment), probs.get(0), probs.get(1), probs.get(2));
    }

    /**
     * @param contents
     * @return
     */
    protected DoubleMatrix computeModelProbabilities(String contents, Buzz3Language language, Function<String, List<String>> tokenizer) {
        return BaseBuzz3.combineModels(language, getModelProbabilities(contents, language, tokenizer));
    }

    private Map<Buzz3Algorithm, DoubleMatrix> getModelProbabilities(String contents, Buzz3Language language, Function<String, List<String>> tokenizer) {
        Map<Buzz3Algorithm, DoubleMatrix> modelProbabilities = new HashMap<>();
        List<IntList> termIndices = Collections.singletonList(getTermIndices(contents, language, tokenizer));
        getAlgorithmMap().get(language).forEach(algorithm -> modelProbabilities.put(
            algorithm.getAlgorithm(), algorithm.calculateListOfDocumentProbabilities(termIndices))
        );
        return modelProbabilities;
    }

    public IntList getTermIndices(String contents, Buzz3Language language, Function<String, List<String>> tokenizer) {
        BuzzTokenizer bt = getTokenizerMap().get(language);
        return tokenizer == null ? bt.tokenIndices(contents) : bt.tokenIndices(contents, tokenizer);
    }

    private Optional<Buzz3Language> getLanguage(String contents, String language) {
        if (StringUtils.isNotBlank(language)) {
            Buzz3Language b3lang = getLanguage(language);
            return b3lang == null ? Optional.empty() : Optional.of(b3lang);
        } else {
            return Optional.empty();
        }
    }

    private static SentimentLabel categoryFromAssignment(int assignment) {
        switch (assignment) {
            case 0: return SentimentLabel.NEGATIVE;
            case 1: return SentimentLabel.NEUTRAL;
            case 2: return SentimentLabel.POSITIVE;
            default: throw new UnsupportedOperationException("Unsupported assignment: " + assignment);
        }
    }
}
