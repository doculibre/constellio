package com.constellio.app.modules.restapi.document.dto;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;

@Data
@Builder
public class DocumentContentDto {
	private InputStream content;
	private String mimeType;
	private String filename;
}
