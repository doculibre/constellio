package com.constellio.app.ui.framework.data;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.ArrayList;
import java.util.List;

public class SearchEventVODataProvider extends AbstractDataProvider {
	private SessionContext sessionContext;
	private ModelLayerFactory modelLayerFactory;
	private RecordToVOBuilder voBuilder;
	private MetadataSchemaVO schemaVO;
	private LogicalSearchQuery query;

    public SearchEventVODataProvider(MetadataSchemaVO schemaVO, SessionContext sessionContext, ModelLayerFactory modelLayerFactory, LogicalSearchQuery query) {
        this.schemaVO = schemaVO;
        this.sessionContext = sessionContext;
        this.modelLayerFactory = modelLayerFactory;
        this.voBuilder = new RecordToVOBuilder();
        this.query = query;
    }

    public SearchEventVODataProvider(MetadataSchemaVO schemaVO, LogicalSearchQuery query) {
        this(schemaVO, ConstellioUI.getCurrent().getSessionContext(), ConstellioFactories.getInstance().getModelLayerFactory(), query);
    }

    private SPEQueryResponse prepareQuery(int startRow, int numberOfRows) {
        query.setStartRow(startRow).setNumberOfRows(numberOfRows);
        return modelLayerFactory.newSearchServices().query(query);
    }

	public List<RecordVO> listRecordVOs(int startIndex, int numberOfItems) {
		List<RecordVO> recordVOs = new ArrayList<>();
		List<Record> recordList = prepareQuery(startIndex, numberOfItems).getRecords();
		for (int i = 0; i < recordList.size(); i++) {
			recordVOs.add(voBuilder.build(recordList.get(i), RecordVO.VIEW_MODE.TABLE, sessionContext));
		}
		return recordVOs;
	}

	public long size() {
		return prepareQuery(0, 1).getNumFound();
	}

	public MetadataSchemaVO getSchema() {
		return schemaVO;
	}

	public LogicalSearchQuery getQuery() {
		return query;
	}
}
