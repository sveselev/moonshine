package com.moonshot.buzz.buzz3.tokenizer.jflex.lang;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



@SuppressWarnings("unused")

%%

%unicode 6.3
%integer
%public
%final
%class JflexBuzzRussianLexer
%implements JflexLexer
%function getNextToken
%char
%buffer 255

%include common-code.jflex

R_HAHA = ([Mm][Uu][Aa])?(([Hh][Aa])|([Aa]+[Hh]+|[Hh]+[Aa]+)[HhAa]+)
R_OMG = [Оо]+[Мм]+[Фф]*[Гг]+|[Oo]+[Mm]+[Ff]*[Gg]+
R_WOW = [Вв][Вв]+[Аа]+[Уу]+|[Вв]+[Аа][Аа]+[Уу]+|[Вв]+[Аа]+[Уу][Уу]+
R_DAMN = [Бб][Бб]+[Лл]+[Ии]+[Нн]+|[Бб]+[Лл][Лл]+[Ии]+[Нн]+|[Бб]+[Лл]+[Ии][Ии]+[Нн]+|[Бб]+[Лл]+[Ии]+[Нн][Нн]+
R_OOPS = [Уу][Уу]+[Пп]+[Сс]+
R_NO = [Нн][Нн]+[Ее]+[Тт]+|[Нн]+[Ее][Ее]+[Тт]+|[Нн]+[Ее]+[Тт][Тт]+
R_LOL = [Лл][Лл]+[Оо]+[Лл]+|[Лл]+[Оо][Оо]+[Лл]+|[Лл]+[Оо]+[Лл][Лл]+|[Ll][Ll]+[Oo]+[Ll]+|[Ll]+[Oo][Oo]+[Ll]+|[Ll]+[Oo]+[Ll][Ll]+
R_FUCK = [Фф]+[Аа]+[Кк]+|[Ff][Ff]+[Uu]+[Cc]+[Kk]+|[Ff]+[Uu][Uu]+[Cc]+[Kk]+|[Ff]+[Uu]+[Cc][Cc]+[Kk]+|[Ff]+[Uu]+[Cc]+[Kk][Kk]+
R_YES = [Дд][Дд]+[Аа]+|[Дд]+[Аа][Аа]+
R_ARGH = [Aa]+[Rr]+[GgHh]+
R_LMAO = [Лл]+[Мм]+[Фф]*[Аа]+[Оо]+|[Ll]+[Mm]+[Ff]*[Aa]+[Oo]+
R_HAHA2 = ([Хх][Аа])|([Аа]+[Хх]+|[Хх]+[Аа]+)[ХхАа]+


%include common-regex.jflex

// we want to split on single quotes, so remove them from the punct char set
PUNCTUATION = [@%:|+#@&/()\[\],.\"“”\-_=~\^]+

%%

// regular apostrophes are replaced with chmodelpunc
{USERNAME} { return JflexBuzzTokenizer.USER; }
{URL} { return JflexBuzzTokenizer.URL; }
{EMOTICON_HEARTS} { return JflexBuzzTokenizer.HEARTS;}
{EMOJI_HEARTS} { return JflexBuzzTokenizer.HEARTS;}
{EMOJI_POS} { return JflexBuzzTokenizer.POSITIVE;}
{EMOJI_NEG} { return JflexBuzzTokenizer.NEGATIVE;}
{POSITIVE_SMILEY} { return JflexBuzzTokenizer.POSITIVE;}
{NEGATIVE_SMILEY} { return JflexBuzzTokenizer.NEGATIVE;}
{R_HAHA} { return JflexBuzzTokenizer.HAHA;}
{R_HAHA2} { return JflexBuzzTokenizer.HAHA;}
{R_OMG} { return JflexBuzzTokenizer.OMG;}
{R_WOW} { return JflexBuzzTokenizer.WOW;}
{R_DAMN} { return JflexBuzzTokenizer.DAMN;}
{R_OOPS} { return JflexBuzzTokenizer.OOPS;}
{R_NO} { return JflexBuzzTokenizer.NO;}
{R_LOL} { return JflexBuzzTokenizer.LOL;}
{R_LMAO} { return JflexBuzzTokenizer.LOL;}
{R_FUCK} { return JflexBuzzTokenizer.FUCK; }
{R_YES} { return JflexBuzzTokenizer.YES;}
{R_ARGH} { return JflexBuzzTokenizer.ARGH;}
{HASHTAG} { return JflexBuzzTokenizer.HASHTAG; }
{EXCLAMATION} { return JflexBuzzTokenizer.EXCLAMATION; }
{QUESTION} { return JflexBuzzTokenizer.QUESTION; }
{PUNCTUATION} { return JflexBuzzTokenizer.PUNCTUATION; }
{ALPHANUM} { return JflexBuzzTokenizer.ALPHANUM; }
<<EOF>> { return YYEOF; }

/** Ignore the rest */
. | {WHITESPACE}                                             { /* ignore */ }
