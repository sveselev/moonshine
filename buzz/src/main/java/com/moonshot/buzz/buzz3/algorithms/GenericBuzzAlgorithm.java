package com.moonshot.buzz.buzz3.algorithms;

import com.moonshot.buzz.buzz3.BaseBuzz3.Buzz3Algorithm;

import it.unimi.dsi.fastutil.ints.IntList;
import org.jblas.DoubleMatrix;

import java.util.List;

/**
 * User: aykut Date: 11/5/13 Time: 8:43 AM
 */
public abstract class GenericBuzzAlgorithm {
    protected final int numberOfClasses = 3;
    protected static String modelFile;

    public abstract Buzz3Algorithm getAlgorithm();

    public abstract DoubleMatrix calculateListOfDocumentProbabilities(List<IntList> indices);

    // @Override
    // public String toString() {
    // return getAlgorithm().name() + " " + modelFile;
    // }
}
