package com.constellio.app.modules.restapi.certification.adaptor;

import com.constellio.app.modules.restapi.certification.dao.CertificationDao;
import com.constellio.app.modules.restapi.certification.dto.CertificationDto;
import com.constellio.app.modules.restapi.certification.dto.RectangleDto;
import com.constellio.app.modules.restapi.resource.adaptor.ResourceAdaptor;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.pdf.signature.PdfSignatureAnnotation;

import javax.inject.Inject;
import java.util.Set;

public class CertificationAdaptor extends ResourceAdaptor<CertificationDto> {

	@Inject
	private CertificationDao certificationDao;

	public CertificationDto adapt(PdfSignatureAnnotation annotation, String documentId) {

		CertificationDto resource = CertificationDto.builder().build();

		resource.setDocumentId(documentId);
		resource.setImageData(annotation.getImageData());
		resource.setUserId(annotation.getUserId());
		resource.setUsername(annotation.getUsername());

		RectangleDto rectangleDto = RectangleDto.builder().build();
		rectangleDto.setX(annotation.getPosition().getX());
		rectangleDto.setY(annotation.getPosition().getY());
		rectangleDto.setHeight(annotation.getPosition().getHeight());
		rectangleDto.setWidth(annotation.getPosition().getWidth());

		resource.setPosition(rectangleDto);

		return resource;
	}

	@Override
	public CertificationDto adapt(CertificationDto resource, Record record, MetadataSchema schema, boolean modified,
								  Set<String> filters) {
		return null;
	}
}
