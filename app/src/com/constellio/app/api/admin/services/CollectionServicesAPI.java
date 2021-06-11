package com.constellio.app.api.admin.services;

import com.constellio.app.client.entities.CollectionResource;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.collections.exceptions.NoMoreCollectionAvalibleException;
import com.constellio.model.services.extensions.ConstellioModulesManagerException.ConstellioModulesManagerException_ModuleInstallationFailed;

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
		try {
			collectionsServices().createCollection(resource.getCollection(), resource.getLanguages());
			return "Ok";
		} catch (NoMoreCollectionAvalibleException noMoreCollectionAvalibleException) {
			noMoreCollectionAvalibleException.printStackTrace();
			return "Not created, maximum number of collection is reached";
		} catch (ConstellioModulesManagerException_ModuleInstallationFailed constellioModulesManagerException_moduleInstallationFailed) {
			throw new RuntimeException(constellioModulesManagerException_moduleInstallationFailed);
		}

	}

	private CollectionsManager collectionsServices() {
		return appServicesFactory().getCollectionsManager();
	}

	private AppLayerFactory appServicesFactory() {
		return AdminServicesUtils.appServicesFactory();
	}

}
