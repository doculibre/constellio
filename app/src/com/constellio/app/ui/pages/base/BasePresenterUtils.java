package com.constellio.app.ui.pages.base;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.icap.IcapException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.security.roles.Roles;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class BasePresenterUtils implements Serializable {

	protected SessionContext sessionContext;

	private transient User currentUser;

	private transient ConstellioFactories constellioFactories;
	private transient RecordServices recordServices;
	private transient PresenterService presenterService;
	private transient SearchServices searchServices;
	private transient SchemasDisplayManager schemasDisplayManager;

	public BasePresenterUtils(ConstellioFactories constellioFactories, SessionContext sessionContext) {
		this.constellioFactories = constellioFactories;
		this.sessionContext = sessionContext;
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		if (constellioFactories == null) {
			constellioFactories = ConstellioFactories.getInstance();
		}
	}

	public final String getCollection() {
		return sessionContext.getCurrentCollection();
	}

	public final ConstellioFactories getConstellioFactories() {
		return constellioFactories;
	}

	public final MetadataSchemaTypes types() {
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory().getMetadataSchemasManager();
		return metadataSchemasManager.getSchemaTypes(sessionContext.getCurrentCollection());
	}

	public final MetadataSchemaType schemaType(String code) {
		return types().getSchemaType(code);
	}

	public final MetadataSchema schema(String code) {
		return types().getSchema(code);
	}

	public final boolean hasSchema(String code) {
		return types().hasSchema(code);
	}

	public final User getCurrentUser() {
		if (currentUser == null) {
			currentUser = presenterService().getCurrentUser(sessionContext);
		}
		return currentUser;
	}

	public final void clearCurrentUserCache() {
		currentUser = null;
	}

	public final Locale getCurrentLocale() {
		return sessionContext.getCurrentLocale();
	}

	public final ModelLayerFactory modelLayerFactory() {
		return constellioFactories.getModelLayerFactory();
	}

	public final AppLayerFactory appLayerFactory() {
		return constellioFactories.getAppLayerFactory();
	}

	public final RecordServices recordServices() {
		//if (recordServices == null) {
		return modelLayerFactory().newRecordServices();
		//}
		//return recordServices;
	}

	public final LoggingServices loggingServices() {
		return modelLayerFactory().newLoggingServices();
	}

	public final PresenterService presenterService() {
		if (presenterService == null) {
			presenterService = appLayerFactory().newPresenterService();
		}
		return presenterService;
	}

	public final SearchServices searchServices() {
		if (searchServices == null) {
			searchServices = modelLayerFactory().newSearchServices();
		}
		return searchServices;
	}

	public final SchemasDisplayManager schemasDisplayManager() {
		if (schemasDisplayManager == null) {
			schemasDisplayManager = appLayerFactory().getMetadataSchemasDisplayManager();
		}
		return schemasDisplayManager;
	}

	public final String buildString(List<String> list) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			stringBuilder.append(list.get(i));
			if (i != list.size() - 1) {
				stringBuilder.append("<br/>");
			}
		}
		return stringBuilder.toString();
	}

	public final List<String> getAllRecordIds(String schemaCode) {
		String collection = sessionContext.getCurrentCollection();
		SearchServices searchServices = modelLayerFactory().newSearchServices();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(collection);
		MetadataSchema schema = schemaTypes.getSchema(schemaCode);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(LogicalSearchQueryOperators.from(schema).returnAll());
		return searchServices.searchRecordIds(query);
	}

	public User wrapUser(Record record) {
		return new User(record, types(), getCollectionRoles());
	}

	public Roles getCollectionRoles() {
		String collection = getCollection();
		return modelLayerFactory().getRolesManager().getCollectionRoles(collection);
	}

	public CollectionsManager getCollectionManager() {
		return appLayerFactory().getCollectionsManager();
	}

	public ContentManager.ContentVersionDataSummaryResponse uploadContent(final InputStream inputStream,
																		  UploadOptions options) {
		try {
			return modelLayerFactory().getContentManager().upload(inputStream, options);
		} catch (final IcapException e) {
			if (e instanceof IcapException.ThreatFoundException) {
				throw new IcapException($(e, e.getFileName(), ((IcapException.ThreatFoundException) e).getThreatName()));
			}

			if (e.getCause() == null) {
				throw new IcapException($(e, e.getFileName()));
			} else {
				throw new IcapException($(e, e.getFileName()), e.getCause());
			}
		}
	}

	public List<String> getConceptsWithPermissionsForCurrentUser(String... permissions) {
		User user = getCurrentUser();
		return presenterService.getConceptsWithPermissionsForUser(user, permissions);
	}

}
