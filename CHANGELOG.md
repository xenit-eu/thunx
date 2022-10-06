# Changelog


## [0.4.1] (2022-10-06)

## Changed

* Bill of Materials is now published as `thunx-bom`, `thunx-dependencies` remains as internal platform by @tgeens in https://github.com/xenit-eu/thunx/pull/54

## [0.4.0] (2022-10-06)

## Changed

* Relocated MavenCentral publication from `eu.xenit.contentcloud.thunx` to `com.contentgrid.thunx` by @tgeens in https://github.com/xenit-eu/thunx/pull/43
* Rename packages from `eu.xenit.contentcloud.thunx` to `com.contentgrid.thunx` by @tgeens in https://github.com/xenit-eu/thunx/pull/44
* Introduces `thunx-dependencies` to manage dependency versions by @tgeens in https://github.com/xenit-eu/thunx/pull/42

## Housekeeping

* Fixes maven central publishing by providing description for thunx-dependencies by @tgeens in https://github.com/xenit-eu/thunx/pull/47
* Use gradle plugin eu.xenit.enterprise-conventions.oss:0.3.0-rc.1 by @tgeens in https://github.com/xenit-eu/thunx/pull/48
* Make spring-boot and spring-cloud dependency versions manageable by dependabot by @tgeens in https://github.com/xenit-eu/thunx/pull/49

## Dependency updates

* Dependency `eu.xenit.contentcloud:opa-async-java-client` relocated to groupId `com.contentgrid.opa` by @tgeens in https://github.com/xenit-eu/thunx/pull/41
* Bump json-unit-assertj from 2.33.0 to 2.34.0 by @dependabot in https://github.com/xenit-eu/thunx/pull/39
* Bump json-unit-assertj from 2.34.0 to 2.35.0 by @dependabot in https://github.com/xenit-eu/thunx/pull/40
* Bump json-unit-assertj from 2.35.0 to 2.36.0 by @dependabot in https://github.com/xenit-eu/thunx/pull/50
* Bump testcontainers-bom from 1.17.3 to 1.17.5 by @dependabot in https://github.com/xenit-eu/thunx/pull/45
* Bump org.ajoberstar.reckon from 0.13.1 to 0.16.1 by @dependabot in https://github.com/xenit-eu/thunx/pull/30
* Bump spring-cloud-dependencies from 2021.0.1 to 2021.0.4 by @dependabot in https://github.com/xenit-eu/thunx/pull/51

**Full Changelog**: https://github.com/xenit-eu/thunx/compare/v0.3.3...v0.4.0

## [0.3.3] (2022-04-14)

### Changed

* H2 is a test-runtime-only dependency, version managed by spring boot platform by [@tgeens] in https://github.com/xenit-eu/thunx/pull/27
* Improved entity lookup by [@vierbergenlars] in https://github.com/xenit-eu/thunx/pull/32
* Removed dependency on `spring-content-rest` by [@tgeens] in https://github.com/xenit-eu/thunx/pull/37

### Dependency updates

* Bump org.springframework.boot from 2.4.6 to 2.6.6 by [@tgeens] in https://github.com/xenit-eu/thunx/pull/36
* Bump spring-content-rest from 1.2.2 to 2.0.0 by [@dependabot] in https://github.com/xenit-eu/thunx/pull/22
* Bump spring-content-rest from 2.0.0 to 2.1.0 by [@dependabot] in https://github.com/xenit-eu/thunx/pull/33
* Bump json-unit-assertj from 2.31.0 to 2.32.0 by [@dependabot] in https://github.com/xenit-eu/thunx/pull/31
* Bump json-unit-assertj from 2.32.0 to 2.33.0 by [@dependabot] in https://github.com/xenit-eu/thunx/pull/34

## [0.3.2] (2022-02-09)

### Fixed
* Updated `opa-async-java-client` to 0.3.0:
  * fixes _InvalidDefinitionException: Java 8 date/time type `java.time.Instant` not supported_ - see [xenit-eu/opa-java-client #3] - [#21]

### Changed
* Cleanup dependencies declaration, use platform version management were possible [#25]
* Updated dependencies

**Full Changelog**: https://github.com/xenit-eu/thunx/compare/v0.3.1...v0.3.2

[xenit-eu/opa-java-client #3]: https://github.com/xenit-eu/opa-java-client/issues/3
[#21]: https://github.com/xenit-eu/thunx/pull/21
[#25]: https://github.com/xenit-eu/thunx/pull/25

## [0.3.1] (2022-02-01)

### Fixed

* Starters: Thunx API starter now pulls in `thunx-predicates-querydsl`

**Full Changelog**: https://github.com/xenit-eu/thunx/compare/v0.3.0...v0.3.1

## [0.3.0] (2022-01-31)

### Added

* Created Spring Boot starters - [#6]
  * for Spring Cloud Gateway: `thunx-gateway-spring-boot-starter`
  * for Spring Data REST: `thunx-api-spring-boot-starter`

### Fixed

* QueryDSL: properly handle `IS NULL` - [#9]
* Better url matching to detect Spring Data REST repository-urls - [#10]
* QueryDSL: fixed AND/OR with more than 2 terms - [#12]

### Changed

* Simplified API: changed resolve into a visitor to reduce expression to minimal form - [#11]

[0.3.0]: https://github.com/xenit-eu/thunx/releases/tag/v0.3.0
[#6]: https://github.com/xenit-eu/thunx/pull/6
[#8]: https://github.com/xenit-eu/thunx/pull/8
[#9]: https://github.com/xenit-eu/thunx/pull/9
[#10]: https://github.com/xenit-eu/thunx/pull/10
[#12]: https://github.com/xenit-eu/thunx/pull/12
[#11]: https://github.com/xenit-eu/thunx/pull/11

**Full Changelog**: https://github.com/xenit-eu/thunx/compare/v0.2.0...v0.3.0

## [0.2.0] (2021-10-21)

### Added

* Support OAuth 2.0 Client-Credentials Grant for Machine-to-Machine authentication and authorization - [#7]

### Changed

* `@EnableAbac` replaces `RootResourceInformationHandlerMethodArgumentResolver` bean with abac-enabled variant - [#2]
* An `IllegalStateException` is thrown when using @EnableAbac but repositories do not extend `QuerydslPredicateExecutor` - [#3]

[0.2.0]: https://github.com/xenit-eu/thunx/releases/tag/v0.2.0
[#7]: https://github.com/xenit-eu/thunx/pull/7
[#2]: https://github.com/xenit-eu/thunx/pull/2
[#3]: https://github.com/xenit-eu/thunx/pull/3

**Full Changelog**: https://github.com/xenit-eu/thunx/compare/v0.1.0...v0.2.0

## [0.1.0] (2021-07-20)

### Added

- Initial Release

[0.1.0]: https://github.com/xenit-eu/thunx/releases/tag/v0.1.0

[@dependabot]: https://github.com/dependabot
[@vierbergenlars]: https://github.com/vierbergenlars
[@tgeens]: https://github.com/tgeens

