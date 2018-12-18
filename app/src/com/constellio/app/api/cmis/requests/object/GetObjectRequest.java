package com.constellio.app.api.cmis.requests.object;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_InvalidArgumentObjectNotSetted;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.utils.CmisContentUtils;
import com.constellio.app.api.cmis.binding.utils.CmisUtils;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.extensions.api.cmis.params.GetObjectParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.constellio.app.api.cmis.requests.navigation.GetChildrenRequest.LEGACY_ROOT_ID;

public class GetObjectRequest extends CmisCollectionRequest<ObjectData> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final String objectId;
	private final String versionRequestsId;
	private final String filter;
	private final Boolean includeAllowableActions;
	private final Boolean includeAcl;
	private final ObjectInfoHandler objectInfos;

	public GetObjectRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
							CallContext context,
							String objectId, String versionRequestsId, String filter, Boolean includeAllowableActions,
							Boolean includeAcl,
							ObjectInfoHandler objectInfos) {
		super(context, repository, appLayerFactory);
		if (objectId == null) {
			// this works only because there are no versions in a file system
			// and the object id and version series id are the same
			this.objectId = versionRequestsId;
		} else {
			this.objectId = objectId;
		}
		this.versionRequestsId = versionRequestsId;
		this.filter = filter;
		this.includeAllowableActions = includeAllowableActions;
		this.includeAcl = includeAcl;
		this.objectInfos = objectInfos;
	}

	@Override
	public ObjectData process() {

		if (objectId == null && versionRequestsId == null) {
			throw new CmisExceptions_InvalidArgumentObjectNotSetted();
		}

		boolean includeAllowableActionsValue = CmisUtils.getBooleanParameter(includeAllowableActions, false);
		boolean includeAclValue = CmisUtils.getBooleanParameter(includeAcl, false);
		Set<String> filterCollection = CmisUtils.splitFilter(filter);

		if (LEGACY_ROOT_ID.equals(objectId) || collection.equals(objectId)) {
			Record collectionRecord = appLayerFactory.getCollectionsManager().getCollection(collection).getWrappedRecord();
			return newObjectDataBuilder()
					.build(collectionRecord, filterCollection, includeAllowableActionsValue, includeAclValue, objectInfos);

		} else if (objectId.startsWith("taxo_")) {
			Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(collection, objectId.split("_")[1]);
			return newTaxonomyObjectBuilder().build(taxonomy, objectInfos, Language.withCode(modelLayerFactory.getCollectionsListManager().getMainDataLanguage()));

		} else if (objectId.startsWith("content_")) {
			ContentCmisDocument content = CmisContentUtils.getContent(objectId, recordServices, types());
			GetObjectParams params = new GetObjectParams(user, content.getRecord());
			appLayerFactory.getExtensions().forCollection(collection).onGetObject(params);

			return newContentObjectDataBuilder()
					.build(content, filterCollection, includeAllowableActionsValue, includeAclValue, objectInfos);

		} else {
			Record record = recordServices.getDocumentById(objectId);
			GetObjectParams params = new GetObjectParams(user, record);
			appLayerFactory.getExtensions().forCollection(collection).onGetObject(params);

			ensureUserHasReadAccessToRecordOrADescendantOf(record);
			return newObjectDataBuilder()
					.build(record, filterCollection, includeAllowableActionsValue, includeAclValue, objectInfos);

		}

	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public String toString() {
		return "getObject(objectId=" + objectId + ", versionRequestsId=" + versionRequestsId + ", filter=" + filter
			   + ", includeAllowableActions=" + includeAllowableActions + ", includeAcl=" + includeAcl + ")";
	}
}
