# Cihazda kabul kontrolü · v0.3

Android 8+ üzerinde, hem normal açılışta hem uygulama zaten açıkken dene. Bu kontroller gerçek cihazda ayrıca yapılmalıdır.

## Hızlı kaydetme

- [ ] Instagram, TikTok ve YouTube'dan Paylaş → SaveWhy. Başlık veya not yazmadan bir niyete dokunup Kaydet ile kayıt oluştur. Klavye otomatik açılmamalı.
- [ ] Yedi niyetin her birini dene; farklı hızlı neden seç. Başlık paylaşım bilgisinden ya da kaynak/içerik kimliğinden oluşmalı.
- [ ] Niyet seçmeden veya geçerli URL eklemeden kaydetme düğmesi etkin olmamalı.
- [ ] Panodan ekle yalnızca düğmeye dokunduğunda panoyu okusun. Geçersiz metin hata mesajı versin.
- [ ] Not ve detayları aç; özel başlık/not yaz. Hızlı nedeni tekrar seçince özel notun yerine yeni seçimi kullanmayı doğrula.
- [ ] Taslak açıkken ekranı döndür; URL, niyet, hızlı neden, açık detaylar ve özel alanlar korunmalı.
- [ ] Başka bir paylaşım açık taslağı değiştirmeden önce seçim sunmalı.

## Tema ve okunabilirlik

- [ ] İlk açılışta AMOLED tema: ana arka plan #000000; kartlar, paneller, yazılar ve düğmeler okunabilir.
- [ ] Aydınlık ve sistem temalarını seç; uygulamayı kapatıp aç; seçimin korunduğunu doğrula.
- [ ] Tema değişiminden sonra açık taslak kaybolmasın; tüm bileşenler yeni temayı kullansın.
- [ ] Büyük yazı boyutu ve küçük ekranda niyet/neden seçimleri, detaylar, klavye ve Kaydet erişilebilir olsun.
- [ ] Fontta Ç, Ğ, İ, Ö, Ş, Ü ve küçük karşılıkları doğru görünsün. TalkBack ikon düğmelerinin adlarını okusun.

## Kullanım ve veri

- [ ] İzle kartından İçeriği izle; ardından İzledim → tamamlananlar → geri al.
- [ ] Bul kartından Web'de ara: sorguyu gözden geçir/düzenle; yalnızca sorgu tarayıcıya gönderilmeli. Özel not gönderilmemeli.
- [ ] Bekleyen yalnızca açık kayıtları, Tümü tüm durumları göstersin; niyet filtresinde tamamlanan kayıtlar da bulunsun.
- [ ] Arama başlık, neden, adım ve Bul sorgusunu kapsasın; Türkçe karakterden bağımsız arama çalışsın. Uzun listede yazma ve kaydırma takılmasın.
- [ ] Kartı düzenle; değişmeyen adımın tamamlanma durumu korunsun. Uygulamayı kapatıp aç; tüm kayıtlar korunsun.
- [ ] Silmeyi önce iptal et; sonra sil ve Geri al ile kurtar. Kalıcı kayıttan da geri gelmeli.
- [ ] Aynı bağlantıyı aynı niyetle tekrar kaydetmeye çalış; hata versin. Başka niyetle kaydetmeye izin versin.
- [ ] JSON yedeğini temiz kurulumda içe aktar; Bul sorgusu ve tüm durumlar korunsun. Aynı yedeği tekrar içe aktarınca kayıtlar çoğalmasın.
- [ ] v0.1/v0.2 yedeklerini aç; eski kayıtlar korunsun. Geçersiz/gelecek sürüm yedek dosyası mevcut veriyi değiştirmesin.
- [ ] İmzalı bir APK güncellemesini eski kurulum üzerine dene. Colab debug imzası değiştiyse uygulamayı silmeden yedek al.

Birim testleri veri işlemlerini; arayüz testleri Android bileşenlerini emüle ederek temel etkileşimleri doğrular. Paylaşım uygulamaları, dosya sağlayıcıları, gerçek klavye ve cihaz performansı bu listede ayrıca doğrulanır.


## v0.4 cihaz kabulü

- Detayları aç: başlık ve not etiketi metinle çakışmamalı; büyük yazı boyutunda Kaydet erişilebilir kalmalı.
- Örnek yapılacaklardan bir madde seç; kendi metnin ve tamamlanan eski maddelerin korunmalı.
- 1 saat sonra veya yakın özel tarih/saat için hatırlatma ekle. Android 13+ izin kabul/ret ve kanal kapalı durumlarını dene.
- Uygulamayı kapatıp bildirimi aç; ilgili kart gelmeli. Tamamlama ve silme sonrası bildirim gelmemeli.
- Telefonu yeniden başlatıp bekleyen hatırlatmayı kontrol et. Android pil tasarrufu bildirim zamanını geciktirebilir.
- Hatırlatmalı JSON yedeğini dışa/içe aktar; tarih korunmalı. Eski sürüm 1/2 yedeklerini de dene.
- Sistem dili Türkçe/İngilizce iken ilk açılışı dene. Ayarlardan diğer dili seç, yeniden aç; seçim korunmalı. Sistem dili seçimine dön, sistem dilini değiştir.
- Kullanıcının başlığı, notu ve kendi yapılacakları dil değişiminde aynen kalmalı.
- Normal splash yaklaşık 2,2 saniye görünmeli; dokununca hemen geçmeli. Paylaşım ve bildirim açılışında beklememeli.


## v0.5 cihaz kabulü

- Ana ekran slogan/sayaç satırlarını göstermemeli. Arama açıklaması kutu içinde, küçük logo başlığın solunda olmalı.
- Ayarlarda Font lisansı, Nasıl çalışır ve sürüm açıklaması olmamalı.
- Unutulanlar eşiğini değiştir; eski/tamamlanmamış kayıt ilgili sekmede görünmeli. Kartı açınca yeniden aktifleşmeli. Tamamlananlar dışarıda kalmalı.
- Niyet ve hızlı neden ekle, adını değiştir, gizle ve yeniden göster. Eski kartlar korunmalı.
- Tüm niyetleri/nedenleri gizleyip düzenleme erişiminin ve isteğe bağlı alanlarla kayıt akışının korunmasını dene.
- Kişisel niyetle hızlı kayıt oluştur, yeniden başlat ve JSON yedeğini başka kurulumda içe aktar.
- Dil değişiminde kişisel adlar ve kullanıcı notları aynı kalmalı.

## v0.6 cihaz kabulü

- Dar ekran/büyük fontta üst başlık ve zil/ayarlar ikonları çakışmamalı; arama ve iki filtre satırı ayrı görünmeli.
- Ana sayfada Nasıl çalışır düğmesi olmamalı. Kaydet hariç ikincil işlemler büyük dolu düğmeler olmamalı.
- Ayarlar tam ekran açılmalı; geri tuşu ana sayfaya dönmeli. Dil/tema değişimi ve ekran dönüşü ayarlar sayfasını korumalı.
- Yakın zamanda bir hatırlatma oluştur; zil okunmamış sayısını göstermeli, merkeze dokununca geçmiş ve yaklaşan tarihler görünmeli.
- Merkezi açınca okunmamış işareti kalkmalı. Telefon yeniden açıldığında geçmiş korunmalı; temizlemek kartları silmemeli.
- Sistem bildirimlerini kapatıp bir hatırlatmayı bekle; uygulama açıldığında yerel merkezde görülmeli. Sistem bildirimi/Doze zamanlaması gerçek cihazda ayrıca denenmeli.
- Sistem animasyonları kapalıyken zil salınımı olmamalı. Arka planda sürekli animasyon olmamalı.
