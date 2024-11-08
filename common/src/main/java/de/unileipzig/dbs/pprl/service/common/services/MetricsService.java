package de.unileipzig.dbs.pprl.service.common.services;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MetricsService {

  private static final Map<String, Meter> meterMap = new HashMap<>();

  public static Timer timer(String name) {
    return (Timer) meter(name, Metrics::timer);
  }

  public static Counter counter(String name) {
    return (Counter) meter(name, Metrics::counter);
  }

  public static DistributionSummary summary(String name) {
    return (DistributionSummary) meter(name, Metrics::summary);
  }

  public static Meter meter(String name, Function<String, Meter> creator) {
    if (!meterMap.containsKey(name)) {
      Meter newTimer = creator.apply(name);
      meterMap.put(name, newTimer);
    }
    return meterMap.get(name);
  }

}
