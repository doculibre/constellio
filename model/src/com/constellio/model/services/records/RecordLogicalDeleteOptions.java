package com.constellio.model.services.records;

public class RecordLogicalDeleteOptions {

	PrincipalConceptDeleteBehavior principalConceptDeleteBehavior = PrincipalConceptDeleteBehavior.KEEP_RECORDS_IN_HIERARCHY;

	public PrincipalConceptDeleteBehavior getPrincipalConceptDeleteBehavior() {
		return principalConceptDeleteBehavior;
	}

	public RecordLogicalDeleteOptions setPrincipalConceptDeleteBehavior(
			PrincipalConceptDeleteBehavior principalConceptDeleteBehavior) {
		this.principalConceptDeleteBehavior = principalConceptDeleteBehavior;
		return this;
	}

	public enum PrincipalConceptDeleteBehavior {KEEP_RECORDS_IN_HIERARCHY, LOGICALLY_DELETE_ALL_RECORDS_HIERARCHY}

}
