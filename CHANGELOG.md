# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [0.3.1] (2021-02-01)

### Fixed

* Starters: Thunx API starter now pulls in `thunx-predicates-querydsl`

## [0.3.0] (2021-01-31)

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
