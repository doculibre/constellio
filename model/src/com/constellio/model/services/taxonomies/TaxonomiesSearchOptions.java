package com.constellio.model.services.taxonomies;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;

public class TaxonomiesSearchOptions {

	ReturnedMetadatasFilter returnedMetadatasFilter = ReturnedMetadatasFilter.idVersionSchemaTitlePath();
	private int rows = 100;
	private int startRow = 0;
	private StatusFilter includeStatus = StatusFilter.ACTIVES;

	//Only supported in the "visible" services
	private boolean alwaysReturnTaxonomyConceptsWithReadAccess = false;
	private String requiredAccess = Role.READ;
	private HasChildrenFlagCalculated hasChildrenFlagCalculated = HasChildrenFlagCalculated.ALWAYS;
	private boolean showInvisibleRecordsInLinkingMode = true;
	private boolean showAllIfHasAccessToManageSecurity = true;
	private FastContinueInfos fastContinueInfos;
	private TaxonomiesSearchFilter filter = new TaxonomiesSearchFilter();
	private boolean linkableFlagCalculated = true;

	public TaxonomiesSearchOptions() {
		super();
	}

	public TaxonomiesSearchOptions(int rows, int startRow, StatusFilter includeStatus) {
		super();
		this.rows = rows;
		this.startRow = startRow;
		setIncludeStatus(includeStatus);
	}

	public TaxonomiesSearchOptions(TaxonomiesSearchOptions cloned) {
		super();
		this.hasChildrenFlagCalculated = cloned.hasChildrenFlagCalculated;
		this.linkableFlagCalculated = cloned.linkableFlagCalculated;
		this.alwaysReturnTaxonomyConceptsWithReadAccess = cloned.alwaysReturnTaxonomyConceptsWithReadAccess;
		this.rows = cloned.rows;
		this.startRow = cloned.startRow;
		this.includeStatus = cloned.includeStatus;
		this.requiredAccess = cloned.requiredAccess;
		this.returnedMetadatasFilter = cloned.returnedMetadatasFilter;
		this.showInvisibleRecordsInLinkingMode = cloned.showInvisibleRecordsInLinkingMode;
		this.fastContinueInfos = cloned.fastContinueInfos;
		this.filter = cloned.filter;
	}

	public TaxonomiesSearchOptions(StatusFilter includeLogicallyDeleted) {
		super();
		this.includeStatus = includeLogicallyDeleted;
	}

	public boolean isLinkableFlagCalculated() {
		return linkableFlagCalculated;
	}

	public TaxonomiesSearchOptions setLinkableFlagCalculated(boolean linkableFlagCalculated) {
		this.linkableFlagCalculated = linkableFlagCalculated;
		return this;
	}

	public FastContinueInfos getFastContinueInfos() {
		return fastContinueInfos;
	}

	public TaxonomiesSearchOptions setFastContinueInfos(
			FastContinueInfos fastContinueInfos) {
		this.fastContinueInfos = fastContinueInfos;
		return this;
	}

	public boolean isShowInvisibleRecordsInLinkingMode() {
		return showInvisibleRecordsInLinkingMode;
	}

	public TaxonomiesSearchOptions setShowInvisibleRecordsInLinkingMode(boolean showInvisibleRecordsInLinkingMode) {
		this.showInvisibleRecordsInLinkingMode = showInvisibleRecordsInLinkingMode;
		return this;
	}

	public HasChildrenFlagCalculated getHasChildrenFlagCalculated() {
		return hasChildrenFlagCalculated;
	}

	public TaxonomiesSearchOptions setHasChildrenFlagCalculated(HasChildrenFlagCalculated hasChildrenFlagCalculated) {
		this.hasChildrenFlagCalculated = hasChildrenFlagCalculated;
		return this;
	}

	public int getRows() {
		return rows;
	}

	public TaxonomiesSearchOptions setRows(int rows) {
		if (rows > 10000) {
			throw new IllegalArgumentException("Rows cannot be higher than 10000");
		}
		this.rows = rows;
		return this;
	}

	public int getStartRow() {
		return startRow;
	}

	public int getEndRow() {
		return startRow + rows;
	}

	public TaxonomiesSearchOptions setStartRow(int startRow) {
		this.startRow = startRow;
		return this;
	}

	public String getRequiredAccess() {
		return requiredAccess;
	}

	public TaxonomiesSearchOptions setRequiredAccess(String requiredAccess) {
		this.requiredAccess = requiredAccess;
		return this;
	}

	public StatusFilter getIncludeStatus() {
		return includeStatus;
	}

	public TaxonomiesSearchOptions setIncludeStatus(StatusFilter includeStatus) {
		if (includeStatus == null) {
			this.includeStatus = StatusFilter.ALL;
		} else {
			this.includeStatus = includeStatus;
		}
		return this;
	}

	public ReturnedMetadatasFilter getReturnedMetadatasFilter() {
		return returnedMetadatasFilter;
	}

	public TaxonomiesSearchOptions setReturnedMetadatasFilter(ReturnedMetadatasFilter returnedMetadatasFilter) {
		this.returnedMetadatasFilter = returnedMetadatasFilter;
		return this;
	}

	public boolean isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable() {
		return alwaysReturnTaxonomyConceptsWithReadAccess;
	}

	public TaxonomiesSearchOptions setAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable(
			boolean alwaysReturnTaxonomyConceptsWithReadAccess) {
		this.alwaysReturnTaxonomyConceptsWithReadAccess = alwaysReturnTaxonomyConceptsWithReadAccess;
		return this;
	}

	public TaxonomiesSearchOptions cloneAddingReturnedField(Metadata metadata) {
		TaxonomiesSearchOptions clonedOptions = new TaxonomiesSearchOptions(this);
		clonedOptions.setReturnedMetadatasFilter(returnedMetadatasFilter.withIncludedMetadata(metadata));
		return clonedOptions;

	}

	public TaxonomiesSearchOptions(ReturnedMetadatasFilter returnedMetadatasFilter) {
		this.returnedMetadatasFilter = returnedMetadatasFilter;
	}

	public TaxonomiesSearchFilter getFilter() {
		return filter;
	}

	public TaxonomiesSearchOptions setFilter(TaxonomiesSearchFilter filter) {
		this.filter = filter;
		return this;
	}

	public boolean isShowAllIfHasAccessToManageSecurity() {
		return showAllIfHasAccessToManageSecurity;
	}

	public TaxonomiesSearchOptions setShowAllIfHasAccessToManageSecurity(boolean showAllIfHasAccessToManageSecurity) {
		this.showAllIfHasAccessToManageSecurity = showAllIfHasAccessToManageSecurity;
		return this;
	}

	public enum HasChildrenFlagCalculated {NEVER, CONCEPTS_ONLY, ALWAYS}
}
