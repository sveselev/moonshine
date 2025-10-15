package com.moonshot.buzz.hf;

import ai.djl.Application;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import com.google.common.collect.ImmutableMap;
import com.moonshot.buzz.emotion.BuzzEmotionClassifier;
import com.moonshot.buzz.emotion.EmotionClassifier;
import com.moonshot.buzz.emotion.EmotionLabel;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class HFEmotionClassifier implements EmotionClassifier {
    private static final ImmutableMap<@NonNull String, @NonNull String> labelMap = ImmutableMap.<String, String>builder()
            .put(EmotionLabel.JOY.name().toLowerCase(), "joy")
            .put(EmotionLabel.SADNESS.name().toLowerCase(), "sadness")
            .put(EmotionLabel.NEUTRAL.name().toLowerCase(), "neutral")
            .put(EmotionLabel.DISGUST.name().toLowerCase(), "disgust")
            .put(EmotionLabel.ANGER.name().toLowerCase(), "anger")
            .put(EmotionLabel.SURPRISE.name().toLowerCase(), "surprise")
            .put(EmotionLabel.FEAR.name().toLowerCase(), "fear").build();

    private final ZooModel<String, Classifications> model;

    public HFEmotionClassifier(String hfModelId) throws Exception {

        var criteria = Criteria.<String, Classifications>builder()
                .optApplication(Application.NLP.TEXT_CLASSIFICATION)
                .setTypes(String.class, Classifications.class)
                //.optModelName(hfModelId)
                .optEngine("PyTorch")
                .optModelUrls("djl://ai.djl.huggingface.pytorch/" + hfModelId)
                .optArgument("applySoftmax", true)
                .build();

        this.model = ModelZoo.loadModel(criteria);
    }

    @Override
    public Optional<EmotionLabel> classify(String text, String language) {
        BuzzEmotionClassifier.SupportedLanguage lang = BuzzEmotionClassifier.SupportedLanguage.ofLanguageCode(language)
                .orElse(BuzzEmotionClassifier.SupportedLanguage.English);
        
        Map<EmotionLabel, Float> scores = score(text, lang);
        return scores.entrySet().stream()
                .filter(s -> s.getValue() > 0.3)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    @Override
    public Map<EmotionLabel, Float> score(String text, BuzzEmotionClassifier.SupportedLanguage lang) {
        try (Predictor<String, Classifications> p = model.newPredictor()) {
            Classifications out = p.predict(text);

            // Start with zeros for your canonical keys
            Map<String, Double> agg = new LinkedHashMap<>();
            for (String k : labelMap.values()) agg.put(k, 0.0);

            // Aggregate model probs into your canonical keys
            for (Classifications.Classification c : out.items()) {
                String modelLbl = c.getClassName();
                double prob = c.getProbability();
                String canon = labelMap.getOrDefault(modelLbl, "neutral"); // default fold-in
                agg.put(canon, agg.get(canon) + prob);
            }

            // Normalize to sum = 1
            double sum = agg.values().stream().mapToDouble(Double::doubleValue).sum();

            if (sum > 0) for (String k : labelMap.values()) agg.put(k, agg.get(k) / sum);
            Map<EmotionLabel, Float> probabilityMap = agg.entrySet().stream()
                    .collect(Collectors.toMap(e -> EmotionLabel.valueOf(e.getKey().toUpperCase()), e-> e.getValue().floatValue()));

            return probabilityMap;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<EmotionLabel, Float> score(String text, BuzzEmotionClassifier.SupportedLanguage lang, boolean sorted) {
        Map<EmotionLabel, Float> scr = score(text, lang);
        if (sorted) {
            return scr.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue,
                            (v1, v2) -> v1, LinkedHashMap::new));
        } else {
            return scr;
        }
    }
}
