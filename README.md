![SmartCache for Retrofit2](logo.png)

## SmartCache - preloaded responses for Retrofit 2 [![Build Status](https://travis-ci.org/dimitrovskif/SmartCache.svg?branch=master)](https://travis-ci.org/dimitrovskif/SmartCache)

#### This library calls your callback twice: first cached, then fresh data 

Instead of showing an empty screen while waiting for a network response, why not load the latest successful response from storage? 

* Responses are saved in device storage & RAM
* No extra logic, only `Call<T>` becomes `SmartCall<T>`
* Extend your own adapters for other caching strategies

### Install and use

1. Gradle installation:
```groovy
repositories {
    ...
    maven { url "https://jitpack.io" }
}
    
dependencies {
    compile 'com.github.dimitrovskif:SmartCache:0.2'
}
```

2. Enable `SmartCallFactory`:
```java
SmartCallFactory smartFactory = new SmartCallFactory(BasicCaching.fromCtx(this));
Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("http://myapi.com")
    .addCallAdapterFactory(smartFactory) // add this!
    .build();
 ```
 
3. Replace `Call<T>` with `SmartCall<T>`.
```java
public interface GitHubService {
  @GET("/users/{user}/repos")
  SmartCall<List<Repo>> listRepos(@Path("user") String user);
}
```
### How it works

![fff](http://dimitrovskif.github.io/SmartCache/res/how_it_works.png)

One request corresponds to two responses: a cache response and a network response. Loading content from your phone is faster than loading from network; therefore your app will show stale content while waiting for a fresh network response. (*Note:* If the network response comes first, cache won't happen.)

### License

    Copyright 2019 Filip Dimitrovski

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
