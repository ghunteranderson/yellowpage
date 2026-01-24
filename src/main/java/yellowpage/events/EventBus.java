package yellowpage.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventBus {

  private final Map<Class<?>, List<EventHandler<Object>>> listeners;
  private final ExecutorService executor;

  public EventBus(){
    this.listeners = new ConcurrentHashMap<>();
    this.executor = new ThreadPoolExecutor(10, 20, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
  }

  @SuppressWarnings("unchecked")
  public <T> void register(Class<T> type, EventHandler<T> handler){
    var list = listeners.computeIfAbsent(type, k -> new ArrayList<>());
    list.add((EventHandler<Object>)handler);
  }

  public void fire(Object event){
    listeners.keySet().forEach(clazz -> {
      if(clazz.isInstance(event)){
        var handlers = listeners.get(clazz);
        for(var handler : handlers)
            executor.submit(() -> (handler).handle(this, event));
      }
    });
  }

  public interface EventHandler<T> {
    void handle(EventBus bus, T value);
  }
  
}
