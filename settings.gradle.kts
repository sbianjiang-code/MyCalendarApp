pluginManagement {
    repositories {
        // 【1】阿里云镜像：必须放在 google() 和 mavenCentral() 的前面
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }

        // 【2】官方源（作为备用）
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 【3】阿里云镜像：同样放在前面
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }

        // 【4】官方源
        google()
        mavenCentral()
    }
}

rootProject.name = "MyCalendarApp" // 你的项目名称，保持原样即可
include(":myapplication")
