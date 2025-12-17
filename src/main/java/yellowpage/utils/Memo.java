package yellowpage.utils;

import java.util.function.Supplier;

public class Memo<T> {

  private T value;
  private Supplier<T> supplier;

  public Memo(Supplier<T> supplier){
    value = null;
    this.supplier = supplier;
  }

  public T get(){
    if(supplier != null)
      load();
    return value;
  }

  private synchronized void load(){
    if(supplier != null){
      value = supplier.get();
      supplier = null;
    }
  }
  
}
