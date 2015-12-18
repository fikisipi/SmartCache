![SmartCache for Retrofit2](res/logo.png)

SmartCache for Retrofit 2 ![Build Status](https://travis-ci.org/dimitrovskif/SmartCache.svg?branch=master)](https://travis-ci.org/dimitrovskif/SmartCache)
==========

A smarter caching layer for the Android library we all love:

* Memory-based caching
* Instant cache responses (cache is read before the network request is started)
* Adapter/extensibility support

### How it works

SmartCache isn't constrained by the 1 response per request limit that Retrofit imposes. This means that your asynchronous callbacks will first get called when cache is loaded, and then when the network response is ready.

![SmartCache for Retrofit2](res/how_it_works.png)

### Documentation and installation

TODO

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
