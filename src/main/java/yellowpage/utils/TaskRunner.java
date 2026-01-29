package yellowpage.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.RequiredArgsConstructor;

public class TaskRunner implements AutoCloseable {

  private static final AtomicInteger NEXT_THREAD_ID = new AtomicInteger(1);

  private final ScheduledThreadPoolExecutor pool;

  public TaskRunner() {
    this.pool = new ScheduledThreadPoolExecutor(0, this::threadFactory);
  }

  private Thread threadFactory(Runnable runnable){
    var thread = new Thread(runnable, "Yellowpage-TaskRunner-" + NEXT_THREAD_ID.getAndIncrement());
    thread.setDaemon(true);
    return thread;
  }

  public ScheduledTask once(Runnable task, int delay, TimeUnit unit) {
    var future = pool.schedule(task, delay, unit);
    return new ScheduledTask(future);
  }

  public ScheduledTask repeat(Runnable task, int delay, TimeUnit unit) {
    var future = pool.scheduleAtFixedRate(task, delay, delay, unit);
    return new ScheduledTask(future);
  }

  @Override
  public void close() {
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
