/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.management.schemas.display.report;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.SchemaUtils;

import java.util.Arrays;
import java.util.List;

public class AllowedMetadataUtil {
    public static boolean isAllowedMetadata(MetadataVO metadataVO) {
        boolean result;
        List<Metadata> restrictedMetadata = Arrays.asList(Schemas.SCHEMA, Schemas.VERSION, Schemas.PATH, Schemas.PRINCIPAL_PATH,
                Schemas.PARENT_PATH, Schemas.AUTHORIZATIONS, Schemas.REMOVED_AUTHORIZATIONS, Schemas.INHERITED_AUTHORIZATIONS,
                Schemas.ALL_AUTHORIZATIONS, Schemas.IS_DETACHED_AUTHORIZATIONS, Schemas.TOKENS, Schemas.COLLECTION,
                Schemas.FOLLOWERS, Schemas.LOGICALLY_DELETED_STATUS, Schemas.TITLE);

        List<MetadataValueType> restrictedType = Arrays.asList(MetadataValueType.STRUCTURE, MetadataValueType.CONTENT);

        List<String> localCodes = new SchemaUtils().toMetadataLocalCodes(restrictedMetadata);

        result = !metadataVO.isMultivalue();
        result = result && !restrictedType.contains(metadataVO.getType());
        result = result && !localCodes.contains(metadataVO.getCode());//getLocalcode()

        return result;
    }
}
