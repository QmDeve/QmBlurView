<div align="center">

<img src="https://socialify.git.ci/QmDeve/QmBlurView/image?description=1&font=Inter&name=1&owner=1&pattern=Floating+Cogs&theme=Light" alt="GitHub" width="100%"/>

## Android UI Blur Component library
  
  <br>

  [![GitHub](https://img.shields.io/badge/GitHub-Repository-black?logo=github)](https://GitHub.com/QmDeve/QmBlurView/)
  [![GitLab](https://img.shields.io/badge/GitLab-Repository-orange?logo=gitlab)](https://gitlab.com/QmDeve/QmBlurView)

  [![Telegram](https://img.shields.io/badge/Telegram%20Group-QmDeves-blue.svg?logo=telegram)](https://t.me/QmDeves)

  <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="MIT"/>
  <img src="https://img.shields.io/badge/Android-5.0%2B-brightgreen.svg" alt="Android 5"/>
  <img src="https://img.shields.io/maven-central/v/com.qmdeve/QmBlurView?label=Maven%20Central%20Latest%20Version" alt="maven"/>

  <br>
  <br>

English | [Français](./README_fr.md) | [简体中文](./README_zh.md) | [Русский](./README_ru.md)

</div>

---

> **Note: Mirror Repository**
>
> This project is maintained on multiple platforms to facilitate developers in different regions. The content of all warehouses should be consistent
> - **Main Repository**：[GitHub](https://github.com/QmDeve/QmBlurView)
> - **Other Repository**：
>   - [GitLab](https://gitlab.com/QmDeve/QmBlurView)

---

## Characteristic
- **View**
  - `BlurView`
  - `BlurViewGroup`
  - `BlurButtonView`
  - `ProgressiveBlurView`
  - `BlurTitlebarView`
  - `BlurSwitchButtonView`
  - `BlurFloatingButtonView`
  - `BlurBottomNavigationView`
- **Minimum support Android 5.0**
- **High Performance**: Native blur algorithm implemented with underlying `Native` calls
- **Automatic Recycling Mechanism**: Prevents memory leaks

---

## Screenshot
### BlurView
<img src="./img/blurview.jpg" alt="Stars"/>

### BlurButtonView
<img src="./img/blurButton.jpg" alt="Stars"/>

### ProgressiveBlurView
<img src="./img/progressiveBlurView.jpg" alt="Stars"/>

### BlurTitleBarView
<img src="./img/blurTitlebarView.jpg" alt="Stars"/>

### BlurSwitchButtonView
<img src="./img/blurSwitchButton_false.jpg" alt="Stars"/>
<img src="./img/blurSwitchButton_true.jpg" alt="Stars"/>

### BlurFloatingButtonView
<img src="./img/blurFloatingButton.jpg" alt="Stars"/>

### BlurBottomNavigationView
<img src="./img/blurBottomNavigation.jpg" alt="Stars"/>

## Demo experience
**[Download Demo](./app/release/app-release.apk?raw=true)**

# Start using
### Quick integration

<img src="https://img.shields.io/maven-central/v/com.qmdeve/QmBlurView?label=Maven%20Central%20Latest%20Version" alt="maven"/>

**Add dependencies in the `dependencies{}` block of `build.gradle`**
```gradle
// QmBlurView Core Dependencies
implementation 'com.qmdeve:QmBlurView:<Version>'

// BottomNavigationView Dependencies (v1.0.4.5-Beta02 and above)
implementation 'com.qmdeve:QmBlurView.BottomNavigation:<Version>'

// Transform Dependencies (v1.0.5-Beta02 and above)
implementation 'com.qmdeve:QmBlurView.Transform:<Version>'
```

## Quick use
**Usage: [https://blur-docs.qmdeve.com](https://blur-docs.qmdeve.com/)**

---

## Star History
[![Star History](https://starchart.qmdeve.com/QmDeve/QmBlurView.svg?variant=adaptive)](https://starchart.qmdeve.com/QmDeve/QmBlurView)

---

## Contributors
<a href="https://github.com/QmDeve/QmBlurView/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=QmDeve/QmBlurView" alt="Contributors"/>
</a>

## My open source project
 - **[AndroidLiquidGlassView](https://github.com/QmDeve/AndroidLiquidGlassView)**
 - **[QmReflection](https://github.com/QmDeve/QmReflection)**
 - **[Qm Authenticator for Android](https://github.com/Rouneant/Qm-Authenticator-for-Android)**

## License
```
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