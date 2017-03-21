package com.constellio.app.ui.framework.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.framework.builders.AuthorizationToVOBuilder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;

public class AuthorizationVODataProvider extends AbstractDataProvider {

	transient RecordServices recordServices;

	transient AuthorizationsServices authorizationsServices;

	transient List<AuthorizationVO> authorizationVOs;

	AuthorizationToVOBuilder voBuilder;

	String recordId;

	transient Integer size;

	public AuthorizationVODataProvider(AuthorizationToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory, String recordId) {
		this.voBuilder = voBuilder;
		init(modelLayerFactory, recordId);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {		
		stream.defaultReadObject();
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getModelLayerFactory(), recordId);
	}

	void init(ModelLayerFactory modelLayerFactory, String recordId) {
		authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		recordServices = modelLayerFactory.newRecordServices();
		authorizationVOs = listAuthorizationVOs(recordId);
	}

	public List<AuthorizationVO> listAuthorizationVOs(String recordId) {
		authorizationVOs = new ArrayList<>();

		Record record = recordServices.getDocumentById(recordId);
		List<Authorization> authorizations = authorizationsServices.getRecordAuthorizations(record);

		for (Authorization authorization : authorizations) {
			AuthorizationVO authorizationVO = voBuilder.build(authorization);
			authorizationVOs.add(authorizationVO);
		}
		return authorizationVOs;
	}

	public AuthorizationVO getAuthorizationVO(Integer index) {
		AuthorizationVO authorizationVO = authorizationVOs.get(index);
		return authorizationVO != null ? authorizationVO : null;
	}

	public int size() {
		if (size == null) {
			size = authorizationVOs.size();
		}
		return size;
	}

	public List<Integer> list() {
		List<Integer> indexes = new ArrayList<>();
		for (int i = 0; i < authorizationVOs.size(); i++) {
			AuthorizationVO authorizationVO = authorizationVOs.get(i);
			if (authorizationVO != null) {
				indexes.add(i);
			}
		}
		return indexes;
	}
}
