package com.constellio.app.api.admin.services;

import com.constellio.app.client.entities.CollectionResource;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.factories.AppLayerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("collections")
@Consumes("application/xml")
@Produces("application/xml")
public class CollectionServicesAPI {

	@POST
	@Path("createCollection")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String createCollection(CollectionResource resource) {
		return "Ok";
	}

	private CollectionsManager collectionsServices() {
		return appServicesFactory().getCollectionsManager();
	}

	private AppLayerFactory appServicesFactory() {
		return AdminServicesUtils.appServicesFactory();
	}

}
