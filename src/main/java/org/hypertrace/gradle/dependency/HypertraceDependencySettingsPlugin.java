package org.hypertrace.gradle.dependency;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.api.artifacts.VersionCatalog;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import org.gradle.api.artifacts.dsl.LockMode;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.initialization.Settings;
import org.gradle.api.initialization.dsl.VersionCatalogBuilder;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class HypertraceDependencySettingsPlugin implements Plugin<Settings> {
  private static final String HYPERTRACE_REPOSITORY_URL =
      "https://hypertrace.jfrog.io/artifactory/maven";
  private static final String CONFLUENT_REPOSITORY_URL = "https://packages.confluent.io/maven";

  @Override
  public void apply(@NotNull Settings settings) {
    DependencyPluginSettingExtension settingExtension = this.createSettingsExtension(settings);
    settings
        .getGradle()
        .settingsEvaluated(
            unused -> {
              this.addDependencyRepositories(settings);
              this.addVersionCatalog(settings, settingExtension);
            });

    settings
        .getGradle()
        .allprojects(
            project ->
                project
                    .getPluginManager()
                    .withPlugin(
                        "java",
                        unused -> {
                          DependencyPluginProjectExtension projectExtension =
                              this.addProjectExtension(project);
                          this.addBomDependencyIfRequested(
                              project, settingExtension, projectExtension);
                          if (settingExtension.useDependencyLocking.get()) {
                            this.addDependencyLocking(project, projectExtension);
                            this.addLockTask(project, projectExtension);
                          }
                        }));
  }

  private void addBomDependencyIfRequested(
      Project project,
      DependencyPluginSettingExtension settingExtension,
      DependencyPluginProjectExtension projectExtension) {
    project.afterEvaluate(
        unused -> {
          projectExtension.autoApplyBom.finalizeValueOnRead();
          if (projectExtension.autoApplyBom.getOrElse(false)) {
            project
                .getDependencies()
                .addProvider(
                    resolveBomHostConfigurationName(project),
                    project
                        .getDependencies()
                        .platform(resolveBomDependency(project, settingExtension)));
          }
        });
  }

  private String resolveBomHostConfigurationName(Project project) {
    return Optional.ofNullable(
            project.getConfigurations().findByName(JavaPlugin.API_CONFIGURATION_NAME))
        .orElseGet(
            () ->
                project.getConfigurations().getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME))
        .getName();
  }

  private Provider<MinimalExternalModuleDependency> resolveBomDependency(
      Project project, DependencyPluginSettingExtension settingExtension) {
    return settingExtension
        .catalogName
        .map(name -> getCatalogByName(project, name))
        .flatMap(
            catalog ->
                settingExtension.bomArtifactName.flatMap(
                    artifactName ->
                        catalog
                            .findLibrary(artifactName)
                            .orElseThrow(
                                () ->
                                    new GradleException(
                                        "No artifact available in catalog declared alias "
                                            + artifactName))));
  }

  private DependencyPluginSettingExtension createSettingsExtension(Settings target) {
    return target
        .getExtensions()
        .create(
            DependencyPluginSettingExtension.EXTENSION_NAME,
            DependencyPluginSettingExtension.class);
  }

  private DependencyPluginProjectExtension addProjectExtension(Project project) {
    return project
        .getExtensions()
        .create(
            DependencyPluginProjectExtension.EXTENSION_NAME,
            DependencyPluginProjectExtension.class);
  }

  private void addVersionCatalog(
      Settings settings, DependencyPluginSettingExtension settingExtension) {
    settings.dependencyResolutionManagement(
        dependencyResolutionManagement -> {
          VersionCatalogBuilder catalogBuilder =
              dependencyResolutionManagement
                  .getVersionCatalogs()
                  .create(settingExtension.catalogName.get());
          catalogBuilder.from(settingExtension.getCatalogArtifactNotation().get());
          catalogBuilder.version(
              settingExtension.bomVersionName.get(), settingExtension.bomVersion.get());
        });
  }

  private void addDependencyRepositories(Settings settings) {
    settings.dependencyResolutionManagement(
        dependencyResolutionManagement ->
            dependencyResolutionManagement.repositories(
                repositoryHandler -> {
                  repositoryHandler.add(repositoryHandler.mavenLocal());
                  repositoryHandler.add(repositoryHandler.mavenCentral());
                  repositoryHandler.add(repositoryHandler.maven(this::configureConfluent));
                  repositoryHandler.add(repositoryHandler.maven(this::configureHypertrace));
                }));
  }

  private void addDependencyLocking(
      Project targetProject, DependencyPluginProjectExtension extension) {
    targetProject.getDependencyLocking().getLockMode().set(LockMode.STRICT);
    targetProject.afterEvaluate(
        unused ->
            targetProject
                .getConfigurations()
                .configureEach(
                    configuration -> {
                      extension.configurationsToLock.finalizeValueOnRead();
                      if (extension.configurationsToLock.get().contains(configuration.getName())) {
                        configuration.getResolutionStrategy().activateDependencyLocking();
                      }
                    }));
  }

  private void addLockTask(Project targetProject, DependencyPluginProjectExtension extension) {
    targetProject
        .getTasks()
        .register("resolveAndLockAll")
        .configure(
            task -> {
              task.doFirst(
                  empty -> {
                    if (!targetProject.getGradle().getStartParameter().isWriteDependencyLocks()) {
                      throw new GradleException(
                          task.getPath()
                              + "must be run from the command line with the '--write-locks' flag");
                    }
                  });
              task.doLast(
                  empty ->
                      extension.configurationsToLock.get().stream()
                          .map(
                              configurationName ->
                                  targetProject.getConfigurations().getByName(configurationName))
                          .filter(Configuration::isCanBeResolved)
                          .forEach(Configuration::resolve));
            });
  }

  private VersionCatalog getCatalogByName(Project project, String catalogName) {
    return project.getExtensions().getByType(VersionCatalogsExtension.class).named(catalogName);
  }

  private void addHypertraceRepositoryToProject(Project project) {
    project
        .getRepositories()
        .maven(
            mavenArtifactRepository -> {
              mavenArtifactRepository.setName("hypertrace-maven");
              mavenArtifactRepository.setUrl(HYPERTRACE_REPOSITORY_URL);
            });
  }

  private void configureConfluent(MavenArtifactRepository artifactRepository) {
    artifactRepository.setName("confluent-maven");
    artifactRepository.setUrl(CONFLUENT_REPOSITORY_URL);
  }

  private void configureHypertrace(MavenArtifactRepository artifactRepository) {
    artifactRepository.setName("hypertrace-maven");
    artifactRepository.setUrl(HYPERTRACE_REPOSITORY_URL);
  }
}
