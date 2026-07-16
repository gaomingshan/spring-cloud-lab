# Root Lombok and MapStruct Configuration

## Goal

Make Lombok available to every Maven child module through the root `pom.xml`, remove repeated Lombok declarations from individual modules, and configure annotation processing so Lombok and MapStruct work together consistently.

## Scope

- Keep dependency versions in the root POM.
- Add Lombok and MapStruct as root-level compile-time dependencies with `provided` scope.
- Configure the root `maven-compiler-plugin` with the Lombok, Lombok-MapStruct binding, and MapStruct processors.
- Remove module-level Lombok dependencies from modules that currently declare them.
- Do not change Java source behavior or add unrelated Maven dependencies.

## Version Compatibility

- Lombok: `1.18.38`
- MapStruct: `1.6.3`
- Lombok-MapStruct binding: `0.2.0`
- Maven Compiler Plugin: `3.14.0`

`lombok-mapstruct-binding` is included as an annotation processor to make MapStruct see Lombok-generated accessors and constructors during compilation. `mapstruct-processor` remains processor-only and is not added to runtime dependencies.

## Maven Design

The root POM will expose `org.projectlombok:lombok` and `org.mapstruct:mapstruct` through its regular `<dependencies>` section, both with `provided` scope. Because child POMs inherit root dependencies, source modules can use Lombok annotations without repeating dependency declarations, while Lombok and MapStruct stay out of packaged runtime artifacts.

The root `maven-compiler-plugin` configuration will define `annotationProcessorPaths` for:

1. Lombok
2. `lombok-mapstruct-binding`
3. `mapstruct-processor`

The same compiler configuration will remain available through both root plugin configuration and plugin management inheritance.

## Validation

Run `mvn clean package -DskipTests` from the repository root. The build must compile and package all modules without executing tests. Confirm that no module POM retains a direct Lombok dependency and that the resulting diff has no whitespace errors.

## Out of Scope

- Migrating existing Java classes to MapStruct.
- Adding tests or changing the repository's test execution policy.
- Changing Spring Boot, Spring Cloud, Java, or other platform versions.
- Modifying existing unrelated staged or working-tree changes.
