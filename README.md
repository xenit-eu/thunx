# contentcloud-abac  
![build](https://github.com/xenit-eu/contentcloud-abac/workflows/build/badge.svg?branch=main)

Content Cloud ABAC is a set of libraries that provides [Attribute Based Access Control](https://en.wikipedia.org/wiki/Attribute-based_access_control)
by integrating [OpenPolicyAgent](https://www.openpolicyagent.org/) with [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
and [Spring Data REST](https://spring.io/projects/spring-data-rest).

This project uses a distributed authorization architecture, by applying:
* early access decisions at the API Gateway 
* postponed access decisions in the Spring Data REST service

When the API Gateway does not have sufficient contextual information to grant or deny access,
the policy decision is delegated to the Spring Data REST service. This service receives an
access-predicate from the Gateway and rewrites the database queries to comply with the predicate,
by converting this to a [QueryDSL](http://www.querydsl.com/) predicate.

## Project overview

### Subprojects

This repository has several modules:

* `contentcloud-abac-pdp` is a central abstraction for a Policy Decision Point (PDP)
* `contentcloud-abac-pdp-opa` is an implementation of the PDP abstraction, that uses [OpenPolicyAgent](https://www.openpolicyagent.org/).
* `contentcloud-abac-predicates` is a set of (vendor-neutral) data structures to model authorization policy expressions 
* `contentcloud-abac-predicates-json` is a JSON-serialization library for thunk-expressions
* `contentcloud-abac-predicates-protobuf` is a protobuf-serialization library for thunk-expressions (NOT IMPLEMENTED)
* `contentcloud-abac-predicates-querydsl` is a library to convert thunk-expressions into QueryDSL predicates
* `contentcloud-abac-spring` provides an integration with Spring Cloud Gateway and Spring Data REST

## Getting Started

### Prerequisites

* Java 11+

### Installation

## License

Apache License Version 2.0
