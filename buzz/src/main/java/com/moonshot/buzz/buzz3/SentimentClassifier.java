package com.moonshot.buzz.buzz3;

import lombok.Value;

public interface SentimentClassifier {

    enum SentimentLabel {
        POSITIVE, NEGATIVE, NEUTRAL
    }

    @Value(staticConstructor = "of")
    class SentimentResult {
        public static final SentimentResult NEUTRAL_RESULT = new SentimentResult(SentimentLabel.NEUTRAL, 0d, 1d, 0d);

        SentimentLabel sentiment;
        double negativeScore;
        double neutralScore;
        double positiveScore;
    }

    SentimentLabel computeSentiment(String contents, String language);

    SentimentResult computeSentimentWithScores(String contents, String language);

}
