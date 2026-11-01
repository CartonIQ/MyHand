MyHand - Android project (version 1.1.0)
=========================================

BUILD IN TERMUX
1. Copy MyHand_Project.zip to your phone's Downloads, then in Termux:
     cd ~ && rm -rf MyHand && unzip -o ~/storage/downloads/MyHand_Project.zip && cd MyHand
     export ANDROID_HOME=$HOME/qrapp/android-sdk
     bash build.sh
2. First build downloads a lot (10-20 minutes). Keep the screen on.
3. Install Downloads/MyHand.apk (allow "install unknown apps" for your file manager).
If it fails, send the LAST 20 LINES of the output.

FEATURES
- Draw mode (66 characters, 3 sets) and Scan mode (beta, printed template)
- Write: 5 papers, 4 inks, size / spacing / slant / natural-look sliders,
  12 festival and card ideas, live preview, export PNG and PDF
- Letters to Future You: sealed letters that unlock on a date, notification when ready,
  read the letter in your own handwriting (alarm can be a few minutes late)
- Signature maker: transparent PNG
- Sticker maker: word stickers (Clear, White outline, 3 bubble colours), PNG or WebP
- Backup and restore: one zip with handwriting and letters
- Pink and lavender design, rounded cards, chips and pill buttons
- Offline for everything except ads
- AdMob: banner on Home, interstitial after export (max 1 per 2.5 min),
  rewarded ad removes the watermark for one export, Google consent form

NOT INCLUDED YET
Time-lapse video, one-tap WhatsApp sticker pack (stickers are shared as images),
widget, cursive connect, doodle layer.

ADS - IMPORTANT
Google TEST ad IDs are in app/src/main/res/values/strings.xml. Test ads are safe to tap.
For the Play Store build, create your own AdMob app and ad units, replace the 4 IDs,
and never tap your own live ads.

KNOWN LIMITS
- Scan needs a straight, well-lit photo with the thick black border fully visible.
- Handwriting lives inside the app (Android does not allow a phone-wide font install).
- Letters are stored privately on the phone but not encrypted. Uninstalling deletes everything
  unless you made a backup.
- This source was written without being compiled. If the build shows an error, send it to me.

RELEASE
Create a keystore with keytool, add a signing config, run: gradle bundleRelease
Also needed: privacy policy URL, Data safety form, content rating, icon, screenshots.
