package com.hugeinc.nearby;

public class Found<T> {
  private final boolean found;
  private final T foundMessage;

  public Found(boolean found, T foundMessage) {
    this.found = found;
    this.foundMessage = foundMessage;
  }

  public boolean isFound() {
    return found;
  }

  public T getFoundMessage() {
    return foundMessage;
  }
}
