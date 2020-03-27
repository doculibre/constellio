package com.constellio.app.ui.framework.data;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SessionContextProvider;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.List;
import java.util.Map;

public class ExternalLinkRecordVODataProvider extends RecordVODataProvider {

	private RecordVO recordVO;

	public ExternalLinkRecordVODataProvider(MetadataSchemaVO schema, RecordToVOBuilder voBuilder,
											SessionContextProvider sessionContextProvider) {
		super(schema, voBuilder, sessionContextProvider);
	}

	public ExternalLinkRecordVODataProvider(MetadataSchemaVO schema, RecordToVOBuilder voBuilder,
											ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
		super(schema, voBuilder, modelLayerFactory, sessionContext);
	}

	public ExternalLinkRecordVODataProvider(List<MetadataSchemaVO> schemas, Map<String, RecordToVOBuilder> voBuilders,
											ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
		super(schemas, voBuilders, modelLayerFactory, sessionContext);
	}

	@Override
	protected void initializeQuery() {
		// No Solr querying
	}

	@Override
	protected boolean isSearchCache() {
		return false;
	}

	@Override
	public LogicalSearchQuery getQuery() {
		// No Solr querying
		return null;
	}

	@Override
	public RecordVO getRecordVO(int index) {
		// TODO Auto-generated method stub
		return super.getRecordVO(index);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return super.size();
	}

	@Override
	public SearchResponseIterator<Record> getIterator() {
		throw new UnsupportedOperationException("Not implemented for external links.");
	}

	@Override
	public List<RecordVO> listRecordVOs(int startIndex, int numberOfItems) {
		// TODO Auto-generated method stub
		return super.listRecordVOs(startIndex, numberOfItems);
	}

	@Override
	public void sort(MetadataVO[] propertyId, boolean[] ascending) {
		// No Solr querying
	}

	@Override
	public int getQTime() {
		// No Solr querying
		return 0;
	}

}
