package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.model.entities.calculators.CalculatorLogger;

public class CopyRetentionRuleCalculatorLogger implements CalculatorLogger {

	CalculatorLogger nested;
	CopyRetentionRule copyRetentionRule;

	public CopyRetentionRuleCalculatorLogger(CalculatorLogger nested, CopyRetentionRule copyRetentionRule) {
		this.nested = nested;
		this.copyRetentionRule = copyRetentionRule;
	}

	@Override
	public boolean isTroubleshooting() {
		return nested.isTroubleshooting();
	}

	@Override
	public void log(String message) {
		nested.log("Délai '" + copyRetentionRule.toString() + "' - " + message);
	}
}
