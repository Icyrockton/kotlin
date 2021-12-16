import org.jetbrains.kotlin.ideaExt.idea

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

project.configureJvmToolchain(JdkMajorVersion.JDK_11)

dependencies {
    testImplementation(kotlinStdlib())
    testImplementation(project(":kotlin-reflect"))
    testImplementation(intellijCoreDep()) { includeJars("intellij-core") }
    testImplementation(intellijPluginDep("java"))
    testImplementation(intellijDep()) { includeJars("commons-lang-2.4", "serviceMessages", rootProject = rootProject) }
    testImplementation(project(":kotlin-compiler-runner-unshaded"))
    testImplementation(projectTests(":compiler:tests-common"))
    testImplementation(projectTests(":compiler:tests-common-new"))
    testImplementation(projectTests(":compiler:test-infrastructure"))
    testImplementation(projectTests(":generators:test-generator"))
    testApiJUnit5()

    testRuntimeOnly(intellijDep()) { includeJars("trove4j", "intellij-deps-fastutil-8.4.1-4") }
}

val generationRoot = projectDir.resolve("tests-gen")

sourceSets {
    "main" { none() }
    "test" {
        projectDefault()
        java.srcDirs(generationRoot.name)
    }
}

if (kotlinBuildProperties.isInJpsBuildIdeaSync) {
    apply(plugin = "idea")
    idea {
        module.generatedSourceDirs.addAll(listOf(generationRoot))
    }
}

enum class TestProperty(shortName: String) {
    // Use a separate Gradle property to pass Kotlin/Native home to tests: "kotlin.internal.native.test.nativeHome".
    // Don't use "kotlin.native.home" and similar properties for this purpose, as these properties may have undesired
    // effect on other Gradle tasks (ex: :kotlin-native:dist) that might be executed along with test task.
    KOTLIN_NATIVE_HOME("nativeHome"),
    COMPILER_CLASSPATH("compilerClasspath"),
    TEST_MODE("mode"),
    USE_CACHE("useCache"),
    EXECUTION_TIMEOUT("executionTimeout");

    private val propertyName = "kotlin.internal.native.test.$shortName"

    fun setUpFromGradleProperty(task: Test, defaultValue: () -> Any? = { null }) {
        val propertyValue = task.project.findProperty(propertyName) ?: defaultValue()
        if (propertyValue != null) task.systemProperty(propertyName, propertyValue)
    }
}

fun blackBoxTest(taskName: String, vararg tags: String) = projectTest(taskName, jUnitMode = JUnitMode.JUnit5) {
    group = "verification"

    if (kotlinBuildProperties.isKotlinNativeEnabled) {
        dependsOn(":kotlin-native:dist")

        workingDir = rootDir
        outputs.upToDateWhen {
            // Don't treat any test task as up-to-date, no matter what.
            // Note: this project should contain only test tasks, including ones that build binaries, and ones that run binaries.
            false
        }

        maxHeapSize = "6G" // Extra heap space for Kotlin/Native compiler.
        jvmArgs("-XX:MaxJavaStackTraceDepth=1000000") // Effectively remove the limit for the amount of stack trace elements in Throwable.

        // Double the stack size. This is needed to compile some marginal tests with extra-deep IR tree, which requires a lot of stack frames
        // for visiting it. Example: codegen/box/strings/concatDynamicWithConstants.kt
        // Such tests are successfully compiled in old test infra with the default 1 MB stack just by accident. New test infra requires ~55
        // additional stack frames more compared to the old one because of another launcher, etc. and it turns out this is not enough.
        jvmArgs("-Xss2m")

        if (!kotlinBuildProperties.isTeamcityBuild
            && minOf(kotlinBuildProperties.junit5NumberOfThreadsForParallelExecution ?: 16, Runtime.getRuntime().availableProcessors()) > 4
        ) {
            logger.info("JVM C2 compiler has been disabled for task $path")
            jvmArgs("-XX:TieredStopAtLevel=1") // Disable C2 if there are more than 4 CPUs at the host machine.
        }

        TestProperty.KOTLIN_NATIVE_HOME.setUpFromGradleProperty(this) {
            project(":kotlin-native").projectDir.resolve("dist").absolutePath
        }

        TestProperty.COMPILER_CLASSPATH.setUpFromGradleProperty(this) {
            configurations.detachedConfiguration(dependencies.project(":kotlin-native-compiler-embeddable")).files.joinToString(";")
        }

        // Pass Gradle properties as JVM properties so test process can read them.
        TestProperty.TEST_MODE.setUpFromGradleProperty(this)
        TestProperty.USE_CACHE.setUpFromGradleProperty(this)
        TestProperty.EXECUTION_TIMEOUT.setUpFromGradleProperty(this)

        ignoreFailures = true // Don't fail Gradle task if there are failed tests. Let the subsequent tasks to run as well.

        useJUnitPlatform {
            includeTags(*tags)
        }
    } else
        doFirst {
            throw GradleException(
                """
                    Can't run task $path. The Kotlin/Native part of the project is currently disabled.
                    Make sure that "kotlin.native.enabled" is set to "true" in local.properties file, or is passed
                    as a Gradle command-line parameter via "-Pkotlin.native.enabled=true".
                """.trimIndent()
            )
        }
}

@Suppress("PropertyName")
val TEST_GROUPING_TASK_MARKER = "groupingTaskMarker"

fun Test.isGroupingTest() = TEST_GROUPING_TASK_MARKER in inputs.properties

// N.B. Have to register grouping tasks as Test, otherwise IDEA will not show test results correctly in the Run tool window.
fun groupingTest(taskName: String, vararg dependencyTasks: Any) = getOrCreateTask<Test>(taskName) {
    group = "verification"
    dependsOn(*dependencyTasks)

    inputs.property(TEST_GROUPING_TASK_MARKER, 1) // Mark it as a test grouping task to distinguish from other test tasks.
}

// Tasks that run different sorts of tests. Most frequent use case: running specific tests from the IDE.
val infrastructureTest = blackBoxTest("infrastructureTest", "infrastructure")
val externalTest = blackBoxTest("externalTest", "external")

// Tasks that do not run tests directly, but group other test tasks. Most frequent use case: running groups of tests on CI server.
val dailyTest = groupingTest("dailyTest", externalTest)
val fullTest = groupingTest("fullTest", dailyTest, infrastructureTest)

// "test" task is created by convention. We can't just remove it. So, let it be just an alias for daily test task.
val test by groupingTest("test", dailyTest)

gradle.taskGraph.whenReady {
    allTasks.forEach { task ->
        if (task.project == project && task is Test && task.isGroupingTest()) {
            val commandLineIncludePatterns = task.commandLineIncludePatterns
            if (commandLineIncludePatterns.isNotEmpty()) {
                val testTasks = tasks.withType<Test>().filterNot { it.isGroupingTest() }.map { it.path }.sorted()
                throw GradleException(
                    buildString {
                        appendLine("Task ${task.path} is only used for grouping of tests. Running it with command-line filter won't have any effect.")
                        appendLine("Make sure you apply the filter to one of the following Kotlin/Native test tasks:")
                        testTasks.forEach { append("  ").appendLine(it) }
                        appendLine("Your command-line filter is: $commandLineIncludePatterns(--tests filter)")
                    }
                )
            }
        }
    }
}

val generateTests by generator("org.jetbrains.kotlin.generators.tests.GenerateNativeBlackboxTestsKt") {
    javaLauncher.set(project.getToolchainLauncherFor(JdkMajorVersion.JDK_11))
    dependsOn(":compiler:generateTestData")
}
