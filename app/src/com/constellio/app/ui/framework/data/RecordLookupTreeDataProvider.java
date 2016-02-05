package com.constellio.app.ui.framework.data;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;
import static com.constellio.model.services.search.query.ReturnedMetadatasFilter.idVersionSchemaTitlePath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.LookupTreeDataProvider;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.TextInputDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.constellio.model.services.users.UserServices;

public class RecordLookupTreeDataProvider implements LookupTreeDataProvider<String> {
	private String taxonomyCode;
	private String schemaTypeCode;
	private Map<String, String> parentCache = new HashMap<>();
	private Map<String, Boolean> selectableCache = new HashMap<>();
	private boolean ignoreLinkability;
	private boolean writeAccess;
	private Map<String, RecordDataTreeNode> nodesCache = new HashMap<>();

	public RecordLookupTreeDataProvider(String schemaTypeCode, String taxonomyCode, boolean writeAccess) {
		this.writeAccess = writeAccess;
		this.schemaTypeCode = schemaTypeCode;
		this.taxonomyCode = taxonomyCode;
		ignoreLinkability = false;
	}

	@Override
	public final String getTaxonomyCode() {
		return taxonomyCode;
	}

	public RecordDataTreeNode getNode(String id) {
		return nodesCache.get(id);
	}

	@Override
	public String getCaption(String id) {
		return getNode(id).getCaption();
	}

	@Override
	public String getDescription(String id) {
		return getNode(id).getDescription();
	}

	@Override
	public ObjectsResponse<String> getRootObjects(int start, int maxSize) {
		ModelLayerFactory modelLayerFactory = getInstance().getModelLayerFactory();
		User currentUser = getCurrentUser(modelLayerFactory);

		TaxonomiesSearchOptions taxonomiesSearchOptions = newTaxonomiesSearchOptions(maxSize, start);
		LinkableTaxonomySearchResponse response = modelLayerFactory.newTaxonomiesSearchService().getLinkableRootConceptResponse(
				currentUser, currentUser.getCollection(), taxonomyCode, schemaTypeCode, taxonomiesSearchOptions);

		List<String> recordIds = new ArrayList<>();
		for (TaxonomySearchRecord searchRecord : response.getRecords()) {
			recordIds.add(saveResultInCacheAndReturnId(searchRecord));
		}
		return new ObjectsResponse<>(recordIds, response.getNumFound());
	}

	private String saveResultInCacheAndReturnId(TaxonomySearchRecord searchRecord) {
		RecordDataTreeNode treeNode = toTreeNode(searchRecord);
		boolean selectable = ignoreLinkability || searchRecord.isLinkable();

		nodesCache.put(searchRecord.getId(), treeNode);
		selectableCache.put(searchRecord.getId(), selectable);
		return searchRecord.getId();
	}

	private User getCurrentUser(ModelLayerFactory modelLayerFactory) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		String currentCollection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();
		UserServices userServices = modelLayerFactory.newUserServices();

		return userServices.getUserInCollection(currentUserVO.getUsername(), currentCollection);
	}

	private Record getRecord(ModelLayerFactory modelLayerFactory, String id) {
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		return recordServices.getDocumentById(id);
	}

	private RecordDataTreeNode toTreeNode(TaxonomySearchRecord searchRecord) {
		Record record = searchRecord.getRecord();
		String schemaType = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		String caption = SchemaCaptionUtils.getCaptionForRecord(record);
		String description = record.get(Schemas.DESCRIPTION_STRING);
		if (description == null) {
			description = record.get(Schemas.DESCRIPTION_TEXT);
		}

		return new RecordDataTreeNode(searchRecord.getId(), caption, description, schemaType, searchRecord.hasChildren());
	}

	@Override
	public String getParent(String child) {
		return parentCache.get(child);
	}

	@Override
	public ObjectsResponse<String> getChildren(String parent, int start, int maxSize) {
		ModelLayerFactory modelLayerFactory = getInstance().getModelLayerFactory();

		User currentUser = getCurrentUser(modelLayerFactory);
		Record record = getRecord(modelLayerFactory, parent);

		TaxonomiesSearchOptions taxonomiesSearchOptions = newTaxonomiesSearchOptions(maxSize, start);
		LinkableTaxonomySearchResponse response = modelLayerFactory.newTaxonomiesSearchService().getLinkableChildConceptResponse(
				currentUser, record, taxonomyCode, schemaTypeCode, taxonomiesSearchOptions);

		List<String> recordIds = new ArrayList<>();
		for (TaxonomySearchRecord searchRecord : response.getRecords()) {
			recordIds.add(saveResultInCacheAndReturnId(searchRecord));
			parentCache.put(searchRecord.getId(), parent);
		}

		return new ObjectsResponse<>(recordIds, response.getNumFound());
	}

	@Override
	public boolean hasChildren(String parent) {
		return getNode(parent).hasChildren();
	}

	@Override
	public boolean isLeaf(String parent) {
		return !getNode(parent).hasChildren();
	}

	@Override
	public boolean isSelectable(String selection) {
		return selectableCache.get(selection);
	}

	@Override
	public TextInputDataProvider<String> search() {
		return new RecordTextInputDataProvider(getInstance(), getCurrentSessionContext(), schemaTypeCode, writeAccess);
	}

	private TaxonomiesSearchOptions newTaxonomiesSearchOptions(int rows, int startRow) {
		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions(rows, startRow, StatusFilter.ACTIVES)
				.setReturnedMetadatasFilter(idVersionSchemaTitlePath().withIncludedMetadata(Schemas.CODE)
						.withIncludedMetadata(Schemas.DESCRIPTION_TEXT).withIncludedMetadata(Schemas.DESCRIPTION_STRING));
		if (writeAccess) {
			options.setRequiredAccess(Role.WRITE);
		}
		return options;
	}

	public void setIgnoreLinkability(boolean ignoreLinkability) {
		this.ignoreLinkability = ignoreLinkability;
	}
}
