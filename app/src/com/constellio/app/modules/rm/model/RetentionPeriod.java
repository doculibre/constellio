package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.model.RetentionPeriod.RetentionPeriodRuntimeException.RetentionPeriodRuntimeException_PeriodIsFixed;
import com.constellio.app.modules.rm.model.RetentionPeriod.RetentionPeriodRuntimeException.RetentionPeriodRuntimeException_PeriodIsOpened;
import com.constellio.app.modules.rm.model.enums.RetentionType;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class RetentionPeriod implements Serializable {

	private static String PERIOD_888 = "888";
	private static String PERIOD_999 = "999";

	private int fixedValue;

	private int variableRetentionRuleCode = 0;

	@JsonValue
	public int getValue() {
		if (variableRetentionRuleCode != 0) {
			return variableRetentionRuleCode;
		} else {
			return fixedValue;
		}
	}

	public int getFixedValue() {
		return fixedValue;
	}

	public boolean isVariablePeriod() {
		return variableRetentionRuleCode != 0;
	}

	public boolean is888() {
		return variableRetentionRuleCode != 0 && !is999();
	}

	public boolean is999() {
		return PERIOD_999.equals("" + variableRetentionRuleCode);
	}

	RetentionPeriod(int fixedValue) {
		if (fixedValue == 888 || fixedValue == 999) {
			this.variableRetentionRuleCode = fixedValue;
		} else {
			this.fixedValue = fixedValue;
		}
	}

	RetentionPeriod(String variablePeriodCode) {
		this.variableRetentionRuleCode = Integer.valueOf(variablePeriodCode);
	}

	public int getFixedPeriod() {
		if (isVariablePeriod()) {
			throw new RetentionPeriodRuntimeException_PeriodIsOpened();
		} else {
			return fixedValue;
		}
	}

	//	public String getVariablePeriodId() {
	//		if (!isVariablePeriod()) {
	//			throw new RetentionPeriodRuntimeException_PeriodIsFixed();
	//		} else {
	//			return variableRetentionRuleId;
	//		}
	//	}

	public String getVariablePeriodCode() {
		if (!isVariablePeriod()) {
			throw new RetentionPeriodRuntimeException_PeriodIsFixed();
		} else {
			return "" + variableRetentionRuleCode;
		}
	}

	@Override
	public String toString() {
		if (variableRetentionRuleCode != 0) {
			return "" + variableRetentionRuleCode;
		} else {
			return "" + fixedValue;
		}
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public static RetentionPeriod fixed(int years) {
		return new RetentionPeriod(years);
	}

	public static RetentionPeriod ZERO = new RetentionPeriod(0);

	public static RetentionPeriod OPEN_888 = new RetentionPeriod("888");

	public static RetentionPeriod OPEN_999 = new RetentionPeriod("999");

	public RetentionType getRetentionType() {
		if (is888()) {
			return RetentionType.OPEN;

		} else if (is999()) {
			return RetentionType.UNTIL_REPLACED;

		} else {
			return RetentionType.FIXED;
		}

	}

	public boolean isNotZero() {
		return !this.equals(RetentionPeriod.ZERO);
	}

	public boolean isZero() {
		return this.equals(RetentionPeriod.ZERO);
	}

	public static RetentionPeriod variable(String code) {
		return new RetentionPeriod(code);
	}

	public static RetentionPeriod variable(VariableRetentionPeriod variablePeriod) {
		return new RetentionPeriod(variablePeriod.getCode());
	}

	public boolean isFixed() {
		return !isVariablePeriod();
	}

	public static class RetentionPeriodRuntimeException extends RuntimeException {

		public RetentionPeriodRuntimeException(String message) {
			super(message);
		}

		public static class RetentionPeriodRuntimeException_PeriodIsFixed extends RetentionPeriodRuntimeException {

			public RetentionPeriodRuntimeException_PeriodIsFixed() {
				super("The period is fixed");
			}
		}

		public static class RetentionPeriodRuntimeException_PeriodIsOpened extends RetentionPeriodRuntimeException {

			public RetentionPeriodRuntimeException_PeriodIsOpened() {
				super("The period is opened");
			}
		}
	}
}
