package com.constellio.model.conf.ldap;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class RegexFilter implements Filter {
	private String acceptedRegex;
	private String rejectedRegex;
	private Pattern acceptedPattern;
	private Pattern rejectedPattern;

	public RegexFilter(String acceptedRegex, String rejectedRegex) {
		this.acceptedRegex = acceptedRegex;
		this.rejectedRegex = rejectedRegex;
		if (acceptedRegex != null && StringUtils.isNotBlank(acceptedRegex)) {
			this.acceptedPattern = Pattern.compile(acceptedRegex);
		}
		if (rejectedRegex != null && StringUtils.isNotBlank(rejectedRegex)) {
			this.rejectedPattern = Pattern.compile(rejectedRegex);
		}
	}

	@Override
	public Boolean isAccepted(String word) {
		if (accepted(word)) {
			return !rejected(word);
		} else {
			return false;
		}
	}

	private boolean rejected(String word) {
		if (this.rejectedPattern == null) {
			return false;
		}
		return this.rejectedPattern.matcher(word).matches();
	}

	private boolean accepted(String word) {
		if (this.acceptedPattern == null) {
			return true;
		}
		return this.acceptedPattern.matcher(word).matches();
	}

	public String getAcceptedRegex() {
		return acceptedRegex;
	}

	public String getRejectedRegex() {
		return rejectedRegex;
	}

}