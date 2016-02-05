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
