package de.unileipzig.dbs.pprl.core.matcher.classification.model;

import java.io.Serializable;
import java.util.Vector;

/***
 * Stores features and their associated label.
 */
public class InstanceCommon implements Serializable {
	private String label;
	private Vector<Double> features;
	private Double probability;

	public InstanceCommon(Vector<Double> features) {
		this(features, null, null);
	}

	public InstanceCommon(Vector<Double> features, String label, Double probability) {
		this.label = label;
		this.features = features;
		this.probability = probability;
	}

	public Boolean isLabeled() {
		return label != null;
	}

	public InstanceCommon getUnlabeledCopy() {
		return new InstanceCommon(this.getFeatures());
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Vector<Double> getFeatures() {
		return features;
	}

	public void setFeatures(Vector<Double> features) {
		this.features = features;
	}

	public Double getProbability() {
		return probability;
	}

	public void setProbability(Double probability) {
		this.probability = probability;
	}

	public InstanceCommon duplicate() {
		return new InstanceCommon(new Vector<>(features), label, probability);
	}
}
