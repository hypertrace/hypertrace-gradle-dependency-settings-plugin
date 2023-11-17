# Hypertrace Dependency Settings Plugin

###### org.hypertrace.dependency-settings

### Purpose

This plugin configures various aspects of dependency management. Where values are configurable, a
default is specified.
Certain values are configurable via `dependencySettings` extension inside each project
build.gradle.kts, but most are
configured only once in a `dependencySettings` extension in settings.gradle.kts.

- Adds a version catalog (default name: `commonLibs`, default
  artifact: `org.hypertrace.bom:hypertrace-version-catalog:<catalogVersion>`. catalogVersion must be
  set explicitly)
- Renames the default `libs` catalog (from `libs.versions.toml`) to `localLibs` to disambiguate
- For each java project:
    - Adds dependency repositories of mavenLocal, mavenCentral, confluent and hypertrace
    - If `autoApplyBom` is specified (default: true), adds a BOM dependency to the `api`
      configuration (falling back
      to `implementation` if `api` is unavailable). The BOM reference to use (`bomArtifactName` -
      default `hypertrace.bom`) and version can also be configured. `bomVersionName` (defaults
      to `hypertrace.bom`)
      describes the name of the version property in the catalog and `bomVersion` (defaults to
      latest - `+`) describes the value to assign.
    - If `useDependencyLocking` is specified (default: true), configures strict dependency locking
      on certain
      configurations (
      default: `annotationProcessor`, `compileClasspath`, `runtimeClasspath`, `testCompileClasspath`, `testRuntimeClasspath`)
    - If `useDependencyLocking` is specified (default: true), adds a project
      task `resolveAndLockAll`which can be use in
      conjunction with the `--write-locks` flag to update all project lockfiles.

Example usage in `settings.gradle.kts`:

```kts
    plugins {
  id("org.hypertrace.dependency-settings") version "0.1.0"
}


configure<DependencyPluginSettingExtension> {
  catalogVersion.set("0.1.0")
}
```