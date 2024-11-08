package de.unileipzig.dbs.pprl.core.matcher.classification.model;

import java.util.Vector;

public class InstanceBinary extends InstanceCommon {
	public final static String TRUE = "true";
	public final static String FALSE = "false";

	public InstanceBinary(Vector<Double> features) {
		super(features);
	}

	public InstanceBinary(Vector<Double> features, Boolean label, Double probability) {
		super(features, label?TRUE:FALSE, probability);
	}

	public Boolean isTrue() {
//		assert isLabeled();
		return TRUE.equals(getLabel());
	}

	@Override
	public InstanceBinary duplicate() {
		return new InstanceBinary(new Vector<>(getFeatures()), isTrue(), getProbability());
	}
}
