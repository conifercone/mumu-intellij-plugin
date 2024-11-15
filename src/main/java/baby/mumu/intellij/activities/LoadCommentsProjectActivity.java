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
package baby.mumu.intellij.activities;

import baby.mumu.intellij.listeners.CommentFileUpdateListener;
import baby.mumu.intellij.services.CommentService;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 加载项目所有注释
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.0.0
 */
public class LoadCommentsProjectActivity implements ProjectActivity, Disposable {

  private final CommentFileUpdateListener commentFileUpdateListener = new CommentFileUpdateListener();
  private Project project;

  @Override
  public @Nullable Object execute(@NotNull Project project,
    @NotNull Continuation<? super Unit> continuation) {
    this.project = project;
    // 在项目启动时自动加载注释
    CommentService commentService = project.getService(CommentService.class);
    commentService.loadCommentsFromFile(project);  // 自动加载注释数据
    ProjectManager.getInstance().addProjectManagerListener(project, commentFileUpdateListener);
    return null;
  }

  @Override
  public void dispose() {
    if (project != null) {
      ProjectManager.getInstance().removeProjectManagerListener(project, commentFileUpdateListener);
    }
  }
}
