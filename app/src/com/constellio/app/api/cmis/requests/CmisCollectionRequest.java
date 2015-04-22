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
package com.constellio.app.api.cmis.requests;

import org.slf4j.Logger;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_Runtime;
import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.builders.object.ContentObjectDataBuilder;
import com.constellio.app.api.cmis.builders.object.ObjectDataBuilder;
import com.constellio.app.api.cmis.builders.object.TaxonomyObjectBuilder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactory;

public abstract class CmisCollectionRequest<T> {

	protected final ConstellioCollectionRepository repository;
	protected final ModelLayerFactory modelLayerFactory;
	protected final AppLayerFactory appLayerFactory;

	public CmisCollectionRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory) {
		this.repository = repository;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
	}

	public ObjectDataBuilder newObjectDataBuilder() {
		return new ObjectDataBuilder(repository, modelLayerFactory);
	}

	public ContentObjectDataBuilder newContentObjectDataBuilder() {
		return new ContentObjectDataBuilder(repository);
	}

	public TaxonomyObjectBuilder newTaxonomyObjectBuilder() {
		return new TaxonomyObjectBuilder();
	}

	public final T processRequest() {
		Logger logger = getLogger();

		logger.info("calling cmis request " + toString().replace("com.constellio.app.api.cmis.requests.", ""));

		try {
			T response = process();
			logger.info("Response : " + response);
			return response;
		} catch (ConstellioCmisException e) {
			logger.error("Constellio exception in " + this, e);
			throw new CmisExceptions_Runtime(e.getMessage());

		} catch (Throwable t) {
			logger.error("Unexpected exception in " + this, t);

			throw new CmisExceptions_Runtime(t.getMessage());
		}
	}

	protected abstract T process()
			throws ConstellioCmisException;

	protected abstract Logger getLogger();
}
