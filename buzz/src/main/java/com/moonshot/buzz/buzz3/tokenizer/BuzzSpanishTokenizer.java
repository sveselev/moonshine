package com.moonshot.buzz.buzz3.tokenizer;

import com.moonshot.buzz.buzz3.tokenizer.jflex.SpanishAnalyzer;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.List;
import java.util.Set;
import java.util.function.Function;


public class BuzzSpanishTokenizer extends BuzzTokenizer {
    private static final String stemSpellFile = "/com/moonshot/buzz/sentiment/tokenizer/spanishStemSpell.map";


    private static final Set<String> notWords = ImmutableSet.of("no");
    private static final Set<String> skipWords = ImmutableSet.of("ser", "soy", "fui", "era", "seria", "sere", "eres", "fuiste", "eras", "serias", "seras",
        "es", "fue", "era", "seria", "sera", "somos", "fuimos", "eramos", "seriamos", "seremos", "sois", "fuisteis", "erais", "seríais",
        "sereis", "son", "fueron", "eran", "serian", "seran", "sea", "fuera", "fuese", "fuere", "seas", "fueras", "fueses", "fueres", "sea",
        "fuera", "fuese", "fuere", "seamos", "fuéramos", "fuésemos", "fuéremos", "seáis", "fuerais", "fueseis", "fuereis", "sean", "fueran",
        "fuesen", "fueren", "se", "sea", "seamos", "sed", "sean", "sido", "haber", "he", "hube", "había", "habría", "habré", "has", "hubiste",
        "habías", "habrías", "habrás", "ha", "hay", "hubo", "había", "habría", "habrá", "hemos", "hubimos", "habíamos", "habríamos", "habremos",
        "habéis", "hubisteis", "habíais", "habríais", "habréis", "han", "hubieron", "habían", "habrían", "habrán", "haya", "hubiera", "hubiese",
        "hubiere", "hayas", "hubieras", "hubieses", "hubieres", "haya", "hubiera", "hubiese", "hubiere", "hayamos", "hubiéramos", "hubiésemos",
        "hubiéremos", "hayáis", "hubierais", "hubieseis", "hubiereis", "hayan", "hubieran", "hubiesen", "hubieren", "he", "haya", "hayamos", "habed",
        "hayan", "habido", "estar", "estoy", "estuve", "estaba", "estaría", "estaré", "estás", "estuviste", "estabas", "estarías", "estarás", "está",
        "estuvo", "estaba", "estaría", "estará", "estamos", "estuvimos", "estábamos", "estaríamos", "estaremos", "estáis", "estuvisteis", "estabais",
        "estaríais", "estaréis", "están", "estuvieron", "estaban", "estarían", "estarán", "esté", "estuviera", "estuviese", "estuviere", "estés", "estuvieras",
        "estuvieses", "estuvieres", "esté", "estuviera", "estuviese", "estuviere", "estemos", "estuviéramos", "estuviésemos", "estuviéremos", "estéis",
        "estuvierais", "estuvieseis", "estuviereis", "estén", "estuvieran", "estuviesen", "estuvieren", "está", "esté", "estemos", "estad", "estado", "estén",
        "tener", "hacer", "hago", "hice", "hacía", "haría", "haré", "haces", "hiciste", "hacías", "harías", "harás", "hace", "hizo", "hacía", "haría",
        "hará", "hacemos", "hicimos", "hacíamos", "haríamos", "haremos", "hacéis", "hicisteis", "hacíais", "haríais", "haréis", "hacen", "hicieron",
        "hacían", "harían", "harán", "haga", "hiciera", "hiciese", "hiciere", "hagas", "hicieras", "hicieses", "hicieres", "haga", "hiciera", "hiciese",
        "hiciere", "hagamos", "hiciéramos", "hiciésemos", "hagáis", "hiciereis", "hagan", "hicieran", "hiciesen", "hicieren", "haz", "haga", "hagamos",
        "haced",
        "hagan", "hecho", "poder", "puedo", "pude", "podía", "podría", "podré", "puedes", "pudiste", "podías", "podrías", "podrás", "puede", "pudo", "podía",
        "podría", "podrá", "podemos", "pudimos", "podíamos", "podríamos", "podremos", "podéis", "pudisteis", "podíais", "podríais", "podréis", "pueden",
        "pudieron",
        "podían", "podrían", "podrán", "pueda", "pudiera", "pudiese", "pudiere", "puedas", "pudieras", "pudieses", "pudieres", "pueda", "pudiera", "pudiese",
        "pudiere",
        "podamos", "pudiéramos", "pudiésemos", "pudiéremos", "podáis", "pudierais", "pudieseis", "pudiereis", "puedan", "pudieran", "pudiesen", "pudieren",
        "puede", "pueda",
        "podamos", "poded", "puedan", "podido", "tener", "tengo", "tuve", "tenía", "tendría", "tendré", "tienes", "tuviste", "tenías", "tendrías", "tendrás",
        "tiene",
        "tuvo", "tenía", "tendría", "tendrá", "tenemos", "tuvimos", "teníamos", "tendríamos", "tendremos", "tenéis", "tuvisteis", "teníais", "tendríais",
        "tendréis",
        "tienen", "tuvieron", "tenían", "tendrían", "tendrán", "tenga", "tuviera", "tuviese", "tuviere", "tengas", "tuvieras", "tuvieses", "tuvieres", "tenga",
        "tuviera", "tuviese", "tuviere", "tengamos", "tuviéramos", "tuviésemos", "tuviéremos", "tengáis", "tuvierais", "tuvieseis", "tuviereis", "tengan",
        "tuvieran",
        "tuviesen", "tuvieren", "ten", "tenga", "tengamos", "tened", "tengan", "tenido", "stopwords", "el", "la", "los", "las", "un", "una", "unos", "unas",
        "le", "lo", "se",
        "la", "las", "el", "ella", "ellas", "ellos", "a", "usted", "ustedes", "yo", "tu", "nosotros", "nosotras", "vosotros", "vosotras", "en", "que", "cual",
        "cuanto", "cuanta",
        "cuantos", "cuantas", "porque", "cuando", "donde", "como", "al", "del", "y", "de", "por", "mi", "mis", "tu", "tus", "su", "sus", "nuestro", "nuestras",
        "nuestros", "nuestra",
        "vuestro", "vuestra", "vuestras", "vuestros", "este", "ese", "aquel", "estos", "esos", "aquellos", "aquello", "esta", "esa", "aquella", "estas",
        "esas", "aquellas", "eso", "me",
        "te", "lo", "la", "nos", "os", "mio", "tuyo", "aqui", "ellos", "ello", "ella", "ellas", "cuando", "donde", "mientras", "quien", "con", "entre", "yo",
        "en", "ante", "antes",
        "porque", "por", "que", "como", "cuanto", "cuantos", "cuanta", "cuantas", "quien", "entonces", "entonce", "alguin", "algun", "alguno", "alguna",
        "algunas", "algunos", "alguien",
        "via", "demas");
    private Object2IntOpenHashMap<String> stemSpellMap = new Object2IntOpenHashMap<>();
    private static final Set<String> conjunctiveWords = ImmutableSet.of("chmodelpunctuation", "chmodelquestion", "chmodelexclamation");

    @Override
    protected void initializeResources() throws Exception {
        loadMap(this.getClass().getResourceAsStream(stemSpellFile), stemSpellMap);
    }

    private IntList negateSpellStem(String[] tokens) {
        int i = 0;

        IntArrayList transformedList = new IntArrayList();
        while (i < tokens.length) {
            String s = tokens[i];
            s = s.replaceAll("\\s+", "");
            int j = 1;

            if (!s.equals("") && !s.equals("chmodelpunctuation")) {
                if (notWords.contains(s)) {
                    while (true) {

                        if (i + j >= tokens.length || conjunctiveWords.contains(tokens[i + j]) || j > 4) {
                            break;
                        }

                        if (!skipWords.contains(tokens[i + j])) {
                            if (stemSpellMap.containsKey(tokens[i + j] + "_not")) {
                                transformedList.add(stemSpellMap.getInt(tokens[i + j] + "_not"));
                            }

                        }
                        j++;
                    }
                } else {
                    if (stemSpellMap.containsKey(s)) {
                        transformedList.add(stemSpellMap.getInt(s));
                    }
                }
            }
            i += j;
        }
        return transformedList;
    }

    public static List<String> tokenize(String sentence) {
        return tokenize(sentence, SpanishAnalyzer.INSTANCE);
    }

    @Override
    public IntList tokenIndices(String sentence) {
        return tokenIndices(sentence, BuzzSpanishTokenizer::tokenize);
    }

    @Override
    public IntList tokenIndices(String string, Function<String, List<String>> tokenizer) {
        return negateSpellStem(tokenizer.apply(string).toArray(new String[0]));
    }

}
