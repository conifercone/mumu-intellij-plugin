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
package baby.mumu.intellij.actions;

import baby.mumu.intellij.kotlin.services.CommentDbService;
import baby.mumu.intellij.kotlin.tools.TranslationBundleTool;
import baby.mumu.intellij.toolwindows.CommentToolWindowRefreshNotifier;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * 添加注释动作
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.0.0
 */
public class AddCommentAction extends AnAction {

  @Override
  public void update(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    VirtualFile selectedFile = e.getData(PlatformCoreDataKeys.VIRTUAL_FILE);
    e.getPresentation().setEnabled(
      project != null && selectedFile != null && project.getService(CommentDbService.class)
        .getConnected()
        && project.getService(CommentDbService.class).getByRelativePath(project, selectedFile)
        == null);
    e.getPresentation()
      .setText(TranslationBundleTool.INSTANCE.getAdaptedMessage("add.comment.action.text"));
    e.getPresentation().setDescription(
      TranslationBundleTool.INSTANCE.getAdaptedMessage("add.comment.action.description"));
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    //noinspection DuplicatedCode
    Project project = e.getProject();
    if (project == null) {
      return;
    }
    VirtualFile selectedFile = e.getData(PlatformCoreDataKeys.VIRTUAL_FILE);
    if (selectedFile == null) {
      Messages.showMessageDialog(
        TranslationBundleTool.INSTANCE.getAdaptedMessage("no.file.or.folder.selected"),
        TranslationBundleTool.INSTANCE.getAdaptedMessage("error"),
        Messages.getErrorIcon());
      return;
    }

    // 显示对话框获取注释内容
    String comment = Messages.showInputDialog(
      TranslationBundleTool.INSTANCE.getAdaptedMessage("please.enter.comment"),
      TranslationBundleTool.INSTANCE.getAdaptedMessage("add.comment.title"),
      Messages.getQuestionIcon(), null, new CommentInputValidator());
    if (StringUtils.isNotBlank(comment)) {
      project.getService(CommentDbService.class).insertComment(project, selectedFile, comment);
      selectedFile.refresh(false, true);
      ProjectView.getInstance(project).refresh();
      project.getMessageBus().syncPublisher(CommentToolWindowRefreshNotifier.TOPIC).refresh();
    }
  }
}
