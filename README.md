# SaveWhy · Android 0.10.0

**Paylaş. Niyetine dokun. Kaydet.**

Sosyal medyada kaydettiğin içeriği neden kaydettiğini hatırla ve zamanı geldiğinde harekete geç.

[Colab'da APK derle](https://colab.research.google.com/github/trdevLabs/SaveWhy/blob/main/colab/SaveWhy_Build.ipynb) · [Ürün yol haritası](docs/ROADMAP.md) · [Cihazda kabul kontrolü](docs/ACCEPTANCE.md)

## Hızlı kullanım

Instagram, TikTok, YouTube veya bir web bağlantısında **Paylaş → SaveWhy**. **Yap / Git / Al / Öğren / Üret / İzle / Bul** niyetlerinden birine dokun; kaydet.

Başlık yazmak veya detay eklemek zorunlu değildir. Başlık paylaşım bilgisinden alınır; bilgi yoksa bağlantının kaynağı ve içerik kimliğinden oluşturulur. Her niyette dört hızlı neden vardır. Özel başlık, not, adımlar ve Bul sorgusu kapalı, isteğe bağlı detay alanındadır. Uygulama panoyu yalnızca **Panodan ekle** düğmesine dokunduğunda okur.

**İzle** daha sonra tüketmek istediğin içerik içindir. **Bul** içerikte gördüğün bir şeyin kimliğini, kaynağını veya benzerini araştırmak içindir. Bul kartında web aramasından önce sorguyu gözden geçirirsin; özel notun sorguya otomatik eklenmez. Al, satın alma kararı; Öğren, öğrenme hedefi içindir.

## v0.10 yenilikleri

- Ekran görüntüsü ve fotoğraf: Galeride **Paylaş → SaveWhy** veya **Yeni kayıt → Görsel ekle**. URL gerekmez; tek görsel bir kayıt oluşturur.
- Hızlı niyet/etiket seçimi ve özel etiketler; kayıt başına en fazla 12 etiket. Önceki etiketler hızlı seçeneklere gelir.
- **Ekran görüntüleri / Videolar / Sosyal** filtreleri. Görsellerde yakınlaştırma; cihaz videolarında kare önizlemesi ve telefonun oynatıcısıyla açma.
- Uygulamaya dahil Latin metin tanıma modeli: Türkçe/İngilizce gibi Latin yazıları cihazda okunup aramaya eklenir; bulut OCR/AI çağrısı yoktur. Başarısız okuma kaydı engellemez, yeniden tarama mümkündür.
- Görselin uygulama içinde kopyası saklanır; galeriden silmek kaydı bozmaz. ZIP yedeği görselleri ve seçilmiş küçük görselleri kapsar; eski JSON yedekleri okunur.
- Görseller, cihaz videoları ve bağlantılar aynı 20 ücretsiz kayıt kapasitesini paylaşır; kullanım barı ana sayfada ve satın alma ekranında görünür.
- Tek seferlik fiyat **$4.99** olarak tasarlandı. Play hazır olduğunda gerçek yerel mağaza fiyatı gösterilir; mağaza ürünü/fiyatı/anahtarı henüz yapılandırılmadığından ödeme canlı değildir.

Ayrıntılar: [görseller, etiketler ve yedekler](docs/SCREENSHOTS.md).

## v0.9 yenilikleri

- Launcher, paylaşım ve bildirim aynı `singleTask` MainActivity'ye gider; tekrar açılış mevcut pencereyi kullanır. Splash bu pencerenin üzerinde kısa süreli bir ekrandır.
- YouTube dahil bütün içerikler kaynak uygulamada veya tarayıcıda açılır; gömülü oynatıcı kaldırıldı.
- Instagram/Facebook için herkese açık Open Graph, TikTok için oEmbed/Open Graph küçük görselleri desteklenir. X/Pinterest önizlemeleri de aynı akışı kullanır.
- Erişimi engellenen/özel içeriklerin otomatik görseli garanti edilemez. Detayda **Küçük görsel seç** ile cihazdan gerçek bir görsel atanabilir.

## v0.8 yenilikleri

- Kart ve üst menü kenarları hizalı; kart boşlukları azaltıldı.
- Launcher ve splash aynı görseli kullanır; splash altına Copy Right 2026 eklendi, görünen dokunma yazısı kaldırıldı.
- İlk 20 kayıt ücretsiz kapasite; daha fazla kayıt için tek seferlik SaveWhy Sınırsız ekranı. Düzenleme/okuma/yedekleme daima erişilebilir.
- Google Play INAPP entegrasyonu, imzalı makbuz doğrulama ve geri yükleme. **Ürün, fiyat ve public key Play Console'da henüz yapılandırılmadı; gerçek ödeme canlı değil.**
- İletişim/destek ve isteğe bağlı memnuniyet geri bildirimi; e-posta konuları savewhy ile başlar.

Kurulum ve güvenlik sınırları: [satın alma ve destek](docs/PURCHASE_AND_SUPPORT.md).

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

Ayrıntılar: [arayüz ve bildirim merkezi](docs/UI_AND_NOTIFICATIONS.md). Bulut AI servisi yoktur; görsellerde cihaz içi metin tanıma kullanılır.

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

Başlıklar **içerik özeti değildir**; uygulama videoyu analiz etmez. Hazır adımlar niyete göre genel başlangıç önerileridir. **AI kart üretimi, semantik arama, nesne tanıma, fiyat takibi ve bulut senkronizasyonu henüz uygulanmadı.** Tek seferlik Google Play satın alma entegrasyonu vardır; mağaza ürünü, fiyat ve doğrulama public key yapılandırılmadan ödeme canlı değildir. Abonelik entegrasyonu yoktur. Ücretli ürün hedefi ve yayın öncesi kontroller [yol haritasında](docs/ROADMAP.md) tanımlıdır.

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

**117 test** (hatırlatma, dil ve Android arayüz testleri dahil), `lintDebug` ve temiz debug APK derlemesi geliştirme ortamında geçti; APK imzası ve ZIP bütünlüğü doğrulandı. **Gerçek cihaz ve Colab oturumunda etkileşimler ayrıca doğrulanmalıdır.** Testlerin geçmesi mağaza yayınına hazır olunduğu anlamına gelmez; cihaz kabul listesi ve ücretli ürün yayın kapıları tamamlanmalıdır.

## Üçüncü taraf varlıklar

Manrope, Google Fonts deposundan alınmıştır: https://github.com/google/fonts/tree/main/ofl/manrope . SIL Open Font License 1.1 metni `app/src/main/res/raw/manrope_license.txt` içinde ve APK'da bulunur. Manrope değişken fontundan 400/700 ağırlıklarında statik dosyalar üretildi; fontun telif ve lisans bilgileri korunur. Sosyal platform vektörleri Simple Icons (CC0) kaynaklıdır; lisans `app/src/main/res/raw/simple_icons_license.txt` içindedir. Diğer vektörler proje içinde tanımlanır. Splash illüstrasyonu proje için oluşturulmuştur. Material Components/AndroidX sürümleri Gradle dosyasında sabitlenmiştir.
