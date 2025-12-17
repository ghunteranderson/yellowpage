package yellowpage.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

public class TaskRunner {

  private static int nextThreadId = 1;

  private static final ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(0, TaskRunner::threadFactory);

  private static final Thread threadFactory(Runnable runnable){
    var thread = new Thread(runnable, "YellowPage-TaskRunner-" + nextThreadId++);
    thread.setDaemon(true);
    return thread;
  }

  public static ScheduledTask once(Runnable task, int delay, TimeUnit unit) {
    var future = pool.schedule(task, delay, unit);
    return new ScheduledTask(future);
  }

  public static ScheduledTask repeat(Runnable task, int delay, TimeUnit unit) {
    var future = pool.scheduleAtFixedRate(task, delay, delay, unit);
    return new ScheduledTask(future);
  }

  public static void shutdown() {
    pool.shutdown();
  }

  @RequiredArgsConstructor
  public static class ScheduledTask {
    private final ScheduledFuture<?> future;

    public boolean cancel() {
      if (future.isDone())
        return true;
      else
        return future.cancel(false);
    }

    public boolean isDone() {
      return future.isDone();
    }

    public void join() {
      try {
        future.get();
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      } catch(ExecutionException ex) {
        throw new RuntimeException("Failed to join with task.", ex);
      }
    }
  }
}
