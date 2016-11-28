# RxNearby: Reactive Observable for Google Nearby API

Based on RxJava RxNearby provides an integration of the [Nearby Message API](https://developers.google.com/nearby/messages/android/get-started) as an Observable.

## Example

```java
NearbyDevices.connect(this, uuid).subscribe(new Action1<Found<String>>() {
  @Override public void call(Found<String> stringFound) {
    if (stringFound.isFound()) {
      //do something when message is found
    } else {
      //do something when message is lost
    }
  }
});
```

Download
============

RxNearby is ready to be used via [jitpack.io](https://jitpack.io/#TechFreak/AndroidNearbyObservable).
Simply add the following code to your root `build.gradle`:

```groovy
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

Now add the gradle dependency in your application's `build.gradle`:

```groovy
dependencies {
    compile 'com.github.hugeinc:AndroidNearbyObservable:0.1'
}
```

## LICENSE

Copyright 2016 Huge, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.