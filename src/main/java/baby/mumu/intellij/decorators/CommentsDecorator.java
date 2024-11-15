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
package baby.mumu.intellij.decorators;

import baby.mumu.intellij.services.CommentService;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * 注释装饰器
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.0.0
 */
public class CommentsDecorator implements ProjectViewNodeDecorator {

  @Override
  public void decorate(@NotNull ProjectViewNode<?> node, PresentationData data) {
    VirtualFile file = node.getVirtualFile();
    if (file != null) {
      CommentService commentService = ApplicationManager.getApplication()
        .getService(CommentService.class);

      // 仅在注释可见时添加注释内容
      if (commentService.isCommentsVisible()) {
        String comment = commentService.getCommentForFile(node.getProject(), file);
        if (StringUtils.isNotBlank(comment)) {
          data.setLocationString("// " + comment);
        }
      }
    }
  }
}