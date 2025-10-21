<div align="center">
  
  # QmBlurView - Lightweight High-Performance Android BlurView, Supporting Custom Blur Radius, Sampling Rate, and Overlay Color, Optimized with Java+Native Implementation
  
  <br>
  <br>
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="Apache"/>
  <img src="https://img.shields.io/badge/Java-11-orange?style=for-the-badge&logo=java" alt="Java 11"/>
  <img src="https://img.shields.io/badge/Android-7.0%2B-brightgreen.svg" alt="Android 7"/>
  <img src="https://jitpack.io/v/QmDeve/QmBlurView.svg" alt="Jitpack 7"/>
<br>
<br>
  
  [简体中文](https://github.com/QmDeve/QmBlurView/blob/master/README_zh.md)
  
</div>

---
## Features

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
   implementation 'com.github.QmDeve:QmBlurView:v1.0.0'
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
    app:qmDownsampleFactor="5"
    app:qmOverlayColor="#66FFFFFF" />
```

### Setting Parameters via Code
```java
QmBlurView blurView = findViewById(R.id.blurView);
blurView.setBlurRadius(20f);
blurView.setDownsampleFactor(5f);
blurView.setOverlayColor(0x66FFFFFF);
```

---

If you find this project helpful, feel free to give it a Star!
