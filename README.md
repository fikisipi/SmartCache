![SmartCache for Retrofit2](logo.png)

SmartCache for Retrofit 2 [![Build Status](https://travis-ci.org/dimitrovskif/SmartCache.svg?branch=master)](https://travis-ci.org/dimitrovskif/SmartCache)
==========

* Memory-based caching
* Adapter/extensibility support
* Asynchronous cache

### The principle behind async cache

One request corresponds to **two** responses: a cache response and a network response. Usually, loading content from your phone is faster than loading from network; therefore your app may show stale content while waiting for a network response.

### Documentation and installation

[**Documentation and further info is available on the website.**](http://dimitrovskif.github.io/SmartCache/)

Gradle installation:
```groovy
repositories {
    ...
    maven { url "https://jitpack.io" }
}
    
dependencies {
    compile 'com.github.dimitrovskif:SmartCache:0.2'
}
```

### License

    Copyright 2015 Filip Dimitrovski

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
