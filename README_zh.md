<div align="center">
  
  # QmBlurView - Android模糊视图 (BlurView) 和模糊按钮视图 (BlurButtonView)
  
  <br>
  <br>
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="Apache"/>
  <img src="https://img.shields.io/badge/Java-8-orange" alt="Java 8"/>
  <img src="https://img.shields.io/badge/Android-5.0%2B-brightgreen.svg" alt="Android 5"/>
  <img src="https://img.shields.io/badge/minSdk-21-green" alt="minSdk"/>
  <img src="https://img.shields.io/badge/targetSdk-36-green" alt="targetSdk"/>
  <img src="https://img.shields.io/badge/🚀-Feature-purple" alt="Feature"/>
  <img src="https://img.shields.io/badge/Version-v1.0.2-blue" alt="Version"/>
  <img src="https://img.shields.io/badge/Release-v1.0.2-green" alt="Release"/>
  <img src="https://jitpack.io/v/QmDeve/QmBlurView.svg" alt="Jitpack"/>
  <img src="https://img.shields.io/github/stars/QmDeve/QmBlurView" alt="Stars"/>
  <br>
  <br>
  
  [English](https://github.com/QmDeve/QmBlurView/blob/master/README.md) | 简体中文
  
</div>

---
## 特性
- **View**
  - `QmBlurView` - 通用模糊视图
  - `QmBlurButtonView` - 模糊按钮视图
- **最低支持 Android 5.0**
- **参数自定义**：
  - 模糊半径
  - 采样比例
  - 叠加颜色
  - 圆角半径
- **高性能**：底层调用 `Native` 实现的原生模糊算法
- **自动回收机制**：防止内存泄漏

---

## 截图
### BlurView
<img src="https://github.com/QmDeve/QmBlurView/blob/master/img/img1.jpg?raw=true" alt="Stars"/>

### BlurButtonView
<img src="https://github.com/QmDeve/QmBlurView/blob/master/img/img2.jpg?raw=true" alt="Stars"/>

---

## Demo 体验
[下载 Demo](https://github.com/QmDeve/QmBlurView/blob/master/app/release/app-release.apk)

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
   implementation 'com.github.QmDeve:QmBlurView:v1.0.2'
}
```

---

## 使用方法
### BlurView
#### XML布局中使用
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

#### 属性说明

| 属性名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `app:qmBlurRadius` | `dimension` | `10` | 模糊半径 |
| `app:qmOverlayColor` | `color` | `#AAFFFFFF` | 叠层颜色 |
| `app:qmCornerRadius` | `dimension` | `0` | 设置视图的圆角半径 |
| `app:qmDownsampleFactor` | `float` | `2` | 采样率（保持默认最佳） |

#### 使用代码设置属性
```java
QmBlurView blurView = findViewById(R.id.blurView);
blurView.setBlurRadius(20f);          // 设置模糊半径
blurView.setDownsampleFactor(2f);     // 设置采样率，使用默认最佳
blurView.setOverlayColor(0x66FFFFFF); // 设置叠层颜色
blurView.setCornerRadius(20);         // 设置圆角半径
```

---

### BlurButtonView
#### 在xml布局使用
```xml
<com.qmdeve.blurview.widget.QmBlurButtonView
        android:id="@+id/button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Button Test"
        app:buttonCornerRadius="12dp"
        app:buttonIconPadding="8dp"
        app:buttonIconSize="24dp"
        app:buttonTextBold="true"
        app:qmBlurRadius="12dp"
        app:qmOverlayColor="#80FFFFFF"
        android:layout_below="@id/button1"
        android:layout_centerHorizontal="true"
        android:icon="?android:attr/actionModeWebSearchDrawable"
        app:buttonIconTint="@color/black"/>
```

#### 属性说明

| 属性名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `app:buttonCornerRadius` | `dimension` | `0` | 设置Button的圆角半径 |
| `app:buttonIconPadding` | `dimension` | `0dp` | 设置Icon的内边距 |
| `app:buttonIconSize` | `dimension` |  | 设置Icon大小 |
| `app:buttonTextBold` | `boolean` | `true` | Text是否粗体 |
| `app:qmBlurRadius` | `dimension` | `10` | 模糊半径 |
| `app:qmOverlayColor` | `color` | `#AAFFFFFF` | 叠层颜色 |
| `app:buttonIconTint` | `color` | 无 | 设置图标颜色 |
| `app:buttonTextColorPressed` | `color` | - | 按下状态文本颜色 |
| `app:buttonTextColorDisabled` | `color` | - | 禁用状态文本颜色 |
| `android:icon` | - | - | 设置图标 |
| `android:text` | - | - | 设置文本 |
| `android:textSize` | - | - | Text大小 |

---

### `BlurUtils` 工具类
```java
BlurUtils.blurBitmap();
BlurUtils.blurResId();
BlurUtils.blurAssets();
BlurUtils.blurFile();
```

---
**详细请参考 `Demo`**

**如果你觉得这个项目有帮助，欢迎点个 `Star` 支持一下！**
