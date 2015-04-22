/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.app.modules.rm.model.RetentionPeriod.RetentionPeriodRuntimeException.RetentionPeriodRuntimeException_PeriodIsFixed;
import com.constellio.app.modules.rm.model.RetentionPeriod.RetentionPeriodRuntimeException.RetentionPeriodRuntimeException_PeriodIsOpened;
import com.constellio.app.modules.rm.model.enums.RetentionType;

public class RetentionPeriod implements Serializable {

	private static int PERIOD_888 = 888;
	private static int PERIOD_999 = 999;

	private int value;

	public int getValue() {
		return value;
	}

	public boolean isVariablePeriod() {
		return is888() || is999();
	}

	public boolean is888() {
		return PERIOD_888 == value;
	}

	public boolean is999() {
		return PERIOD_999 == value;
	}

	public RetentionPeriod(int value) {
		this.value = value;
	}

	public int getFixedPeriod() {
		if (isVariablePeriod()) {
			throw new RetentionPeriodRuntimeException_PeriodIsOpened();
		} else {
			return value;
		}
	}

	public int getVariablePeriod() {
		if (!isVariablePeriod()) {
			throw new RetentionPeriodRuntimeException_PeriodIsFixed();
		} else {
			return value;
		}
	}

	@Override
	public String toString() {
		return "" + value;
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

	public static RetentionPeriod OPEN_888 = new RetentionPeriod(PERIOD_888);

	public static RetentionPeriod OPEN_999 = new RetentionPeriod(PERIOD_999);

	public static RetentionPeriod ZERO = new RetentionPeriod(0);

	public RetentionType getRetentionType() {
		if (OPEN_888.equals(this)) {
			return RetentionType.OPEN;

		} else if (OPEN_999.equals(this)) {
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