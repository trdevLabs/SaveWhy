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
