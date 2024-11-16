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
package baby.mumu.intellij.kotlin.dos

import org.jetbrains.exposed.sql.Table

/**
 * 注释表
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.1.0
 */
object MuMuComments : Table("mumu_comments") {

    // 主键
    val id = integer("id").autoIncrement()

    // 相对路径
    val relativePath = varchar("relative_path", 500).uniqueIndex()

    // 注释内容
    val comment = text("comment")

    override val primaryKey = PrimaryKey(id)
}