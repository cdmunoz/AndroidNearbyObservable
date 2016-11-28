package com.hugeinc.nearby;

import com.google.android.gms.common.ConnectionResult;

public class PermissionsException extends Throwable {
  private ConnectionResult connectionResult;

  public PermissionsException(ConnectionResult connectionResult) {
    this.connectionResult = connectionResult;
  }

  public ConnectionResult getConnectionResult() {
    return connectionResult;
  }
}
