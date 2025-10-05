package com.moonshot.buzz.emotion;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

/**
 * csullivan - progenitor, jamdor - implementatorer
 *
 * Runs emotional analysis. Unlike the other post-level analysis, this runs on already existing PostDocuments, since it is run after Buzz during
 * analysis.
 *
 */
public class BuzzEmotionClassifier implements EmotionClassifier {

    public static final BuzzEmotionClassifier INSTANCE = new BuzzEmotionClassifier();

    private static final String MODEL_PATH = "/com/moonshot/buzz/emotion/clf_weights.txt";
    private static final String IDF_FILE_PATH = "/com/moonshot/buzz/emotion/idfs.txt";

    // ImmutableMap guarantees deterministic ordering
    private static final ImmutableMap<String, Double> labeledWeights = ImmutableMap.<String, Double>builder()
        .put(EmotionLabel.JOY.name(), -1.33189862779)
        .put(EmotionLabel.SADNESS.name(), -2.03161022334)
        .put(EmotionLabel.NEUTRAL.name(), -3.09896323712)
        .put(EmotionLabel.DISGUST.name(), -1.95629889028)
        .put(EmotionLabel.ANGER.name(), -1.87811753242)
        .put(EmotionLabel.SURPRISE.name(), -2.73447450525)
        .put(EmotionLabel.FEAR.name(), -2.24383445594).build();

    private static final DoubleMatrix intercept;

    static {
        intercept = DoubleMatrix.zeros(labeledWeights.size());
        int index = 0;
        for (Double value : labeledWeights.values()) {
            intercept.put(index, value);
            index++;
        }
    }
    private final Map<String, Double> idfWeights;
    private final Map<String, DoubleMatrix> featureCoefficients;
    private final FeatureExtractor extractor;

    private BuzzEmotionClassifier() {
        try {
            this.idfWeights = loadFeatureMap();
            this.featureCoefficients = loadModel();
            this.extractor = new FeatureExtractor(featureCoefficients.keySet());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Get a single emotion for some text
     * @param language of text
     */
    @Override
    public Optional<EmotionLabel> classify(String text, String language) {
            Optional<SupportedLanguage> lang = SupportedLanguage.ofLanguageCode(language);
        if (lang.isEmpty()) {
            return Optional.empty();
        }
        final Map<EmotionLabel, Float> scores = score(text, lang.get());
        Optional<Map.Entry<EmotionLabel, Float>> cat = scores.entrySet().stream()
            .filter(s -> s.getValue() > .3)
            .max(Map.Entry.comparingByValue());

        return cat.map(Entry::getKey);
    }

    /**
     * Get all emotion category scores for some text
     * @param lang currently unused - for future extension to non-english
     */
    @Override
    public Map<EmotionLabel, Float> score(String text, SupportedLanguage lang) {
        return score(text, extractor::extractFeatures, lang);
    }

    // extension for custom extractor
    protected Map<EmotionLabel, Float> score(String text, Function<String,List<String>> extractor, SupportedLanguage lang) {
        if (lang == null || StringUtils.isBlank(text)) {
            return Collections.emptyMap();
        }

        // Get Features
        List<String> features = extractor.apply(text);
        // Count # of occurrences for each feature
        Map<String, Double> tf = new HashMap<>();

        for (String feat : features) {
            tf.put(feat, tf.getOrDefault(feat, 0.0) + 1.0);
        }

        // Create empty vector for features
        DoubleMatrix featVector = DoubleMatrix.zeros(tf.size());

        int i = 0;

        // set feature values to term-frequency * inverse-document-frequency
        for (Map.Entry<String, Double> entry : tf.entrySet()) {
            tf.put(entry.getKey(), entry.getValue() * idfWeights.get(entry.getKey()));
            featVector.put(i, entry.getValue());
            i++;
        }

        // Normalize feature vector by L2 norm
        double norm = featVector.norm2();
        featVector.divi(norm);

        // create empty n features * m classes matrix
        DoubleMatrix matrix = DoubleMatrix.zeros(features.size(), labeledWeights.size());

        // multiply each feature value by corresponding model coefficients
        i = 0;
        for (Map.Entry<String, Double> entry : tf.entrySet()) {
            tf.put(entry.getKey(), featVector.get(i)); // for easier debugging purposes.
            matrix.putRow(i, featureCoefficients.get(entry.getKey()).transpose().mul(featVector.get(i)));
            i++;
        }

        // sum feature scores to get label scores
        DoubleMatrix scores = matrix.columnSums();

        // add intercept vector to regularize (~prior probability)
        scores = scores.add(intercept);

        // Apply liblinear style one-versus-rest logistic function
        scores.muli(-1.);
        MatrixFunctions.expi(scores);
        scores.addi(1.);
        MatrixFunctions.powi(scores, -1.);

        // Normalize to create probabilities between 0-1
        DoubleMatrix probabilities = scores.div(scores.sum());

        // Create empty label->prob map
        Map<EmotionLabel, Float> probabilityMap = new HashMap<>();

        // store label probabilities in hashmap
        final List<String> labels = labeledWeights.keySet().asList();
        for (int l = 0; l < labels.size(); l++) {
            probabilityMap.put(EmotionLabel.valueOf(labels.get(l)), (float) probabilities.get(l));
        }
        return probabilityMap;
    }

    private Map<String, Double> loadFeatureMap() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(IDF_FILE_PATH), StandardCharsets.UTF_8));

        Map<String, Double> featureMap = new HashMap<>();

        reader.lines().forEach(l -> {
            String[] columns = l.split("\t");
            featureMap.put(columns[0], Double.parseDouble(columns[1]));
        });

        return Collections.unmodifiableMap(featureMap);
    }

    private Map<String, DoubleMatrix> loadModel() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(MODEL_PATH), StandardCharsets.UTF_8));
        Map<String, DoubleMatrix> model = new HashMap<>();

        // skip header
        reader.readLine();

        reader.lines().forEach(l -> {
            String[] columns = l.split("\t");
            String featureName = columns[0];
            DoubleMatrix weights = DoubleMatrix.zeros(columns.length - 1);
            for (int i = 1; i < columns.length; i++) {
                double weight = Double.parseDouble(columns[i]);
                weights.put(i - 1, weight);
            }
            model.put(featureName, weights);
        });

        return Collections.unmodifiableMap(model);
    }

    public enum SupportedLanguage {
        English("en"); // english only to start

        private static final Map<String, SupportedLanguage> lookup = new HashMap<>();
        static {
            for (SupportedLanguage language : SupportedLanguage.values()) {
                lookup.put(language.languageCode, language);
            }
        }

        private final String languageCode;

        SupportedLanguage(String languageCode) {
            this.languageCode = languageCode;
        }

        public static Optional<SupportedLanguage> ofLanguageCode(String code) {
            return Optional.ofNullable(code)
                .filter(StringUtils::isNotBlank)
                .map(lookup::get);
        }
    }
}
