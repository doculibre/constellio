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
package com.constellio.app.api.admin.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.constellio.app.client.entities.CollectionResource;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.collections.CollectionsManager;

@Path("collections")
@Consumes("application/xml")
@Produces("application/xml")
public class CollectionServicesAPI {

	@POST
	@Path("createCollection")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String createCollection(CollectionResource resource) {
		collectionsServices().createCollectionInCurrentVersion(resource.getCollection(), resource.getLanguages());
		return "Ok";
	}

	private CollectionsManager collectionsServices() {
		return appServicesFactory().getCollectionsManager();
	}

	private AppLayerFactory appServicesFactory() {
		return AdminServicesUtils.appServicesFactory();
	}

}
