package com.constellio.app.modules.rm.services.reports.xml;

import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Builder
@Data
public class XMLDataSourceGeneratorParams {
	private XMLDataSourceType xmlDataSourceType;
	private String schemaType;
	private List<String> recordIds;
	private LogicalSearchQuery query;
	private Integer numberOfCopies;
	private Integer startingPosition;
	private String username;
	private Locale locale;
	private Set<String> requiredMetadataCodes;
	private Integer depth;
	private boolean ignoreReferences;
	private boolean isXmlForTest;
}
