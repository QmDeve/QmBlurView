<div align="center">
  
  # QmBlurView - 轻量级高性能Android模糊视图，支持模糊半径、采样率、叠加颜色、圆角半径自定义，基于Java+Native优化实现
  
  <br>
  <br>
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="Apache"/>
  <img src="https://img.shields.io/badge/Java-8-orange" alt="Java 8"/>
  <img src="https://img.shields.io/badge/Android-5.0%2B-brightgreen.svg" alt="Android 5"/>
  <img src="https://img.shields.io/badge/minSdk-21-green" alt="minSdk"/>
  <img src="https://img.shields.io/badge/targetSdk-36-green" alt="targetSdk"/>
  <img src="https://img.shields.io/badge/🚀-Feature-purple" alt="Feature"/>
  <img src="https://img.shields.io/badge/Version-v1.0.1-blue" alt="Version"/>
  <img src="https://img.shields.io/badge/Release-v1.0.1-green" alt="Release"/>
  <img src="https://jitpack.io/v/QmDeve/QmBlurView.svg" alt="Jitpack"/>
  <img src="https://img.shields.io/github/stars/QmDeve/QmBlurView" alt="Stars"/>
  <br>
  <br>
  
  [English](https://github.com/QmDeve/QmBlurView/blob/master/README.md)
  
</div>

---
## 特性

- **最低支持 Android 5.0**
- **参数自定义**：
  - 模糊半径
  - 采样比例
  - 叠加颜色
  - 圆角半径
- **高性能**：底层调用 `Native` 实现的原生模糊算法
- **自动回收机制**：防止内存泄漏与

---

## 快速集成
### 1. 添加仓库：
在项目根目录的 settings.gradle 中添加：

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
       mavenCentral()
       maven { url 'https://jitpack.io' }
  }
}
```

### 2. 添加依赖：
在模块的 build.gradle 中添加：

```gradle
dependencies {
   implementation 'com.github.QmDeve:QmBlurView:v1.0.1'
}
```

---

## 快速使用
### 在xml布局使用
```xml
<com.qmdeve.blurview.widget.QmBlurView
    android:id="@+id/blurView"
    android:layout_width="match_parent"
    android:layout_height="100dp"
    app:qmBlurRadius="20dp"
    app:qmDownsampleFactor="2"
    app:qmOverlayColor="#66FFFFFF" 
    app:qmCornerRadius="24dp"/>
```

### 使用代码设置参数
```java
QmBlurView blurView = findViewById(R.id.blurView);
blurView.setBlurRadius(20f);          // 设置模糊半径
blurView.setDownsampleFactor(2f);     // 设置采样率，可不设置,使用默认最佳
blurView.setOverlayColor(0x66FFFFFF); // 设置叠层颜色
blurView.setCornerRadius(20);         // 设置圆角半径
```

### BlurUtils
```java
BlurUtils.blurBitmap();
BlurUtils.blurResId();
BlurUtils.blurAssets();
BlurUtils.blurFile();
```

---

如果你觉得这个项目有帮助，欢迎点个 Star 支持一下！
