import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.date
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.java)
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.intellij)
    alias(libs.plugins.changelog)
}

fun properties(key: String) = providers.gradleProperty(key)

@Suppress("UnstableApiUsage")
val gitHash = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
}.standardOutput.asText.get().trim()

group = findProperty("group")!! as String
val versionString = findProperty("version")!! as String
version =
    if (versionString.contains("-")) "$versionString-$gitHash" else versionString

repositories {
    maven(url = "https://maven.aliyun.com/repository/public")
    maven(url = "https://www.jetbrains.com/intellij-repository/releases")
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2.6")
    type.set("IU") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
}

changelog {
    header.set(provider { "[${version.get()}] - ${date()}" })
    headerParserRegex.set("""(\d+\.\d+\.\d+)""".toRegex())
}

dependencies {
    implementation(platform(libs.jackson.bom))
    implementation(platform(libs.guava.bom))
    implementation(libs.bundles.jackson)
    implementation(libs.mapstruct)
    implementation(libs.guava)
    implementation(libs.commons.lang3)
    implementation(libs.jetbrains.annotations)
    annotationProcessor(libs.mapstruct.processor)
}

tasks {

    register("installGitHooks", Copy::class) {
        group = "setup"
        description = "Copies git hooks to .git/hooks"
        // 源文件路径
        val hooksDir = file("${project.rootDir}/.git/hooks")
        val sourceDir = file("${project.rootDir}/scripts/git/hooks")
        val updateLicenseShell = file("${project.rootDir}/update_license.sh")
        // 将文件从源目录拷贝到目标目录
        from(sourceDir)
        // 目标目录
        into(hooksDir)
        // 设置执行权限（可选）
        doLast {
            // 设置 update_license.sh 的执行权限
            updateLicenseShell.setExecutable(true)
            // 设置 pre-commit 的执行权限
            hooksDir.resolve("pre-commit").setExecutable(true)
            // 设置 commit-msg 的执行权限
            hooksDir.resolve("commit-msg").setExecutable(true)
        }
    }

    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
        @Suppress("SpellCheckingInspection")
        options.compilerArgs.add("-Amapstruct.unmappedTargetPolicy=IGNORE")
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }

    jar {
        into("META-INF/") {
            from(rootProject.file("LICENSE"))
        }
        manifest {
            attributes(
                "Implementation-Title" to archiveBaseName.get(),
                "Implementation-Version" to archiveVersion.get(),
                "Application-Version" to archiveVersion.get(),
                "Built-Gradle" to gradle.gradleVersion,
                "Build-OS" to System.getProperty("os.name"),
                "Build-Jdk" to System.getProperty("java.version"),
                "Build-Timestamp" to OffsetDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
            )
        }
    }

    patchPluginXml {
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")
        pluginDescription = projectDir.resolve("DESCRIPTION.md").readText()
        changeNotes.set(provider {
            with(changelog) {
                renderItem(
                    (getOrNull(version.get()) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML
                )
            }
        })
    }

    signPlugin {
        val certificateChain = "CERTIFICATE_CHAIN"
        val privateKey = "PRIVATE_KEY"
        val privateKeyPassword = "PRIVATE_KEY_PASSWORD"
        if (!System.getenv(certificateChain).isNullOrBlank() &&
            !System.getenv(privateKey).isNullOrBlank() &&
            !System.getenv(privateKeyPassword).isNullOrBlank()
        ) {
            certificateChainFile.set(file(System.getenv(certificateChain)))
            privateKeyFile.set(file(System.getenv(privateKey)))
            password.set(System.getenv(privateKeyPassword))
        }
    }

    publishPlugin {
        val publishToken = "PUBLISH_TOKEN"
        if (!System.getenv(publishToken).isNullOrBlank()) {
            token.set(System.getenv(publishToken))
        }
    }
}
