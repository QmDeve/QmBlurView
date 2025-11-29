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

[English](./README.md) | [Français](./README_fr.md) | [简体中文](./README_zh.md) | Русский

</div>

---

> **Примечание: Зеркало Репозитория**
>
> Этот проект поддерживается на нескольких платформах. Содержимое всех репозиториев согласовано.
> - **Основной Репозиторий**: [GitHub](https://github.com/QmDeve/QmBlurView)
> - **Зеркало**: [GitLab](https://gitlab.com/QmDeve/QmBlurView)

---

## Введение

**QmBlurView** — это высокопроизводительная библиотека пользовательского интерфейса Android, обеспечивающая эффекты размытия (матового стекла) в реальном времени. Она использует нативный код C++ для эффективной обработки изображений и предлагает полный набор размытых компонентов UI для улучшения дизайна вашего приложения.

## Особенности

- **Высокая Производительность**: Нативный алгоритм размытия, реализованный на C/C++ для максимальной скорости и плавности.
- **Размытие в Реальном Времени**: Автоматически обновляет эффект размытия при изменении содержимого фона.
- **Богатая Библиотека Компонентов**:
  - `BlurView`: Настраиваемый контейнер для любой разметки.
  - `BlurButtonView`: Кнопки с эффектом матового стекла.
  - `BlurBottomNavigationView`: Стильная нижняя навигация с размытием.
  - `BlurTitlebarView`, `BlurSwitchButtonView`, `BlurFloatingButtonView` и `ProgressiveBlurView`.
- **Поддержка Загрузки Изображений**: Встроенные трансформации для **Glide** и **Picasso**.
- **Простая Интеграция**: Простые XML-атрибуты и Java/Kotlin API.
- **Широкая Совместимость**: Поддержка Android 5.0 (API 21) и выше.

## Установка

Добавьте зависимости в файл `build.gradle` вашего модуля:

```gradle
dependencies {
    // Основная библиотека (Обязательно)
    implementation 'com.qmdeve:QmBlurView:1.0.5-Beta03'

    // Поддержка нижней навигации (Опционально)
    implementation 'com.qmdeve:QmBlurView.BottomNavigation:1.0.5-Beta03'

    // Трансформации для загрузки изображений (Опционально - Glide/Picasso)
    implementation 'com.qmdeve:QmBlurView.Transform:1.0.5-Beta03'
}
```

*Проверьте значок выше для получения последней версии.*

## Использование

### 1. Базовый BlurView

Используйте `BlurView` для размытия любой части вашего интерфейса. Он лучше всего работает, когда размещен поверх другого контента (например, в `FrameLayout` или `RelativeLayout`).

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

Настраиваемая кнопка с размытым фоном.

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

Нижняя навигационная панель со встроенным эффектом размытия.

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

### 4. Трансформации для Загрузки Изображений

Применяйте размытие непосредственно к изображениям, загруженным с помощью Glide или Picasso.

**Glide:**
```java
Glide.with(context)
    .load(imageUrl)
    .transform(new com.qmdeve.blurview.transform.glide.BlurTransformation(context, 25, 1)) // радиус, сэмплинг
    .into(imageView);
```

**Picasso:**
```java
Picasso.get()
    .load(imageUrl)
    .transform(new com.qmdeve.blurview.transform.picasso.BlurTransformation(context, 25))
    .into(imageView);
```

## Общие Атрибуты

| Атрибут | Описание |
|---|---|
| `app:blurRadius` | Радиус эффекта размытия (выше = более размыто). |
| `app:overlayColor` | Цвет наложения, рисуемый поверх размытого изображения. |
| `app:cornerRadius` | Радиус закругления углов фона. |
| `app:downsampleFactor` | Фактор даунсэмплинга для оптимизации производительности. |

## Скриншоты

| BlurView | BlurButtonView | ProgressiveBlurView |
|:---:|:---:|:---:|
| <img src="./img/blurview.jpg" width="250"/> | <img src="./img/blurButton.jpg" width="250"/> | <img src="./img/progressiveBlurView.jpg" width="250"/> |

| BlurTitleBarView | BlurSwitchButtonView | BlurBottomNavigationView |
|:---:|:---:|:---:|
| <img src="./img/blurTitlebarView.jpg" width="250"/> | <img src="./img/blurSwitchButton_true.jpg" width="250"/> | <img src="./img/blurBottomNavigation.jpg" width="250"/> |

## История Звезд

[![Star History](https://starchart.qmdeve.com/QmDeve/QmBlurView.svg?variant=adaptive)](https://starchart.qmdeve.com/QmDeve/QmBlurView)

## Участники

<a href="https://github.com/QmDeve/QmBlurView/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=QmDeve/QmBlurView" alt="Contributors"/>
</a>

## Другие Проекты

- **[AndroidLiquidGlassView](https://github.com/QmDeve/AndroidLiquidGlassView)**
- **[QmReflection](https://github.com/QmDeve/QmReflection)**
- **[Qm Authenticator for Android](https://github.com/Rouneant/Qm-Authenticator-for-Android)**

## Лицензия

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
