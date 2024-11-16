/*
 * Copyright (c) 2024-2024, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package baby.mumu.intellij.widgets;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts.ConfigurableName;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * 注释功能开始状态小组件
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.1.0
 */
public class ToggleCommentVisibilityWidgetFactory extends StatusBarEditorBasedWidgetFactory {


  @Override
  public @NotNull @NonNls String getId() {
    return "ToggleCommentVisibilityWidget";
  }

  @Override
  public @NotNull @ConfigurableName String getDisplayName() {
    return "Toggle Comment";
  }

  @Override
  public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
    return new ToggleCommentVisibilityWidget(project);
  }
}
