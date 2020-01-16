package com.constellio.app.api.cmis.requests.object;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.extensions.api.cmis.params.DeleteTreeParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class DeleteTreeRequest extends CmisCollectionRequest<FailedToDeleteData> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final String folderId;
	private final Boolean continueOnFailure;

	public DeleteTreeRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
							 CallContext context, String folderId, Boolean continueOnFailure) {
		super(context, repository, appLayerFactory);
		this.folderId = folderId;
		this.continueOnFailure = continueOnFailure;
	}

	@Override
	public FailedToDeleteData process() {
		Record record = recordServices.getDocumentById(folderId, user);
		ensureUserHasAllowableActionsOnRecord(record, Action.CAN_DELETE_TREE);

		DeleteTreeParams params = new DeleteTreeParams(user, record);
		recordServices.logicallyDelete(record, user);

		try {
			recordServices.physicallyDelete(record, user);

		} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord e) {
			recordServices.restore(record, user);
			throw new RuntimeException(e);
		}

		FailedToDeleteDataImpl result = new FailedToDeleteDataImpl();
		result.setIds(new ArrayList<String>());

		return result;
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
