# Delorean 
Delorean: a Time Machine for your Spring Boot based applications. 
 
Delorean provides a mechanism and approach that enables both automated and manual integration testing of your application's behaviour as of any date/time without having to adjust your environment/system clock. No Plutonium required.

Need to test how your system behaves on Tax Day? November 5th 1955? 12:00am on January 1 of next year? February 29th?

Delorean has you covered.

## Overview
Delorean enables setting the "Current" date and/or time in your application on a per-request basis via either an HTTP Header or a Cookie value. It allows you to time travel to either a precise instant (date and time) or to a fixed date allowing the time portion (if used) to be sourced from the default system clock.

When enabled, Delorean intercepts all request, checking for an Http Request Header or Cookie value containing the time travel date/time. If populated Delorean stores the provided date/time as an InheritableThreadLocal variable making it available to the request thread as well as any threads spawned from it.

To leverage this functionality simply replace all calls to obtain the "Current Date" in your application with calls to `com.slalom.delorean.DateFactory`. For example, replace `LocalDate.now()` with calls to `DateFactory.today()` and `LocalDateTime.now()` with `DateFactory.now()`. If Delorean is not enabled, or no time travel date/time are provided on the request, the DateFactory methods return the default system clock date/times.

Additionally, to support MicroService based architectures, when enabled and configured Delorean intercepts outbound Web API calls made with Spring's `RestTemplate`, OkHttp3's `OkHttpClient`, or Apache's `HttpClient` and injects the Time Machine header. Thus allowing all downstream systems using Delorean to operate with the same date/time.

## Why Delorean

Java 8's [Clock] already provides an abstraction for overriding the default system clock. In fact, under the covers Delorean is using this abstraction to manage the Time Travel date/time. Natively leveraging the `Clock` solution requires passing an instance of the `Clock` on each call to a `java.time.*` method that requires the current instant. This means that the `Clock` must be exposed as a Spring Managed Bean and injected into any class that may need to use the "Current" date. 

While this is certainly a valid, logical approach, we feel that the static methods on `DateFactory` provide a more pragmatic solution that avoids the possibility of making classes Spring Managed Beans solely to inject `Clock` instance into them. 

Additionally Delorean provides the ability to easily configure a request-persistent Time Travel date/time via Http Request Header or Cookie, to make a configured date/time available to a javascript UI and/or to pass it in an Http Request Headers to downstream systems.

## Requirements
Delorean requires Java >= 8 and has been tested against Spring Boot 1.4.x (though it should work with earlier versions) and presently only provides results using Java 8 `java.time` classes - though support for legacy applications using `Joda-Time` is a potential future enhancement.

## Usage
In order to use Delorean you must first add it to your project dependencies. You must first add JCenter to your list of repositories and then include the dependency

### Maven

Add: 
```xml
<repositories>
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>central</id>
        <name>bintray</name>
        <url>http://jcenter.bintray.com</url>
    </repository>
</repositories>
``` 

to your pom.xml or settings.xml and then import the dependency:

```xml
<dependency>
  <groupId>com.slalom.delorean</groupId>
  <artifactId>delorean</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Gradle
Add:
```groovy
repositories {
    jcenter()
}
```
to your build.gradle and then import the dependency:

```groovy
compile 'com.slalom.delorean:delorean:1.0.0'
```

Then, in the appropriate non-production Spring Profiles enable Delorean by setting `com.slalom.delorean.enabled=true`

Delorean implements SpringBoot AutoConfiguration to enable an inbound Http Request Interceptor to determine the Time Travel date/time.

If Cookie support is enabled a Spring `@Controller` is AutoConfigured to allow the Time Travel date/time to be set via the browser. This is mainly useful for manual or automated browser-based integration testing.

Additionally Delorean will automatically register outbound Http Request Interceptors into the Spring `ApplicationContext` for Spring `RestTemplate`, `OkHttpClient` and Apache `HttpClient` if the appropriate libraries are found on the classpath.

### RestTemplate

```java
@Autowired
private SpringRestTemplateTimeMachineOutboundRequestInterceptor restTemplateInterceptor;
...
RestTemplate template = new RestTemplateBuilder().additionalInterceptors(restTemplateInterceptor).build();
```
### OkHttp3
```java
@Autowired
private OkHttp3OutboundTimeMachineRequestInterceptor okHttpInterceptor;
...
OkHttpClient client = new OkHttpClient.Builder().addInterceptor(okHttpInterceptor).build()
```
### Apache HttpClient
```java
@Autowired
private ApacheHttpClientOutboundTimeMachineRequestInterceptor apacheInterceptor
...
HttpClient client = HttpClients.custom().addInterceptorFirst(apacheInterceptor).build();
```

### Http Header
By default Delorean checks for an Http Request Header with name `X-Delorean-Time-Machine` with a value provided as an ISO formatted date or datetime such as `2000-10-15` or `2000-10-15T10:00:00`.

### Cookies
If cookie support is enabled Delorean automatically exposes the following endpoints:

Endpoint | Http Method | Description
---------|-------------|------------
`/time-machine` | `GET` | Return the currently configured Test Date/Time or empty result if none configured
`/time-machine` | `DELETE` | Removes Time Machine Cookie
`/time-machine/{dateTime}` | `GET` | Sets the Test Date to the provided date or date/time
`/time-machine/clear` | `GET` | Removes Time Machine Cookie

After a cookie has been set all subsequent requests will use the configured Time Travel date/time until the browser is closed or the cookie is cleared. If both an Http Request Header and Cookie value are provided the Request Header takes precedence and is used for the request, though any existing cookie value is not modified.

As the Time Travel date/time is stored in a cookie it can be leveraged from your application's javascript UI components to enable using the same "Current" date/time across both the back-end and UI tier as well.

## Configuration

property | default | description
---------|---------|------------
`slalom.delorean.enabled` |  `true` | Whether Delorean is enabled
`slalom.delorean.headerName` | `X-Delorean-Time-Machine` | The name of the HTTP Request Header used to set the Time Travel date/time
`slalom.delorean.outboundRequestHeader.enabled` | `true` | If the Time Machine header should be injected into outbound requests
`slalom.delorean.outboundRequestHeader.name` | `X-Delorean-Time-Machine` | The name of the HTTP Request Header to be injected into outbound requests
`slalom.delorean.cookie.enabled` | `false` | Whether Delorean should allow setting/reading the Time Travel date/time via an HTTP Cookie
`slalom.delorean.cookie.path` | `time-machine` | The url relative to the application's context root where the cookie management endpoints are exposed
`slalom.delorean.cookie.name` | `Delorean-Time-Machine` | The name of the cookie used to store the Time Travel date/time


## Be Aware

Delorean uses an InheritableThreadLocal to ensure that any configured Time Travel date/time is available to child threads spawned from the main request thread. Delorean automatically clears the Time Travel date for all Servlet Request handler threads -  but be aware that if your application implements a Thread Pool aproach for spawning child threads it *must* call `DateFactory.clearTestDate()` before returning any thread  to the pool. Failure to do so may result in unexpected behavior as threads returned to the pool may continue to have an unexpected Time Travel date/time configured.

## Contributing
[Pull requests] are welcome

## License
Delorean is released under the [MIT License].

[Pull requests]: https://help.github.com/categories/collaborating-on-projects-using-issues-and-pull-requests/
[MIT License]: https://opensource.org/licenses/MIT
[Clock]: https://docs.oracle.com/javase/8/docs/api/java/time/Clock.html

