// 使用 toml 中的别名来声明插件，版本由 toml 文件统一管理
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // ↓↓↓ 添加下面这一行 ↓↓↓
    alias(libs.plugins.kotlin.kapt) apply false
}
