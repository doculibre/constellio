package com.constellio.app.ui.pages.search.criteria;

import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_EmptyCondition;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_TooManyClosedParentheses;
import com.constellio.app.ui.pages.search.criteria.ConditionException.ConditionException_UnclosedParentheses;
import com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator;
import com.constellio.app.ui.pages.search.criteria.RelativeCriteria.RelativeSearchOperator;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.criteria.MeasuringUnitTime;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.List;
import java.util.ListIterator;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ConditionBuilder {
	private MetadataSchemaType schemaType;
	private String schemaCode;

	private String languageCode;

	public ConditionBuilder(MetadataSchemaType schemaType, String languageCode) {
		this(schemaType, "", languageCode);
	}

	public ConditionBuilder(MetadataSchemaType schemaType, String schemaCode, String languageCode) {
		this.schemaType = schemaType;
		this.languageCode = languageCode;
		this.schemaCode = schemaCode;
	}

	public LogicalSearchCondition build(List<Criterion> criteria)
			throws ConditionException {
		if (criteria.isEmpty()) {
			throw new ConditionException_EmptyCondition();
		}
		return buildOuterCondition(criteria.listIterator());
	}

	private LogicalSearchCondition buildOuterCondition(ListIterator<Criterion> criteria)
			throws ConditionException {
		ConditionAppender appender = new ConditionAppender(schemaType, schemaCode);
		BooleanOperator operator = BooleanOperator.AND;
		while (criteria.hasNext()) {
			Criterion criterion = criteria.next();
			if (criterion.isLeftParens()) {
				appender.append(buildInnerCondition(criterion, criteria), operator);
				operator = criteria.previous().getBooleanOperator();
				criteria.next();
			} else if (criterion.isRightParens()) {
				throw new ConditionException_TooManyClosedParentheses();
			} else {
				appender.append(buildClause(criterion), operator);
				operator = criterion.getBooleanOperator();
			}
		}
		return appender.build();
	}

	private LogicalSearchCondition buildInnerCondition(Criterion first, ListIterator<Criterion> criteria)
			throws ConditionException {
		ConditionAppender appender = new ConditionAppender(schemaType, schemaCode);
		appender.append(buildClause(first), BooleanOperator.AND);
		BooleanOperator operator = first.getBooleanOperator();
		while (criteria.hasNext()) {
			Criterion criterion = criteria.next();
			if (criterion.isLeftParens()) {
				appender.append(buildInnerCondition(criterion, criteria), operator);
				operator = criteria.previous().getBooleanOperator();
				criteria.next();
			} else {
				appender.append(buildClause(criterion), operator);
				operator = criterion.getBooleanOperator();
			}
			if (criterion.isRightParens()) {
				return appender.build();
			}
		}
		throw new ConditionException_UnclosedParentheses();
	}

	private LogicalSearchCondition buildClause(Criterion criterion) {
		Metadata metadata;
		if (StringUtils.isBlank(schemaCode)) {
			metadata = schemaType.getMetadata(criterion.getMetadataCode());
		}else{
			metadata = schemaType.getSchema(schemaCode).getMetadata(criterion.getMetadataCode());
		}

		Object value;
		Object endValue;
		switch (criterion.getSearchOperator()) {
			case EQUALS:
				value = getValue(criterion, metadata, false);
				if (metadata.getType() == MetadataValueType.DATE_TIME && value instanceof LocalDate && value != null) {
					return generateFrom().where(metadata).isValueInRange(((LocalDate) value).toLocalDateTime(LocalTime.MIDNIGHT),
							((LocalDate) value).toLocalDateTime(new LocalTime(23, 59, 59, 999)));
				} else {
					return generateFrom().where(metadata).isEqualTo(value);
				}
			case CONTAINS_TEXT:
				String stringValue = (String) criterion.getValue();

				if (metadata.getType() == MetadataValueType.STRING && !metadata.isSearchable()) {
					return generateFrom().where(metadata).isContainingText(stringValue);
				} else {
					return generateFrom().where(metadata.getAnalyzedField(languageCode)).query(stringValue.replace("-", "\\-"));
				}

			case LESSER_THAN:
				value = getValue(criterion, metadata, false);
				return generateFrom().where(metadata).isLessThan(value);
			case GREATER_THAN:
				value = getValue(criterion, metadata, false);
				return generateFrom().where(metadata).isGreaterThan(value);
			case BETWEEN:
				value = getValue(criterion, metadata, false);
				endValue = getValue(criterion, metadata, true);
				return generateFrom().where(metadata).isValueInRange(value, endValue);
			case IS_TRUE:
				return generateFrom().where(metadata).isTrue();
			case IS_FALSE:
				return generateFrom().where(metadata).isFalseOrNull();
			case IN_HIERARCHY:
				return generateFrom().where(Schemas.PATH).isContainingText("/" + criterion.getValue() + "/");
			case IS_NULL:
				return generateFrom().where(metadata).isNull();
			case IS_NOT_NULL:
				return generateFrom().where(metadata).isNotNull();
			default:
				throw new RuntimeException("Unsupported search operator");
		}
	}

	private OngoingLogicalSearchCondition generateFrom() {
		if (StringUtils.isBlank(schemaCode)) {
			return from(schemaType);
		}

		return from(schemaType.getSchema(schemaCode));
	}

	private Object getValue(Criterion criterion, Metadata metadata, boolean isEndValue) {
		Object value;
		if (metadata.getType().equals(MetadataValueType.DATE_TIME) || metadata.getType().equals(MetadataValueType.DATE)) {
			value = getDateValue(criterion, isEndValue);
		} else {
			if (isEndValue) {
				value = criterion.getEndValue();
			} else {
				value = criterion.getValue();
			}
		}
		return value;
	}

	private LocalDate getDateValue(Criterion criterion, boolean isEndValue) {
		LocalDate dateValue = null;
		LocalDate now = TimeProvider.getLocalDate();
		RelativeSearchOperator relativeSearchOperator;
		MeasuringUnitTime measuringUnitTime;
		Object value;
		if (isEndValue) {
			relativeSearchOperator = criterion.getRelativeCriteria().getEndRelativeSearchOperator();
			measuringUnitTime = criterion.getRelativeCriteria().getEndMeasuringUnitTime();
			value = criterion.getEndValue();
		} else {
			relativeSearchOperator = criterion.getRelativeCriteria().getRelativeSearchOperator();
			measuringUnitTime = criterion.getRelativeCriteria().getMeasuringUnitTime();
			value = criterion.getValue();
		}

		if (relativeSearchOperator == RelativeSearchOperator.TODAY) {
			dateValue = now;
		} else if (relativeSearchOperator == RelativeSearchOperator.EQUALS) {
			if (value instanceof LocalDateTime) {
				dateValue = ((LocalDateTime) value).toLocalDate();
			} else {
				dateValue = (LocalDate) value;
			}
		} else if (relativeSearchOperator == RelativeSearchOperator.PAST) {
			int intValue = ((Double) value).intValue();
			if (measuringUnitTime == MeasuringUnitTime.DAYS) {
				dateValue = now.minusDays(intValue);
			} else if (measuringUnitTime == MeasuringUnitTime.WEEKS) {
				dateValue = now.minusWeeks(intValue);
			} else if (measuringUnitTime == MeasuringUnitTime.MONTHS) {
				dateValue = now.minusMonths(intValue);
			} else if (measuringUnitTime == MeasuringUnitTime.YEARS) {
				dateValue = now.minusYears(intValue);
			}
		} else if (relativeSearchOperator == RelativeSearchOperator.FUTURE) {
			int intValue = ((Double) value).intValue();
			if (measuringUnitTime == MeasuringUnitTime.DAYS) {
				dateValue = now.plusDays(intValue);
			} else if (measuringUnitTime == MeasuringUnitTime.WEEKS) {
				dateValue = now.plusWeeks(intValue);
			} else if (measuringUnitTime == MeasuringUnitTime.MONTHS) {
				dateValue = now.plusMonths(intValue);
			} else if (measuringUnitTime == MeasuringUnitTime.YEARS) {
				dateValue = now.plusYears(intValue);
			}
		}
		return dateValue;
	}
}
