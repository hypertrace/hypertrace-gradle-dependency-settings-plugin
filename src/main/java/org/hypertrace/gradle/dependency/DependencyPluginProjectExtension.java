package org.hypertrace.gradle.dependency;

import javax.inject.Inject;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public class DependencyPluginProjectExtension {
  static final String EXTENSION_NAME = "dependencySettings";

  public final ListProperty<String> configurationsToLock;

  public final Property<Boolean> autoApplyBom;

  @Inject
  public DependencyPluginProjectExtension(ObjectFactory objectFactory) {
    this.configurationsToLock =
        objectFactory.listProperty(String.class);
    this.autoApplyBom = objectFactory.property(Boolean.class).convention(true);
  }
}
