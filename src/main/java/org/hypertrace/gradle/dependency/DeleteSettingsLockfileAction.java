package org.hypertrace.gradle.dependency;

import java.io.File;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.flow.FlowAction;
import org.gradle.api.flow.FlowParameters;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.hypertrace.gradle.dependency.DeleteSettingsLockfileAction.Parameters;

public abstract class DeleteSettingsLockfileAction implements FlowAction<Parameters> {
  interface Parameters extends FlowParameters {
    @Input
    Property<File> getDeleteTarget();
  }

  @Inject
  protected abstract FileSystemOperations getFileSystemOperations();

  @Override
  public void execute(@Nonnull Parameters parameters) {
    getFileSystemOperations().delete(spec -> spec.delete(parameters.getDeleteTarget()));
  }
}
