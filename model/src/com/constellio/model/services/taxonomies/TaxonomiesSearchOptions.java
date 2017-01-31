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
	private boolean hasChildrenFlagCalculated = true;

	private int previousBatchLastVisibleIndex;

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
		this.hasChildrenFlagCalculated = hasChildrenFlagCalculated;
		this.rows = cloned.rows;
		this.startRow = cloned.startRow;
		this.includeStatus = cloned.includeStatus;
		this.returnedMetadatasFilter = cloned.returnedMetadatasFilter;
	}

	public TaxonomiesSearchOptions(StatusFilter includeLogicallyDeleted) {
		super();
		this.includeStatus = includeLogicallyDeleted;
	}

	public boolean isHasChildrenFlagCalculated() {
		return hasChildrenFlagCalculated;
	}

	public TaxonomiesSearchOptions setHasChildrenFlagCalculated(boolean hasChildrenFlagCalculated) {
		this.hasChildrenFlagCalculated = hasChildrenFlagCalculated;
		return this;
	}

	public int getRows() {
		return rows;
	}

	public TaxonomiesSearchOptions setRows(int rows) {
		this.rows = rows;
		return this;
	}

	public int getStartRow() {
		return startRow;
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

	public void setIncludeStatus(StatusFilter includeStatus) {
		if (includeStatus == null) {
			this.includeStatus = StatusFilter.ALL;
		} else {
			this.includeStatus = includeStatus;
		}
	}

	public ReturnedMetadatasFilter getReturnedMetadatasFilter() {
		return returnedMetadatasFilter;
	}

	public TaxonomiesSearchOptions setReturnedMetadatasFilter(ReturnedMetadatasFilter returnedMetadatasFilter) {
		this.returnedMetadatasFilter = returnedMetadatasFilter;
		return this;
	}

	public boolean isAlwaysReturnTaxonomyConceptsWithReadAccess() {
		return alwaysReturnTaxonomyConceptsWithReadAccess;
	}

	public TaxonomiesSearchOptions setAlwaysReturnTaxonomyConceptsWithReadAccess(
			boolean alwaysReturnTaxonomyConceptsWithReadAccess) {
		this.alwaysReturnTaxonomyConceptsWithReadAccess = alwaysReturnTaxonomyConceptsWithReadAccess;
		return this;
	}

	public TaxonomiesSearchOptions cloneAddingReturnedField(Metadata metadata) {
		TaxonomiesSearchOptions clonedOptions = new TaxonomiesSearchOptions(this);
		clonedOptions.setReturnedMetadatasFilter(returnedMetadatasFilter.withIncludedMetadata(metadata));
		clonedOptions.setIncludeStatus(includeStatus);
		clonedOptions.setRequiredAccess(requiredAccess);
		clonedOptions.setRows(rows);
		clonedOptions.setStartRow(startRow);
		clonedOptions.setAlwaysReturnTaxonomyConceptsWithReadAccess(alwaysReturnTaxonomyConceptsWithReadAccess);
		clonedOptions.setPreviousBatchLastVisibleIndex(previousBatchLastVisibleIndex);
		return clonedOptions;

	}

	public int getPreviousBatchLastVisibleIndex() {
		return previousBatchLastVisibleIndex;
	}

	/**
	 * Taxonomy services are basically iterating over a query and then filtering nodes based on some criteria.
	 * Rows and startRow parameter are the range of filtered records, it gives no clue on the index in the main iterator.
	 *
	 * The facultative 'previousBatchLastVisibleIndex' is the index of the last visible node, it helps accelerate services
	 * This information is available in taxonomy search response object
	 */
	public TaxonomiesSearchOptions setPreviousBatchLastVisibleIndex(int previousBatchLastVisibleIndex) {
		this.previousBatchLastVisibleIndex = previousBatchLastVisibleIndex;
		return this;
	}
}
