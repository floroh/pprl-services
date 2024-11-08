package de.unileipzig.dbs.pprl.core.matcher.classification.weka;

/**
 * Interface for classifiers that are not trained from scratch with an updated training set.
 * Instead, they reuse the existing model and update it based on the updated data.
 * Hence, the classifier must not be reset before updating.
 */
public interface ShiftingClassifier {
}
