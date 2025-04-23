##bdownloader
<p align="center">
<img src="https://github.com/EurikaOrmanel/bdownloader/blob/main/assets/icon.jpg">
</p>

# **bdownloader** is a multi-connection file downloading library with a pause,stop and resume feature.

### Features

* Multi-threaded Downloads: Splits files into chunks and downloads them concurrently for maximum speed.

* Pause & Resume: Gracefully suspend and continue downloads—even across app restarts.

* Stop & Cancel: Stop downloads at any time, with the option to cancel and remove all partial data.

* Progress Tracking: Real-time progress updates for each file and its chunks.

* Error Handling & Retry Logic: Automatically retries failed chunks to ensure downloads complete smoothly.

* Built-in Support for Large Files: Handles multi-gigabyte files with ease.

* Optimized for Android: Works efficiently across API levels with foreground services and proper lifecycle awareness.


>bdownloader utilizes kotline's coroutine and okhttp.


Add this in your `settings.gradle`:
```groovy
maven { url 'https://jitpack.io' }
```

If you are using `settings.gradle.kts`, add the following:
```kotlin
maven { setUrl("https://jitpack.io") }
```



Add this in your `build.gradle`
```groovy
implementation 'com.github.EurikaOrmanel:bdownloader:0.0.4'
```
If you're using a `build.gradle.kts`
```gradle.kts
implementation ("com.github.EurikaOrmanel:bdownloader:0.0.4")

```


Do not forget to add internet permission in manifest if already not present
```xml
<uses-permission android:name="android.permission.INTERNET" />
```
Then initialize it by passing context to the class's constructor:
```kotlin
 val bdownloader = BDownloader(context)

```

### Pause a download request
```kotlin
bdownloader.pause(downloadId);
```

### Resume a download request
```kotlin
bdownloader.resume(downloadId);
```

### Cancel a download request
```kotlin
// Cancel with the download id
bdownloader.cancel(downloadId);
// The tag can be set to any request and then can be used to cancel the request
bdownloader.cancel(downloadId);
// Cancel all the requests
bdownloader.cancelAll();
```

### Status of a download request
```kotlin
val  status = bdownloader.getStatus(downloadId);
```


##TODO:
- [x] Add file to db in room
- [x] Pause and resume download
- [x] Check if item already in queue and flag when attempting a resume
- [ ] Download percentage calculation after resume inapt.
