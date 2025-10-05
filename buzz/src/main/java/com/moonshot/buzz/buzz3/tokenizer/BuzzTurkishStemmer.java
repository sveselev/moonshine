package com.moonshot.buzz.buzz3.tokenizer;

import zemberek.morphology.TurkishMorphology;
import zemberek.morphology.analysis.SingleAnalysis;
import zemberek.morphology.analysis.WordAnalysis;
import zemberek.tokenization.TurkishTokenizer;
import zemberek.tokenization.Token;
import zemberek.core.turkish.TurkishAlphabet;

import java.util.Collection;
import java.util.List;

public class BuzzTurkishStemmer {

    private final TurkishMorphology morphology;
    private final TurkishAlphabet alphabet = TurkishAlphabet.INSTANCE;

    private enum IslemTipi {
        YAZI_DENETLE,
        YAZI_COZUMLE,
        ASCII_TURKCE,
        TURKCE_ASCII,
        HECELE,
        ONER
    }

    public BuzzTurkishStemmer() {
        this.morphology = TurkishMorphology.createWithDefaults();
    }

    public char[] ozelKarakterDizisiGetir() {
        return new char[] {'ç','ğ','ı','ö','ş','ü','Ç','Ğ','İ','Ö','Ş','Ü'};
    }

    public String correct(String word) {
        word = word.replaceAll("[!“”|\"?.,:;/\\-\\)\\((*^\\\"]", " ");
        String turkish = null;
        try {
            turkish = asciiToTurkce(word);
            if (turkish.contains("[")) {
                turkish = turkish.substring(1, turkish.lastIndexOf(" "));
            }
        } catch (Exception e) {
            return word;
        }
        return turkish;
    }

    public String stem(String word) {
        try {
            List<Token> tokens = TurkishTokenizer.DEFAULT.tokenize(word);
            StringBuilder sb = new StringBuilder();
            for (Token t : tokens) {
                if (t.getType() == Token.Type.Word) {
                    WordAnalysis wa = morphology.analyze(t.getText());
                    if (wa.analysisCount() > 0) {
                        String lemma = wa.getAnalysisResults().get(0).getDictionaryItem().lemma;
                        sb.append(lemma);
                    } else {
                        sb.append(t.getText());
                    }
                } else {
                    sb.append(t.getText());
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return word;
        }
    }

    public String islemUygula(String islemTipi, String giris) {

        IslemTipi islem;
        try {
            islem = IslemTipi.valueOf(islemTipi);
            return islemUygula(islem, giris);
        } catch (IllegalArgumentException e) {
            System.out.println("istenilen islem:" + islemTipi + " mevcut degil");
            return "";
        }
    }

    public String islemUygula(IslemTipi islemTipi, String giris) {
        switch (islemTipi) {
            case YAZI_DENETLE:
                return yaziDenetle(giris);
            case YAZI_COZUMLE:
                return yaziCozumle(giris);
            case ASCII_TURKCE:
                return asciiToTurkceTahmin(giris);
            case TURKCE_ASCII:
                return turkceToAscii(giris);
            case HECELE:
                return hecele(giris);
            case ONER:
                return oner(giris);
            default:
                return "";
        }
    }

    public String yaziDenetle(String giris) {
        List<Token> tokens = TurkishTokenizer.DEFAULT.tokenize(giris);
        StringBuilder sonuc = new StringBuilder();
        for (Token t : tokens) {
            if (t.getType() == Token.Type.Word) {
                WordAnalysis wa = morphology.analyze(t.getText());
                if (wa.analysisCount() == 0) {
                    sonuc.append(hataliKelimeBicimle(t.getText()));
                } else {
                    sonuc.append(t.getText());
                }
            } else {
                sonuc.append(kelimeHariciBicimle(t.getText()));
            }
        }
        return sonuc.toString();
    }

    public String yaziCozumle(String giris) {
        List<Token> tokens = TurkishTokenizer.DEFAULT.tokenize(giris);
        StringBuilder sonuc = new StringBuilder();
        for (Token t : tokens) {
            if (t.getType() == Token.Type.Word) {
                WordAnalysis wa = morphology.analyze(t.getText());
                if (wa.analysisCount() == 0) {
                    sonuc.append(" :cozulemedi\n");
                } else {
                    for (SingleAnalysis sa : wa) {
                        sonuc.append(sa.formatLong()).append("\n");
                    }
                }
            }
        }
        return sonuc.toString();
    }

    public String asciiToTurkce(String giris) {
        // If needed, add zemberek-normalization and use TurkishDeasciifier here.
        return giris;
    }

    public String asciiToTurkceTahmin(String giris) {
        return asciiToTurkce(giris);
    }

    public String turkceToAscii(String giris) {
        StringBuilder sb = new StringBuilder(giris.length());
        for (char c : giris.toCharArray()) {
            switch (c) {
                case 'ç': case 'Ç': sb.append(c == 'Ç' ? 'C' : 'c'); break;
                case 'ğ': case 'Ğ': sb.append(c == 'Ğ' ? 'G' : 'g'); break;
                case 'ı': sb.append('i'); break;
                case 'İ': sb.append('I'); break;
                case 'ö': case 'Ö': sb.append(c == 'Ö' ? 'O' : 'o'); break;
                case 'ş': case 'Ş': sb.append(c == 'Ş' ? 'S' : 's'); break;
                case 'ü': case 'Ü': sb.append(c == 'Ü' ? 'U' : 'u'); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    public String hecele(String giris) {
        // Syllabification utilities live in other modules; return input unchanged for now.
        return giris;
    }

    public String oner(String giris) {
        List<Token> tokens = TurkishTokenizer.DEFAULT.tokenize(giris);
        StringBuilder sonuc = new StringBuilder();
        for (Token t : tokens) {
            if (t.getType() == Token.Type.Word) {
                // No direct equivalent in new API; just return the word as is or empty.
                // Placeholder: return the token text.
                sonuc.append(t.getText());
            } else {
                sonuc.append(t.getText());
            }
        }
        return sonuc.toString();
    }

    private String kelimeHariciBicimle(String str) {
        if (str.equals("\n")) {
            return "<br>";
        } else {
            return str;
        }
    }

    private String koseliParantezStringDiziBicimle(String[] cozumler, String ayrac) {
        StringBuilder bfr = new StringBuilder("[");
        for (int j = 0; j < cozumler.length; j++) {
            bfr.append(cozumler[j]);
            if (j < cozumler.length - 1) {
                bfr.append(ayrac);
            }
        }
        bfr.append("]");
        return bfr.toString();
    }

    private String koseliParantezStringDiziBicimle(Collection<String> cozumler, String ayrac) {
        return koseliParantezStringDiziBicimle(cozumler.toArray(new String[cozumler.size()]), ayrac);
    }

    private String hataliKelimeBicimle(String kelime) {
        return "<font color=\"#FF0033\">" + kelime + "</font>";
    }

    private String applyCase(String caseStr, String source) {
        char[] chrs = caseStr.toCharArray();
        char[] chrz = source.toCharArray();
        for (int i = 0; i < Math.min(chrs.length, chrz.length); i++) {
            if (Character.isUpperCase(chrs[i])) {
                chrz[i] = toUpperTr(chrz[i]);
            }
        }
        return new String(chrz);
    }

    private char toUpperTr(char c) {
        switch (c) {
            case 'i': return 'İ';
            case 'ı': return 'I';
            case 'ç': return 'Ç';
            case 'ğ': return 'Ğ';
            case 'ö': return 'Ö';
            case 'ş': return 'Ş';
            case 'ü': return 'Ü';
            default: return Character.toUpperCase(c);
        }
    }

}
