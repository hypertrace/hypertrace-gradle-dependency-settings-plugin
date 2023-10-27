package org.hypertrace.gradle.dependency;

import javax.inject.Inject;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

public class DependencyPluginSettingExtension {
  static final String EXTENSION_NAME = "dependencySettings";
  private static final String DEFAULT_CATALOG_GROUP = "org.hypertrace.bom";
  private static final String DEFAULT_CATALOG_ARTIFACT = "hypertrace-version-catalog";
  private static final String DEFAULT_CATALOG_NAME = "commonLibs";
  private static final String DEFAULT_BOM_ARTIFACT_NAME = "hypertrace.bom";
  private static final String DEFAULT_BOM_VERSION_NAME = "hypertrace.bom";
  private static final boolean DEFAULT_USE_DEPENDENCY_LOCKING = true;

  public final Property<String> catalogGroup;
  public final Property<String> catalogArtifact;
  public final Property<String> catalogVersion;
  public final Property<String> catalogName;
  public final Property<Boolean> useDependencyLocking;
  public final Property<String> bomArtifactName;
  public final Property<String> bomVersionName;
  public final Property<String> bomVersion;

  @Inject
  public DependencyPluginSettingExtension(ObjectFactory objectFactory) {
    this.catalogGroup = objectFactory.property(String.class).convention(DEFAULT_CATALOG_GROUP);
    this.catalogGroup.disallowUnsafeRead();
    this.catalogArtifact =
        objectFactory.property(String.class).convention(DEFAULT_CATALOG_ARTIFACT);
    this.catalogArtifact.disallowUnsafeRead();
    this.catalogVersion = objectFactory.property(String.class);
    this.catalogVersion.disallowUnsafeRead();
    this.catalogName = objectFactory.property(String.class).convention(DEFAULT_CATALOG_NAME);
    this.catalogName.disallowUnsafeRead();
    this.useDependencyLocking =
        objectFactory.property(Boolean.class).convention(DEFAULT_USE_DEPENDENCY_LOCKING);
    this.useDependencyLocking.disallowUnsafeRead();
    this.bomArtifactName =
        objectFactory.property(String.class).convention(DEFAULT_BOM_ARTIFACT_NAME);
    this.bomArtifactName.disallowUnsafeRead();
    this.bomVersionName = objectFactory.property(String.class).convention(DEFAULT_BOM_VERSION_NAME);
    this.bomVersionName.disallowUnsafeRead();
    this.bomVersion = objectFactory.property(String.class).convention(this.catalogVersion);
    this.bomVersion.disallowUnsafeRead();
  }

  Provider<String> getCatalogArtifactNotation() {
    return catalogGroup.flatMap(
        groupString ->
            catalogArtifact.flatMap(
                artifactString ->
                    catalogVersion.map(
                        versionString ->
                            String.format(
                                "%s:%s:%s", groupString, artifactString, versionString))));
  }
}
