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
import rx.Subscription;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

  private static final String KEY_UUID = "key_uuid";
  private static final int REQUEST_CODE = 100;

  private ArrayAdapter<String> mNearbyDevicesArrayAdapter;
  private Subscription subscription;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    final List<String> nearbyDevicesArrayList = new ArrayList<>();
    mNearbyDevicesArrayAdapter =
        new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nearbyDevicesArrayList);
    final ListView nearbyDevicesListView = (ListView) findViewById(R.id.users);
    if (nearbyDevicesListView != null) {
      nearbyDevicesListView.setAdapter(mNearbyDevicesArrayAdapter);
    }
  }

  @Override protected void onResume() {
    super.onResume();
    init();
  }

  private void init() {
    String uuid = getUUID(getSharedPreferences("name", MODE_PRIVATE));
    subscription = NearbyDevices.connect(this, uuid).subscribe(new Action1<Found<String>>() {
      @Override public void call(Found<String> stringFound) {
        if (stringFound.isFound()) {
          mNearbyDevicesArrayAdapter.add(stringFound.getFoundMessage());
        } else {
          mNearbyDevicesArrayAdapter.remove(stringFound.getFoundMessage());
        }
      }
    }, new Action1<Throwable>() {
      @Override public void call(Throwable throwable) {
        if (throwable instanceof PermissionsException) {
          requestPermissions(((PermissionsException) throwable).getConnectionResult());
        }
      }
    });
  }

  @Override protected void onPause() {
    super.onPause();
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
