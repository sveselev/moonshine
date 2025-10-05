// common code for jflex buzz tokenizers

%{
	public final int yychar() {
	    return yychar;
	}
	
	/**
	 * Fills CharTermAttribute with the current token text.
	 */
	public final void getText(CharTermAttribute t) {
	    t.copyBuffer(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
	}
%}
