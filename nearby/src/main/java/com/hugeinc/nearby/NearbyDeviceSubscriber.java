package com.hugeinc.nearby;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import rx.Observable;
import rx.Subscriber;

class NearbyDeviceSubscriber<T> implements Observable.OnSubscribe<Found<T>> {

  private final int TTL_IN_SECONDS = 3 * 60;
  private final Strategy PUB_SUB_STRATEGY =
      new Strategy.Builder().setTtlSeconds(TTL_IN_SECONDS).build();
  private final T message;
  private final GoogleApiClient googleApiClient;
  private final Gson gson = new Gson();
  private Subscriber<? super Found<T>> subscriber;

  NearbyDeviceSubscriber(Context context, T message) {
    this.message = message;
    googleApiClient = new GoogleApiClient.Builder(context).addApi(Nearby.MESSAGES_API,
        new MessagesOptions.Builder().build())
        .addOnConnectionFailedListener(connectionFailedListener)
        .addConnectionCallbacks(connectionCallbacks)
        .build();
  }

  @Override public void call(Subscriber<? super Found<T>> subscriber) {
    this.subscriber = subscriber;
    googleApiClient.connect();
  }

  private MessageListener messageListener = new MessageListener() {
    @Override public void onFound(final Message msg) {
      try {
        subscriber.onNext(foundMessage(msg, message.getClass()));
      } catch (ConnectionErrorException e) {
        subscriber.onError(e);
      }
    }

    @Override public void onLost(final Message msg) {
      try {
        subscriber.onNext(lostMessage(msg, message.getClass()));
      } catch (ConnectionErrorException e) {
        subscriber.onError(e);
      }
    }
  };

  private Found<T> foundMessage(Message message, Class aClass) throws ConnectionErrorException {
    try {
      return new Found<>(true, messageFrom(message, aClass));
    } catch (Exception e) {
      throw new ConnectionErrorException();
    }
  }

  private Found<T> lostMessage(Message message, Class aClass) throws ConnectionErrorException {
    try {
      return new Found<>(false, messageFrom(message, aClass));
    } catch (Exception e) {
      throw new ConnectionErrorException();
    }
  }

  private T messageFrom(Message message, Type type) {
    String stringMessage = new String(message.getContent()).trim();
    return gson.fromJson(stringMessage, type);
  }

  private GoogleApiClient.ConnectionCallbacks connectionCallbacks =
      new GoogleApiClient.ConnectionCallbacks() {
        @Override public void onConnected(@Nullable Bundle bundle) {
          publish(message);
          subscribe();
        }

        @Override public void onConnectionSuspended(int i) {
          subscriber.onError(new ConnectionErrorException());
        }
      };

  private void publish(T message) {
    PublishOptions options = new PublishOptions.Builder().setStrategy(PUB_SUB_STRATEGY)
        .setCallback(new PublishCallback() {
          @Override public void onExpired() {
            super.onExpired();
            subscriber.onError(new ConnectionErrorException());
          }
        })
        .build();

    Message publishMessage = new Message(gson.toJson(message).getBytes(Charset.forName("UTF-8")));
    Nearby.Messages.publish(googleApiClient, publishMessage, options)
        .setResultCallback(new ResultCallback<Status>() {
          @Override public void onResult(@NonNull Status status) {
            if (!status.isSuccess()) {
              subscriber.onError(new ConnectionErrorException());
            }
          }
        });
  }

  private void subscribe() {
    SubscribeOptions options = new SubscribeOptions.Builder().setStrategy(PUB_SUB_STRATEGY)
        .setCallback(new SubscribeCallback() {
          @Override public void onExpired() {
            super.onExpired();
            subscriber.onError(new ConnectionErrorException());
          }
        })
        .build();

    Nearby.Messages.subscribe(googleApiClient, messageListener, options)
        .setResultCallback(new ResultCallback<Status>() {
          @Override public void onResult(@NonNull Status status) {
            if (!status.isSuccess()) {
              subscriber.onError(new ConnectionErrorException());
            }
          }
        });
  }

  private final GoogleApiClient.OnConnectionFailedListener connectionFailedListener =
      new GoogleApiClient.OnConnectionFailedListener() {
        @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
          if (connectionResult.getResolution() != null) {
            subscriber.onError(new PermissionsException(connectionResult));
          } else {
            subscriber.onError(new ConnectionErrorException());
          }
        }
      };
}
