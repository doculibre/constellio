package com.constellio.app.modules.rm.model.calculators;

import com.constellio.model.entities.calculators.MetadataValueCalculator;
import org.joda.time.LocalDate;

import java.util.List;

public class FolderCopyRulesExpectedDestructionDatesCalculator extends DummyListDateCalculator
		implements MetadataValueCalculator<List<LocalDate>> {
}
