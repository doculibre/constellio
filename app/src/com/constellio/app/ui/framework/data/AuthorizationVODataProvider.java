/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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

public class AuthorizationVODataProvider implements DataProvider {

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
