# v0.11 — Android paylaşımı ve kopyasız yerel dosyalar

Ana akış Android **Paylaş → SaveWhy**'dır. Sosyal bağlantı doğrudan hızlı kayıt ekranına gelir. Galerideki görsel/video `ACTION_SEND` ve content URI ile alınır; Yeni kayıt → Görsel ekle / Video ekle de Android sistem dosya seçicisini açar. Yüzen simge, Usage Access, Accessibility, ekran yakalama servisi ve başka uygulama içeriklerini izleme bu sürümde yoktur.

## Kopyasız ilişkilendirme

Yeni yerel kayıt, dosyanın aslı yerine sağlayıcının content:// URI'sini ve Android kalıcı salt-okuma iznini kullanır. Dosya adı yalnız görünen ad/arama bilgisidir; kimlik değildir. Özdeş URI ve niyet tekrar kaydedilemez. Dosya RAM'de küçük örneklem olarak okunur; asıl görsel/video uygulamanın images/videos dizinlerine yeniden yazılmaz. Etiket, not, OCR sonucu ve dosya bağlantısı kart verisinde tutulur. Yeni bağlı videolar eski 64 MB kopyalama sınırına tabi değildir; video kareleri için Android decoder/codec desteği gerekir.

ACTION_SEND her sağlayıcıda kalıcı izin sunmaz. Kalıcı izin alınamazsa paylaşımın mevcut URI'si güvenilir uzun süreli kayıt olarak saklanmaz: kullanıcıdan aynı dosyayı ACTION_OPEN_DOCUMENT ile seçmesi istenir. Dosya seçicisi de kalıcı izin vermiyorsa işlem açık bir hata mesajıyla durur; başka dosya kaynağı seçilebilir. Uygulama tüm galeriyi okuma izni istemez, özel Android izin ekranlarını açmaz ve izin sorununu gizli kopyalamayla aşmaz.

Dosyayı taşımak/silmek, uygulamayı kaldırmak/verisini temizlemek veya erişim iznini iptal etmek dosya bağlantısını bozabilir. Etiket ve notlar otomatik silinmez. Detayda erişim durumu, dosya adı ve **Dosyayı yeniden bağla** bulunur. Yeniden bağlamak aynı kartın kimliğini/niyetini/etiketini/notunu/hatırlatmasını korur; yeni görsele geçildiğinde eski OCR sonucu temizlenip yeniden taranır. Galeri dosyası hiçbir zaman SaveWhy tarafından değiştirilmez veya silinmez.

## Etiket, arama ve oynatma

Tek görsel/video bir kayıttır. Yerel medya ve sosyal bağlantılar aynı 20 ücretsiz kayıt kapasitesini paylaşır; satın alma $4.99 tek seferlik hedef fiyatla tasarlanmıştır, gerçek Play fiyatı yerel para biriminde alınır. Ekran görüntüleri/Videolar/Sosyal filtreleri ve kullanıcı tanımlı etiketler bulunur. En fazla 12 etiket, etiket başına 40 karakter vardır. Bağlı dosyanın görünen adı da aranır. URI referansı kalıcıdır, dosyanın her cihazda erişilebilir olduğu garantisi değildir.

Görseller uygulamada büyütülür, videolar seçilmiş URI için geçici okuma izniyle cihaz oynatıcısında açılır. Bundled Latin ML Kit modeli görselleri cihazda okur; yeni referanslar da aynı OCR aramasını kullanır. OCR tamamlanması kayıt yapmayı bekletmez, hata not/etiketi silmez ve yeniden tarama mümkündür. En fazla 4000 karakter indekslenir. Video içeriği/konuşması analiz edilmez. Model doğruluğu ve sağlayıcı izinleri fiziksel cihazda denenmelidir.

## Göç ve yedek

Kayıt biçimi v6 mediaUri/mediaName ekler; v1–5 okunur. Önceki sürümde kopyalanmış görsel/video dosyaları ve kayıtlar otomatik silinmez. Bunlar bir mevcut galeri dosyasına yeniden bağlanabilir; eski dosya daha sonra normal 7 günlük artık dosya temizliğiyle kaldırılabilir. Silme/geri alma dönemi korunur ve okunamayan kayıt dosyası varken dosya temizliği yapılmaz.

Yeni referansların ZIP/JSON yedeği yalnız kart bilgisi, etiket, not ve URI'yi taşır; asıl dosya veya Android izinleri yedeklenmez. Başka cihazda/yeniden kurulumda dosyaları yeniden bağlamak gerekir. Export bu bilgiyi ayarlarda, import referans varsa onay metninde açıklar. Legacy sahip olunan kopyalar ve seçilmiş sosyal küçük görseller ZIP'e dahil olmaya devam eder. ZIP yolu, boyut, SHA-256 ve bütünlük kontrolleri eski kopyalarda korunur. Limitler: 4 MB kart metadatası, 5000 kayıt, eski kopya için 2 MB görsel / 64 MB video, açılmış ZIP toplamında 2 GB. Yeni referanslar için asıl dosya boyutu bu yedek sınırlarına dahil değildir.

Kaynaklar:
- https://developer.android.com/training/data-storage/shared/documents-files
- https://developers.google.com/ml-kit/vision/text-recognition/v2/android
