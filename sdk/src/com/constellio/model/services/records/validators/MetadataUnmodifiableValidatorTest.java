package com.constellio.model.services.records.validators;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.validators.MetadataUnmodifiableValidator;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

public class MetadataUnmodifiableValidatorTest extends ConstellioTest {

	public static final String UNMODIFIABLE_METADATA =
			MetadataUnmodifiableValidator.class.getName() + "_modifiedUnmodifiableMetadata";

	@Mock Metadata metadata;

	@Mock Record record;

	MetadataUnmodifiableValidator validator;

	ValidationErrors validationErrors;

	@Before
	public void setUp() {
		List<Metadata> metadatas = new ArrayList<>();
		metadatas.add(metadata);

		validator = new MetadataUnmodifiableValidator(metadatas, false);

		validationErrors = new ValidationErrors();
	}

}
