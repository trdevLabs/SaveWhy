# SaveWhy · Android 0.7.0

**Paylaş. Niyetine dokun. Kaydet.**

Sosyal medyada kaydettiğin içeriği neden kaydettiğini hatırla ve zamanı geldiğinde harekete geç.

[Colab'da APK derle](https://colab.research.google.com/github/trdevLabs/SaveWhy/blob/main/colab/SaveWhy_Build.ipynb) · [Ürün yol haritası](docs/ROADMAP.md) · [Cihazda kabul kontrolü](docs/ACCEPTANCE.md)

## Hızlı kullanım

Instagram, TikTok, YouTube veya bir web bağlantısında **Paylaş → SaveWhy**. **Yap / Git / Al / Öğren / Üret / İzle / Bul** niyetlerinden birine dokun; kaydet.

Başlık yazmak veya detay eklemek zorunlu değildir. Başlık paylaşım bilgisinden alınır; bilgi yoksa bağlantının kaynağı ve içerik kimliğinden oluşturulur. Her niyette dört hızlı neden vardır. Özel başlık, not, adımlar ve Bul sorgusu kapalı, isteğe bağlı detay alanındadır. Uygulama panoyu yalnızca **Panodan ekle** düğmesine dokunduğunda okur.

**İzle** daha sonra tüketmek istediğin içerik içindir. **Bul** içerikte gördüğün bir şeyin kimliğini, kaynağını veya benzerini araştırmak içindir. Bul kartında web aramasından önce sorguyu gözden geçirirsin; özel notun sorguya otomatik eklenmez. Al, satın alma kararı; Öğren, öğrenme hedefi içindir.

## v0.7 yenilikleri

- Yeni sayfalarda sistem çubuğu boşlukları hemen uygulanır; yukarı taşma düzeltildi.
- Tema adı Karanlık oldu; AMOLED siyah zemin korunur.
- Kompakt liste: küçük görsel, daha küçük başlık, niyet rozeti ve kaynak adı.
- Tüm platformlarda içerik kaynak uygulamada/tarayıcıda açılır; uygulama içi oynatıcı yoktur.
- Küçük görseller önbelleğe alınır; çevrimdışı kayıt akışı korunur.

Ayrıntılar ve platform sınırları: [medya önizleme](docs/MEDIA_PREVIEW.md).

## v0.6 yenilikleri

- Kompakt başlık: küçük logo, bildirim zili ve ayarlar; arama ve iki filtre satırı.
- Ana sayfadan Nasıl çalışır düğmesi kaldırıldı; kayıt ekranı ve ikincil düğmeler sadeleştirildi.
- Android ayarlarına benzer bölüm başlıkları, mevcut değerler ve kompakt satırlardan oluşan tam ekran ayarlar.
- Yerel bildirim merkezi: okunmamış sayacı, kısa zil animasyonu, kalıcı hatırlatma geçmişi ve yaklaşan tarihler.

Ayrıntılar: [arayüz ve bildirim merkezi](docs/UI_AND_NOTIFICATIONS.md). Sunucu veya AI kullanımı yoktur.

## v0.5 yenilikleri

- Sade ana ekran: slogan/sayaç satırları kaldırıldı, arama açıklaması kutunun içine alındı; küçük splash logosu uygulama adının solunda.
- Unutulanlar: varsayılan 15 günden uzun süre işlem görmeyen tamamlanmamış kayıtlar; ayarlardan 1–365 gün.
- Niyet ve hızlı neden ekleme, yeniden adlandırma, göster/gizle; kişisel seçenekler kalıcıdır ve JSON yedeğiyle taşınır.
- Ayarlar menüsü sadeleşti; adımsız kartların Niyetiyle kaydedildi satırı kaldırıldı.

Davranış ve veri geçişi: [kişiselleştirme](docs/CUSTOMIZATION.md).

## v0.4 yenilikleri

- Detay alanlarında dış etiketler ve ayrı açıklamalar; üst üste binen hint kaldırıldı.
- İsteğe bağlı yapılacaklar listesi; örnek işler seçilerek mevcut listeye eklenir.
- Yerel tek seferlik hatırlatma: bir saat sonra, akşam, yarın, hafta sonu veya özel tarih/saat.
- Android 13+ bildirim izni yalnızca hatırlatıcı seçildiğinde istenir. Bildirimler cihazında görünür; birlikte seçimi başka kişiye mesaj göndermez.
- Türkçe / English / Sistem dili. Varsayılan sistem dili, desteklenmeyen sistem dillerinde İngilizce.
- 2,2 saniyelik, dokunarak geçilebilir AMOLED splash. Paylaşım ve bildirim açılışları splash beklemez.

Android pil tasarrufu nedeniyle hatırlatmalar seçilen saatten sonra gelebilir. Ayrıntılar: [hatırlatmalar ve dil](docs/REMINDERS_AND_LANGUAGES.md).

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

Başlıklar **içerik özeti değildir**; uygulama videoyu analiz etmez. Hazır adımlar niyete göre genel başlangıç önerileridir. **AI kart üretimi, semantik arama, nesne tanıma, fiyat takibi ve bulut senkronizasyonu henüz uygulanmadı.** Cihaz içi OCR ve tek seferlik Google Play satın alma entegrasyonu vardır; mağaza ürünü ve doğrulama anahtarı yapılandırılmadan ödeme canlı değildir. Ücretli ürün hedefi ve yayın öncesi kontroller [yol haritasında](docs/ROADMAP.md) tanımlıdır.

## Colab derlemesi

1. Yukarıdaki Colab bağlantısını aç; CPU çalışma zamanı yeterlidir.
2. Üç kod hücresini sırayla çalıştır: kurulum/repo, test/derleme, APK indirme.
3. `app-debug.apk` dosyasını telefona kur; hızlı paylaşım ve temaları dene.

JDK 17, Gradle 8.9, Android SDK 35 ve Build Tools 34.0.0 kullanılır. SDK/Gradle arşivlerinin SHA-256 özetleri sabitlenmiştir. İlk derleme ve arayüz testleri bağımlılıkları indirir. GitHub token veya model API anahtarı gerekmez. SDK lisans kabulü defterde açıklanır. Debug APK mağaza paketi değildir.

Colab her yeni ortamda farklı debug imza anahtarı oluşturabilir. Telefon eski uygulamanın üzerine APK'yı kabul etmezse **uygulamayı silmeden önce görselleri içeren yedek oluştur**; yeniden kurulumdan sonra içe aktar.

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

Kayıtlar ve hatırlatma tarihleri uygulamaya özel `cards.json` dosyasında saklanır. Hesap veya AI sunucusu yoktur. Küçük görseller için internet izni vardır; desteklenen sosyal bağlantılar ve görsel CDN'leri üzerinden herkese açık önizleme verisi alınır. Başlıklar, notlar ve kayıt listesi gönderilmez. Kayıt verileri cihazında kalır. Tüm içerikler ve web aramaları tarayıcıda/kaynak uygulamasında açılır. Kullanıcının seçtiği küçük görseller ve kaydedilen ekran görüntüleri cihazda saklanır; yeni ZIP yedeğine dahildir. Uygulama silinirse yerel dosya silinir. Menüden **Yedek oluştur** ile başka bir konuma yedek al.

İçe aktarma mevcut kayıtları değiştirmeden yeni kayıtları ekler. Aynı bağlantı/niyet veya görsel/niyet atlanır; aynı kimlikte farklı içerik varsa yeni kimlik verilir. Geçersiz dosya mevcut kayıtları etkilemez. Sınırlar: 4 MB kayıt metadatası, 5000 kart, kart başına 30 adım; görsel içe aktarmada 12 MB, kayıtlı görselde 2 MB, cihaz videosunda 64 MB, ZIP yedeğinin toplam açılmış içeriğinde 2 GB. Okunamayan yerel dosya otomatik sıfırlanmaz; yazma işlemleri durdurulur.

v0.1'in dizi biçimi ve v0.2'nin sürüm 1 JSON biçimi okunur. v0.10 kayıt biçimi **sürüm 5**'tir; görsel kimliği, etiketler ve okunan metin eklenmiştir. Sürüm 1–4 JSON yedekleri de okunur; eski uygulama yeni biçimi okuyamaz. Eski APK'ya dönüş için uygulamayı silmek veri kaybına neden olabilir; yedek ve sürüm uyumluluğunu önce kontrol et.

## Doğrulama

Veri testleri URL işlemleri, Türkçe arama, eski kayıt geçişi, yedekleme/birleştirme, yedi niyet ve başlık/not yazmadan hızlı kaydetmeyi kapsar. Robolectric arayüz testleri hızlı paylaşım kaydını, AMOLED/aydınlık renkleri ve taslağın yeniden oluşturulmasını kontrol eder. Arayüz testleri yerel inceleme için `app/build/reports/ui` altında görüntüler üretir.

**105 test** (hatırlatma, dil ve Android arayüz testleri dahil), `lintDebug` ve temiz debug APK derlemesi geliştirme ortamında geçti; APK imzası ve ZIP bütünlüğü doğrulandı. **Gerçek cihaz ve Colab oturumunda etkileşimler ayrıca doğrulanmalıdır.** Testlerin geçmesi mağaza yayınına hazır olunduğu anlamına gelmez; cihaz kabul listesi ve ücretli ürün yayın kapıları tamamlanmalıdır.

## Üçüncü taraf varlıklar

Manrope, Google Fonts deposundan alınmıştır: https://github.com/google/fonts/tree/main/ofl/manrope . SIL Open Font License 1.1 metni `app/src/main/res/raw/manrope_license.txt` içinde ve APK'da bulunur. Manrope değişken fontundan 400/700 ağırlıklarında statik dosyalar üretildi; fontun telif ve lisans bilgileri korunur. Sosyal platform vektörleri Simple Icons (CC0) kaynaklıdır; lisans `app/src/main/res/raw/simple_icons_license.txt` içindedir. Diğer vektörler proje içinde tanımlanır. Splash illüstrasyonu proje için oluşturulmuştur. Material Components/AndroidX sürümleri Gradle dosyasında sabitlenmiştir.


## v0.8 cihaz ve mağaza kabulü

- Ana arama, başlık/ikonlar, filtre ve kartların dış kenarları aynı hizada olmalı.
- Uygulama simgesi splash görseliyle aynı mint yer imi olmalı; alt copyright kesilmemeli ve dokunma yazısı görünmemeli.
- 20 kayıt oluştur; 21. yeni kayıt satın alma ekranına gitmeli. Düzenleme/okuma/silme/yedekleme kullanılmalı.
- İçe aktarma 20'yi aşıyorsa hiçbir yeni kayıt yazılmamalı. 20'den fazla eski veri korunmalı.
- Destek/geri bildirim mail taslağında alıcı tr.developer@hotmail.com, konu başı savewhy olmalı. Kendi kendine mail gönderilmemeli.
- Internal testing Play kurulumu: tek seferlik fiyat, iptal, bekleyen ödeme, satın alma, acknowledge, yeniden kurulum/geri yükleme ve iade sonrası sahiplik sorgusu denenmeli.
- Public key veya ürün yoksa satın alma açıkmış gibi görünmemeli; debug test bypass bulunmamalı.

## v0.9 doğrulama

- 126 otomatik test: 0 hata / 0 başarısız / 0 atlanan.
- Launcher'ın MainActivity'yi çözümlemesi, singleTask/never manifest ayarları ve splash'ın ikinci MainActivity açmadan dönmesi doğrulandı.
- Dört sosyal kaynakta dış bağlantı ve WebView olmaması UI testleriyle doğrulandı.
- Gerçek JPEG fixture'larıyla dört kaynak için önbellek bağlama, manuel görselin yeniden açılışta kalması ve bozuk dosyanın reddi doğrulandı. Bu fixture testleri platformların canlı erişim garantisi değildir.
- Cihazda son uygulamalar/paylaşım/hatırlatma ve canlı platform bağlantılarının son kabulü yapılmalı.

## v0.10 doğrulama

- 151 otomatik test: görsel/etiket/OCR sonuçlarının korunması ve araması, v4 göçü, 20 kayıt ortak kapasitesi, yerel görsel büyütme, Android image/video paylaşımı ve dosya seçici.
- Bozuk/eksik/limit dışı ZIP girişleri, zip-slip ve checksum uyuşmazlığı reddi; JPEG + video + etiketli kayıt yedeğinin farklı özel dizine geri yüklenmesi ve asset geri alma doğrulandı.
- Gerçek ffmpeg MP4 fixture'ıyla byte-for-byte özel video kopyası; testte sahte Android kare çıkarıcısıyla poster akışı. Görsel metin modeli de UI iş akışı testinde fake OCR ile çalıştırıldı; fiziksel cihazda OCR doğruluğu/codec oynatma ayrıca kontrol edilmeli.
- $4.99 satın alma ekranı, screenshot/video özellikleri, 3/20 kullanım barı ve ödeme yapılandırılmadığında satın alma düğmesinin kapalı kalması UI testleriyle doğrulandı.
- APK derlemesi, ZIP bütünlüğü ve imza doğrulaması başarılı; lint sıfır Error/Fatal.
