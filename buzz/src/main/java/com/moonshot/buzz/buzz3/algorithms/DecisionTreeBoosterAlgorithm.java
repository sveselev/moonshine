package com.moonshot.buzz.buzz3.algorithms;

import com.moonshot.buzz.buzz3.BaseBuzz3.Buzz3Algorithm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: aykut Date: 11/5/13 Time: 8:43 AM
 */

public class DecisionTreeBoosterAlgorithm extends GenericBuzzAlgorithm {
    private static final Logger logger = LoggerFactory.getLogger(DecisionTreeBoosterAlgorithm.class);

    private static final Pattern commaPattern = Pattern.compile("\\s*,\\s*");
    private static final Pattern pipePattern = Pattern.compile("(\\s+|\\s*\\.$)");

    private final Model model;
    private final Buzz3Algorithm algorithm;

    public DecisionTreeBoosterAlgorithm(InputStream names, InputStream shyp, Buzz3Algorithm algorithm) throws IOException {
        this.algorithm = algorithm;
        model = new Model(names, shyp, Model.TYPE_NGRAM, 1);
    }

    @Override
    public Buzz3Algorithm getAlgorithm() {
        return algorithm;
    }

    protected double[] decode(String line) {
        Example example = model.readExample(line);
        double[] result = new double[model.labels.length];
        for (int i = 0; i < model.numIterations; i++) {
            Classifier classifier = model.classifiers.get(i);
            classifier.classify(example.features.get(classifier.column), result);
        }
        for (int i = 0; i < result.length; i++) {
            result[i] /= model.numIterations;
        }
        return result;
    }

    protected DoubleMatrix transformScoresToProbability(double[] scores) {
        DoubleMatrix p = new DoubleMatrix(scores);
        p.subi(p.max());
        MatrixFunctions.expi(p.muli(2 * model.classifiers.size()));
        p.divi(p.norm1());
        return p;
    }

    protected double[] calculateDocumentScores(IntList indices) {
        final IntSet indicesSet = new IntOpenHashSet(indices);
        double[] result = new double[model.labels.length];
        for (int i = 0; i < model.numIterations; i++) {
            TextClassifier classifier = (TextClassifier) model.classifiers.get(i);
            if (indicesSet.contains(classifier.tokenInt)) {
                for (int j = 0; j < result.length; j++) {
                    result[j] += classifier.c1[j];
                }
            } else {
                for (int j = 0; j < result.length; j++) {
                    result[j] += classifier.c0[j];
                }
            }
        }
        for (int i = 0; i < result.length; i++) {
            result[i] /= model.numIterations;
        }

        return result;
    }

    protected DoubleMatrix calculateDocumentProbability(IntList indices) {
        return transformScoresToProbability(calculateDocumentScores(indices));
    }

    @Override
    public DoubleMatrix calculateListOfDocumentProbabilities(List<IntList> indices) {
        DoubleMatrix probabilities = DoubleMatrix.zeros(indices.size(), numberOfClasses);
        for (int i = 0; i < indices.size(); i++) {
            probabilities.putRow(i, transformScoresToProbability(calculateDocumentScores(indices.get(i))));
        }
        return probabilities;
    }

    public class Feature {
    }

    private class ContinuousFeature extends Feature {
        double value;

        public ContinuousFeature(String content) {
            value = Double.parseDouble(content);
        }

        public double getValue() {
            return value;
        }
    }

    private class TextFeature extends Feature {
        protected Set<String> valueSet;

        protected TextFeature() {
            valueSet = new HashSet<>();
        }

        protected TextFeature(String content) {
            valueSet = new HashSet<>(1);
            valueSet.add(content);
        }

        protected boolean contains(String s) {
            return valueSet.contains(s);
        }
    }

    private class NGramFeature extends TextFeature {

        protected NGramFeature(String content, int length) {
            valueSet = new HashSet<>();

            String[] tokens = content.split(" ");
            for (int i = 0; i < tokens.length; i++) {
                StringBuilder ngram = new StringBuilder(tokens[i]);
                logger.trace("{} {} {}", tokens[i], i, length);
                for (int j = 0; j < length && i + j < tokens.length; j++) {
                    ngram.append("_");
                    ngram.append(tokens[i + j]);
                    String s = ngram.toString();
                    valueSet.add(s);
                }
            }
        }

    }

    private class FGramFeature extends TextFeature {

        protected FGramFeature(String content, int length) {
            valueSet = new HashSet<>();

            String[] tokens = content.split(" ");
            for (int i = 0; i < tokens.length - length; i++) {
                StringBuilder ngram = new StringBuilder(tokens[i]);
                for (int j = 0; j < length; j++) {
                    ngram.append("_");
                    ngram.append(tokens[i + j]);
                }
                String s = ngram.toString();
                valueSet.add(s);
            }
        }

    }

    private class SGramFeature extends TextFeature {

        protected SGramFeature(String content, int length) {
            valueSet = new HashSet<>();

            String[] tokens = content.split(" ");
            for (int i = 0; i < tokens.length - length; i++) {
                String s = tokens[i] + "_" + tokens[i + length];
                valueSet.add(s);
            }
        }

    }

    private class UndefinedFeature extends Feature {
    }

    public class Classifier {
        protected double alpha;
        protected int column;
        protected double[] c0;
        protected double[] c1;
        protected double[] c2;

        /**
         * @param feature
         * @param result
         */
        public void classify(Feature feature, double[] result) {}

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(alpha + " " + column);
            builder.append("\n");
            if (c0 != null) {
                builder.append("  c0 ");
                for (double v : c0) {
                    builder.append(v + " ");
                }
                builder.append("\n");
            }
            if (c1 != null) {
                builder.append("  c1 ");
                for (double v : c1) {
                    builder.append(v + " ");
                }
                builder.append("\n");
            }
            if (c2 != null) {
                builder.append("  c2 ");
                for (double v : c2) {
                    builder.append(v + " ");
                }
                builder.append("\n");
            }
            return builder.toString();
        }
    }

    public class ThresholdClassifier extends Classifier {
        double threshold;

        @Override
        public String toString() {
            String output = super.toString();
            return "threshold:" + threshold + ":" + output;
        }

        public ThresholdClassifier(double threshold, int column, double alpha, double[] c0, double[] c1, double[] c2) {
            this.threshold = threshold;
            this.column = column;
            this.alpha = alpha;
            this.c0 = c0;
            this.c1 = c1;
            this.c2 = c2;
        }

        @Override
        public void classify(Feature feature, double[] result) {
            if (UndefinedFeature.class.isInstance(feature)) {
                for (int i = 0; i < result.length; i++) {
                    result[i] += c0[i];
                }
            } else {
                double value = ((ContinuousFeature) feature).getValue();
                if (value < threshold) {
                    for (int i = 0; i < result.length; i++) {
                        result[i] += c1[i];
                    }
                } else {
                    for (int i = 0; i < result.length; i++) {
                        result[i] += c2[i];
                    }
                }
            }
        }
    }

    public class TextClassifier extends Classifier {
        String type = "text";
        String token;
        public int tokenInt;

        @Override
        public String toString() {
            String output = super.toString();
            return "text:" + token + ":" + output;
        }

        public TextClassifier(String token, int column, double alpha, double[] c0, double[] c1) {
            this.token = token;
            this.tokenInt = Integer.parseInt(token);
            this.column = column;
            this.alpha = alpha;
            this.c0 = c0;
            this.c1 = c1;
        }

        @Override
        public void classify(Feature feature, double[] result) {
            if (UndefinedFeature.class.isInstance(feature)) {
                for (int i = 0; i < result.length; i++) {
                    result[i] += c0[i];
                }
            } else {
                TextFeature textFeature = (TextFeature) feature;
                if (textFeature.contains(token)) {
                    for (int i = 0; i < result.length; i++) {
                        result[i] += c1[i];
                    }
                } else {
                    for (int i = 0; i < result.length; i++) {
                        result[i] += c0[i];
                    }
                }
            }
        }
    }

    public class Model {
        public ImmutableList<Classifier> classifiers;
        public ImmutableList<String> types;
        public ImmutableMap<String, Integer> mapping;

        static final int TYPE_CONTINUOUS = 0;
        static final int TYPE_TEXT = 1;
        static final int TYPE_IGNORE = 1;

        public static final int TYPE_SGRAM = 2;
        public static final int TYPE_FGRAM = 1;
        public static final int TYPE_NGRAM = 0;

        public String[] labels;
        int[] typesAsInt;
        public int numIterations;
        int ngramLength = 0;
        int ngramType = 0;

        public Model(InputStream names, InputStream shyp, int ngramLength, int ngramType) throws IOException {
            this.ngramLength = ngramLength;
            this.ngramType = ngramType;
            loadNames(names);
            loadShyp(shyp);
        }

        public void loadNames(InputStream stream) throws IOException {
            ImmutableMap.Builder<String, Integer> mappingBuilder = ImmutableMap.<String, Integer>builder();
            ImmutableList.Builder<String> typesBuilder = ImmutableList.builder();
            boolean firstLine = true;
            BufferedReader input = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String line;
            int typesCount = 0;
            while (null != (line = input.readLine())) {
                line = line.trim();
                if (line.matches("^(\\s*|\\s*\\|.*)$")) {
                    continue;
                }
                if (firstLine) {
                    labels = line.split("(^\\s+|\\s*,\\s*|\\s*\\.?$)");
                    firstLine = false;
                } else {
                    String[] result = line.split("(^\\s+|\\s*:\\s*|\\s*\\.?$)");
                    if (result.length == 2) {
                        mappingBuilder.put(result[0], typesCount);
                        typesBuilder.add(result[1]);
                        typesCount++;
                    }
                }
            }
            types = typesBuilder.build();
            typesAsInt = new int[types.size()];
            for (int i = 0; i < types.size(); i++) {
                if ("ignore".equals(types.get(i))) {
                    typesAsInt[i] = TYPE_IGNORE;
                } else if ("continuous".equals(types.get(i))) {
                    typesAsInt[i] = TYPE_CONTINUOUS;
                } else {
                    typesAsInt[i] = TYPE_TEXT;
                }
            }
            mapping = mappingBuilder.build();
        }

        public void loadShyp(InputStream stream) throws IOException {
            boolean seenIterations = false;
            BufferedReader input = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String line;
            double alpha = Double.NaN;
            String name;
            String token = null;
            double threshold;
            double[] c0 = null;
            double[] c1 = null;
            double[] c2 = null;
            int column = -1;
            ImmutableList.Builder<Classifier> classifierBuilder = ImmutableList.builder();
            while (null != (line = input.readLine())) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (!seenIterations) {
                    numIterations = Integer.parseInt(line);
                    seenIterations = true;
                } else {
                    Pattern textPattern = Pattern.compile("^\\s*(\\S+)\\s+Text:SGRAM:([^:]+):(.*?) *$");
                    Matcher textMatcher = textPattern.matcher(line);
                    Pattern thresholdPattern = Pattern.compile("^\\s*(\\S+)\\s+Text:THRESHOLD:([^:]+):");
                    Matcher thresholdMatcher = thresholdPattern.matcher(line);
                    if (textMatcher.matches()) {
                        alpha = Double.parseDouble(textMatcher.group(1));
                        name = textMatcher.group(2);
                        token = textMatcher.group(3);
                        Integer columnInteger = mapping.get(name);
                        if (columnInteger != null) {
                            column = columnInteger;
                            String type = types.get(column);
                            if (type.contains(",")) {
                                String[] values = type.split("\\s*,\\s*");
                                int tokenId = Integer.parseInt(token);
                                if (tokenId < 0 || tokenId >= values.length) {
                                    throw new RuntimeException("ERROR: value not found \"" + tokenId + "\" in names file");
                                }
                                token = values[Integer.parseInt(token)];
                            }
                        } else {
                            throw new RuntimeException("ERROR: name not found \"" + name + "\" in names file");
                        }
                        c0 = null;
                        c1 = null;
                        c2 = null;
                    } else if (thresholdMatcher.matches()) {
                        alpha = Double.parseDouble(thresholdMatcher.group(1));
                        name = thresholdMatcher.group(2);
                        Integer columnInteger = mapping.get(name);
                        if (columnInteger != null) {
                            column = columnInteger;
                            String type = types.get(column);
                            if (!"continuous".equals(type)) {
                                throw new RuntimeException("ERROR: unsupported type \"" + type + "\" for threshold");
                            }
                        } else {
                            throw new RuntimeException("ERROR: name not found \"" + name + "\" in names file");
                        }
                        c0 = null;
                        c1 = null;
                        c2 = null;
                        token = null;
                    } else {
                        String[] values = line.split(" ");
                        if (values.length == labels.length) {
                            if (c0 == null) {
                                c0 = new double[labels.length];
                                for (int i = 0; i < values.length; i++) {
                                    c0[i] = Double.parseDouble(values[i]);
                                }
                            } else if (c1 == null) {
                                c1 = new double[labels.length];
                                for (int i = 0; i < values.length; i++) {
                                    c1[i] = Double.parseDouble(values[i]);
                                }
                                if (token != null) {
                                    TextClassifier classifier = new TextClassifier(token, column, alpha, c0, c1);
                                    classifierBuilder.add(classifier);
                                }
                            } else {
                                c2 = new double[labels.length];
                                for (int i = 0; i < values.length; i++) {
                                    c2[i] = Double.parseDouble(values[i]);
                                }
                            }
                        } else if (values.length == 1) {
                            threshold = Double.parseDouble(line);
                            ThresholdClassifier classifier = new ThresholdClassifier(threshold, column, alpha, c0, c1, c2);
                            classifierBuilder.add(classifier);
                        } else {
                            throw new RuntimeException("ERROR: unexpected line \"" + line + "\" in shyp file");
                        }
                    }
                }
            }
            classifiers = classifierBuilder.build();
        }

        public Example readExample(String line) {
            Example example = new Example();
            String[] fields = commaPattern.split(line);
            if (fields.length == typesAsInt.length || fields.length == typesAsInt.length + 1) {
                for (int i = 0; i < typesAsInt.length; i++) {
                    if ("?".equals(fields[i])) {
                        example.features.add(new UndefinedFeature());
                    } else {
                        if (typesAsInt[i] == TYPE_TEXT) {
                            if (ngramType == TYPE_NGRAM) {
                                example.features.add(new NGramFeature(fields[i], ngramLength));
                            } else if (ngramType == TYPE_FGRAM) {
                                example.features.add(new FGramFeature(fields[i], ngramLength));
                            } else if (ngramType == TYPE_SGRAM) {
                                example.features.add(new SGramFeature(fields[i], ngramLength));
                            }
                        } else if (typesAsInt[i] == TYPE_CONTINUOUS) {
                            example.features.add(new ContinuousFeature(fields[i]));
                        }
                    }
                }
                if (fields.length == typesAsInt.length + 1) {
                    String[] labels = pipePattern.split(fields[fields.length - 1]);
                    for (int i = 0; i < labels.length; i++) {
                        example.labels.add(labels[i]);
                    }
                }
            }
            return example;
        }
    }

    public class Example {
        public Vector<Feature> features = new Vector<>();
        public Vector<String> labels = new Vector<>();
    }
}
