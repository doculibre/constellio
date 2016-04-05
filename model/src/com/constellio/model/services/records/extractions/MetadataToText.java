package com.constellio.model.services.records.extractions;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Majid
 */
@XmlRootElement
public class MetadataToText extends ExtractorSupplier<String> {
	public static boolean LOG_CONTENT_MISSING = true;
	private static final Logger LOGGER = LoggerFactory.getLogger(MetadataToText.class);

	private LoadingCache<String, ParsedContent> cachedParsedContentProvider;
	private Metadata inputMetadata;

	@XmlElement
	private String metadataCode;

	public MetadataToText(String metadataCode) {
		this.metadataCode = metadataCode;
	}

	public MetadataToText() {
	}

	@Override
	public void init(LoadingCache<String, ParsedContent> cachedParsedContentProvider, MetadataSchema schema) {
		this.cachedParsedContentProvider = cachedParsedContentProvider;
		this.inputMetadata = schema.get(metadataCode);
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
						ParsedContent parsedContent = cachedParsedContentProvider.get(content.getCurrentVersion().getHash());
						res = Collections.singletonList(parsedContent.getParsedContent());
					} catch (Exception e) {
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

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		MetadataToText rhs = (MetadataToText) obj;
		return new EqualsBuilder()
				.append(metadataCode, rhs.metadataCode)
				.isEquals();

	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
