package com.moonshot.buzz.buzz3.algorithms;

import com.moonshot.buzz.buzz3.BaseBuzz3.Buzz3Algorithm;

import it.unimi.dsi.fastutil.ints.IntList;
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * User: aykut Date: 11/5/13 Time: 8:43 AM
 */

public class AdditiveLinearAlgorithm extends GenericBuzzAlgorithm {
    private static final Logger log = LoggerFactory.getLogger(AdditiveLinearAlgorithm.class);

    private final DoubleMatrix model;
    private final Buzz3Algorithm algorithm;

    public AdditiveLinearAlgorithm(InputStream modelStream, Buzz3Algorithm algorithm, int modelSize) throws IOException {
        this.algorithm = algorithm;
        this.model = DoubleMatrix.zeros(modelSize, numberOfClasses);
        loadModels(modelStream);
    }

    @Override
    public Buzz3Algorithm getAlgorithm() {
        return algorithm;
    }

    protected void loadModels(InputStream modelStream) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(modelStream))) {
            String line;
            int rowNumber = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                for (int i = 1; i < numberOfClasses + 1; i++) {
                    model.put(rowNumber, i - 1, Double.parseDouble(values[i]));
                }
                rowNumber++;
            }
        }
    }

    protected DoubleMatrix transformScoresToProbability(double[] scores) {
        DoubleMatrix p = new DoubleMatrix(scores);
        p.subi(p.max());
        MatrixFunctions.expi(p);
        p.divi(p.norm1());
        return p;
    }

    protected double[] calculateDocumentScores(IntList indices) {
        double[] scores = new double[numberOfClasses];
        for (Integer index : indices) {
            for (int j = 0; j < numberOfClasses; j++) {
                scores[j] += model.get(index, j);
            }
        }

        return scores;
    }


    @Override
    public DoubleMatrix calculateListOfDocumentProbabilities(List<IntList> indices) {
        log.trace("Probs:" + indices.size());
        log.trace("modelsize:" + model.rows);
        DoubleMatrix probabilities = DoubleMatrix.zeros(indices.size(), numberOfClasses);
        for (int i = 0; i < indices.size(); i++) {
            probabilities.putRow(i, transformScoresToProbability(calculateDocumentScores(indices.get(i))));
        }

        return probabilities;
    }
}
