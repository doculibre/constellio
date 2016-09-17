package com.constellio.app.services.schemas.bulkImport;

import static com.constellio.data.utils.LangUtils.replacingLiteral;
import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.io.File.separator;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.ImportServices;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.LangUtils.StringReplacer;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;

public class RecordsImportServices implements ImportServices {

	private ModelLayerFactory modelLayerFactory;
	private MetadataSchemasManager schemasManager;
	private RecordServices recordServices;
	private ContentManager contentManager;
	private IOServices ioServices;
	private SearchServices searchServices;
	private int batchSize;
	private URLResolver urlResolver;

	public RecordsImportServices(ModelLayerFactory modelLayerFactory) {
		this(modelLayerFactory, new DefaultURLResolver(modelLayerFactory.getIOServicesFactory().newIOServices()));
	}

	public RecordsImportServices(ModelLayerFactory modelLayerFactory, URLResolver urlResolver) {
		this.modelLayerFactory = modelLayerFactory;
		this.batchSize = batchSize;
		schemasManager = modelLayerFactory.getMetadataSchemasManager();
		recordServices = modelLayerFactory.newRecordServices();
		searchServices = modelLayerFactory.newSearchServices();
		contentManager = modelLayerFactory.getContentManager();
		ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		this.urlResolver = urlResolver;
	}

	@Override
	public BulkImportResults bulkImport(ImportDataProvider importDataProvider,
			final BulkImportProgressionListener bulkImportProgressionListener,
			final User user, List<String> collections)
			throws RecordsImportServicesRuntimeException, ValidationException {
		return bulkImport(importDataProvider, bulkImportProgressionListener, user, collections, new BulkImportParams());
	}

	public BulkImportResults bulkImport(ImportDataProvider importDataProvider,
			final BulkImportProgressionListener bulkImportProgressionListener,
			final User user, List<String> collections, BulkImportParams params)
			throws RecordsImportServicesRuntimeException, ValidationException {

		return newExecutor(importDataProvider, bulkImportProgressionListener, user, collections, params).bulkImport();
	}

	RecordsImportServicesExecutor newExecutor(ImportDataProvider importDataProvider,
			BulkImportProgressionListener bulkImportProgressionListener, User user, List<String> collections,
			BulkImportParams params)
			throws ValidationException {
		URLResolver urlResolver = new DefaultURLResolver(modelLayerFactory.getIOServicesFactory().newIOServices());
		return new RecordsImportServicesExecutor(modelLayerFactory, urlResolver, importDataProvider,
				bulkImportProgressionListener, user, collections, params);
	}

	public BulkImportResults bulkImport(ImportDataProvider importDataProvider,
			final BulkImportProgressionListener bulkImportProgressionListener, final User user)
			throws RecordsImportServicesRuntimeException, ValidationException {
		return bulkImport(importDataProvider, bulkImportProgressionListener, user, new ArrayList<String>());
	}

	public BulkImportResults bulkImport(ImportDataProvider importDataProvider,
			final BulkImportProgressionListener bulkImportProgressionListener, final User user,
			final BulkImportParams params)
			throws RecordsImportServicesRuntimeException, ValidationException {
		return bulkImport(importDataProvider, bulkImportProgressionListener, user, new ArrayList<String>(), params);
	}

}
