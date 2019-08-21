package com.constellio.app.modules.restapi.document.dto;

import com.constellio.app.modules.restapi.resource.dto.AceDto;
import com.constellio.app.modules.restapi.resource.dto.ExtendedAttributeDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import javax.validation.Valid;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Data
@Builder
@JsonRootName("Document")
public class DocumentDto {
	private String id;
	private String folderId;
	@Valid
	private DocumentTypeDto type;
	@Valid
	private ContentDto content;
	private String title;
	private List<String> keywords;
	private String author;
	private String subject;
	private String organization;
	@Valid
	private List<AceDto> directAces;
	@Valid @JsonProperty(access = READ_ONLY)
	private List<AceDto> inheritedAces;
	@Valid
	private List<ExtendedAttributeDto> extendedAttributes;
	@JsonIgnore @Getter(onMethod = @__(@JsonIgnore))
	private String eTag;
}
