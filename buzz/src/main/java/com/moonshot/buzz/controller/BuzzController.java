package com.moonshot.buzz.controller;

import com.moonshot.buzz.buzz3.SentimentClassifier;
import com.moonshot.buzz.buzz3.SentimentClassifier.SentimentLabel;
import com.moonshot.buzz.buzz3.SentimentClassifier.SentimentResult;
import com.moonshot.buzz.buzz3.SimpleBuzz;
import com.moonshot.buzz.emotion.BuzzEmotionClassifier;
import com.moonshot.buzz.emotion.BuzzEmotionClassifier.SupportedLanguage;
import com.moonshot.buzz.emotion.EmotionClassifier;
import com.moonshot.buzz.emotion.EmotionLabel;
import com.moonshot.buzz.swagger.LanguageParam;
import com.moonshot.buzz.swagger.TextInputParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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
@Tag(name = "Buzz", description = "Endpoints for sentiment and emotion analysis")
public class BuzzController {

    private final String DEFAULT_TXT = "Wish you were here";
    private final String DEFAULT_LNG = "en";

    private final EmotionClassifier emotionClassifier;
    private final SentimentClassifier sentimentClassifier;

    public BuzzController(EmotionClassifier emotionClassifier, SentimentClassifier sentimentClassifier) {
        this.emotionClassifier = emotionClassifier;
        this.sentimentClassifier = sentimentClassifier;
    }

    @Operation(
        summary = "Compute overall sentiment label",
        description = "Returns POSITIVE, NEGATIVE, or NEUTRAL for the given text and language code."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sentiment label returned",
            content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping(path = "/sentiment")
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<?> getSentiment(
            @TextInputParam
            @RequestParam(defaultValue = DEFAULT_TXT) String text,
            @LanguageParam
            @RequestParam(defaultValue = DEFAULT_LNG) String lang) {
        SentimentLabel snt = sentimentClassifier.computeSentiment(text, lang);
        return ResponseEntity.ok(snt.name());
    }

    @Operation(
        summary = "Classify dominant emotion",
        description = "Returns the dominant emotion label for the provided text (English only)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Emotion label returned",
            content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping(path = "/emotion")
    public ResponseEntity<?> getEmotion(
            @TextInputParam
            @RequestParam(defaultValue = DEFAULT_TXT) String text) {
        Optional<EmotionLabel> emo = emotionClassifier.classify(text, DEFAULT_LNG);
        return ResponseEntity.ok(emo.map(Enum::name).orElse("NO EMOTION"));
    }

    @Operation(
        summary = "Get emotion scores",
        description = "Returns normalized scores for each emotion for the given text (English only)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Map of emotion scores",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping(path = "/emotion/score")
    public Map<String, Map<EmotionLabel, Float>> getScore(
            @TextInputParam
            @RequestParam(defaultValue = DEFAULT_TXT) String text) {
        Map<String, Map<EmotionLabel, Float>> response = new HashMap<>();
        response.put(text, emotionClassifier.score(text, SupportedLanguage.English));
        return response;
    }

    @Operation(
        summary = "Combined emotion and sentiment scores",
        description = "Returns both emotion scores and sentiment probability scores for the supplied text and language."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Composite score map",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping(path = "/score", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBuzzScore(
            @TextInputParam
            @RequestParam(defaultValue = DEFAULT_TXT) String text,
            @LanguageParam
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

    @Operation(
        summary = "Quick emotion & sentiment labels",
        description = "Convenience endpoint returning top emotion and sentiment labels for the text."
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Labels map", content = @Content(mediaType = "application/json"))})
    @GetMapping()
    public ResponseEntity<?> getBuzz(
            @TextInputParam
            @RequestParam String text,
            @LanguageParam
            @RequestParam(defaultValue = DEFAULT_LNG) String lang) {
        Map<String, String> res = new HashMap<>();
        res.put("Emotion", emotionClassifier.classify(text, DEFAULT_LNG).map(Enum::name).orElse("NO EMOTION"));
        res.put("Sentiment", sentimentClassifier.computeSentiment(text, lang).name());
        return ResponseEntity.ok(res);
    }

    @Hidden
    @GetMapping(value="/{text:[a-z-]+}.{number:[\\d]+}") //Just for regexp fun (use: /api/buzz/text.1
    public String regularExp(@PathVariable String text, @PathVariable String number) {
        return "Got your text and number !!";
    }


    @Configuration
    public static class BuzzControllerConfiguration {
        @Bean
        @Lazy
        public EmotionClassifier getEmotionClassifier() {
            return BuzzEmotionClassifier.INSTANCE;
        }

        @Bean
        @Lazy
        public SentimentClassifier getSentimentClassifier() {
            return SimpleBuzz.INSTANCE;
        }
    }
}
