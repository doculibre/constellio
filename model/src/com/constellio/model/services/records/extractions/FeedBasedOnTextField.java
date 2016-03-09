package com.constellio.model.services.records.extractions;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.contents.ContentManagerRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Majid
 */
class FeedBasedOnTextField implements FeedsExtractor<String>{
	public static boolean LOG_CONTENT_MISSING = true;
	private static final Logger LOGGER = LoggerFactory.getLogger(FeedBasedOnTextField.class);

	private Metadata inputMetadata;
	private ParsedContentProvider parsedContentProvider;
	public FeedBasedOnTextField(Metadata inputMetadata, ParsedContentProvider parsedContentProvider) {
		this.inputMetadata = inputMetadata;
	}

	@Override
	public Collection<String> getFeeds(Record record) {
		List<String> res = Collections.emptyList();
		if (inputMetadata.isMultivalue()) {
			res = record.getList(inputMetadata);
		} else {
			Object value = record.get(inputMetadata);
			if (value != null) {
				if (inputMetadata.getType().equals(MetadataValueType.CONTENT)) {
					Content content = (Content) value;
					try {
						ParsedContent parsedContent = parsedContentProvider.getParsedContentParsingIfNotYetDone(
								content.getCurrentVersion().getHash());
						res = Collections.singletonList(parsedContent.getParsedContent());
					} catch (ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent e) {
						if (LOG_CONTENT_MISSING) {
							LOGGER.error("No content " + content.getCurrentVersion().getHash());
						}
					}

				} else if (inputMetadata.getType().isStringOrText()) {
					res = Collections.singletonList((String) value);
				}
			}
		}
		return res;
	}
}
