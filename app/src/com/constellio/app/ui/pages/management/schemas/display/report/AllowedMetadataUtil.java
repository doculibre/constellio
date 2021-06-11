package com.constellio.app.ui.pages.management.schemas.display.report;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.SeparatedStructureFactory;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.services.schemas.SchemaUtils;

import java.util.Arrays;
import java.util.List;

public class AllowedMetadataUtil {
	public static boolean isAllowedMetadata(MetadataVO metadataVO) {
		boolean result;
		List<Metadata> restrictedMetadata = Arrays.asList(Schemas.SCHEMA, Schemas.VERSION, Schemas.PATH, Schemas.PRINCIPAL_PATH,
				Schemas.ATTACHED_ANCESTORS, Schemas.REMOVED_AUTHORIZATIONS,
				Schemas.ALL_REMOVED_AUTHS, Schemas.IS_DETACHED_AUTHORIZATIONS, Schemas.TOKENS, Schemas.COLLECTION,
				Schemas.LOGICALLY_DELETED_STATUS, Schemas.TITLE);

		List<MetadataValueType> restrictedType = Arrays.asList(MetadataValueType.STRUCTURE, MetadataValueType.CONTENT);

		List<Class<? extends StructureFactory>> allowedStructureType = Arrays.asList(SeparatedStructureFactory.class);

		List<String> localCodes = new SchemaUtils().toMetadataLocalCodes(restrictedMetadata);

		result = !restrictedType.contains(metadataVO.getType()) ||
				 allowedStructureType.stream().anyMatch(ast -> ast.isInstance(metadataVO.getStructureFactory()));
		result = result && !localCodes.contains(metadataVO.getCode());//getLocalcode()

		return result;
	}
}
