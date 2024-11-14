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
package baby.mumu.intellij.services;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 注释服务
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.0.0
 */
@State(
  name = "FileComments",
  storages = @Storage(value = "mumu_comments.xml")
)
public class CommentService implements PersistentStateComponent<CommentService.State> {

  public static class State {

    public Map<String, String> fileComments = new HashMap<>();
    public boolean commentsVisible = true;
  }

  private State state = new State();

  @Nullable
  @Override
  public State getState() {
    return state;
  }

  @Override
  public void loadState(@NotNull State state) {
    this.state = state;
  }

  // 获取当前项目的 .idea 目录路径
  private Optional<File> getIdeaFolderForProject(@NotNull Project project) {
    File ideaDir = new File(project.getBasePath(), ".idea");
    if (!ideaDir.exists()) {
      return Optional.empty();
    }
    return Optional.of(ideaDir);
  }

  // 获取存储文件的完整路径
  private Optional<File> getCustomStorageFile(@NotNull Project project) {
    return getIdeaFolderForProject(project).map(
      file -> new File(file, "mumu_comments.xml"));
  }

  // 从文件中加载注释
  public void loadCommentsFromFile(@NotNull Project project) {
    getCustomStorageFile(project).ifPresent(storageFile -> {
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(storageFile);
        document.getDocumentElement().normalize();

        NodeList commentNodes = document.getElementsByTagName("comment");

        for (int i = 0; i < commentNodes.getLength(); i++) {
          Element commentElement = (Element) commentNodes.item(i);
          String file = commentElement.getAttribute("file");
          String comment = commentElement.getTextContent();
          state.fileComments.put(file, comment);
        }
      } catch (Exception ignore) {
      }
    });
  }

  // 保存注释到文件
  public void saveCommentsToFile(@NotNull Project project) {
    getCustomStorageFile(project).ifPresent(storageFile -> {
      try {
        // 创建 XML 文档
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        // 根元素
        Element rootElement = document.createElement("comments");
        document.appendChild(rootElement);

        // 遍历并添加注释
        for (Map.Entry<String, String> entry : state.fileComments.entrySet()) {
          Element commentElement = document.createElement("comment");
          commentElement.setAttribute("file", entry.getKey());
          commentElement.setTextContent(entry.getValue());
          rootElement.appendChild(commentElement);
        }
        // 写入文件
        // 可以使用 Transformer 将 Document 写入 XML 文件
        // 创建 TransformerFactory 实例
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // 创建 Transformer 实例
        Transformer transformer = transformerFactory.newTransformer();
        // 可选：设置输出格式（例如：缩进）
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        // 创建 DOMSource（将 Document 转换为源）
        DOMSource source = new DOMSource(document);
        // 创建 StreamResult（将内容写入文件）
        StreamResult result = new StreamResult(storageFile);
        // 执行转换，将 Document 输出到文件
        transformer.transform(source, result);
      } catch (Exception ignore) {
      }
    });
  }

  // 添加文件注释
  public void addCommentForFile(@NotNull Project project, @NotNull VirtualFile file,
    @NotNull String comment) {
    state.fileComments.put(getRelativePath(project, file), comment);
  }

  @Nullable
  public String getCommentForFile(@NotNull Project project, @NotNull VirtualFile file) {
    return state.fileComments.get(getRelativePath(project, file));
  }

  public void removeCommentForFile(@NotNull Project project, @NotNull VirtualFile file) {
    state.fileComments.remove(getRelativePath(project, file));
  }

  public boolean isCommentsVisible() {
    return state.commentsVisible;
  }

  public void setCommentsVisible(boolean visible) {
    state.commentsVisible = visible;
  }

  public static @NotNull String getRelativePath(@NotNull Project project,
    @NotNull VirtualFile file) {
    if (StringUtils.isBlank(project.getBasePath())) {
      return "";
    }
    if (file.getPath().equals(project.getBasePath())) {
      return "./";
    } else {
      return file.getPath().replace(project.getBasePath(), ".");
    }
  }
}