![](logo.png)

[![Build Status](https://travis-ci.org/dimitrovskif/Retrofit-SmartCache.svg?branch=master)](https://travis-ci.org/dimitrovskif/Retrofit-SmartCache)

Retrofit SmartCache - caching system, done right
==========

*SmartCache* offers some features over the default HTTP based Retrofit caching:

* **Instant response:** The cache hit happens before an actual request is dispatched, unlike Retrofit's cache which waits for network call to finish (or timeout) before serving cache

  * How Retrofit/OkHttp works: One request always has one response, so it either gives you the cached data or the network response
  
  * How SmartCache works: One request has 2 responses: the cached data is dispatched immediately, and a subsequent network call is tried too

* **Memory based caching:** This caching is not on the networking layer, which means memory layout can be preserved - and data can be loaded without slow deserialization/serialization

* **Extensible system:** Write cache adapters or use `@Cache` annotations