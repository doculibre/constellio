package com.constellio.app.ui.pages.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.security.AuthorizationsServices;

public class PresenterService {
	private ModelLayerFactory modelLayerFactory;

	public PresenterService(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public User getCurrentUser(SessionContext sessionContext) {
		User currentUser;
		UserVO currentUserVO = sessionContext.getCurrentUser();
		String currentCollection = sessionContext.getCurrentCollection();
		if (currentUserVO != null) {
			currentUser = modelLayerFactory.newUserServices().getUserInCollection(currentUserVO.getUsername(), currentCollection);
		} else {
			currentUser = null;
		}
		return currentUser;
	}

	public RecordVO getRecordVO(String id, VIEW_MODE viewMode, SessionContext sessionContext) {
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		return voBuilder.build(getRecord(id), viewMode, sessionContext);
	}

	public Record getRecord(String id) {
		return modelLayerFactory.newRecordServices().getDocumentById(id);
	}

	public MetadataVO getMetadataVO(String metadataCode, SessionContext sessionContext) {
		if (metadataCode == null) {
			return null;
		}
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager()
				.getSchemaTypes(sessionContext.getCurrentCollection());
		MetadataToVOBuilder builder = new MetadataToVOBuilder();
		Metadata metadata = types.getMetadata(metadataCode);
		return builder.build(metadata, sessionContext);
	}
	
	public ConstellioEIMConfigs getSystemConfigs() {
		return modelLayerFactory.getSystemConfigs();
	}

	public List<String> getConceptsWithPermissionsForUser(User user, String...permissions) {
		Set<String> recordIds = new HashSet<>();
		AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		for (String permission : permissions) {
			recordIds.addAll(authorizationsServices.getConceptsForWhichUserHasPermission(permission, user));
		}
		return new ArrayList<>(recordIds);
	}
	
}

