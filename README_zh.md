# QmBlurView

<div align="center">

<img src="https://socialify.git.ci/QmDeve/QmBlurView/image?description=1&font=Inter&name=1&owner=1&pattern=Floating+Cogs&theme=Light" alt="QmBlurView" width="100%"/>

<br>

[![GitHub](https://img.shields.io/badge/GitHub-Repository-black?logo=github)](https://GitHub.com/QmDeve/QmBlurView/)
[![GitLab](https://img.shields.io/badge/GitLab-Repository-orange?logo=gitlab)](https://gitlab.com/QmDeve/QmBlurView)
[![Telegram](https://img.shields.io/badge/Telegram%20Group-QmDeves-blue.svg?logo=telegram)](https://t.me/QmDeves)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/QmDeve/QmBlurView/blob/master/LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-brightgreen.svg)](https://developer.android.com)
[![Maven Central](https://img.shields.io/maven-central/v/com.qmdeve/QmBlurView?label=Maven%20Central)](https://central.sonatype.com/artifact/com.qmdeve/QmBlurView)

<br>

[English](./README.md) | [Français](./README_fr.md) | 简体中文 | [Русский](./README_ru.md)

</div>

---

> **注意：镜像仓库**
>
> 本项目在多个平台进行维护。所有仓库的内容保持一致。
> - **主仓库**：[GitHub](https://github.com/QmDeve/QmBlurView)
> - **镜像**：[GitLab](https://gitlab.com/QmDeve/QmBlurView)

---

## 简介

**QmBlurView** 是一个高性能的 Android UI 库，提供实时、动态的模糊效果（毛玻璃）。它利用原生 C++ 代码进行高效的图像处理，并提供了一整套模糊 UI 组件来增强您的应用设计。

## 特性

- **高性能**：使用 C/C++ 实现的原生模糊算法，确保最大的速度和流畅度。
- **实时模糊**：随着背景内容的变化自动更新模糊效果。
- **丰富的组件库**：
  - `BlurView`：适用于任何布局的可定制容器。
  - `BlurButtonView`：具有毛玻璃效果的按钮。
  - `BlurBottomNavigationView`：时尚的模糊底部导航栏。
  - `BlurTitlebarView`、`BlurSwitchButtonView`、`BlurFloatingButtonView` 和 `ProgressiveBlurView`。
- **图片加载支持**：内置针对 **Glide** 和 **Picasso** 的变换支持。
- **易于集成**：简单的 XML 属性和 Java/Kotlin API。
- **广泛兼容**：支持 Android 5.0 (API 21) 及以上版本。

## 安装

在模块的 `build.gradle` 文件中添加依赖项：

```gradle
dependencies {
    // 核心库（必需）
    implementation 'com.qmdeve:QmBlurView:1.0.5-Beta03'

    // 底部导航支持（可选）
    implementation 'com.qmdeve:QmBlurView.BottomNavigation:1.0.5-Beta03'

    // 图片加载变换（可选 - Glide/Picasso）
    implementation 'com.qmdeve:QmBlurView.Transform:1.0.5-Beta03'
}
```

*查看上方的徽章以获取最新版本。*

## 使用方法

### 1. 基础 BlurView

使用 `BlurView` 模糊 UI 的任何部分。当放置在其他内容之上（例如在 `FrameLayout` 或 `RelativeLayout` 中）时效果最佳。

**XML:**
```xml
<com.qmdeve.blurview.widget.BlurView
    android:id="@+id/blurView"
    android:layout_width="200dp"
    android:layout_height="200dp"
    app:blurRadius="25dp"
    app:cornerRadius="15dp"
    app:overlayColor="#80FFFFFF"
    android:layout_centerInParent="true" />
```

**Java:**
```java
BlurView blurView = findViewById(R.id.blurView);
blurView.setBlurRadius(25);
blurView.setCornerRadius(15);
blurView.setOverlayColor(Color.parseColor("#80FFFFFF"));
```

### 2. BlurButtonView

具有模糊背景的可定制按钮。

**XML:**
```xml
<com.qmdeve.blurview.widget.BlurButtonView
    android:id="@+id/blurButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Blur Button"
    app:blurRadius="12dp"
    app:buttonCornerRadius="12dp"
    app:overlayColor="#80FFFFFF"
    app:buttonIconSize="24dp"
    app:buttonTextBold="true" />
```

### 3. BlurBottomNavigationView

内置模糊效果的底部导航栏。

**XML:**
```xml
<com.qmdeve.blurview.widget.BlurBottomNavigationView
    android:id="@+id/bottomNav"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:menu="@menu/bottom_nav_menu"
    app:navOverlayColor="#AAFFFFFF"
    app:navSelectedColor="#0161F2"
    app:navUnselectedColor="#000000"
    android:layout_alignParentBottom="true" />
```

### 4. 图片加载变换

直接对使用 Glide 或 Picasso 加载的图片应用模糊。

**Glide:**
```java
Glide.with(context)
    .load(imageUrl)
    .transform(new com.qmdeve.blurview.transform.glide.BlurTransformation(context, 25, 1)) // 半径, 采样
    .into(imageView);
```

**Picasso:**
```java
Picasso.get()
    .load(imageUrl)
    .transform(new com.qmdeve.blurview.transform.picasso.BlurTransformation(context, 25))
    .into(imageView);
```

## 常用属性

| 属性 | 描述 |
|---|---|
| `app:blurRadius` | 模糊效果的半径（值越大越模糊）。 |
| `app:overlayColor` | 绘制在模糊图像之上的覆盖颜色。 |
| `app:cornerRadius` | 视图背景的圆角半径。 |
| `app:downsampleFactor` | 用于性能优化的降采样因子。 |

## 文档
**详细文档请查看；[https://blur-docs.qmdeve.com](https://blur-docs.qmdeve.com)**

## 截图

| BlurView | BlurButtonView | ProgressiveBlurView |
|:---:|:---:|:---:|
| <img src="./img/blurview.jpg" width="250"/> | <img src="./img/blurButton.jpg" width="250"/> | <img src="./img/progressiveBlurView.jpg" width="250"/> |

| BlurTitleBarView | BlurSwitchButtonView | BlurBottomNavigationView |
|:---:|:---:|:---:|
| <img src="./img/blurTitlebarView.jpg" width="250"/> | <img src="./img/blurSwitchButton_true.jpg" width="250"/> | <img src="./img/blurBottomNavigation.jpg" width="250"/> |

## Star 历史

[![Star History](https://starchart.qmdeve.com/QmDeve/QmBlurView.svg?variant=adaptive)](https://starchart.qmdeve.com/QmDeve/QmBlurView)

## 贡献者

<a href="https://github.com/QmDeve/QmBlurView/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=QmDeve/QmBlurView" alt="Contributors"/>
</a>

## 其他项目

- **[AndroidLiquidGlassView](https://github.com/QmDeve/AndroidLiquidGlassView)**
- **[QmReflection](https://github.com/QmDeve/QmReflection)**
- **[Qm Authenticator for Android](https://github.com/Rouneant/Qm-Authenticator-for-Android)**

## 许可证

```text
Copyright ©️ 2025 QmDeve

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
