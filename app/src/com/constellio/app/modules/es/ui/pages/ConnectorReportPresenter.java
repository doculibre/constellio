package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.navigation.ESViews;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVOWithDistinctSchemasDataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

public class ConnectorReportPresenter extends BasePresenter<ConnectorReportView> {

	private String connectorId;
	private String reportMode;

	private transient ESSchemasRecordsServices es;
	private ConnectorInstance connectorInstance;
	private Connector connector;

	public ConnectorReportPresenter(ConnectorReportView view) {
		super(view);
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		es = new ESSchemasRecordsServices(collection, appLayerFactory);

	}

	public void forParams(String parameters) {
		Map<String, String> params = ParamUtils.getParamsMap(parameters);
		connectorId = params.get(ConnectorReportView.CONNECTOR_ID);
		reportMode = params.get(ConnectorReportView.REPORT_MODE);
		connectorInstance = es.getConnectorManager().getConnectorInstance(connectorId);
		connector = es.instanciate(connectorInstance);

		if (ConnectorReportView.ERRORS.equals(reportMode)) {
			view.setTitle($("ConnectorReportView.viewTitle.error"));
		} else {
			view.setTitle($("ConnectorReportView.viewTitle.indexing"));
		}
	}

	public RecordVOWithDistinctSchemasDataProvider getDataProvider() {
		final List<MetadataSchemaType> types = getMetadataSchemaTypes();
		final List<String> reportMetadata = connector.getReportMetadatas(reportMode);

		List<MetadataSchemaVO> schemaVOs = new ArrayList<>();
		for (MetadataSchemaType type : types) {
			MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
					.build(type.getSchema(connectorId), VIEW_MODE.TABLE, reportMetadata, view.getSessionContext());
			schemaVOs.add(schemaVO);
		}

		return new RecordVOWithDistinctSchemasDataProvider(schemaVOs, new RecordToVOBuilder(), modelLayerFactory,
				view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				LogicalSearchCondition condition = from(types).where(es.connectorDocument.connector()).isEqualTo(
						connectorId);
				if (ConnectorReportView.ERRORS.equals(reportMode)) {
					condition = condition.andWhere(es.connectorDocument.errorsCount()).isGreaterOrEqualThan(1);
				}
				return new LogicalSearchQuery(condition);
			}
		};
	}

	public List<String> getReportMetadataList() {
		return connector.getReportMetadatas(reportMode);
	}

	public RecordVOWithDistinctSchemasDataProvider getFilteredDataProvider(final String filterString) {
		final List<MetadataSchemaType> types = getMetadataSchemaTypes();
		final List<String> reportMetadata = connector.getReportMetadatas(reportMode);
		final List<MetadataSchemaVO> schemaVOs = new ArrayList<>();
		for (MetadataSchemaType type : types) {
			MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
					.build(type.getSchema(connectorId), VIEW_MODE.TABLE, view.getSessionContext());
			schemaVOs.add(schemaVO);
		}

		return new RecordVOWithDistinctSchemasDataProvider(schemaVOs, new RecordToVOBuilder(), modelLayerFactory,
				view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				LogicalSearchCondition condition = where(es.connectorDocument.connector()).isEqualTo(connectorId);
				if (ConnectorReportView.ERRORS.equals(reportMode)) {
					condition = condition.andWhere(es.connectorDocument.errorsCount()).isGreaterOrEqualThan(1);
				}

				List<LogicalSearchCondition> filterConditions = new ArrayList<>();
				List<Metadata> fileterMetadatas = new ArrayList<>();
				for (String metadataCode : reportMetadata) {
					for (MetadataSchemaVO schemaVO : schemaVOs) {
						MetadataVO metadataVO = schemaVO.getMetadata(metadataCode);
						Metadata metadata = types().getMetadata(metadataVO.getCode());
						if (STRING.equals(metadata.getType()) || TEXT
								.equals(metadata.getType())) {
							fileterMetadatas.add(metadata);
						}
					}
				}

				LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(condition);
				if (StringUtils.isNotBlank(filterString)) {
					for (Metadata fileterMetadata : fileterMetadatas) {
						filterConditions.add(where(fileterMetadata).isContainingText(filterString));
					}
					return logicalSearchQuery.setCondition(from(types)
							.whereAllConditions(condition, anyConditions(filterConditions)));
				} else {
					return logicalSearchQuery.setCondition(from(types)
							.where(condition));
				}
			}
		};
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public Long getTotalDocumentsCount() {

		final List<MetadataSchemaType> types = getMetadataSchemaTypes();


		LogicalSearchCondition condition = from(types).returnAll();
		condition = condition.andWhere(es.connectorDocument.connector()).isEqualTo(connectorId);
		if (ConnectorReportView.ERRORS.equals(reportMode)) {
			condition = condition.andWhere(es.connectorDocument.errorsCount()).isGreaterOrEqualThan(1);
		}
		return modelLayerFactory.newSearchServices().getResultsCount(condition);

	}

	private List<MetadataSchemaType> getMetadataSchemaTypes() {
		List<String> typeCodes = connector.getConnectorDocumentTypes();
		return types().getSchemaTypesWithCode(typeCodes);
	}

	public Long getFetchedDocumentsCount() {

		final List<MetadataSchemaType> types = getMetadataSchemaTypes();

		LogicalSearchCondition condition = from(types).where(es.connectorDocument.fetched()).isTrue();
		condition = condition.andWhere(es.connectorDocument.connector()).isEqualTo(connectorId);
		if (ConnectorReportView.ERRORS.equals(reportMode)) {
			condition = condition.andWhere(es.connectorDocument.errorsCount()).isGreaterOrEqualThan(1);
		}
		return modelLayerFactory.newSearchServices().getResultsCount(condition);
	}

	public Long getUnfetchedDocumentsCount() {

		final List<MetadataSchemaType> types = getMetadataSchemaTypes();

		LogicalSearchCondition condition = from(types).where(es.connectorDocument.fetched()).isFalse();
		condition = condition.andWhere(es.connectorDocument.connector()).isEqualTo(connectorId);
		if (ConnectorReportView.ERRORS.equals(reportMode)) {
			condition = condition.andWhere(es.connectorDocument.errorsCount()).isGreaterOrEqualThan(1);
		}
		return modelLayerFactory.newSearchServices().getResultsCount(condition);
	}

	public void filterButtonClicked() {
		view.filterTable();
	}

	public void backButtonClicked() {
		view.navigate().to(ESViews.class).displayConnectorInstance(connectorId);
	}
}
