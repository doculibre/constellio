package com.constellio.app.api.cmis.requests.navigation;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.api.cmis.binding.utils.CmisUtils;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;

public class GetObjectByPathRequest extends CmisCollectionRequest<ObjectData> {

	private static final String TAXONOMY_PREFIX = "taxo_";

	private static final Logger LOGGER = LoggerFactory.getLogger(GetObjectByPathRequest.class);

	private final String folderPath;
	private final Set<String> filter;
	private final boolean includeAllowableActions;
	private final boolean includeACL;
	private final ObjectInfoHandler objectInfos;

	public GetObjectByPathRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context, String folderPath, String filter, boolean includeAllowableActions, boolean includeACL,
			ObjectInfoHandler objectInfo) {
		super(context, repository, appLayerFactory);
		this.folderPath = folderPath;
		if (filter != null) {
			this.filter = CmisUtils.splitFilter(filter);
		} else {
			this.filter = null;
		}
		this.includeAllowableActions = includeAllowableActions;
		this.includeACL = includeACL;
		this.objectInfos = objectInfo;
	}

	@Override
	public ObjectData process()
			throws ConstellioCmisException {

		if (folderPath.equals("/")) {
			return collectionObjectData();
		} else {
			String[] pathPart = folderPath.split("/");
			String lastPathPart = pathPart[pathPart.length - 1];
			if (lastPathPart.startsWith(TAXONOMY_PREFIX)) {
				return taxoObjectData(lastPathPart.substring(TAXONOMY_PREFIX.length()));
			} else {
				return recordObjectData(lastPathPart);
			}
		}
	}

	private ObjectData recordObjectData(String id) {
		Record record = modelLayerFactory.newRecordServices().getDocumentById(id);
		ensureUserHasReadAccessToRecordOrADescendantOf(record);
		return recordObjectData(record);
	}

	private ObjectData taxoObjectData(String taxonomyCode) {
		Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(collection, taxonomyCode);
		return newTaxonomyObjectBuilder().build(taxonomy, objectInfos);
	}

	private ObjectData collectionObjectData() {
		return recordObjectData(appLayerFactory.getCollectionsManager().getCollection(collection).getWrappedRecord());
	}

	private ObjectData recordObjectData(Record record) {
		return newObjectDataBuilder().build(record, filter, includeAllowableActions, includeACL, objectInfos);
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
