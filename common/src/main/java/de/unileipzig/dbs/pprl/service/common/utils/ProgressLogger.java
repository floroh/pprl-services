package de.unileipzig.dbs.pprl.service.common.utils;

import lombok.Builder;
import lombok.Getter;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ProgressLogger: logs progress every N items OR every T duration (whichever comes first).
 *
 * Construct with Lombok builder:
 * try (ProgressLogger pl = ProgressLogger.builder()
 *       .log(log)
 *       .total(totalItems)
 *       .logIntervalItems(500)
 *       .logIntervalDuration(Duration.ofSeconds(30))
 *       .prefix("[MyTask]")
 *       .build()) {
 *   ...
 * }
 */
public final class ProgressLogger implements AutoCloseable {

  private final Logger log;
  private final String prefix;
  private final long total;
  private final long logIntervalItems;
  private final Duration logIntervalDuration;

  // runtime state
  @Getter
  private final AtomicLong processed = new AtomicLong(0);
  private final Instant startTime;
  private volatile Instant lastLogTime;
  private final AtomicLong lastLoggedProcessed = new AtomicLong(0);

  /**
   * Lombok builder-targeted constructor.
   * Use ProgressLogger.builder()...build() to create an instance.
   */
  @Builder
  private ProgressLogger(Logger log,
                         Long total,
                         Long logIntervalItems,
                         Duration logIntervalDuration,
                         String prefix) {
    this.log = Objects.requireNonNull(log, "log must not be null");
    this.prefix = (prefix == null) ? "Progress" : prefix;
    this.total = (total == null) ? Long.MAX_VALUE : Math.max(0L, total);
    this.logIntervalItems = Math.max(1L, (logIntervalItems == null ? 1L : logIntervalItems));
    this.logIntervalDuration = (logIntervalDuration == null)
            ? Duration.ZERO
            : (logIntervalDuration.isNegative() ? Duration.ZERO : logIntervalDuration);

    this.startTime = Instant.now();
    this.lastLogTime = this.startTime;

    // initial log
    log.info("{} Starting: total = {}", this.prefix, this.total == Long.MAX_VALUE ? "unknown" : Long.toString(this.total));
  }

  /**
   * Advance progress by n items (n should be >= 0).
   */
  public void stepBy(long n) {
    if (n == 0) {
      maybeLog(processed.get());
    } else {
      long nowProcessed = processed.addAndGet(n);
      maybeLog(nowProcessed);
    }
  }

  /**
   * Force an immediate log irrespective of thresholds.
   */
  public void forceLog() {
    long nowProcessed = processed.get();
    doLog(nowProcessed);
  }

  private void maybeLog(long nowProcessed) {
    // item-count trigger
    if (nowProcessed - lastLoggedProcessed.get() >= logIntervalItems) {
      doLog(nowProcessed);
      return;
    }

    // time trigger (if enabled)
    if (!logIntervalDuration.isZero()) {
      Instant now = Instant.now();
      Duration sinceLast = Duration.between(lastLogTime, now);
      if (!sinceLast.minus(logIntervalDuration).isNegative()) { // sinceLast >= logIntervalDuration
        doLog(nowProcessed);
      }
    }
  }

  private synchronized void doLog(long nowProcessed) {
    // avoid duplicate quick successive logs
    if (nowProcessed == lastLoggedProcessed.get() && Duration.between(lastLogTime, Instant.now()).toMillis() < 1) {
      return;
    }

    Instant now = Instant.now();
    double elapsedSec = Math.max(1e-6, Duration.between(startTime, now).toMillis() / 1000.0);
    double rate = nowProcessed / elapsedSec; // items per second

    String totalStr = (total == Long.MAX_VALUE) ? "unknown" : Long.toString(total);
    String pctStr = (total > 0 && total != Long.MAX_VALUE)
            ? String.format(Locale.ENGLISH, "%.1f%%", Math.min(100.0, (nowProcessed * 100.0) / total))
            : "n/a";
    String rateStr = String.format(Locale.ENGLISH, "%.2f items/s", rate);

    String etaStr = "unknown";
    if (total > 0 && total != Long.MAX_VALUE && rate > 0.0) {
      long remaining = Math.max(0, total - nowProcessed);
      long etaSeconds = (long) Math.ceil(remaining / rate);
      etaStr = formatDurationSeconds(etaSeconds);
    }

    log.info("{} {}/{} ({}), rate={}, ETA={}",
            prefix, nowProcessed, totalStr, pctStr, rateStr, etaStr);

    lastLoggedProcessed.set(nowProcessed);
    lastLogTime = now;
  }

  private static String formatDurationSeconds(long seconds) {
    if (seconds <= 0) return "0s";
    long hrs = seconds / 3600;
    long mins = (seconds % 3600) / 60;
    long secs = seconds % 60;
    if (hrs > 0) return String.format("%dh%02dm%02ds", hrs, mins, secs);
    if (mins > 0) return String.format("%dm%02ds", mins, secs);
    return String.format("%ds", secs);
  }

  /**
   * Closes the logger and emits final report.
   */
  @Override
  public void close() {
    long nowProcessed = processed.get();
    if (nowProcessed >= total && total != Long.MAX_VALUE) {
      double elapsedSec = Math.max(1e-6, Duration.between(startTime, Instant.now()).toMillis() / 1000.0);
      String finishedMsg = String.format(Locale.US,
              "%s Finished: %d/%d in %s (avg rate=%.2f items/s)",
              prefix, nowProcessed, total, formatDurationSeconds((long)Math.ceil(elapsedSec)), nowProcessed / elapsedSec);
      log.info(finishedMsg);
    } else {
      // emit a final progress log and note close
      doLog(nowProcessed);
      log.info("{} ProgressLogger closed (processed={})", prefix, nowProcessed);
    }
  }
}
