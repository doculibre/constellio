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
package com.constellio.model.services.schemas.validators;

import static com.constellio.model.entities.records.Record.PUBLIC_TOKEN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class ManualTokenValidator implements RecordMetadataValidator<List<String>> {

	@Override
	public void validate(Metadata metadata, List<String> tokens, ConfigProvider configProvider,
			ValidationErrors validationErrors) {

		if (tokens != null) {
			for (String token : tokens) {
				if (token != null && !token.equals(PUBLIC_TOKEN)) {
					if (!token.startsWith("r") && !token.startsWith("w") && !token.startsWith("d")) {
						Map<String, String> parameters = new HashMap<>();
						parameters.put("invalidToken", token);
						validationErrors.add(getClass(), "tokenMustStartWith_R_W_OR_D", parameters);
					}
				}
			}
		}
	}
}
