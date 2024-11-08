package de.unileipzig.dbs.pprl.service.linkageunit.data.mongo;

public enum ProjectState {
  COLLECTING, BLOCKING, LINKING, CLASSIFICATION, POSTPROCESSING, CLUSTERING, DONE;

  public boolean isAtLeast(ProjectState reference) {
    return this.ordinal() >= reference.ordinal();
  }
  public boolean isAtMost(ProjectState reference) {
    return this.ordinal() <= reference.ordinal();
  }

  public boolean isBetween(ProjectState lower, ProjectState upper) {
    return this.isAtLeast(lower) && this.isAtMost(upper);
  }

}
