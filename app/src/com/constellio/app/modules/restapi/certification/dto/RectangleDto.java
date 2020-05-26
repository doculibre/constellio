package com.constellio.app.modules.restapi.certification.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonRootName("Rectangle")
public class RectangleDto {
	public double x;
	public double y;
	public double width;
	public double height;
}
