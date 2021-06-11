package com.constellio.app.servlet;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonRootName("Error")
public class BaseServletErrorResponse {
	private int code;
	private String description;
	private String message;
}
