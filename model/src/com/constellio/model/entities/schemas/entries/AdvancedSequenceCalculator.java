package com.constellio.model.entities.schemas.entries;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.schemas.Metadata;

import java.util.List;

public interface AdvancedSequenceCalculator {

	String INHERITED_FROM_PARENT = "INHERITED_FROM_PARENT";

	/**
	 * Declare all dependencies that could be used in following methods.
	 * Only local and reference dependencies are supported
	 */
	List<? extends Dependency> getDependencies();


	//Not yet supported
	//default boolean isSequenceTableDecrementedWhenDeletingLastValue() {
	//	return false;
	//}

	/**
	 * Used to regroup sequence tables
	 */
	default String getSequenceGroupName(Metadata metadata) {
		return metadata.getSchemaTypeCode() + "-" + metadata.getLocalCode();
	}

	/**
	 * Compute a sequence table id based on the parameter values which is used to assign a sequence value
	 * Ex. "categoryCode + createdOn.year" would means that a sequence table is created per category per year
	 * <p>
	 * This method may return null, which means no sequence value
	 */
	String computeSequenceTableId(CalculatorParameters parameters);

	/**
	 * Build a decorated value using the already assigned sequence value
	 * Ex. categoryCode + "-" + createdOn.year + "-" + sequenceValue;
	 */
	String computeSequenceTableValue(CalculatorParameters parameters, int sequenceValue);


}
