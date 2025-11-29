<div align="center">

<img src="https://socialify.git.ci/QmDeve/QmBlurView/image?description=1&font=Inter&name=1&owner=1&pattern=Floating+Cogs&theme=Light" alt="GitHub" width="100%"/>

## Bibliothèque de composants flous gaussiens de l'interface utilisateur Android

  <br>

[![GitHub](https://img.shields.io/badge/GitHub-Repository-black?logo=github)](https://GitHub.com/QmDeve/QmBlurView/)
[![GitLab](https://img.shields.io/badge/GitLab-Repository-orange?logo=gitlab)](https://gitlab.com/QmDeve/QmBlurView)

[![Telegram](https://img.shields.io/badge/Telegram%20Group-QmDeves-blue.svg?logo=telegram)](https://t.me/QmDeves)

  <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="MIT"/>
  <img src="https://img.shields.io/badge/Android-5.0%2B-brightgreen.svg" alt="Android 5"/>
  <img src="https://img.shields.io/maven-central/v/com.qmdeve/QmBlurView?label=Maven%20Central%20Latest%20Version" alt="maven"/>

  <br>
  <br>

[English](./README.md) | Français | [简体中文](./README_zh.md) | [Русский](./README_ru.md)

</div>

---

> **Note: Autres dépôts**
>
> Le projet est maintenu sur plusieurs plateformes pour la commodité des développeurs dans différentes régions. Le contenu de tous les entrepôts doit être cohérent
> - **Main Repo**：[GitHub](https://github.com/QmDeve/QmBlurView)
> - **Other Repo**：
>   - [GitLab](https://gitlab.com/QmDeve/QmBlurView)

---

## Caractéristique
- **View**
    - `BlurView` - Vue floue universelle
    - `BlurViewGroup`
    - `BlurButtonView` - Vue floue du bouton
    - `ProgressiveBlurView` - Vue floue progressive
    - `BlurTitlebarView` - Vue de la barre de titre floue
    - `BlurSwitchButtonView` - Vue du bouton de l'interrupteur flou
    - `BlurFloatingButtonView` - Vue floue du bouton flottant
    - `BlurBottomNavigationView` - Flou la vue de la barre de navigation inférieure
- **Prise en charge minimale pour Android 5.0**
- **Haute performance** : L'algorithme flou natif implémenté par l'appel sous-jacent `Native`
- **Mécanisme de récupération automatique** : prévenir les fuites de mémoire

---

## Aperçu des captures d'écran
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

## De démonstration
**[Télécharger démonstration](./app/release/app-release.apk?raw=true)**

# Commencez à utiliser
### Intégration rapide

<img src="https://img.shields.io/maven-central/v/com.qmdeve/QmBlurView?label=Maven%20Central%20Latest%20Version" alt="maven"/>

**Ajouter des dépendances au bloc `dependencies{}` du fichier `build.gradle`**
```gradle
// Dépendances de base
implementation 'com.qmdeve:QmBlurView:<Version>'

// BottomNavigationView Dependencies (v1.0.4.5-Beta02 and above)
implementation 'com.qmdeve:QmBlurView.BottomNavigation:<Version>'

// Transform Dependencies (v1.0.5-Beta02 and above)
implementation 'com.qmdeve:QmBlurView.Transform:<Version>'
```

## Comment utiliser
**Veuillez vérifier le document: [https://blur-docs.qmdeve.com](https://blur-docs.qmdeve.com/fr/)**

---

## Star History
[![Star History](https://starchart.qmdeve.com/QmDeve/QmBlurView.svg?variant=adaptive)](https://starchart.qmdeve.com/QmDeve/QmBlurView)

---

## Collaborateur
<a href="https://github.com/QmDeve/QmBlurView/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=QmDeve/QmBlurView" alt="Contributors"/>
</a>

## Mon projet open source
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