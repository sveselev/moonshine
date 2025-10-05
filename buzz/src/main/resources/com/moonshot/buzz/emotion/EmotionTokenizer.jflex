package com.moonshot.buzz.emotion;


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
%final
%class EmotionTokenizerImpl
%function getNextToken
%char
%buffer 255

%{

	public static final int ALPHANUM          = EmotionTokenizer.ALPHANUM;
	public static final int APOSTROPHE        = EmotionTokenizer.APOSTROPHE;
	public static final int URL = EmotionTokenizer.URL;
	public static final int HEARTS = EmotionTokenizer.HEARTS;
	public static final int POSITIVE = EmotionTokenizer.POSITIVE;
	public static final int NEGATIVE = EmotionTokenizer.NEGATIVE;
	public static final int SAD = EmotionTokenizer.SAD;
	public static final int ANGER = EmotionTokenizer.ANGER;
	public static final int FEAR = EmotionTokenizer.FEAR;
	public static final int DISGUST = EmotionTokenizer.DISGUST;
	public static final int HAHA = EmotionTokenizer.HAHA;
	public static final int OMG = EmotionTokenizer.OMG;
	public static final int WOW = EmotionTokenizer.WOW;
	public static final int REALLY = EmotionTokenizer.REALLY;
	public static final int SO = EmotionTokenizer.SO;
	public static final int DAMN = EmotionTokenizer.DAMN;
	public static final int OOPS = EmotionTokenizer.OOPS;
	public static final int NO = EmotionTokenizer.NO;
	public static final int LOL = EmotionTokenizer.LOL;
	public static final int FUCK = EmotionTokenizer.FUCK;
	public static final int SHIT = EmotionTokenizer.SHIT;
	public static final int HAPPY = EmotionTokenizer.HAPPY;
	public static final int LOVE = EmotionTokenizer.LOVE;
	public static final int YES = EmotionTokenizer.YES;
	public static final int ARGH = EmotionTokenizer.ARGH;
	public static final int UGH = EmotionTokenizer.UGH;
	public static final int LMAO = EmotionTokenizer.LMAO;
	public static final int USER = EmotionTokenizer.USER;
	public static final int HASHTAG = EmotionTokenizer.HASHTAG;
	
	public static final String [] TOKEN_TYPES = EmotionTokenizer.TOKEN_TYPES;

	public final int yychar()
	{
	    return yychar;
	}
	
	/**
	 * Fills CharTermAttribute with the current token text.
	 */
	public final void getText(CharTermAttribute t) {
	    t.copyBuffer(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
	}
	
	/**
	  * Sets the scanner buffer size in chars
	  */
	  public final void setBufferSize(int numChars) {
	      ZZ_BUFFERSIZE = numChars;
	      char[] newZzBuffer = new char[ZZ_BUFFERSIZE];
	      System.arraycopy(zzBuffer, 0, newZzBuffer, 0, Math.min(zzBuffer.length, ZZ_BUFFERSIZE));
	      zzBuffer = newZzBuffer;
	  }
%}

R_UGH = [Uu]+[Gg]+[Hh]+
R_HAHA = ([Mm][Uu][Aa])?(([Hh][Aa])|([Aa]+[Hh]+|[Hh]+[Aa]+)[HhAa]+)
R_OMG = [Oo]+[Mm]+[Ff]*[Gg]+
R_WOW = [Ww][Ww]+[Oo]+[Ww]+|[Ww]+[Oo][Oo]+[Ww]+|[Ww]+[Oo]+[Ww][Ww]+
R_REALLY = [Rr][Rr]+[Ee]+[Aa]+[Ll]+[Yy]+|[Rr]+[Ee][Ee]+[Aa]+[Ll]+[Yy]+|[Rr]+[Ee]+[Aa][Aa]+[Ll]+[Yy]+|[Rr]+[Ee]+[Aa]+[Ll][Ll]+[Yy]+|[Rr]+[Ee]+[Aa]+[Ll]+[Yy][Yy]+
R_SO = [Ss][Ss]+[Oo]+|[Ss]+[Oo][Oo]+
R_DAMN = [Dd][Dd]+[Aa]+[Mm]+[Nn]+|[Dd]+[Aa][Aa]+[Mm]+[Nn]+|[Dd]+[Aa]+[Mm][Mm]+[Nn]+|[Dd]+[Aa]+[Mm]+[Nn][Nn]+
R_OOPS = [Oo][Oo]+[Pp]+[Ss]+
R_NO = [Nn][Nn]+[Oo]+|[Nn]+[Oo][Oo]+
R_LOL = [Ll][Ll]+[Oo]+[Ll]+|[Ll]+[Oo][Oo]+[Ll]+|[Ll]+[Oo]+[Ll][Ll]+
R_FUCK = [Ff][Ff]+[Uu]+[Cc]+[Kk]+|[Ff]+[Uu][Uu]+[Cc]+[Kk]+|[Ff]+[Uu]+[Cc][Cc]+[Kk]+|[Ff]+[Uu]+[Cc]+[Kk][Kk]+
R_SHIT = [Ss][Ss]+[Hh]+[Ii]+[Tt]+|[Ss]+[Hh][Hh]+[Ii]+[Tt]+|[Ss]+[Hh]+[Ii][Ii]+[Tt]+|[Ss]+[Hh]+[Ii]+[Tt][Tt]+
R_HAPPY = [Hh][Hh]+[Aa]+[Pp][Pp]+[Yy]+|[Hh]+[Aa][Aa]+[Pp][Pp]+[Yy]+|[Hh]+[Aa]+[Pp][Pp][Pp]+[Yy]+|[Hh]+[Aa]+[Pp][Pp]+[Yy][Yy]+
R_LOVE = [Ll]+[[Oo][Uu]]+[Vv]+[Ee]*[Ss]*
R_YEAH = [Yy][Yy]+[Ee]+[Aa]+[Hh]+|[Yy]+[Ee][Ee]+[Aa]+[Hh]+|[Yy]+[Ee]+[Aa][Aa]+[Hh]+|[Yy]+[Ee]+[Aa]+[Hh][Hh]+
R_YES = [Yy]+[Ee]+[Ss]+
R_ARGH = [Aa]+[Rr]+[GgHh]+
R_LMAO = [Ll]+[Mm]+[Ff]*[Aa]+[Oo]+

EMOTICON_HEARTS = <3
SIMPLE_HEARTS = [\u2764\u2665]
EMOJI_HEARTS = [\u{1F495}\u{1F499}\u{1F49A}\u{1F49B}\u{1F49C}\u{1F496}\u{1F497}\u{1F498}\u{1F49D}\u{1F49E}\u{1F49F}\u{1F493}\u{1F492}\u{1F491}\u{1F490}\u{1F48F}\u{1F48C}]
POSITIVE_SMILEY = [<>]?[:;=8][-o*']?[\)\]DPpd]+|[\(\[P]+[-o*']?[:;=8][<>]?
EMOJI_POS = [\u{1F600}\u{1F60E}\u{1F603}\u{1F48B}\u{1F61D}\u{1F44F}\u{1F602}\u{263A}\u{1F60D}\u{1F63B}\u{1F618}\u{1F44C}\u{1F60A}\u{1F60F}\u{1F601}\u{1F633}\u{270C}\u{1F44D}\u{1F609}\u{1F60C}\u{1F61C}\u{1F60B}\u{1F64F}\u{1F604}\u{1F339}\u{1F388}\u{1F606}\u{1F61B}\u{1F389}\u{1F619}]
NEGATIVE_SMILEY = [<>]?[:;=8][\-o*']?[\(\[]+|[\)\]]+[-o*']?[:;=8][<>]?
EMOJI_SAD = [\u{1F494}\u{1F62B}\u{1F629}\u{1F62D}\u{1F61E}\u{1F614}\u{1F615}\u{1F622}]
EMOJI_ANGER = [\u{1F612}\u{1F621}\u{1F620}\u{1F4A2}\u{1F4A9}\u{1F44E}\u{1F624}]
EMOJI_FEAR = [\u{1F628}\u{1F631}\u{1F616}\u{1F630}]
EMOJI_DISGUST = [\u{1F637}\u{1F635}]

SPECIAL_CHARS = ["_"\u200c\u200d\ua67e\u05be\u05f3\u05f4\uff5e\u301c\u309b\u309c\u30a0\u30fb\u3003\u0f0b\u0f0c\u00b7]
HASHTAG_LETTERS_AND_MARKS = [\p{L}\p{M}]|{SPECIAL_CHARS}
HASHTAG_LETTERS_MARKS_NUMERALS = [\p{Nd}\p{L}\p{M}]|{SPECIAL_CHARS}

// Twitter hashtag: # followed by alphnumeric characters and/or -, _, or @
ENGLISH_HASHTAG = [#\uFF03][a-zA-Z0-9\-_@]+
HASHTAG = [#\uFF03]{HASHTAG_LETTERS_MARKS_NUMERALS}*{HASHTAG_LETTERS_AND_MARKS}{HASHTAG_LETTERS_MARKS_NUMERALS}*

ALPHANUM   = ({LETTER}|[_]|[:digit:])+

// internal apostrophes: O'Reilly, you're, O'Reilly's
// use a post-filter to remove possessives
APOSTROPHE =  {ALPHA} (('|´|’) {ALPHA})+

// Set of symbols that are allowed in Twitter usernames
ALPHANUM_TWITTER_USERNAME   = ({LETTER}|[:digit:])+

ALPHA      = ({LETTER})+

// From the JFlex manual: "the expression that matches everything of <a> not matched by <b> is !(!<a>|<b>)"
LETTER     = !(![:letter:])

WHITESPACE = \r\n | [ \r\n\t\f\u0085\u000B\u2028\u2029]

URL = ((https?|ftp):\/{1,3}|www\\.)[^\s\/$.?#].[^\s]*

// Twitter username: @ followed by alphanumeric characters and/or _ (Thai is not allowed)
USERNAME = "@" ("_"|{ALPHANUM_TWITTER_USERNAME})+

%%

{APOSTROPHE}                                                   { return APOSTROPHE; }
{USERNAME}                                                     { return USER; }
{URL} { return URL; }
{EMOTICON_HEARTS} { return HEARTS;}
{SIMPLE_HEARTS} { return HEARTS;}
{EMOJI_HEARTS} { return HEARTS;}
{EMOJI_POS} { return POSITIVE;}
{POSITIVE_SMILEY} { return POSITIVE;}
{NEGATIVE_SMILEY} { return NEGATIVE;}
{EMOJI_SAD} { return SAD;}
{EMOJI_ANGER} { return ANGER;}
{EMOJI_FEAR} { return FEAR;}
{EMOJI_DISGUST} { return DISGUST;}
{R_HAHA} { return HAHA;}
{R_OMG} { return OMG;}
{R_WOW} { return WOW;}
{R_REALLY} { return REALLY;}
{R_SO} { return SO;}
{R_DAMN} { return DAMN;}
{R_OOPS} { return OOPS;}
{R_NO} { return NO;}
{R_LOL} { return LOL;}
{R_FUCK} { return FUCK;}
{R_SHIT} { return SHIT;}
{R_HAPPY} { return HAPPY;}
{R_LOVE} { return LOVE;}
{R_YEAH} { return YES;}
{R_YES} { return YES;}
{R_ARGH} { return ARGH;}
{R_UGH} { return UGH;}
{R_LMAO} { return LMAO;}
{ENGLISH_HASHTAG} { return HASHTAG;}
{HASHTAG} { /* ignore */}
{ALPHANUM}                                                     { return ALPHANUM; }

<<EOF>> { return YYEOF; }

/** Ignore the rest */
. | {WHITESPACE}                                             { /* ignore */ }
