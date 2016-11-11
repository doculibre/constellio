package com.constellio.app.api.extensions.taxonomies;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderParams;
import org.joda.time.LocalDateTime;

import java.util.List;

/**
 * Created by Constelio on 2016-10-19.
 */
public class FolderDeletionEvent {
	Folder folder;

	public FolderDeletionEvent(Folder folder) {
		this.folder = folder;
	}

	public Folder getFolder() {
		return this.folder;
	}
}
