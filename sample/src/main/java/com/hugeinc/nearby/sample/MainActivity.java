package com.hugeinc.nearby.sample;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.android.gms.common.ConnectionResult;
import com.hugeinc.nearby.Found;
import com.hugeinc.nearby.NearbyDevices;
import com.hugeinc.nearby.PermissionsException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

public class MainActivity extends AppCompatActivity {

  private static final String KEY_UUID = "key_uuid";
  private static final int REQUEST_CODE = 100;

  private ArrayAdapter<String> mNearbyDevicesArrayAdapter;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    init();
  }

  private void init() {
    final List<String> nearbyDevicesArrayList = new ArrayList<>();
    mNearbyDevicesArrayAdapter =
        new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nearbyDevicesArrayList);
    final ListView nearbyDevicesListView = (ListView) findViewById(R.id.users);
    if (nearbyDevicesListView != null) {
      nearbyDevicesListView.setAdapter(mNearbyDevicesArrayAdapter);
    }
    String uuid = getUUID(getSharedPreferences("name", MODE_PRIVATE));
    Observable<Found<String>> nearby = NearbyDevices.connect(this, uuid);
    Subscription subscription = nearby.subscribe(new Subscriber<Found<String>>() {
      @Override public void onCompleted() {

      }

      @Override public void onError(Throwable e) {
        if (e instanceof PermissionsException) {
          requestPermissions(((PermissionsException) e).getConnectionResult());
        }
      }

      @Override public void onNext(Found<String> stringFound) {
        if (stringFound.isFound()) {
          mNearbyDevicesArrayAdapter.add(stringFound.getFoundMessage());
        } else {
          mNearbyDevicesArrayAdapter.remove(stringFound.getFoundMessage());
        }
      }
    });
    subscription.unsubscribe();
  }

  private void requestPermissions(ConnectionResult result) {
    if (result.hasResolution()) {
      try {
        result.startResolutionForResult(this, REQUEST_CODE);
      } catch (IntentSender.SendIntentException ignored) {
      }
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        init();
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  private static String getUUID(SharedPreferences sharedPreferences) {
    String uuid = sharedPreferences.getString(KEY_UUID, "");
    if (TextUtils.isEmpty(uuid)) {
      uuid = UUID.randomUUID().toString();
      sharedPreferences.edit().putString(KEY_UUID, uuid).apply();
    }
    return uuid;
  }
}
