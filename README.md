# SaveWhy · Android 0.3.0

**Paylaş. Niyetine dokun. Kaydet.**

Sosyal medyada kaydettiğin içeriği neden kaydettiğini hatırla ve zamanı geldiğinde harekete geç.

[Colab'da APK derle](https://colab.research.google.com/github/trdevLabs/SaveWhy/blob/main/colab/SaveWhy_Build.ipynb) · [Ürün yol haritası](docs/ROADMAP.md) · [Cihazda kabul kontrolü](docs/ACCEPTANCE.md)

## Hızlı kullanım

Instagram, TikTok, YouTube veya bir web bağlantısında **Paylaş → SaveWhy**. **Yap / Git / Al / Öğren / Üret / İzle / Bul** niyetlerinden birine dokun; kaydet.

Başlık yazmak veya detay eklemek zorunlu değildir. Başlık paylaşım bilgisinden alınır; bilgi yoksa bağlantının kaynağı ve içerik kimliğinden oluşturulur. Her niyette dört hızlı neden vardır. Özel başlık, not, adımlar ve Bul sorgusu kapalı, isteğe bağlı detay alanındadır. Uygulama panoyu yalnızca **Panodan ekle** düğmesine dokunduğunda okur.

**İzle** daha sonra tüketmek istediğin içerik içindir. **Bul** içerikte gördüğün bir şeyin kimliğini, kaynağını veya benzerini araştırmak içindir. Bul kartında web aramasından önce sorguyu gözden geçirirsin; özel notun sorguya otomatik eklenmez. Al, satın alma kararı; Öğren, öğrenme hedefi içindir.

## Arayüz

- Varsayılan **simsiyah AMOLED** tema: ana arka plan `#000000`.
- Aydınlık ve sistem tercihi seçenekleri; tema seçimi cihazda saklanır.
- Uygulamaya gömülü, Türkçe karakterleri destekleyen **Manrope** fontu.
- Material 3 kartlar, seçim chipleri, alt paneller, metin alanları ve düğmeler.
- Vektör ikonlar; ikon düğmelerinde erişilebilir adlar.
- Uzun listeler için RecyclerView; arama süzmesi arka plan iş parçacığında, kısa gecikmeyle yapılır.
- Geri alınabilir silme; kaydetme/tamamlama geri bildirimi.

## Özellikler ve sınırlar

Paylaşım bağlantısı alma, niyetle kaydetme, hızlı nedenler, isteğe bağlı eylem adımları, Türkçe karakterlerden bağımsız arama, niyet/durum filtreleri, düzenleme, tamamlandı işaretleme, kaynağı açma, yedekleme ve eski veri geçişi uygulanmıştır.

Başlıklar **içerik özeti değildir**; uygulama videoyu analiz etmez. Hazır adımlar niyete göre genel başlangıç önerileridir. **AI kart üretimi, semantik arama, hatırlatmalar, nesne tanıma, fiyat takibi ve bulut senkronizasyonu henüz uygulanmadı.** Uygulamada ödeme veya abonelik entegrasyonu yoktur. Ücretli ürün hedefi ve yayın öncesi kontroller [yol haritasında](docs/ROADMAP.md) tanımlıdır.

## Colab derlemesi

1. Yukarıdaki Colab bağlantısını aç; CPU çalışma zamanı yeterlidir.
2. Üç kod hücresini sırayla çalıştır: kurulum/repo, test/derleme, APK indirme.
3. `app-debug.apk` dosyasını telefona kur; hızlı paylaşım ve temaları dene.

JDK 17, Gradle 8.9, Android SDK 35 ve Build Tools 34.0.0 kullanılır. SDK/Gradle arşivlerinin SHA-256 özetleri sabitlenmiştir. İlk derleme ve arayüz testleri bağımlılıkları indirir. GitHub token veya model API anahtarı gerekmez. SDK lisans kabulü defterde açıklanır. Debug APK mağaza paketi değildir.

Colab her yeni ortamda farklı debug imza anahtarı oluşturabilir. Telefon eski uygulamanın üzerine APK'yı kabul etmezse **uygulamayı silmeden önce JSON yedeği oluştur**; yeniden kurulumdan sonra içe aktar.

## Yerel geliştirme

JDK 17, Android SDK Platform 35 ve Build Tools 34.0.0 gerektirir. Android Studio'da klasörü aç veya SDK yolunu `ANDROID_HOME` ile ayarla.

```bash
./gradlew clean testDebugUnitTest lintDebug assembleDebug
```

Windows'ta `gradlew.bat` kullan. APK: `app/build/outputs/apk/debug/app-debug.apk`.

SDK kurulumunu da yapan Linux/Colab scripti:

```bash
SAVEWHY_BUILD_CACHE=/content/savewhy-tools ACCEPT_ANDROID_SDK_LICENSES=yes bash scripts/build_colab.sh
```

Script `javac`, `curl`, `unzip` ve `python3` gerektirir; lisans değişkeni verilmezse etkileşimli kabul sorar. Notebook yalnızca başarılı derlemeyle aynı commit ve özet değerine sahip APK'yı indirir.

## Veri ve yedekler

Kayıtlar uygulamaya özel `cards.json` dosyasında saklanır. Hesap veya sunucu yoktur; ağ izni istenmez. Web araması ve kaynak açma cihazın tarayıcısında/kaynak uygulamasında yapılır. Uygulama silinirse yerel dosya silinir. Menüden **JSON yedeği oluştur** ile başka bir konuma yedek al.

İçe aktarma mevcut kayıtları değiştirmeden yeni kayıtları ekler. Aynı bağlantı/niyet atlanır; aynı kimlikte farklı içerik varsa yeni kimlik verilir. Geçersiz dosya mevcut kayıtları etkilemez. Sınırlar: 4 MB JSON, 5000 kart, kart başına 30 adım. Okunamayan yerel dosya otomatik sıfırlanmaz; yazma işlemleri durdurulur.

v0.1'in dizi biçimi ve v0.2'nin sürüm 1 JSON biçimi okunur. v0.3 yedek biçimi **sürüm 2**'dir ve Bul sorgusunu içerir; v0.2 uygulaması yeni biçimi okuyamaz. Eski APK'ya dönüş için uygulamayı silmek veri kaybına neden olabilir; yedek ve sürüm uyumluluğunu önce kontrol et.

## Doğrulama

Veri testleri URL işlemleri, Türkçe arama, eski kayıt geçişi, yedekleme/birleştirme, yedi niyet ve başlık/not yazmadan hızlı kaydetmeyi kapsar. Robolectric arayüz testleri hızlı paylaşım kaydını, AMOLED/aydınlık renkleri ve taslağın yeniden oluşturulmasını kontrol eder. Arayüz testleri yerel inceleme için `app/build/reports/ui` altında görüntüler üretir.

**60 test** (6 Android arayüz testi dahil), `lintDebug` ve temiz debug APK derlemesi geliştirme ortamında geçti; APK imzası ve ZIP bütünlüğü doğrulandı. **Gerçek cihaz ve Colab oturumunda etkileşimler ayrıca doğrulanmalıdır.** Testlerin geçmesi mağaza yayınına hazır olunduğu anlamına gelmez; cihaz kabul listesi ve ücretli ürün yayın kapıları tamamlanmalıdır.

## Üçüncü taraf varlıklar

Manrope, Google Fonts deposundan alınmıştır: https://github.com/google/fonts/tree/main/ofl/manrope . SIL Open Font License 1.1 metni `app/src/main/res/raw/manrope_license.txt` içinde ve APK'da bulunur. Manrope değişken fontundan 400/700 ağırlıklarında statik dosyalar üretildi; fontun telif ve lisans bilgileri korunur. Vektör ikonlar proje içinde tanımlanır. Material Components/AndroidX sürümleri Gradle dosyasında sabitlenmiştir.
