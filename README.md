![SmartCache for Retrofit2](logo.png)

## SmartCache - preloaded responses for Retrofit 2 [![Build Status](https://www.travis-ci.com/fikisipi/SmartCache.svg?branch=master)](https://travis-ci.com/fikisipi/SmartCache)

#### This library calls your callback twice: first cached, then fresh data

Instead of showing an empty screen while waiting for a network response, why not load the latest successful response from storage?

* Responses are saved in device storage & RAM
* No extra logic, only `Call<T>` becomes `SmartCall<T>`
* Extend your own adapters for other caching strategies

### Install and use

1. Add SmartCache to Gradle dependencies, and JitPack to repos:
```gradle
repositories {
    ...
    maven { url "https://jitpack.io" }
                 ^^^^^^^^^^^^^^^^^^^ ⚡Add this!
}

dependencies {
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.github.fikisipi:SmartCache:2.9.0'
                    ^^^^^^^^^^^^^^^^^^^ ⚡Add this!
}
```

2. Add `SmartCallFactory` to your Retrofit `Builder` (where `this` is an Activity/Context):
```java
Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://your-api.org")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(SmartCallFactory.createBasic(this))
                               ^^^^^^^^^^^^^^^^ ⚡Add this!
        .build();
```

3. Replace `Call<T>` with `SmartCall<T>`.
```java
public interface GitHubService {
  @GET("/users/{user}/repos")
  SmartCall<List<Repo>> listRepos(@Path("user") String user);
  ^^^^^^^^^ ⚡ Swap from Call<T> to SmartCall<T>
}
```

### Checking if the callback data is preloaded

The preloaded data callback is fired before the network callback. You can check which is which using
`SmartCache.isResponseFromNetwork(response)`:

```java
new Callback<YourModel>() {
  @Override
  public void onResponse(Call<YourModel> call, Response<YourModel> response) {
    if(SmartCache.isResponseFromNetwork(response)) {
       ...
```

### Clearing cache and choosing which requests should be cached

Most apps shouldn't cache stateful routes like `POST/PUT` so by default only `GET` routes are cached.
You can change this by supplying `filter: (Request) -> bool` when creating the factory
`SmartCallFactory.createBasic(this, filter)`.

Each interface `CachingSystem` now has `void clearCache()` to delete the cache. By default,
SmartCache uses `BasicCaching` as a LRU system, so clearing can be done like this:
```
BasicCaching.buildFromContext(getApplicationContext()).clearCache();
```

**💻 Check the demo app for these examples.**

### Demo app

For a complete example check [demoapp/](/demoapp).

![...](demoapp_scr.png)

### How it works

![...](works_diagram.png)

One request corresponds to two responses: a cache response and a network response. Loading content from your phone is faster than loading from network; therefore your app will show stale content while waiting for a fresh network response. (*Note:* If the network response comes first, cache won't happen.)

Help needed from contributors:

* Make it work with RxJava
* Implement a pool of threads

### License

    Copyright 2015-2021 Filip Dimitrovski

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
