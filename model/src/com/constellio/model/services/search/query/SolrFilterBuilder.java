package com.constellio.model.services.search.query;

import com.constellio.model.entities.schemas.Metadata;

class SolrFilterBuilder {

	static final int MAXIMUM_LOGICAL_QUERY_LEVEL = 7;

	StringBuilder stringBuilder;

	int currentLevel;
	boolean[] levelHasAtLeastOneCondition = new boolean[MAXIMUM_LOGICAL_QUERY_LEVEL];
	boolean[] levelIsAndOperation = new boolean[MAXIMUM_LOGICAL_QUERY_LEVEL];
	Boolean[] levelDefaultValue = new Boolean[MAXIMUM_LOGICAL_QUERY_LEVEL];

	private SolrFilterBuilder(boolean andOperation, boolean rootDefaultValue) {
		stringBuilder = new StringBuilder();

		currentLevel = 0;
		levelHasAtLeastOneCondition[currentLevel] = false;
		levelIsAndOperation[currentLevel] = andOperation;
		levelDefaultValue[currentLevel] = rootDefaultValue;
	}

	public static SolrFilterBuilder createAndFilterReturningTrueIfEmpty() {
		return new SolrFilterBuilder(true, true);
	}

	public static SolrFilterBuilder createAndFilterReturningFalseIfEmpty() {
		return new SolrFilterBuilder(true, false);
	}

	public static SolrFilterBuilder createOrFilterReturningTrueIfEmpty() {
		return new SolrFilterBuilder(false, true);
	}

	public static SolrFilterBuilder createOrFilterReturningFalseIfEmpty() {
		return new SolrFilterBuilder(false, false);
	}

	public SolrFilterBuilder openANDGroupReturningTrueIfEmpty() {
		currentLevel++;
		levelHasAtLeastOneCondition[currentLevel] = false;
		levelIsAndOperation[currentLevel] = true;
		levelDefaultValue[currentLevel] = true;

		return this;
	}

	public SolrFilterBuilder openANDGroupReturningFalseIfEmpty() {
		currentLevel++;
		levelHasAtLeastOneCondition[currentLevel] = false;
		levelIsAndOperation[currentLevel] = true;
		levelDefaultValue[currentLevel] = false;
		return this;
	}

	public SolrFilterBuilder openANDGroupRemovedIfEmpty() {
		currentLevel++;
		levelHasAtLeastOneCondition[currentLevel] = false;
		levelIsAndOperation[currentLevel] = true;
		levelDefaultValue[currentLevel] = null;
		return this;
	}

	public SolrFilterBuilder openORGroupReturningTrueIfEmpty() {
		currentLevel++;
		levelHasAtLeastOneCondition[currentLevel] = false;
		levelIsAndOperation[currentLevel] = false;
		levelDefaultValue[currentLevel] = true;
		return this;
	}

	public SolrFilterBuilder openORGroupReturningFalseIfEmpty() {
		currentLevel++;
		levelHasAtLeastOneCondition[currentLevel] = false;
		levelIsAndOperation[currentLevel] = false;
		levelDefaultValue[currentLevel] = false;
		return this;
	}

	public SolrFilterBuilder openORGroupRemovedIfEmpty() {
		currentLevel++;
		levelHasAtLeastOneCondition[currentLevel] = false;
		levelIsAndOperation[currentLevel] = false;
		levelDefaultValue[currentLevel] = null;
		return this;
	}

	public SolrFilterBuilder closeGroup() {

		if (levelHasAtLeastOneCondition[currentLevel]) {
			stringBuilder.append(")");
			levelHasAtLeastOneCondition[currentLevel - 1] = true;

		} else {

			if (Boolean.FALSE.equals(levelDefaultValue[currentLevel])) {
				addOperationIfNecessary();
				stringBuilder.append("collection_s:_A38_");
				levelHasAtLeastOneCondition[currentLevel - 1] = true;
				stringBuilder.append(")");
			}

			if (Boolean.TRUE.equals(levelDefaultValue[currentLevel])) {
				addOperationIfNecessary();
				stringBuilder.append("*:*");
				levelHasAtLeastOneCondition[currentLevel - 1] = true;
				stringBuilder.append(")");
			}
		}

		currentLevel--;
		return this;
	}

	private boolean requiresOperation() {
		return levelHasAtLeastOneCondition[currentLevel];
	}


	public void append(String field, String condition) {
		addOperationIfNecessary();

		stringBuilder.append(field);
		stringBuilder.append(":");
		stringBuilder.append(condition);
		levelHasAtLeastOneCondition[currentLevel] = true;
	}

	public void appendNegative(String field, String condition) {
		addOperationIfNecessary();

		stringBuilder.append("-");
		stringBuilder.append(field);
		stringBuilder.append(":");
		stringBuilder.append(condition);
		levelHasAtLeastOneCondition[currentLevel] = true;
	}

	protected void addOperationIfNecessary() {
		if (!levelHasAtLeastOneCondition[currentLevel]) {
			String toAppend = "";
			for (int i = currentLevel; i >= 0; i--) {
				if (!levelHasAtLeastOneCondition[i]) {
					if (i > 0) {
						toAppend = "(" + toAppend;
					}
					levelHasAtLeastOneCondition[i] = true;
				} else {
					toAppend = (levelIsAndOperation[i] ? " AND " : " OR ") + toAppend;
					break;
				}
			}
			stringBuilder.append(toAppend);
		} else {
			stringBuilder.append(levelIsAndOperation[currentLevel] ? " AND " : " OR ");
		}
	}


	public void append(Metadata field, String condition) {
		append(field.getDataStoreCode(), condition);
	}

	public void appendNegative(Metadata field, String condition) {
		appendNegative(field.getDataStoreCode(), condition);
	}


	public String build() {
		if (stringBuilder.length() == 0) {
			if (levelDefaultValue[currentLevel]) {
				return "*:*";
			} else {
				return "collection_s:_A38_";
			}
		}
		return stringBuilder.toString();
	}

	public String toString() {
		return build();
	}

	public boolean isAlwaysTrue() {
		return stringBuilder.length() == 0 && levelDefaultValue[currentLevel];
	}
}
