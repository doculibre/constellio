package com.constellio.app.ui.pages.search.criteria;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.services.search.query.logical.criteria.MeasuringUnitTime;

public class RelativeCriteria implements Serializable {

	public enum RelativeSearchOperator {
		EQUALS,
		TODAY,
		PAST,
		FUTURE
	}

	private MeasuringUnitTime measuringUnitTime;
	private MeasuringUnitTime endMeasuringUnitTime;
	private RelativeSearchOperator relativeSearchOperator;
	private RelativeSearchOperator endRelativeSearchOperator;

	public RelativeCriteria() {
	}

	public MeasuringUnitTime getMeasuringUnitTime() {
		return measuringUnitTime;
	}

	public void setMeasuringUnitTime(MeasuringUnitTime measuringUnitTime) {
		this.measuringUnitTime = measuringUnitTime;
	}

	public MeasuringUnitTime getEndMeasuringUnitTime() {
		return endMeasuringUnitTime;
	}

	public void setEndMeasuringUnitTime(MeasuringUnitTime endMeasuringUnitTime) {
		this.endMeasuringUnitTime = endMeasuringUnitTime;
	}

	public RelativeSearchOperator getRelativeSearchOperator() {
		return relativeSearchOperator;
	}

	public void setRelativeSearchOperator(RelativeSearchOperator relativeSearchOperator) {
		this.relativeSearchOperator = relativeSearchOperator;
	}

	public RelativeSearchOperator getEndRelativeSearchOperator() {
		return endRelativeSearchOperator;
	}

	public void setEndRelativeSearchOperator(RelativeSearchOperator endRelativeSearchOperator) {
		this.endRelativeSearchOperator = endRelativeSearchOperator;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
