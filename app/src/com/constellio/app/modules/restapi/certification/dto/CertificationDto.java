package com.constellio.app.modules.restapi.certification.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import javax.validation.Valid;

@Data
@Builder
@JsonRootName("Certification")
public class CertificationDto {

	private String id;
	private String documentId;
	private int page;
	private String userId;
	private String username;
	private String imageData;
	@Valid
	private RectangleDto position;
	@JsonIgnore @Getter(onMethod = @__(@JsonIgnore))
	private String eTag;
}
