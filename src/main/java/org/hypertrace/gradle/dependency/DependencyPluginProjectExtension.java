package org.hypertrace.gradle.dependency;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.List;

public class DependencyPluginProjectExtension {
  static final String EXTENSION_NAME = "dependencySettings";
  private static final List<String> DEFAULT_LOCKED_CONFIGURATIONS =
      List.of(
          JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME,
          JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME,
          JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME,
          JavaPlugin.TEST_COMPILE_CLASSPATH_CONFIGURATION_NAME,
          JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME);
  public final ListProperty<String> configurationsToLock;

  public final Property<Boolean> autoApplyBom;

  @Inject
  public DependencyPluginProjectExtension(ObjectFactory objectFactory) {
    this.configurationsToLock =
        objectFactory.listProperty(String.class).convention(DEFAULT_LOCKED_CONFIGURATIONS);
    this.autoApplyBom = objectFactory.property(Boolean.class).convention(true);
  }
}
