package com.constellio.app.api.cmis.requests.discovery;

import java.math.BigInteger;
import java.util.regex.Pattern;

import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;

public class QueryUnsupportedRequest extends CmisCollectionRequest<ObjectList> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private static final Pattern IN_FOLDER_QUERY_PATTERN = Pattern
			.compile("(?i)select\\s+.+\\s+from\\s+(\\S*).*\\s+where\\s+in_folder\\('(.*)'\\)");
	private final CallContext callContext;
	private final String statement;
	private final boolean includeAllowableActions;
	private final BigInteger maxItems;
	private final BigInteger skipCount;
	private final ObjectInfoHandler objectInfos;

	public QueryUnsupportedRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext callContext,
			String statement, Boolean includeAllowableActions, BigInteger maxItems, BigInteger skipCount,
			ObjectInfoHandler objectInfos) {
		super(repository, appLayerFactory);
		this.callContext = callContext;
		this.statement = statement;
		this.includeAllowableActions = includeAllowableActions;
		this.maxItems = maxItems;
		this.skipCount = skipCount;
		this.objectInfos = objectInfos;
	}

	/**
	 * CMIS query (simple IN_FOLDER queries only)
	 */
	@Override
	public ObjectList process() {

		throw new UnsupportedOperationException("Query is not supported");
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
