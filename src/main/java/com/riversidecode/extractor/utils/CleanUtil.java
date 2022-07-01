package com.riversidecode.extractor.utils;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CleanUtil {
	private static final Pattern ALIAS_REGEX = Pattern.compile("([^+][A-Za-z0-9_]{0,50}[@]{1})"); // '([^+@]+)+?([^@]*(.*))'

	@Inject
	public CleanUtil() {
		// inject
	}

	public String aliasRemoval(String record) {
		if(!record.contains("+")) {
			return record;
		}

		record = ALIAS_REGEX.matcher(record).replaceAll(Matcher.quoteReplacement("@"));
		return record.replaceAll("\\+@", "@");
	}
}
