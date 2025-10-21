<div align="center">
  
  # QmBlurView - Lightweight High-Performance Android BlurView, Supporting Custom Blur Radius, Sampling Rate, and Overlay Color, Optimized with Java+Native Implementation
  
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
  
  [简体中文](https://github.com/QmDeve/QmBlurView/blob/master/README_zh.md)
  
</div>

---

## Features

- **Minimum support Android 5.0**
- **Customizable Parameters**:
  - Blur radius
  - Sampling scale
  - Overlay color
- **High Performance**: Native blur algorithm implemented with underlying `Native` calls
- **Automatic Recycling Mechanism**: Prevents memory leaks

---

## Quick Integration
### 1. Add Repository:
Add the following to the settings.gradle in your project root directory:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
       mavenCentral()
       maven { url 'https://jitpack.io' }
  }
}
```

2. Add Dependency:
Add the following to the build.gradle of your module:

```gradle
dependencies {
   implementation 'com.github.QmDeve:QmBlurView:v1.0.1'
}
```

---

## Quick Usage
### Using in XML Layout
```xml
<com.qmdeve.blurview.widget.QmBlurView
    android:id="@+id/blurView"
    android:layout_width="match_parent"
    android:layout_height="100dp"
    app:qmBlurRadius="20dp"
    app:qmDownsampleFactor="2"
    app:qmOverlayColor="#66FFFFFF"
    app:qmCornerRadius="24dp" />
```

### Setting Parameters via Code
```java
QmBlurView blurView = findViewById(R.id.blurView);
blurView.setBlurRadius(20f);
blurView.setDownsampleFactor(2f);     // You can leave it unset and keep the default optimal setting.
blurView.setOverlayColor(0x66FFFFFF);
blurView.setCornerRadius(20);
```

### BlurUtils
```java
BlurUtils.blurBitmap();
BlurUtils.blurResId();
BlurUtils.blurAssets();
BlurUtils.blurFile();
```

---

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=QmDeve/QmBlurView&type=date&legend=bottom-right)](https://www.star-history.com/#QmDeve/QmBlurView&type=date&legend=bottom-right)

---

If you find this project helpful, feel free to give it a Star!
