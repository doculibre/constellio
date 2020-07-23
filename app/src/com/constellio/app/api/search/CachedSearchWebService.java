package com.constellio.app.api.search;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CachedSearchWebService extends AbstractSearchServlet {

	private static final String OPTIONAL_COLLECTION_PARAM = "collection";
	private static final String SCHEMA_TYPE_PARAM = "schemaType";
	private static final String FL_PARAM = "fl";

	private static final String MISSING_PARAMS_ERROR = "The request cannot be completed as some parameters are missing. Please specify a schema type (schemaType) and returned fields (fl)";
	private static final String INVALID_SCHEMA_TYPE_ERROR = "Invalid schemaType. Could not be found";
	private static final String UNSUPPORTED_SCHEMA_TYPE_ERROR = "Unsupported schemaType. Must be fully cached and unsecured";

	@Override
	protected void doGet(SystemWideUserInfos user, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String collectionParam = req.getParameter(OPTIONAL_COLLECTION_PARAM);
		String schemaTypeCode = req.getParameter(SCHEMA_TYPE_PARAM);
		String[] fl = StringUtils.split(req.getParameter(FL_PARAM));

		if (!ObjectUtils.allNotNull(schemaTypeCode, fl)) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, MISSING_PARAMS_ERROR);
			return;
		}


		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		List<String> collectionsContainingSchemaType = metadataSchemasManager.getAllCollectionsSchemaTypes().stream()
				.filter(collectionSchemaTypes -> StringUtils.isBlank(collectionParam) ? collectionSchemaTypes.hasType(schemaTypeCode) : collectionParam.equals(collectionSchemaTypes.getCollection()) && collectionSchemaTypes.hasType(schemaTypeCode))
				.map(collectionSchemaTypes -> collectionSchemaTypes.getCollection())
				.collect(Collectors.toList());
		metadataSchemasManager.getAllCollectionsSchemaTypes().stream().forEach(collectionSchemaTypes -> collectionSchemaTypes.hasType(schemaTypeCode));
		if (collectionsContainingSchemaType.isEmpty()) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_SCHEMA_TYPE_ERROR + " " + schemaTypeCode);
			return;
		}

		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collectionsContainingSchemaType.get(0)).getSchemaType(schemaTypeCode);
		if (!(schemaType.getCacheType() == RecordCacheType.FULLY_CACHED && !schemaType.hasSecurity())) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, UNSUPPORTED_SCHEMA_TYPE_ERROR + " " + schemaTypeCode);
			return;
		}

		RecordsCaches recordsCaches = modelLayerFactory.getRecordsCaches();
		List<Record> allSchemaTypeRecords = collectionsContainingSchemaType.stream()
				.map(collection -> recordsCaches.getCache(collection).getAllValues(schemaTypeCode))
				.flatMap(List::stream)
				.collect(Collectors.toList());

		JsonObject responseJson = new JsonObject();
		for (Record record : allSchemaTypeRecords) {
			JsonObject recordJson = new JsonObject();
			Arrays.stream(fl).forEach(metadataLocalCode -> recordJson.addProperty(metadataLocalCode, (String) record.get(metadataSchemasManager.getSchemaOf(record).getMetadata(metadataLocalCode))));
			responseJson.add(record.getId(), recordJson);
		}

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setContentType("application/json; charset=UTF-8");
		resp.getWriter().write(responseJson.toString());
	}
}
