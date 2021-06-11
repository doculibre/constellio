package com.constellio.app.modules.rm.services.reports.printable;

import com.constellio.app.modules.rm.services.reports.xml.XMLDataSourceType;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Builder
@Data
public class PrintableGeneratorParams {
	private XMLDataSourceType XMLDataSourceType;
	private int numberOfCopies;
	private Integer startingPosition;
	private String printableId;
	private PrintableExtension printableExtension;
	private String schemaType;
	private List<String> recordIds;
	private LogicalSearchQuery query;
	private Locale locale;
	private String username;
	private Set<String> requiredMetadataCodes;
	private Integer depth;
	private boolean ignoreReferences;
}
