<div align="center">

# QmBlurView – Android BlurView and BlurButtonView and ProgressiveBlurView and BlurTitleBarView

  <br>
  <br>
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="Apache"/>
  <img src="https://img.shields.io/badge/Java-8-orange" alt="Java 8"/>
  <img src="https://img.shields.io/badge/Android-5.0%2B-brightgreen.svg" alt="Android 5"/>
  <img src="https://img.shields.io/badge/minSdk-21-green" alt="minSdk"/>
  <img src="https://img.shields.io/badge/targetSdk-36-green" alt="targetSdk"/>
  <img src="https://img.shields.io/badge/🚀-Feature-purple" alt="Feature"/>
  <img src="https://img.shields.io/badge/Version-v1.0.3.1-blue" alt="Version"/>
  <img src="https://img.shields.io/badge/Release-v1.0.3.1-green" alt="Release"/>
  <img src="https://jitpack.io/v/QmDeve/QmBlurView.svg" alt="Jitpack"/>
  <img src="https://img.shields.io/github/stars/QmDeve/QmBlurView" alt="Stars"/>
  <br>
  <br>

English | [简体中文](https://github.com/QmDeve/QmBlurView/blob/master/README_zh.md)

</div>

---
## Characteristic
- **View**
  - `BlurView`
  - `BlurButtonView`
  - `ProgressiveBlurView`
  - `BlurTitlebarView`
- **Minimum support Android 5.0**
- **Customizable Parameters**:
  - Blur radius
  - Sampling scale
  - Overlay color
  - Wait...
- **High Performance**: Native blur algorithm implemented with underlying `Native` calls
- **Automatic Recycling Mechanism**: Prevents memory leaks

---

## Screenshot
### BlurView
<img src="https://github.com/QmDeve/QmBlurView/blob/master/img/blurview.jpg?raw=true" alt="Stars"/>

### BlurButtonView
<img src="https://github.com/QmDeve/QmBlurView/blob/master/img/blurButton.jpg?raw=true" alt="Stars"/>

### ProgressiveBlurView
<img src="https://github.com/QmDeve/QmBlurView/blob/master/img/progressiveBlurView.jpg?raw=true" alt="Stars"/>

### BlurTitleBarView
<img src="https://github.com/QmDeve/QmBlurView/blob/master/img/blurTitlebarView.jpg?raw=true" alt="Stars"/>

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
   implementation 'com.github.QmDeve:QmBlurView:v1.0.3'
}
```

---

## How to use
### BlurView
#### Used in XML layout
```xml
<com.qmdeve.blurview.widget.BlurView
        android:id="@+id/blurView"
        android:layout_width="match_parent"
        android:layout_height="100dp"
        app:blurRadius="20dp"
        app:overlayColor="#66FFFFFF"
        app:cornerRadius="24dp"/>
```

#### Property Description

| Attribute Name    | Type | Default value | Description |
|--------------------|------|--------|------|
| `app:blurRadius`   | `dimension` | `10` | Set blur radius |
| `app:overlayColor` | `color` | `#AAFFFFFF` | Set overlay color |
| `app:cornerRadius` | `dimension` | `0` | Set corner radius |

#### Use code to set properties
```java
QmBlurView blurView = findViewById(R.id.blurView);
blurView.setBlurRadius(20f);
blurView.setOverlayColor(0x66FFFFFF);
blurView.setCornerRadius(20);
```

---

### BlurButtonView
#### Used in XML layout
```xml
<com.qmdeve.blurview.widget.BlurButtonView
        android:id="@+id/button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Button Test"
        app:buttonCornerRadius="12dp"
        app:buttonIconPadding="8dp"
        app:buttonIconSize="24dp"
        app:buttonTextBold="true"
        app:blurRadius="12dp"
        app:overlayColor="#80FFFFFF"
        android:icon="?android:attr/actionModeWebSearchDrawable"
        app:buttonIconTint="@color/black"/>
```

#### Property Description

| Attribute Name                 | Type | Default value | Description |
|------------------------------|------|---------------|------|
| `app:buttonCornerRadius`     | `dimension` | `0`           | Set the rounded radius of the Button |
| `app:buttonIconPadding`      | `dimension` | `0dp`         | Set the padding of the Icon |
| `app:buttonIconSize`         | `dimension` | -             | Set Icon Size |
| `app:buttonTextBold`         | `boolean` | `true`        | Text if Bold |
| `app:blurRadius`             | `dimension` | `10`          | Set blur radius |
| `app:overlayColor`           | `color` | `#AAFFFFFF`   | Set overlay color |
| `app:buttonIconTint`         | `color` | -             | Set ButtonIconTint |
| `app:buttonTextColorPressed` | `color` | -             | Press state text color |
| `app:buttonTextColorDisabled` | `color` | -             | Disable the text color of the status |
| `android:icon`               | - | -             | Set Icon |
| `android:text`               | - | -             | Set Text |
| `android:textSize`           | - | -             | Set Text Size |

---

### BlurTitleBarView
#### Used in XML layout
```xml
<com.qmdeve.blurview.widget.BlurTitlebarView
        android:id="@+id/blurTitlebar1"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:overlayColor="#D1FFFFFF"
        app:blurRadius="30dp"
        app:showBack="true"
        app:menuIcon="?android:attr/actionModeWebSearchDrawable"
        app:titleText="Title Test"
        app:subtitleText="Subheading Test"
        app:centerTitle="false"/>
```

#### Property Description

| Attribute Name               | Type       | Default Value | Description |
|------------------------------|------------|---------------|-------------|
| `app:titleText`              | `string`   | -             | Title text |
| `app:subtitleText`           | `string`   | -             | Subtitle text |
| `app:titleTextColor`         | `color`    | -             | Title text color |
| `app:subtitleTextColor`      | `color`    | -             | Subtitle text color |
| `app:showBack`               | `boolean`  | `false`       | Whether to show back button |
| `app:backIcon`               | `reference`| -             | Back icon resource |
| `app:backIconTint`           | `color`    | -             | Back icon tint |
| `app:menuText`               | `string`   | -             | Menu text |
| `app:menuTextColor`          | `color`    | -             | Menu text color |
| `app:menuIcon`               | `reference`| -             | Menu icon resource |
| `app:menuIconTint`           | `color`    | -             | Menu icon tint |
| `app:centerTitle`            | `boolean`  | `false`       | Whether to center the title |

#### Switch the title position dynamically using code
```java
blurTitlebarView.setCenterTitle(true);
```

---

### ProgressiveBlurView
#### Used in XML layout
```xml
<com.qmdeve.blurview.widget.ProgressiveBlurView
        android:layout_width="match_parent"
        android:layout_height="150dp"
        app:blurRadius="20dp"
        app:progressiveDirection="topToBottom"
        app:progressiveLayers="5"
        app:progressiveOverlayColor="#80FFFFFF" />
```

#### Property Description

| Attribute Name               | Type       | Default Value | Description |
|------------------------------|------------|---------------|-------------|
| `app:progressiveOverlayColor` | `color`    | -             | Progressive overlay color |
| `app:progressiveDirection`    | `enum`     | `topToBottom` | Progressive direction: topToBottom, bottomToTop, leftToRight, rightToLeft |
| `app:progressiveLayers`       | `integer`  | -             | Progressive layers |
| `app:progressiveBlurRadius`   | `dimension`| -             | Progressive blur radius |

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