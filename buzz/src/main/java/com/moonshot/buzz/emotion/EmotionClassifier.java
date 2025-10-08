package com.moonshot.buzz.emotion;

import com.moonshot.buzz.emotion.BuzzEmotionClassifier.SupportedLanguage;

import java.util.Map;
import java.util.Optional;


public interface EmotionClassifier {

    /**
     * Get a single emotion for some text
     * @param language of text
     */
    Optional<EmotionLabel> classify(String text, String language);

    /**
     * Get all emotion category scores for some text
     * @param lang currently unused - for future extension to non-english
     */
    Map<EmotionLabel, Float> score(String text, SupportedLanguage lang);

    /**
     * Get all emotion category scores for some text sorted from high to low
     * @param lang currently unused - for future extension to non-english
     */
    Map<EmotionLabel, Float> score(String text, SupportedLanguage lang, boolean sorted);
}
