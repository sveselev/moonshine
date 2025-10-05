// common regexes for buzz tokenization - all languages

EMOTICON_HEARTS = <3+
EMOJI_HEARTS = [\u2764\u2665\u{1F495}\u{1F499}\u{1F49A}\u{1F49B}\u{1F49C}\u{1F496}\u{1F497}\u{1F498}\u{1F49D}\u{1F49E}\u{1F49F}\u{1F493}\u{1F492}\u{1F491}\u{1F490}\u{1F48F}\u{1F48C}]
POSITIVE_SMILEY = [<>]?[:;=8][-o*']?[\)\]DPpd]+|[\(\[P]+[-o*']?[:;=8][<>]?
EMOJI_POS = [\u{1F603}\u{1F48B}\u{1F61D}\u{1F44F}\u{1F602}\u{263A}\u{1F60D}\u{1F618}\u{1F44C}\u{1F60A}\u{1F60F}\u{1F601}\u{1F633}\u{270C}\u{1F44D}\u{1F609}\u{1F60C}\u{1F61C}\u{1F60B}\u{1F64F}\u{1F604}\u{1F339}\u{1F388}\u{1F606}]
NEGATIVE_SMILEY = [<>]?[:;=8][\-o*']?[\(\[]+|[\)\]]+[-o*']?[:;=8][<>]?
EMOJI_NEG = [\u{1F494}\u{1F62B}\u{1F612}\u{1F629}\u{1F62D}\u{1F61E}\u{1F621}\u{1F620}\u{1F4A2}]

EXCLAMATION = \!+
QUESTION = \?+

SPECIAL_CHARS = ["_"\u200c\u200d\ua67e\u05be\u05f3\u05f4\uff5e\u301c\u309b\u309c\u30a0\u30fb\u3003\u0f0b\u0f0c\u00b7]
HASHTAG_LETTERS_AND_MARKS = [\p{L}\p{M}]|{SPECIAL_CHARS}
HASHTAG_LETTERS_MARKS_NUMERALS = [\p{Nd}\p{L}\p{M}]|{SPECIAL_CHARS}

// Twitter hashtag: # followed by alphnumeric characters and/or -, _, or @
HASHTAG = [#\uFF03]{HASHTAG_LETTERS_MARKS_NUMERALS}*{HASHTAG_LETTERS_AND_MARKS}{HASHTAG_LETTERS_MARKS_NUMERALS}*

ALPHANUM   = ({LETTER}|[_]|[:digit:])+
PUNCTUATION = [@%:|+#@&/()\[\],.'´’\"“”\-_=~\^]+

// Set of symbols that are allowed in Twitter usernames
ALPHANUM_TWITTER_USERNAME   = ({LETTER}|[:digit:])+

// From the JFlex manual: "the expression that matches everything of <a> not matched by <b> is !(!<a>|<b>)"
LETTER     = !(![:letter:])

WHITESPACE = \r\n | [ \r\n\t\f\u0085\u000B\u2028\u2029]

URL = ((https?|ftp):\/{1,3}|www\\.)[^\s\/$.?#].[^\s]*

// Twitter username: @ followed by alphanumeric characters and/or _ (Thai is not allowed)
USERNAME = "@" ("_"|{ALPHANUM_TWITTER_USERNAME})+

