package com.moonshot.buzz;

import com.moonshot.buzz.buzz3.SentimentClassifier;
import com.moonshot.buzz.buzz3.SentimentClassifier.SentimentLabel;
import com.moonshot.buzz.buzz3.SentimentClassifier.SentimentResult;
import com.moonshot.buzz.buzz3.SimpleBuzz;
import com.moonshot.buzz.emotion.BuzzEmotionClassifier;
import com.moonshot.buzz.emotion.BuzzEmotionClassifier.SupportedLanguage;
import com.moonshot.buzz.emotion.EmotionClassifier;
import com.moonshot.buzz.emotion.EmotionLabel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/buzz")
public class BuzzController {

    private final String DEFAULT_TXT = "Wish you were here";
    private final String DEFAULT_LNG = "en";

    private final EmotionClassifier emotionClassifier;
    private final SentimentClassifier sentimentClassifier;

    public BuzzController(EmotionClassifier emotionClassifier, SentimentClassifier sentimentClassifier) {
        this.emotionClassifier = emotionClassifier;
        this.sentimentClassifier = sentimentClassifier;
    }

    @GetMapping(path = "/sentiment")
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<?> getSentiment(@RequestParam(defaultValue = DEFAULT_TXT) String text,
                                          @RequestParam(defaultValue = DEFAULT_LNG) String lang) {
        SentimentLabel snt = sentimentClassifier.computeSentiment(text, lang);
        return ResponseEntity.ok(snt.name());
    }

    @GetMapping(path = "/emotion")
    public ResponseEntity<?> getEmotion(@RequestParam(defaultValue = DEFAULT_TXT) String text) {
        Optional<EmotionLabel> emo = emotionClassifier.classify(text, DEFAULT_LNG);
        return ResponseEntity.ok(emo.map(Enum::name).orElse("NO EMOTION"));
    }

    @GetMapping(path = "/emotion/score")
    public Map<String, Map<EmotionLabel, Float>> getScore(@RequestParam(defaultValue = DEFAULT_TXT) String text) {
        Map<String, Map<EmotionLabel, Float>> response = new HashMap<>();
        response.put(text, emotionClassifier.score(text, SupportedLanguage.English));
        return response;
    }

    @GetMapping(path = "/score", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBuzzScore(@RequestParam(defaultValue = DEFAULT_TXT) String text,
                                          @RequestParam(defaultValue = DEFAULT_LNG) String lang) {
        Map<String, BigDecimal> emoMap = emotionClassifier.score(text, SupportedLanguage.English)
                .entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> {
                    BigDecimal bd = BigDecimal.valueOf(e.getValue());
                    return bd.setScale(4, RoundingMode.HALF_UP);
                }));

        SentimentResult result = sentimentClassifier.computeSentimentWithScores(text, lang);
        Map<String, BigDecimal> buzzMap = new HashMap<>();
        buzzMap.put(SentimentLabel.NEUTRAL.name(), BigDecimal.valueOf(result.getNeutralScore()).setScale(4, RoundingMode.HALF_UP));
        buzzMap.put(SentimentLabel.POSITIVE.name(), BigDecimal.valueOf(result.getPositiveScore()).setScale(4, RoundingMode.HALF_UP));
        buzzMap.put(SentimentLabel.NEGATIVE.name(), BigDecimal.valueOf(result.getNegativeScore()).setScale(4, RoundingMode.HALF_UP));

        Map<String, Map<String, BigDecimal>> resMap = new HashMap<>();
        resMap.put("Emotion", emoMap);
        resMap.put("Sentiment", buzzMap);

        return ResponseEntity.ok(resMap);
    }

    @GetMapping()
    public ResponseEntity<?> getBuzz(@RequestParam(defaultValue = DEFAULT_TXT) String text,
                                       @RequestParam(defaultValue = DEFAULT_LNG) String lang) {
        Map<String, String> res = new HashMap<>();
        res.put("Emotion", emotionClassifier.classify(text, DEFAULT_LNG).map(Enum::name).orElse("NO EMOTION"));
        res.put("Sentiment", sentimentClassifier.computeSentiment(text, lang).name());
        return ResponseEntity.ok(res);
    }


    @GetMapping(value="/{text:[a-z-]+}.{number:[\\d]+}") //Just for regexp fun (use: /api/buzz/text.1
    public String regularExp(@PathVariable String text, @PathVariable String number) {
        return "Got your text and number !!";
    }


    @Configuration
    public static class BuzzControllerConfiguration {
        @Bean
        public EmotionClassifier getEmotionClassifier() {
            return BuzzEmotionClassifier.INSTANCE;
        }

        @Bean
        public SentimentClassifier getSentimentClassifier() {
            return SimpleBuzz.INSTANCE;
        }
    }
}
