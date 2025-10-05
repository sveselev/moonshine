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
%class JflexBuzzArabicLexer
%implements JflexLexer
%function getNextToken
%char
%buffer 255

%include common-code.jflex

R_HAHA = [\u0647][\u0647]+[\u0627]+|[\u0647][\u0627][\u0627]+
R_OMG = [Oo]+[Mm]+[Ff]*[Gg]+
R_WOW = [Ww][Ww]+[Oo]+[Ww]+|[Ww]+[Oo][Oo]+[Ww]+|[Ww]+[Oo]+[Ww][Ww]+
R_DAMN = [Dd][Dd]+[Aa]+[Mm]+[Nn]+|[Dd]+[Aa][Aa]+[Mm]+[Nn]+|[Dd]+[Aa]+[Mm][Mm]+[Nn]+|[Dd]+[Aa]+[Mm]+[Nn][Nn]+
R_OOPS = \u0639+\u0641+\u0648+\u0627+
R_NO = \u0646+\u0648+|\u0644\u0644+\u064A+\u0633+|\u0644+\u064A\u064A+\u0633+|\u0644+\u064A+\u0633\u0633+
R_LOL = [\u0644][\u0644]+[\u0648]+[\u0644]+|[\u0644]+[\u0648][\u0648]+[\u0644]+|[\u0644]+[\u0648]+[\u0644][\u0644]+
R_FUCK = [\u0627][\u0644]+[\u0644]+[\u0639]+[\u0646]+[\u0629]
R_HAPPY = [\u0633][\u0633]+[\u0639]+[\u064A]+[\u062F]+|[\u0633]+[\u0639][\u0639]+[\u064A]+[\u062F]+|[\u0633]+[\u0639]+[\u064A][\u064A]+[\u062F]+|[\u0633]+[\u0639]+[\u064A]+[\u062F][\u062F]+
R_LOVE = [\u062D][\u062D]+[\u0628]+[\u0643]*|[\u062D]+[\u0628][\u0628]+[\u0643]*|[\u062D]+[\u0628]+[\u0643][\u0643]+
R_YEAH = [Yy][Yy]+[Ee]+[Aa]+[Hh]+|[Yy]+[Ee][Ee]+[Aa]+[Hh]+|[Yy]+[Ee]+[Aa][Aa]+[Hh]+|[Yy]+[Ee]+[Aa]+[Hh][Hh]+
R_YES = [\u0646]+[\u0639]+[\u0645][\u0645]+|[\u0646]+[\u0639][\u0639]+[\u0645]+|[\u0646][\u0646]+[\u0639]+[\u0645]+
R_ARGH = [Aa]+[Rr]+[GgHh]+
R_LMAO = [Ll]+[Mm]+[Ff]*[Aa]+[Oo]+

APOSTROPHE_NT = [:letter:]+['´’][Nn]+[Tt]|[:letter:]+[Nn]+['´’][Tt]+

%include common-regex.jflex

// need to include arabic spacing marks
ALPHANUM   = ({LETTER}|[_]|[:digit:]|\p{arabic}|\p{block=arabicextendeda})+

%%

// regular apostrophes are replaced with chmodelpunc
{APOSTROPHE_NT} { return JflexBuzzTokenizer.APOSTROPHE_NT; }
{USERNAME} { return JflexBuzzTokenizer.USER; }
{URL} { return JflexBuzzTokenizer.URL; }
{EMOTICON_HEARTS} { return JflexBuzzTokenizer.HEARTS;}
{EMOJI_HEARTS} { return JflexBuzzTokenizer.HEARTS;}
{EMOJI_POS} { return JflexBuzzTokenizer.POSITIVE;}
{EMOJI_NEG} { return JflexBuzzTokenizer.NEGATIVE;}
{POSITIVE_SMILEY} { return JflexBuzzTokenizer.POSITIVE;}
{NEGATIVE_SMILEY} { return JflexBuzzTokenizer.NEGATIVE;}
{R_HAHA} { return JflexBuzzTokenizer.HAHA;}
{R_OMG} { return JflexBuzzTokenizer.OMG;}
{R_WOW} { return JflexBuzzTokenizer.WOW;}
{R_DAMN} { return JflexBuzzTokenizer.DAMN;}
{R_OOPS} { return JflexBuzzTokenizer.OOPS;}
{R_NO} { return JflexBuzzTokenizer.NO;}
{R_LOL} { return JflexBuzzTokenizer.LOL;}
{R_LMAO} { return JflexBuzzTokenizer.LOL;}
{R_FUCK} { return JflexBuzzTokenizer.FUCK; }
{R_HAPPY} { return JflexBuzzTokenizer.HAPPY;}
{R_LOVE} { return JflexBuzzTokenizer.LOVE;}
{R_YEAH} { return JflexBuzzTokenizer.YES;}
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
