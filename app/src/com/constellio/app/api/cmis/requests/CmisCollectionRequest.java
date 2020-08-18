package com.constellio.app.api.cmis.requests;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_Runtime;
import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_UnsupportedOperation;
import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.api.cmis.builders.object.AllowableActionsBuilder;
import com.constellio.app.api.cmis.builders.object.ContentObjectDataBuilder;
import com.constellio.app.api.cmis.builders.object.ObjectDataBuilder;
import com.constellio.app.api.cmis.builders.object.TaxonomyObjectBuilder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.icap.IcapException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class CmisCollectionRequest<T> {

	private static boolean LOG_CMIS = false;

	static {
		try {
			Map<String, String> constellioProperties = PropertyFileUtils.loadKeyValues(new FoldersLocator().getConstellioProperties());
			String logRequest = constellioProperties.get("cmis.logRequests");
			LOG_CMIS = Boolean.parseBoolean(logRequest);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	protected final ConstellioCollectionRepository repository;
	protected final ModelLayerFactory modelLayerFactory;
	protected final AppLayerFactory appLayerFactory;
	protected final IOServices ioServices;
	protected final RecordServices recordServices;
	protected final SearchServices searchServices;
	protected final TaxonomiesSearchServices taxonomiesSearchServices;
	protected final TaxonomiesManager taxonomiesManager;
	protected final ContentManager contentManager;
	protected final CallContext callContext;
	protected final User user;
	protected final String collection;
	protected final AllowableActionsBuilder allowableActionsBuilder;

	public CmisCollectionRequest(CallContext callContext, ConstellioCollectionRepository repository,
								 AppLayerFactory appLayerFactory) {
		this.repository = repository;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.callContext = callContext;
		this.user = (User) callContext.get(ConstellioCmisContextParameters.USER);
		this.collection = (String) callContext.get(ConstellioCmisContextParameters.COLLECTION);
		this.recordServices = modelLayerFactory.newRecordServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.contentManager = modelLayerFactory.getContentManager();
		this.ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		this.allowableActionsBuilder = new AllowableActionsBuilder(repository, appLayerFactory, callContext);
	}

	public ObjectDataBuilder newObjectDataBuilder() {
		return new ObjectDataBuilder(repository, appLayerFactory, callContext);
	}

	public ContentObjectDataBuilder newContentObjectDataBuilder() {
		return new ContentObjectDataBuilder(repository, appLayerFactory, callContext);
	}

	public TaxonomyObjectBuilder newTaxonomyObjectBuilder() {
		return new TaxonomyObjectBuilder(callContext);
	}

	protected MetadataSchemaTypes types() {
		return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
	}

	public final T processRequest() {
		Logger logger = getLogger();

		try {
			T response = process();
			if (LOG_CMIS) {
				String requestString = toString().replace("com.constellio.app.api.cmis.requests.", "");
				logger.info("Logging cmis request ' " + requestString + "'");
			}
			return response;

		} catch (UnsupportedOperationException e) {
			throw new CmisExceptions_UnsupportedOperation();

		} catch (ConstellioCmisException e) {
			String requestString = toString().replace("com.constellio.app.api.cmis.requests.", "");
			logger.error("Constellio exception while calling cmis request ' " + requestString + "'", e);
			throw new CmisExceptions_Runtime(e.getMessage());

		} catch (Throwable t) {
			String requestString = toString().replace("com.constellio.app.api.cmis.requests.", "");
			logger.error("Unepected exception while calling cmis request ' " + requestString + "'", t);

			throw new CmisExceptions_Runtime(t.getMessage());
		}
	}

	protected abstract T process()
			throws ConstellioCmisException;

	protected abstract Logger getLogger();

	protected Set<Action> getAllowableActionsOn(Record record) {
		return allowableActionsBuilder.build(record).getAllowableActions();
	}

	protected void ensureUserHasAllowableActionsOnRecord(Record record, Action... actions) {
		Set<Action> recordAllowableActions = getAllowableActionsOn(record);
		for (Action action : actions) {
			if (!recordAllowableActions.contains(action)) {
				throw new CmisPermissionDeniedException($("CmisCollectionRequest_forbiddenAction",
						user.getUsername(), action.name(), record.getId(), record.getTitle()));
			}
		}
	}

	protected void ensureUserHasReadAccessToRecordOrADescendantOf(Record record) {
		if (!user.hasReadAccess().on(record)) {
			Taxonomy taxonomy = taxonomiesManager.getTaxonomyOf(record);
			if (taxonomy == null || taxonomy.hasSameCode(taxonomiesManager.getPrincipalTaxonomy(record.getCollection()))) {
				TaxonomiesSearchOptions options = new TaxonomiesSearchOptions().setRows(1)
						.setAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable(true);
				if (taxonomiesSearchServices.getVisibleChildConcept(user, record, options).isEmpty()) {
					throw new CmisPermissionDeniedException($("CmisCollectionRequest_noReadAccess",
							user.getUsername(), record.getId(), record.getTitle()));
				}
			}
		}
	}

	protected void ensureUserHasWriteAccessOnRecord(Record record) {
		if (!user.hasWriteAccess().on(record)) {
			throw new CmisPermissionDeniedException($("CmisCollectionRequest_noWriteAccess",
					user.getUsername(), record.getId(), record.getTitle()));
		}
	}

	protected ContentManager.ContentVersionDataSummaryResponse uploadContent(final InputStream inputStream,
																			 final String fileName) {

		UploadOptions options = new UploadOptions(fileName).setHandleDeletionOfUnreferencedHashes(false);
		try {
			return modelLayerFactory.getContentManager().upload(inputStream, options);
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
}
