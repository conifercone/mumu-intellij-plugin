import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.date
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.java)
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.intellij)
    alias(libs.plugins.changelog)
}

fun properties(key: String) = providers.gradleProperty(key)

val gitHash = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
}.standardOutput.asText.get().trim()
val suffixes = listOf("-alpha", "-beta", "-snapshot", "-dev", "-test", "-pre")
val now: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

@Suppress("SpellCheckingInspection")
val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssXXX")
val formattedTime: String = now.format(formatter)
fun endsWithAny(input: String, suffixes: List<String>): Boolean {
    return suffixes.any { input.endsWith(it, ignoreCase = true) }
}

group = findProperty("group")!! as String
val versionString = findProperty("version")!! as String
version =
    if (endsWithAny(
            versionString,
            suffixes
        )
    ) "$versionString-$gitHash-$formattedTime" else versionString

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = properties("pluginUntilBuild")
        }
        description = projectDir.resolve("DESCRIPTION.md").readText()
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
    signing {
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

    publishing {
        val publishToken = "PUBLISH_TOKEN"
        if (!System.getenv(publishToken).isNullOrBlank()) {
            token.set(System.getenv(publishToken))
        }
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaUltimate(properties("intellijIdeaUltimate"))
        pluginVerifier()
        zipSigner()
    }
    implementation(platform(libs.guava.bom))
    implementation(libs.bundles.exposed)
    implementation(libs.sqlite.jdbc)
    implementation(libs.mapstruct)
    implementation(libs.guava)
    implementation(libs.flyway.core)
    implementation(libs.commons.lang3)
    implementation(libs.jetbrains.annotations)
    implementation(libs.hikariCP)
    annotationProcessor(libs.mapstruct.processor)
}

changelog {
    header.set(provider { "[${version.get()}] - ${date()}" })
    headerParserRegex.set("""(\d+\.\d+\.\d+)""".toRegex())
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
        sourceCompatibility = JavaVersion.VERSION_21.toString()
        targetCompatibility = JavaVersion.VERSION_21.toString()
        @Suppress("SpellCheckingInspection")
        options.compilerArgs.add("-Amapstruct.unmappedTargetPolicy=IGNORE")
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
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
}
