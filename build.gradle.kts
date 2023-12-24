import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
    id("com.google.devtools.ksp") version "1.8.21-1.0.11"
}




group = "com.hys"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}


val sqllinVersion = "1.2.3"

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material3:material3-desktop:1.5.11")
    implementation("com.ctrip.kotlin:sqllin-dsl:$sqllinVersion")
    // sqllin-driver
    implementation("com.ctrip.kotlin:sqllin-driver:$sqllinVersion")
    // The sqllin-dsl serialization and deserialization depends on kotlinx-serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
//    // Since 1.2.2, sqllin-dsl depends on kotlinx.coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.ctrip.kotlin:sqllin-processor:$sqllinVersion")
    ksp("com.ctrip.kotlin:sqllin-processor:$sqllinVersion")



}
kotlin {
    jvmToolchain(11)
}


compose.desktop {


    application {
        mainClass = "MainKt"


        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "genshinAssistant"
            packageVersion = "1.0.1"
            modules("java.sql")
            includeAllModules = true
            description = "a mihoyo game assistant"
            copyright = "© 2022 职业程序员. All rights reserved."
            vendor = "小黄头号应援粉丝团"


            windows {
                // a version for all Windows distribution
                packageVersion = "1.2.4"
                // a version only for the msi package
                msiPackageVersion = "1.2.4"
                // a version only for the exe package
                exePackageVersion = "1.2.4"
            }


            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
        }

    }

}


fun runCommand(command: String, timeout: Long = 120): Pair<Boolean, String> {
    val process = ProcessBuilder()
        .command(command.split(" "))
        .directory(rootProject.projectDir)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
    process.waitFor(timeout, TimeUnit.SECONDS)
    val result = process.inputStream.bufferedReader().readText()
    val error = process.errorStream.bufferedReader().readText()
    return if (error.isBlank()) {
        Pair(true, result)
    }
    else {
        Pair(false, error)
    }
}

gradle.projectsEvaluated {
    tasks.named("prepareAppResources") {
        dependsOn("compileJni")
    }
}

tasks.register("compileJni") {
    description = "compile jni binary file for desktop"

    val resourcePath = File(rootProject.projectDir, "resources/windows")
    val binFilePath = File(resourcePath, "wregistry.dll")
    val cppFileDirectory = File(rootProject.projectDir, "src/main/wregistry/cpp")
    val cppFilePath = File(cppFileDirectory, "wregistry.cpp")

    // 指定输入、输出文件，用于增量编译
    inputs.dir(cppFileDirectory)
    outputs.file(binFilePath)

    doLast {
        project.logger.info("compile jni for desktop running……")

        val jdkFile = org.gradle.internal.jvm.Jvm.current().javaHome
        val systemPrefix: String

        val os: OperatingSystem = DefaultNativePlatform.getCurrentOperatingSystem()

        systemPrefix = if (os.isWindows) {
            "win32"
        } else if (os.isMacOsX) {
            "darwin"
        } else if (os.isLinux) {
            "linux"
        } else {
            project.logger.error("UnSupport System for compiler cpp, please compiler manual")
            return@doLast
        }

        val includePath1 = jdkFile.resolve("include")
        val includePath2 = includePath1.resolve(systemPrefix)

        if (!includePath1.exists() || !includePath2.exists()) {
            val msg = "ERROR: $includePath2 not found!\nMaybe it's because you are using JetBrain Runtime (Jbr)\nTry change Gradle JDK to another jdk which provide jni support"
            throw GradleException(msg)
        }

        project.logger.info("Check Desktop Resources Path……")

        if (!resourcePath.exists()) {
            project.logger.info("${resourcePath.absolutePath} not exists, create……")
            mkdir(resourcePath)
        }

        val runTestResult = runCommand("g++ --version")
        if (!runTestResult.first) {
            throw GradleException("Error: Not find command g++, Please install it and add to your system environment path\n${runTestResult.second}")
        }

        val command = "g++ ${cppFilePath.absolutePath} -o ${binFilePath.absolutePath} -shared -fPIC -I ${includePath1.absolutePath} -I ${includePath2.absolutePath} -static-libgcc -static-libstdc++ -Wl,-Bstatic,--whole-archive -lwinpthread -Wl,--no-whole-archive"

        project.logger.info("running command $command……")

        val compilerResult = runCommand(command)

        if (!compilerResult.first) {
            throw GradleException("Command run fail: ${compilerResult.second}")
        }

        project.logger.info(compilerResult.second)

        project.logger.lifecycle("compile jni for desktop all done")
    }

}
