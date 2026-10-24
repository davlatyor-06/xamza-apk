# Xamzabek Video Yozuvchi

Bu loyiha GitHub Actions orqali avtomatik APK yig'adi.

## Qanday APK olish mumkin?

### 1-usul: GitHub Actions (eng oson)

1. GitHub.com ga kiring (hisob oching: github.com/signup)
2. "New repository" → nom bering (masalan: `xamzabek`)
3. Bu papkadagi barcha fayllarni yuklang:
   - `app/` papkasi
   - `gradle/` papkasi
   - `.github/` papkasi
   - `build.gradle`, `settings.gradle`, `gradlew`, `gradlew.bat`
4. GitHub avtomatik build boshlaydi!
5. **Actions** → **Build Xamzabek APK** → **Artifacts** → `Xamzabek-APK.zip` ni yuklab oling
6. Zip ichidagi `app-debug.apk` faylini telefoningizga o'tkazing

### Telefonda o'rnatish
1. **Sozlamalar** → **Xavfsizlik** → **Noma'lum manbalar** → ✅ ruxsat
2. `app-debug.apk` faylini oching va o'rnating

## Dastur imkoniyatlari
- 🔴 Orqa kamera bilan video yozish
- 📳 Yozayotganda bildirishnoma ko'rinib turadi
- 🔄 Boshqa dasturlarga kirsangiz ham yozaveradi
- ⏹ Bildirishnomadagi "To'xtatish" yoki dasturga qaytib bosish bilan to'xtatish
- 💾 Video: `Movies/Xamzabek/` papkasiga saqlanadi
