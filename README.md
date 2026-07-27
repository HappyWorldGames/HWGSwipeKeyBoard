🇬🇧 [English](#hwg-swipe-keyboard) | 🇷🇺 [Русский](#-описание-на-русском)

---

# HWG Swipe Keyboard

A custom Android keyboard with swipe gesture support, developed and published on Google Play. This project demonstrates a deep understanding of Android input methods, touch event handling, and performance optimization.

[![Google Play](https://img.shields.io/badge/Google_Play-Download-green?style=for-the-badge&logo=google-play)](https://play.google.com/store/apps/details?id=com.happyworldgames.keyboard)

## ✨ Features
- **Swipe Gestures**: Custom implementation of swipe-to-type functionality.
- **Multi-language Support**: Dynamic layout switching for multiple languages.
- **System Integration**: Seamless text input into any third-party application via `InputConnection`.
- **Optimized Performance**: Minimal object allocation during high-frequency `MotionEvent` processing to prevent UI jank and GC pauses.

## 🛠️ Tech Stack
- **Language**: Kotlin
- **Core APIs**: `InputMethodService`, `InputConnection`, `MotionEvent`
- **UI**: ViewBinding, Custom Views
- **Tools**: Android Studio, Git, Google Play Console

## ⚙️ How it works (Under the hood)
- **Touch Handling**: Intercepts `ACTION_DOWN`, `ACTION_MOVE`, and `ACTION_UP` events to track finger trajectory.
- **Coordinate Mapping**: Translates physical screen coordinates into logical key indices (e.g., `posToNumberPos` function).
- **Text Commit**: Uses `currentInputConnection.commitText()` and `deleteSurroundingText()` to interact with the host application's text field efficiently and safely.

## 📸 Screenshots
![Demonstration app work](https://github.com/HappyWorldGames/HWGSwipeKeyBoard/raw/master/screenshots/use_keyboard.gif)

## 🚀 How to Run
1. Clone the repository.
2. Open the project in Android Studio.
3. Build and run it on an emulator or a device.

---

🇬🇧 [Back to English](#hwg-swipe-keyboard) | 🇷🇺 Русский

## 🇷🇺 Описание на русском

Кастомная клавиатура для Android с поддержкой жестового ввода (swipe). Проект опубликован в Google Play и демонстрирует понимание работы систем ввода Android.

### ✨ Возможности
- **Жестовый ввод (Swipe)**: Собственная реализация распознавания свайпов.
- **Мультиязычность**: Динамическое переключение раскладок.
- **Интеграция с системой**: Ввод текста в любые приложения через `InputConnection`.
- **Оптимизация**: Минимизация создания объектов при обработке касаний для предотвращения лагов UI.

### 🛠️ Технологии
- **Язык**: Kotlin
- **API**: `InputMethodService`, `InputConnection`, `MotionEvent`
- **UI**: ViewBinding, Custom Views

### ⚙️ Как это работает
- **Обработка касаний**: Отслеживание `ACTION_DOWN`, `ACTION_MOVE`, `ACTION_UP` для определения траектории пальца.
- **Маппинг координат**: Перевод физических координат экрана в индексы клавиш.
- **Ввод текста**: Использование `commitText()` и `deleteSurroundingText()` для безопасного взаимодействия с текстовыми полями.

### 📸 Скриншоты
![Demonstration app work](https://github.com/HappyWorldGames/HWGSwipeKeyBoard/raw/master/screenshots/use_keyboard.gif)

### 🚀 Как запустить
1. Склонируй репозиторий.
2. Открой проект в Android Studio.
3. Собери и запусти на эмуляторе или устройстве.

---
