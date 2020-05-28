package org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CompletableFutureJava8Compatibility {

  // Java 8 replacement for CompletableFuture#delayedExecutor
  // snippet from https://stackoverflow.com/a/58708611/3165190
  // prefer this constructor with zero core threads for a shared pool,
  // to avoid blocking JVM exit
  static final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(0);
  static Executor delayedExecutor(long delay, TimeUnit unit)
  {
    return delayedExecutor(delay, unit, ForkJoinPool.commonPool());
  }
  static Executor delayedExecutor(long delay, TimeUnit unit, Executor executor)
  {
    return r -> SCHEDULER.schedule(() -> executor.execute(r), delay, unit);
  }

}
