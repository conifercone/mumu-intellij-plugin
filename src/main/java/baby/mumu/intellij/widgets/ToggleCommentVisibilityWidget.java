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

import static baby.mumu.intellij.constants.WidgetIds.TOGGLE_COMMENT_VISIBILITY_WIDGET_ID;

import baby.mumu.intellij.kotlin.tools.TranslationBundleTool;
import baby.mumu.intellij.services.CommentConfigService;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 注释功能开始状态小组件
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.1.0
 */
public class ToggleCommentVisibilityWidget extends EditorBasedWidget {

  private final Project project;

  protected ToggleCommentVisibilityWidget(@NotNull Project myProject) {
    super(myProject);
    this.project = myProject;
  }

  @Override
  public @NotNull String ID() {
    return TOGGLE_COMMENT_VISIBILITY_WIDGET_ID;
  }

  @Override
  public @Nullable WidgetPresentation getPresentation() {
    return new IconPresentation() {
      @Override
      public @Nullable Icon getIcon() {
        return project.getService(CommentConfigService.class).isCommentsVisible()
          ? IconLoader.findIcon("/icons/turnOff.svg", ToggleCommentVisibilityWidget.class)
          : IconLoader.findIcon("/icons/turnOn.svg", ToggleCommentVisibilityWidget.class);
      }

      @Override
      public @NotNull String getTooltipText() {
        return project.getService(CommentConfigService.class).isCommentsVisible()
          ? TranslationBundleTool.INSTANCE.getAdaptedMessage("comment.is.active")
          : TranslationBundleTool.INSTANCE.getAdaptedMessage("comment.is.inactive");
      }

      @Override
      public @NotNull Consumer<MouseEvent> getClickConsumer() {
        return __ -> {
          project.getService(CommentConfigService.class).setCommentsVisible(
            !project.getService(CommentConfigService.class).isCommentsVisible());
          WindowManager.getInstance().getStatusBar(project).updateWidget(ID());
          ProjectView.getInstance(project).refresh();
        };
      }
    };
  }
}
