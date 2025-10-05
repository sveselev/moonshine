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
%class JflexBuzzTurkishLexer
%implements JflexLexer
%function getNextToken
%char
%buffer 255

%include common-code.jflex

R_HAHA = ([Mm][Uu][Aa])?(([Hh][Aa])|([Aa]+[Hh]+|[Hh]+[Aa]+)[HhAa]+)
R_OMG = [Oo]+[Mm]+[Ff]*[Gg]+
R_WOW = [Vv]+[Aa]+[Yy]+
R_DAMN = [Dd][Dd]+[Aa]+[Mm]+[Nn]+|[Dd]+[Aa][Aa]+[Mm]+[Nn]+|[Dd]+[Aa]+[Mm][Mm]+[Nn]+|[Dd]+[Aa]+[Mm]+[Nn][Nn]+
R_OOPS = [Oo][Oo]+[Pp]+[Ss]+
R_NO = [Hh][Hh]+[Aa]+[Yy]+[Iıİi]+[Rr]+|[Hh]+[Aa][Aa]+[Yy]+[Iıİi]+[Rr]+|[Hh]+[Aa]+[Yy][Yy]+[Iıİi]+[Rr]+|[Hh]+[Aa]+[Yy]+[Iıİi][Iıİi]+[Rr]+|[Hh]+[Aa]+[Yy]+[Iıİi]+[Rr][Rr]+
R_LOL = [Ll][Ll]+[Oo]+[Ll]+|[Ll]+[Oo][Oo]+[Ll]+|[Ll]+[Oo]+[Ll][Ll]+
R_FUCK = [Ss]+[Iıİi]+[Kk]+[Tt]+[Iıİi]+[Rr]+|[Ss]+[Kk]+[Tt]+[Rr]+[Gg]+[Tt]+|[Aa]+[Mm]+[Kk]+|[Oo]+[CcÇç]+|[Pp]+[Ee]*[Zz]+[Ee]*[Vv]+[Ee]*[Nn]+[Kk]+|[Oo]+[Rr]+[Oo]+[Ss]+[Pp]+[Uu]+|[Yy]+[Aa]+[Vv]+[SsŞş]+[Aa]+[Kk]+|[Yy]+[Aa]+[Vv]+[SsŞş]+[Aa]+[Kk]+[Ll]+[Aa]+[Rr]+|[Yy]+[Vv]+[SsŞş]+[Kk]+|[Hh]+[Aa]+[Ss]+[Tt]+[Iıİi]+[Rr]+|[Pp]+[Iıİi]+[CcÇç]+|[Ss]+[Iıİi]+[CcÇç]+[Aa]+[Rr]+[Iıİi]+[Mm]+|[Ss]+[Iıİi]+[CcÇç]+[Tt]+[iı]+[gğ]+[Iıİi]+[Mm]+|[Mm][Aa][Ll][Mm][Iıİi][Ss][Iıİi][Nn]
R_HAPPY = [Hh][Hh]+[Aa]+[Pp][Pp]+[Yy]+|[Hh]+[Aa][Aa]+[Pp][Pp]+[Yy]+|[Hh]+[Aa]+[Pp][Pp][Pp]+[Yy]+|[Hh]+[Aa]+[Pp][Pp]+[Yy][Yy]+
R_LOVE = [Aa]+[SsŞş]+[Kk]+[Iıİi]+[Mm]+|[Ss]+[Ee]+[Vv]+[Gg]+[Iıİi]+[Ll]+[Iıİi]+[Mm]+|[Aa]+[SsŞş]+[Kk]+
R_YEAH = [Yy][Yy]+[Ee]+[Ss]+|[Yy]+[Ee][Ee]+[Ss]+|[Yy]+[Ee]+[Ss][Ss]+
R_YES = [Ee][Ee]+[Vv]+[Ee]+[Tt]+|[Ee]+[Vv][Vv]+[Ee]+[Tt]+|[Ee]+[Vv]+[Ee][Ee]+[Tt]+|[Ee]+[Vv]+[Ee]+[Tt][Tt]+
R_ARGH = [UuÜü]+[Ff]+
R_LMAO = [Ll]+[Mm]+[Ff]*[Aa]+[Oo]+

APOSTROPHE_NTVE = [:letter:]+['´’][Nn]+[Tt]+['´’][Vv]+[Ee]+|[:letter:]+[Nn]+['´’][Tt]+['´’][Vv]+[Ee]+
APOSTROPHE_NT = [:letter:]+['´’][Nn]+[Tt]|[:letter:]+[Nn]+['´’][Tt]+

%include common-regex.jflex

// include \u00A0 as whitespace
WHITESPACE = \r\n | [ \r\n\t\f\u0085\u000B\u2028\u2029\u00A0]

%%

// regular apostrophes are replaced with chmodelpunc
{APOSTROPHE_NT} { return JflexBuzzTokenizer.APOSTROPHE_NT; }
{APOSTROPHE_NTVE} { return JflexBuzzTokenizer.APOSTROPHE_NTVE; }
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
