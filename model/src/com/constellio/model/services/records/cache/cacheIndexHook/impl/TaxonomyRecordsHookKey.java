package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.utils.dev.Toggle;

public class TaxonomyRecordsHookKey {

	private int principalId;
	private int secondaryConceptId;
	private boolean write;
	private boolean visible;

	private TaxonomyRecordsHookKey(RecordId principalId, RecordId secondaryConceptId, boolean write,
								   boolean visible) {
		this.principalId = principalId == null ? 0 : principalId.intValue();
		this.secondaryConceptId = secondaryConceptId == null ? 0 : secondaryConceptId.intValue();
		this.write = write;
		this.visible = visible;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TaxonomyRecordsHookKey)) {
			return false;
		}

		TaxonomyRecordsHookKey that = (TaxonomyRecordsHookKey) o;

		if (principalId != that.principalId) {
			return false;
		}
		if (secondaryConceptId != that.secondaryConceptId) {
			return false;
		}
		if (write != that.write) {
			return false;
		}
		return visible == that.visible;
	}

	@Override
	public int hashCode() {
		int result = principalId;
		result = 31 * result + secondaryConceptId;
		result = 31 * result + (write ? 1 : 0);
		result = 31 * result + (visible ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		return "TaxonomyRecordsHookKey{" +
			   "principalId=" + principalId +
			   ", secondaryConceptId=" + secondaryConceptId +
			   ", write=" + write +
			   ", visible=" + visible +
			   '}';
	}

	public static class TaxonomyRecordsHookKey_Debug extends TaxonomyRecordsHookKey {

		String stringValue;

		private TaxonomyRecordsHookKey_Debug(RecordId principalId, RecordId secondaryConceptId, boolean write,
											 boolean visible) {

			super(principalId, secondaryConceptId, write, visible);
			stringValue = principalId + "-" + secondaryConceptId + "-" + write + "-" + visible;
		}

		@Override
		public String toString() {
			return stringValue;
		}
	}


	public static TaxonomyRecordsHookKey principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(
			RecordId principalConceptId, RecordId secondaryConceptId, boolean visible) {
		if (Toggle.DEBUG_TAXONOMY_RECORDS_HOOK.isEnabled()) {
			return new TaxonomyRecordsHookKey_Debug(principalConceptId, secondaryConceptId, true, visible);
		} else {
			return new TaxonomyRecordsHookKey(principalConceptId, secondaryConceptId, true, visible);
		}
	}

	public static TaxonomyRecordsHookKey principalAccessOnRecordInConcept(
			RecordId principalId, RecordId conceptId, boolean write, boolean visible) {
		if (Toggle.DEBUG_TAXONOMY_RECORDS_HOOK.isEnabled()) {
			return new TaxonomyRecordsHookKey_Debug(principalId, conceptId, write, visible);
		} else {
			return new TaxonomyRecordsHookKey(principalId, conceptId, write, visible);
		}
	}

	public static TaxonomyRecordsHookKey attachedRecordInPrincipalConcept(RecordId conceptId, boolean visible) {
		if (Toggle.DEBUG_TAXONOMY_RECORDS_HOOK.isEnabled()) {
			return new TaxonomyRecordsHookKey_Debug(conceptId, null, true, visible);
		} else {
			return new TaxonomyRecordsHookKey(conceptId, null, true, visible);
		}
	}

	public static TaxonomyRecordsHookKey recordInSecondaryConcept(RecordId conceptId, boolean visible) {
		if (Toggle.DEBUG_TAXONOMY_RECORDS_HOOK.isEnabled()) {
			return new TaxonomyRecordsHookKey_Debug(null, conceptId, true, visible);
		} else {
			return new TaxonomyRecordsHookKey(null, conceptId, true, visible);
		}
	}

}

