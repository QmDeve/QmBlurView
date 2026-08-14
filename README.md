<div align="center">

## QmBlurView

**QmBlurView is an `Android UI` component library that provides real-time, dynamic Gaussian blur effects. It uses native C++ code for efficient blur processing and provides a set of blur UI components to enhance your application design**

<br>

[![GitHub](https://img.shields.io/badge/GitHub-Repository-black?logo=github)](https://GitHub.com/QmDeve/QmBlurView/)
[![Publish New Version](https://github.com/QmDeve/QmBlurView/actions/workflows/publish.yml/badge.svg)](https://github.com/QmDeve/QmBlurView/actions/workflows/publish.yml)

[![License](https://img.shields.io/github/license/QmDeve/QmBlurView.svg?logo=github&color=blue&label=License)](https://github.com/QmDeve/QmBlurView/blob/master/LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-brightgreen.svg)](https://developer.android.com)

[![Maven Central Version](https://img.shields.io/maven-central/v/com.qmdeve.blurview/core?label=Maven%20Central)](https://central.sonatype.com/artifact/com.qmdeve.blurview/core)
[![JitPack](https://jitpack.io/v/com.qmdeve/QmBlurView.svg)](https://jitpack.io/#com.qmdeve/QmBlurView)
[![GitHub Releases](https://img.shields.io/github/release/QmDeve/QmBlurView?label=GitHub%20Releases)](https://github.com/QmDeve/QmBlurView/releases)

</div>

---

## Features

- **High Performance**: Native blur algorithm implemented in C/C++ for maximum speed and smoothness.
- **Rich Component Library**: Component that includes multiple types of blur effects.
- **Image Loading Support**: Built-in transformations for **Glide** and **Picasso**.

## Preview

|                               BlurView                                |                             BlurButtonView                              |                               ProgressiveBlurView                                |
| :-------------------------------------------------------------------: | :---------------------------------------------------------------------: | :------------------------------------------------------------------------------: |
| <img src="https://blurview.qmdeve.com/img/BlurView.jpg" width="250"/> | <img src="https://blurview.qmdeve.com/img/BlurButton.jpg" width="250"/> | <img src="https://blurview.qmdeve.com/img/ProgressiveBlurView.jpg" width="250"/> |

|                               BlurTitleBarView                                |                                BlurSwitchButtonView                                |                             BlurBottomNavigationView                              |
| :---------------------------------------------------------------------------: | :--------------------------------------------------------------------------------: | :-------------------------------------------------------------------------------: |
| <img src="https://blurview.qmdeve.com/img/BlurTitlebarView.jpg" width="250"/> | <img src="https://blurview.qmdeve.com/img/BlurSwitchButton_true.jpg" width="250"/> | <img src="https://blurview.qmdeve.com/img/BlurBottomNavigation.jpg" width="250"/> |

## Integration

[![Maven Central](https://img.shields.io/maven-central/v/com.qmdeve.blurview/core?label=Maven%20Central)](https://central.sonatype.com/artifact/com.qmdeve.blurview/core)

Add the dependencies to your module's `build.gradle` file:

```gradle
dependencies {
    // Core Library (Required)
    implementation 'com.qmdeve.blurview:core:1.3.0'

    // Navigation Support (Optional)
    implementation 'com.qmdeve.blurview:navigation:1.3.0'

    // Image Loading Transformations (Optional - Glide/Picasso)
    implementation 'com.qmdeve.blurview:transform:1.3.0'
}
```

## Usage

Please refer to the documentation to learn how to use the library

**Documentation: [https://blurview.qmdeve.com](https://blurview.qmdeve.com)**

---

## License

```
Copyright ©️ 2025-2026 Donny Yang

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
