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

[English](./README.md) | Français | [简体中文](./README_zh.md) | [Русский](./README_ru.md)

</div>

---

> **Remarque : Dépôt Miroir**
>
> Ce projet est maintenu sur plusieurs plateformes. Le contenu de tous les dépôts est cohérent.
> - **Dépôt Principal** : [GitHub](https://github.com/QmDeve/QmBlurView)
> - **Miroir** : [GitLab](https://gitlab.com/QmDeve/QmBlurView)

---

## Introduction

**QmBlurView** est une bibliothèque d'interface utilisateur Android haute performance qui fournit des effets de flou dynamiques en temps réel (verre dépoli). Elle exploite le code natif C++ pour un traitement efficace des images et offre une suite complète de composants d'interface utilisateur floutés pour améliorer la conception de votre application.

## Fonctionnalités

- **Haute Performance** : Algorithme de flou natif implémenté en C/C++ pour une vitesse et une fluidité maximales.
- **Flou en Temps Réel** : Met automatiquement à jour l'effet de flou lorsque le contenu de l'arrière-plan change.
- **Bibliothèque de Composants Riche** :
  - `BlurView` : Conteneur personnalisable pour n'importe quelle mise en page.
  - `BlurButtonView` : Boutons avec effets de verre dépoli.
  - `BlurBottomNavigationView` : Navigation inférieure élégante et floutée.
  - `BlurTitlebarView`, `BlurSwitchButtonView`, `BlurFloatingButtonView`, et `ProgressiveBlurView`.
- **Support de Chargement d'Images** : Transformations intégrées pour **Glide** et **Picasso**.
- **Intégration Facile** : Attributs XML simples et APIs Java/Kotlin.
- **Large Compatibilité** : Supporte Android 5.0 (API 21) et supérieur.

## Installation

Ajoutez les dépendances au fichier `build.gradle` de votre module :

```gradle
dependencies {
    // Bibliothèque Principale (Requis)
    implementation 'com.qmdeve:QmBlurView:1.0.5-Beta03'

    // Support de Navigation Inférieure (Optionnel)
    implementation 'com.qmdeve:QmBlurView.BottomNavigation:1.0.5-Beta03'

    // Transformations de Chargement d'Images (Optionnel - Glide/Picasso)
    implementation 'com.qmdeve:QmBlurView.Transform:1.0.5-Beta03'
}
```

*Vérifiez le badge ci-dessus pour la dernière version.*

## Utilisation

### 1. BlurView Basique

Utilisez `BlurView` pour flouter n'importe quelle partie de votre interface utilisateur. Il fonctionne mieux lorsqu'il est placé au-dessus d'autres contenus (par exemple, dans un `FrameLayout` ou `RelativeLayout`).

**XML :**
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

**Java :**
```java
BlurView blurView = findViewById(R.id.blurView);
blurView.setBlurRadius(25);
blurView.setCornerRadius(15);
blurView.setOverlayColor(Color.parseColor("#80FFFFFF"));
```

### 2. BlurButtonView

Un bouton personnalisable avec un fond flou.

**XML :**
```xml
<com.qmdeve.blurview.widget.BlurButtonView
    android:id="@+id/blurButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Bouton Flou"
    app:blurRadius="12dp"
    app:buttonCornerRadius="12dp"
    app:overlayColor="#80FFFFFF"
    app:buttonIconSize="24dp"
    app:buttonTextBold="true" />
```

### 3. BlurBottomNavigationView

Une barre de navigation inférieure avec un effet de flou intégré.

**XML :**
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

### 4. Transformations de Chargement d'Images

Appliquez le flou directement aux images chargées avec Glide ou Picasso.

**Glide :**
```java
Glide.with(context)
    .load(imageUrl)
    .transform(new com.qmdeve.blurview.transform.glide.BlurTransformation(context, 25, 1)) // rayon, échantillonnage
    .into(imageView);
```

**Picasso :**
```java
Picasso.get()
    .load(imageUrl)
    .transform(new com.qmdeve.blurview.transform.picasso.BlurTransformation(context, 25))
    .into(imageView);
```

## Attributs Communs

| Attribut | Description |
|---|---|
| `app:blurRadius` | Rayon de l'effet de flou (plus élevé = plus flou). |
| `app:overlayColor` | Couleur de superposition dessinée au-dessus de l'image floutée. |
| `app:cornerRadius` | Rayon des coins pour le fond de la vue. |
| `app:downsampleFactor` | Facteur de sous-échantillonnage pour l'optimisation des performances. |

## Documentation
**Detailed Documentation；[https://blur-docs.qmdeve.com](https://blur-docs.qmdeve.com)**

## Captures d'écran

| BlurView | BlurButtonView | ProgressiveBlurView |
|:---:|:---:|:---:|
| <img src="./img/blurview.jpg" width="250"/> | <img src="./img/blurButton.jpg" width="250"/> | <img src="./img/progressiveBlurView.jpg" width="250"/> |

| BlurTitleBarView | BlurSwitchButtonView | BlurBottomNavigationView |
|:---:|:---:|:---:|
| <img src="./img/blurTitlebarView.jpg" width="250"/> | <img src="./img/blurSwitchButton_true.jpg" width="250"/> | <img src="./img/blurBottomNavigation.jpg" width="250"/> |

## Historique des Étoiles

[![Star History](https://starchart.qmdeve.com/QmDeve/QmBlurView.svg?variant=adaptive)](https://starchart.qmdeve.com/QmDeve/QmBlurView)

## Contributeurs

<a href="https://github.com/QmDeve/QmBlurView/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=QmDeve/QmBlurView" alt="Contributors"/>
</a>

## Autres Projets

- **[AndroidLiquidGlassView](https://github.com/QmDeve/AndroidLiquidGlassView)**
- **[QmReflection](https://github.com/QmDeve/QmReflection)**
- **[Qm Authenticator for Android](https://github.com/Rouneant/Qm-Authenticator-for-Android)**

## Licence

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
