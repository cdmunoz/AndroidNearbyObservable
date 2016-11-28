package com.hugeinc.nearby;

import android.content.Context;
import rx.Observable;

public class NearbyDevices {
  public static <T> Observable<Found<T>> connect(final Context context, final T message) {
    return Observable.create(new NearbyDeviceSubscriber<>(context, message));
  }
}
