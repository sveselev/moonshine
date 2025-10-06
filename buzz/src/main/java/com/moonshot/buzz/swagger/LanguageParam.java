package com.moonshot.buzz.swagger;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(
        name = "lang",
        description = "BCP-47 language code (e.g., en, es, fr)",
        example = "en",
        schema = @Schema(
                type = "string",
                allowableValues = { "en", "es", "fr", "de", "it", "nl", "ru", "pt", "sv", "tr", "zh", "ja", "ko", "ar", "id", "ms"}
        )
)
public @interface LanguageParam {}