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
package baby.mumu.intellij.kotlin.services

import baby.mumu.intellij.kotlin.dos.MuMuComment
import baby.mumu.intellij.kotlin.dos.MuMuComments
import baby.mumu.intellij.toolwindows.CommentToolWindowRefreshNotifier
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apache.commons.lang3.StringUtils
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

@Suppress("SqlNoDataSourceInspection")
private const val PRAGMA_USER_VERSION_ = "PRAGMA user_version;"

/**
 * 注释数据库服务
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.1.0
 */
@Service(Service.Level.PROJECT)
class CommentDbService : Disposable {

    private var connected = false

    private var database: Database? = null
    private var dataSource: HikariDataSource? = null

    fun getConnected(): Boolean {
        return connected
    }

    fun connectDatabase(project: Project) {
        // 将路径转换为字符串并连接到数据库
        val url = "jdbc:sqlite:" + File(
            File(project.basePath, ".idea"),
            "mumu_comments.db"
        ).path.replace("\\", "/")
        val config = HikariConfig().apply {
            jdbcUrl = url
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 6
            isReadOnly = false
            transactionIsolation = "TRANSACTION_SERIALIZABLE"
        }
        dataSource = HikariDataSource(config)
        database = Database.connect(dataSource!!)
        transaction(database) {
            // 执行一个简单的查询，如检查表是否存在（即使表可能不存在，也不会报错）
            exec(PRAGMA_USER_VERSION_) // 查询数据库版本号
        }
        val currentThread = Thread.currentThread()
        val originalClassLoader = currentThread.contextClassLoader
        val pluginClassLoader = javaClass.classLoader
        try {
            currentThread.contextClassLoader = pluginClassLoader
            // 设置 Flyway 配置
            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .load()
            // 执行迁移
            flyway.migrate()
            connected = true
            project.messageBus.syncPublisher(CommentToolWindowRefreshNotifier.TOPIC).refresh()
        } finally {
            currentThread.contextClassLoader = originalClassLoader
        }
    }

    /**
     * 新增注释
     */
    fun insertComment(project: Project, virtualFile: VirtualFile, comment: String) {
        transaction(database) {
            MuMuComments.insert {
                it[relativePath] = getRelativePath(project, virtualFile)
                it[MuMuComments.comment] = comment
            }
        }
    }

    /**
     * 根据 relativePath 获取 MuMuComments 对象
     */
    fun getByRelativePath(project: Project, virtualFile: VirtualFile): MuMuComment? {
        return transaction(database) {
            MuMuComments
                .selectAll()
                .where(MuMuComments.relativePath.eq(getRelativePath(project, virtualFile)))
                .map { row ->
                    MuMuComment(
                        id = row[MuMuComments.id],
                        relativePath = row[MuMuComments.relativePath],
                        comment = row[MuMuComments.comment]
                    )
                }.singleOrNull() // 如果没有匹配的结果返回 null
        }
    }

    fun removeById(id: Int) {
        transaction(database) {
            MuMuComments.deleteWhere { MuMuComments.id eq id }
        }
    }

    fun removeAll() {
        transaction(database) {
            MuMuComments.deleteAll()
        }
    }

    fun findAll(commentOrPath: String?): List<MuMuComment> {
        return transaction(database) {
            if (commentOrPath.isNullOrEmpty()) {
                // 如果 comment 为 null 或空字符串，则查询所有记录
                MuMuComments
                    .selectAll()
                    .map { row ->
                        MuMuComment(
                            id = row[MuMuComments.id],
                            relativePath = row[MuMuComments.relativePath],
                            comment = row[MuMuComments.comment]
                        )
                    }
            } else {
                // 如果 comment 不为 null，则进行模糊查询
                MuMuComments
                    .selectAll()
                    .where { MuMuComments.comment like "%$commentOrPath%" or (MuMuComments.relativePath like "%$commentOrPath%") }
                    .map { row ->
                        MuMuComment(
                            id = row[MuMuComments.id],
                            relativePath = row[MuMuComments.relativePath],
                            comment = row[MuMuComments.comment]
                        )
                    }
            }
        }
    }


    fun removeByRelativePath(project: Project, virtualFile: VirtualFile) {
        transaction(database) {
            MuMuComments.deleteWhere { relativePath eq getRelativePath(project, virtualFile) }
        }
    }

    fun updateRelativePathByRelativePath(oldPath: String, newPath: String) {
        transaction(database) {
            // 更新与 oldPath 关联的注释记录的 relativePath
            MuMuComments
                .update({ MuMuComments.relativePath eq oldPath }) {
                    it[relativePath] = newPath
                }
        }
    }

    /**
     * 更新注释
     */
    fun updateComment(project: Project, virtualFile: VirtualFile, comment: String) {
        transaction(database) {
            MuMuComments.update(where = {
                MuMuComments.relativePath eq getRelativePath(
                    project,
                    virtualFile
                )
            }) {
                it[MuMuComments.comment] = comment
            }
        }
    }

    private fun getRelativePath(
        project: Project,
        file: VirtualFile
    ): String {
        if (StringUtils.isBlank(project.basePath)) {
            return ""
        }
        return if (file.path == project.basePath) {
            "./"
        } else {
            file.path.replace(project.basePath!!, ".")
        }
    }

    fun getRelativePath(
        project: Project,
        path: String
    ): String {
        if (StringUtils.isBlank(project.basePath)) {
            return ""
        }
        return if (path == project.basePath) {
            "./"
        } else {
            path.replace(project.basePath!!, ".")
        }
    }

    override fun dispose() {
        // 释放资源
        dataSource?.close()
    }
}
