# v0.10 — Ekran görüntüleri ve etiketler

Tek bir ekran görüntüsü veya fotoğraf Android Paylaş → SaveWhy üzerinden ya da Yeni kayıt → Görsel ekle ile alınır. Android sistem dosya seçicisi kullanılır; tüm galeriyi okuma izni istenmez. Bir görsel bir kayıt oluşturur. Çoklu toplu paylaşım ve otomatik galeri tarama bu sürümde yoktur.

Görsel için URL zorunluluğu yoktur. Mevcut niyet/hızlı neden/hatırlatma/yapılacaklar akışı kullanılır; başlık isteğe bağlıdır. En fazla 12 etiket (40 karakter) seçilebilir; özel etiketler virgülle girilir ve önceki etiketler hızlı seçeneklerde gösterilir. Etiketler kayda aittir; düzenle ekranından değiştirilebilir. Ekran görüntüleri filtresi görsel kayıtlarını, Videolar filtresi yerel video kayıtlarını, Sosyal filtresi sosyal/web bağlantılarını gösterir; normal Bekleyen/Unutulanlar/Tamamlanan filtreleri de çalışır. Arama başlık, neden, etiket ve OCR metnini birlikte tarar.

## Cihaz içi metin tanıma

`com.google.mlkit:text-recognition:16.0.1` Latin modeli APK içinde paketlenir. Türkçe ve İngilizce dahil Latin yazıları destekleyen cihaz içi OCR akışıdır; bulut OCR API'si, hesap veya abonelik kullanılmaz. Görselin kendisi bu uygulama tarafından OCR sunucusuna gönderilmez. ML Kit SDK'sının tanılama/performans verileri kendi şartlarına tabidir; Play Data Safety hazırlanırken SDK beyanları ayrıca incelenmelidir.

Kaydetme OCR tamamlanmasını beklemez. Durum pending/ready/failed olarak saklanır; en fazla 4000 karakter arama indeksine girer. Uygulama yeniden açılınca yarım kalan tarama sürdürülür. Hata görseli/notu/etiketleri silmez, önceki metni korur ve yeniden tarama mümkündür. Okunan metin detaydan görüntülenip seçilebilir. Okuma otomatik bir işlemdir ve son kullanıcı etkinliği tarihini değiştirmez. Modelin gerçek tanıma doğruluğu görüntü kalitesiyle değişir; iş akışı testlerindeki fake OCR sonuçları modelin cihaz doğruluğu testi değildir.

## Görsel saklama

Seçim arka planda uygulamanın özel dizinine JPEG kopyası olarak alınır, EXIF yönü uygulanır ve metadata yeniden sıkıştırma sırasında atılır. Galerideki asıl dosya değiştirilmez. Girdi en fazla 12 MB, 16384 px kenar / 100 milyon piksel; kayıtlı kopya yaklaşık en fazla 2560 px uzun kenar ve 2 MB'dir. Aynı kopya SHA-256 kimliğiyle tekrar kullanılır. Aynı görsel aynı niyetle ikinci kez kaydedilemez; başka niyetle saklanabilir. Görseller bağlantılarla ortak 20 ücretsiz kayıt kapasitesini paylaşır. Liste küçük örneklemi arka planda yükler; detay tam ekran pinch zoom ve sürükleme sunar.

Kayıtlı kopya galeri dosyasına/URI iznine bağımlı değildir. Silinen kayıtların görsel dosyası geri alma için hemen silinmez. Kayda/taslağa bağlı olmayan dosyalar 7 gün sonra sağlıklı bir sonraki açılışta temizlenir; bozuk kayıt dosyası varsa otomatik temizlik yapılmaz. Silinen görselin dosya zamanı güncellenerek geri alma için 7 günlük süre korunur. Veri temizleme ve uygulamayı kaldırma görselleri de kaldırır.

## Taşınabilir yedek

Yeni dışa aktarma ZIP'tir: records.json (kayıt biçimi v5), images/<SHA-256>.jpg videos/<SHA-256>.mp4 ve previews/<anahtar>.jpg. Görsel kayıtlarının dosyaları ve kullanıcının seçtiği sosyal önizlemeler dahildir; otomatik sosyal önizleme önbelleği taşınmaz. Eski v1–4 JSON ve eski yalın dizi yedekleri okunur. Yeni görsel kayıtlarını yalnız JSON ile taşımak eksik dosya oluşturacağından reddedilir.

ZIP içe aktarma canlı kayıtlara dokunmadan geçici dizinde bütünüyle doğrulanır: bilinmeyen yollar/zip-slip, tekrarlar, fazla giriş, eksik görsel, bozuk görsel, yanlış SHA-256 ve limitsiz açılma reddedilir. Kayıt metadatası 4 MB, toplam açılmış içerik 2 GB, kayıt sayısı 5000 ile sınırlıdır. İçe aktarma kapasite kontrolünden ve kullanıcı onayından sonra dosyaları taşır; kayıt yazımı başarısız olursa yeni taşınmış dosyalar geri alınır. Mevcut kayıtlar ve görseller korunur. ZIP yedeği şifreli değildir; ekran görüntüleri hassas bilgi içerebilir, saklama konumu kullanıcı tarafından seçilir.

Kaynak: https://developers.google.com/ml-kit/vision/text-recognition/v2/android

SDK veri açıklamaları: https://developers.google.com/ml-kit/android-data-disclosure

## Cihaz videoları ve fiyat

Yeni kayıt → Video ekle veya Android Paylaş → SaveWhy ile tek cihaz videosu eklenir. Video en fazla 64 MB olabilir; dosya belleğe bütünüyle alınmadan akış halinde kopyalanır ve SHA-256 ile adlandırılır. Android MediaMetadataRetriever videoda görüntü izi olduğunu denetler ve küçük bir kare üretir. Kare elde edilemezse oynatma işareti gösterilir; kare simgesi gerçek kare gibi sunulmaz. Video yeniden kodlanmaz; uygulamanın özel alanında byte-for-byte kopya saklanır. Galerideki aslı silinse de kopya kalır. Video etiketlenir, not/niyet/hatırlatma eklenebilir; videonun konuşması veya tamamı OCR/AI ile analiz edilmez.

Oynatma telefonun video uygulamasına ACTION_VIEW ile devredilir. Dış uygulamaya yalnız seçilmiş video için geçici okuma izni veren FileProvider kullanılır; sağlayıcı yalnız videos/ dizinini açar. Codec/container desteği cihaz oynatıcısına bağlıdır. Sosyal bağlantılar da kaynak uygulamada/tarayıcıda açılır. ZIP hem video dosyasını hem kare önizlemesini kapsar; video girişleri 64 MB sınırıyla akış halinde doğrulanır/geri yüklenir. Kayıtsız video dosyalarında da 7 günlük temizleme/geri alma süresi uygulanır.

Satın alma ekranında yapılandırılmış başlangıç fiyatı **$4.99**, screenshot/video/etiket/OCR/yedek özellikleri ve kullanım barı vardır. Play ürün bilgisi geldiğinde ödeme fiyatının kaynağı bu ürünün yerel biçimlenmiş fiyatıdır; USD yazısı gerçek mağaza fiyatının üstüne bindirilmez. Play Console'da savewhy_lifetime ürününün USD taban fiyatı 4.99 olarak ayarlanmalıdır; kod bu fiyatı mağazada değiştirmez ve ödeme yapılandırılmadan satın alma açılmaz. Ücretsiz bar kayıt sayısı/20 gösterir, ücretli lisans doğrulandığında gizlenir.
