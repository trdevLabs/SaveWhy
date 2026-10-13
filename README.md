# SaveWhy · Android 0.2.0

**Kaydettiğin içeriği neden kaydettiğini hatırla, eyleme dönüştür.**

Instagram, TikTok, YouTube veya bir web bağlantısını **Yap / Git / Al / Öğren / Üret** niyetlerinden biriyle kaydet. Başlık ve neden yaz, kendi eylem adımlarını oluştur, tamamladıklarını işaretle.

[Colab'da APK derle](https://colab.research.google.com/github/trdevLabs/SaveWhy/blob/main/colab/SaveWhy_Build.ipynb)

## Colab'da başla

1. Yukarıdaki bağlantıyı aç; CPU çalışma zamanı yeterlidir.
2. Defterdeki üç kod hücresini sırayla çalıştır: kurulum/repo, test/derleme, APK indirme.
3. `app-debug.apk` dosyasını telefona kur; Paylaş → SaveWhy akışını dene.

Defter JDK 17, Gradle 8.9, Android SDK 35 ve Build Tools 34.0.0 kullanır. SDK ve Gradle arşivlerinin SHA-256 özetleri sabitlenmiştir. GitHub token veya API anahtarı gerekmez. SDK lisans kabulü defterde açıklanır. Üretilen debug APK mağaza paketi değildir.

Colab her yeni ortamda farklı debug imza anahtarı oluşturabilir. Telefon eski imzayla kurulu uygulamanın üzerine yeni APK'yı kabul etmezse **uygulamayı silmeden önce JSON yedeği oluştur**; yeniden kurulumdan sonra içe aktar.

## Uygulanan özellikler

- Android Paylaş menüsünden `text/plain` bağlantı ve varsa paylaşım başlığı alma.
- Beş niyet, başlık, kaydetme nedeni ve isteğe bağlı eylem adımları.
- Niyete göre düzenlenebilir adım şablonları; kullanıcı eklediğinde uygulanır.
- Adımları tek tek işaretleme; kartı tamamlandı olarak işaretleme ve geri alma.
- Türkçe karakterlerden bağımsız başlık/neden/adım/bağlantı araması.
- Seçili niyeti gösteren filtreler, bekleyen/tamamlanan kayıt sayıları.
- Bağlantının orijinalini açma, kartı düzenleme ve onayla silme.
- Yaygın takip parametrelerini temizleme; video kimliği ve başlangıç zamanı gibi anlamlı parametreleri koruma.
- Aynı normalize bağlantı + niyet için tekrar kayıt kontrolü.
- JSON yedeğini sistem dosya seçicisiyle dışa aktarma ve doğrulayarak birleştirme.
- Ekran döndüğünde açık kayıt formundaki taslağı koruma.
- Atomik yerel kayıt; v0.1'in JSON kayıtlarını otomatik okuma.

Uygulama içerik analizi yapmaz. Adım şablonları niyete göre genel başlangıç önerileridir, AI çıktısı değildir. **AI kart üretimi, semantik arama, hatırlatmalar, harita ve bulut senkronizasyonu henüz uygulanmadı.**

## Yerel geliştirme

Gerekenler: JDK 17, Android SDK Platform 35, Build Tools 34.0.0. Android Studio ile klasörü aç veya SDK yolunu `ANDROID_HOME` değişkeniyle ayarla.

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Windows'ta `gradlew.bat` kullan. Gradle Wrapper repoda bulunur. APK: `app/build/outputs/apk/debug/app-debug.apk`.

Linux/Colab'da SDK kurulumunu da yapan script:

```bash
SAVEWHY_BUILD_CACHE=/content/savewhy-tools ACCEPT_ANDROID_SDK_LICENSES=yes bash scripts/build_colab.sh
```

Script `javac`, `curl`, `unzip` ve `python3` gerektirir. `ACCEPT_ANDROID_SDK_LICENSES` verilmezse lisansları etkileşimli sorar.

## Veri ve yedekler

Kayıtlar uygulamanın özel alanında `cards.json` dosyasında saklanır; ağ izni, hesap veya sunucu yoktur. Uygulama silinirse bu dosya silinir. Üstteki **••• → JSON yedeği oluştur** menüsüyle başka bir konuma yedek al.

İçe aktarma mevcut kayıtları değiştirmeden yeni kayıtları ekler. Aynı bağlantı ve niyet atlanır; aynı kimlikte farklı bir içerik varsa yeni kimlik verilir. Geçersiz dosya mevcut kayıtları etkilemez. Sınırlar: 4 MB JSON, 5000 kart, kart başına 30 adım. Okunamayan yerel dosya otomatik olarak sıfırlanmaz; uygulama dosyayı korumak için yazma işlemlerini durdurur.

## Doğrulama

25 JVM birim testi; URL doğrulama/temizleme, Türkçe arama, adım durumlarını koruma, yedek gidiş-dönüşü, eski veri geçişi ve birleştirme çakışmalarını kapsar.

Geliştirme ortamında JDK 17 ile `testDebugUnitTest lintDebug assembleDebug` başarıyla çalıştırıldı; 25 test geçti ve debug APK üretildi. APK imzası ve ZIP bütünlüğü ayrıca doğrulandı. Colab scripti APK oluşturmadan önce aynı test ve lint kontrollerini çalıştırır. **Gerçek cihazda kurulum ve Colab oturumunda çalıştırma henüz doğrulanmadı.** Cihazdaki kontroller için [kabul kontrolü](docs/ACCEPTANCE.md) listesine bak.

## Sonraki geliştirme

Gerçek cihazda paylaşım ve yedekleme akışını doğrula. Ardından kullanıcı notu/metin/transkript üzerinden sunucuda AI kart üretimi, niyete özel yapılandırılmış alanlar ve semantik arama ekle. Sadece bir URL, videonun içeriğine erişildiği anlamına gelmez; APK içine API anahtarı gömülmemelidir.
