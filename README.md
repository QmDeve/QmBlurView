<div align="center">
  
  # QmBlurView – Android BlurView and BlurButtonView
  
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
## Characteristic
- **View**
  - `QmBlurView`
  - `QmBlurButtonView`
- **Minimum support Android 5.0**
- **Customizable Parameters**:
  - Blur radius
  - Sampling scale
  - Overlay color
- **High Performance**: Native blur algorithm implemented with underlying `Native` calls
- **Automatic Recycling Mechanism**: Prevents memory leaks

---

## Screenshot
### BlurView
<img src="https://github.com/QmDeve/QmBlurView/blob/master/img/img1.jpg?raw=true" alt="Stars"/>

### BlurButtonView
<img src="https://github.com/QmDeve/QmBlurView/blob/master/img/img2.jpg?raw=true" alt="Stars"/>

---

## Demo experience
[Download Demo](https://github.com/QmDeve/QmBlurView/blob/master/app/release/app-release.apk)

---

## Quick Integration
### Add Repository:
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

### 2. Add Dependency:
Add the following to the build.gradle of your module:

```gradle
dependencies {
   implementation 'com.github.QmDeve:QmBlurView:v1.0.2'
}
```

---

## How to use
### BlurView
#### Used in XML layout
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

#### Property explaination

| Property name | Type | Default value | Explaination |
|--------|------|--------|------|
| `app:qmBlurRadius` | `dimension` | `10` | Set blur radius |
| `app:qmOverlayColor` | `color` | `#AAFFFFFF` | Set overlay color |
| `app:qmCornerRadius` | `dimension` | `0` | Set corner radius |
| `app:qmDownsampleFactor` | `float` | `2` | Set DownsampleFactor (keep the default best)） |

#### Use code to set properties
```java
QmBlurView blurView = findViewById(R.id.blurView);
blurView.setBlurRadius(20f);
blurView.setDownsampleFactor(2f);
blurView.setOverlayColor(0x66FFFFFF);
blurView.setCornerRadius(20);
```

---

### BlurButtonView
#### Used in XML layout
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

#### Property explaination

| Property name | Type | Default value | Explaination |
|--------|------|--------|------|
| `app:buttonCornerRadius` | `dimension` | `0` | Set the rounded radius of the Button |
| `app:buttonIconPadding` | `dimension` | `0dp` | Set the padding of the Icon |
| `app:buttonIconSize` | `dimension` |  | Set Icon Size |
| `app:buttonTextBold` | `boolean` | `true` | Text if Bold |
| `app:qmBlurRadius` | `dimension` | `10` | Set blur radius |
| `app:qmOverlayColor` | `color` | `#AAFFFFFF` | Set overlay color |
| `app:buttonIconTint` | `color` | 无 | Set ButtonIconTint |
| `app:buttonTextColorPressed` | `color` | - | Press state text color |
| `app:buttonTextColorDisabled` | `color` | - | Disable the text color of the status |
| `android:icon` | - | - | Set Icon |
| `android:text` | - | - | Set Text |
| `android:textSize` | - | - | Set Text Size |

---

### `BlurUtils` Utils Calss
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
**For details, please refer to `Demo`**

**If you find this project helpful, feel free to give it a `Star` to show your support!**
