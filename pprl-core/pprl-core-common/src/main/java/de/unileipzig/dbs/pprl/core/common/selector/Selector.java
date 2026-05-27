package de.unileipzig.dbs.pprl.core.common.selector;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.function.Predicate;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public interface Selector<T> extends Predicate<T>, Serializable {

//  boolean isSelected();

}
