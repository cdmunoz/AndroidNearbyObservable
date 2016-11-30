package com.hugeinc.nearby;

import android.app.PendingIntent;

public class PermissionsException extends Throwable {
  private PendingIntent pendingIntent;

  public PermissionsException(PendingIntent pendingIntent) {
    this.pendingIntent = pendingIntent;
  }

  public PendingIntent getPendingIntent() {
    return pendingIntent;
  }
}
