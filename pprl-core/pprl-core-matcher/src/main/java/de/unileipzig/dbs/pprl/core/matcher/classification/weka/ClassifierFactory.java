package de.unileipzig.dbs.pprl.core.matcher.classification.weka;

import de.unileipzig.dbs.pprl.core.matcher.classification.model.ClassifierConfig;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.HoeffdingTree;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.core.Utils;
import weka.filters.supervised.instance.ClassBalancer;
import weka.filters.supervised.instance.SpreadSubsample;

public class ClassifierFactory {

	public static Classifier getClassifier(ClassifierConfig config) throws Exception {
		Classifier classifier;
		switch (config.getClassifierMethod()) {
			case WEKA_J48:
				classifier = new J48();
				((J48) classifier).setOptions(Utils.splitOptions(config.getClassifierOptions()));
				break;
			case WEKA_HOEFFDING_TREE:
				classifier = new HoeffdingTree();
				((HoeffdingTree) classifier).setOptions(Utils.splitOptions(config.getClassifierOptions()));
				break;
			case WEKA_NAIVE_BAYES_UPDATEABLE:
				classifier = new NaiveBayesUpdateable();
				((NaiveBayesUpdateable) classifier).setOptions(Utils.splitOptions(config.getClassifierOptions()));
				break;
			case WEKA_IBK:
				classifier = new IBk(5);
				((IBk) classifier).setOptions(Utils.splitOptions(config.getClassifierOptions()));
				break;
			case WEKA_REP_TREE:
				classifier = new REPTree();
				((REPTree) classifier).setOptions(Utils.splitOptions(config.getClassifierOptions()));
				break;
			case WEKA_RANDOM_FOREST:
				classifier = new RandomForest();
				((RandomForest) classifier).setOptions(Utils.splitOptions(config.getClassifierOptions()));
				break;
			case WEKA_EXP_RANDOM_FOREST:
				classifier = new ExpandableRandomForest();
				((ExpandableRandomForest) classifier).setOptions(Utils.splitOptions(config.getClassifierOptions()));
				break;
			case WEKA_ADABOOST:
				classifier = new AdaBoostM1();
				((AdaBoostM1) classifier).setOptions(Utils.splitOptions(config.getClassifierOptions()));
				break;
			default:
				throw new IllegalArgumentException("Classifier method not supported: " + config.getClassifierMethod());
		}
		switch (config.getClassBalancerMethod()) {
			case NONE:
				break;
			case UPSAMPLING:
				FilteredClassifier filteredClassifier = new FilteredClassifier();
				filteredClassifier.setClassifier(classifier);
				ClassBalancer balancer = new ClassBalancer();
				filteredClassifier.setFilter(balancer);
				classifier = filteredClassifier;
				break;
			case DOWNSAMPLING:
				filteredClassifier = new FilteredClassifier();
				filteredClassifier.setClassifier(classifier);
				SpreadSubsample subSampler = new SpreadSubsample();
				subSampler.setDistributionSpread(1.0);
				filteredClassifier.setFilter(subSampler);
				classifier = filteredClassifier;
				break;
			case COMBINEDSAMPLING:
				break;
			default:
				throw new IllegalArgumentException("Class balancer method not supported: " + config.getClassBalancerMethod());
		}
		return classifier;
	}
}
