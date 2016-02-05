package com.constellio.app.ui.pages.search.criteria;

public class ConditionException extends Exception {
	public static class ConditionException_TooManyClosedParentheses extends ConditionException {
	}

	public static class ConditionException_UnclosedParentheses extends ConditionException {
	}

	public static class ConditionException_EmptyCondition extends ConditionException {
	}
}
