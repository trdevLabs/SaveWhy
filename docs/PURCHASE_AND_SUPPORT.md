# v0.8 — 20 ücretsiz kayıt, tek seferlik satın alma ve destek

## Ürün davranışı

Ücretsiz sürüm cihazda en fazla **20 kayıt** tutar; tamamlanan, unutulan ve gizli niyetli kayıtlar da sayılır. Bu toplam kullanım hakkı değil, eşzamanlı kayıt kapasitesidir. Silme yer açar. Yeni kayıt ve sınırı aşan toplu içe aktarma engellenir. Mevcut kayıtlar her zaman okunur, düzenlenir, tamamlanır, silinir ve yedeklenir. Eski kurulumda 20'den fazla kayıt varsa hiçbir kayıt silinmez; satın alma olmadan yeni ekleme yapılamaz.

Sınır yalnızca ekranda değil, ana uygulamanın CardStore yazma ve birleştirme yolunda uygulanır. İçe aktarma sınırı aşarsa dosya kısmen yazılmaz. Sistem hatırlatmaları yalnızca var olan kayıtları günceller. Kapasite kısıtı diğer temel özellikleri kapatmaz. Satın alma 20 kayıt sınırını kaldırır; var olan teknik dosya sınırı 5.000 kayıt / 4 MB olarak kalır ve satın alma ekranında belirtilir.

Satın alma ekranı, sınırı kaldıran **tek seferlik**, tüketilmeyen Google Play INAPP ürünü içindir. Abonelik yoktur. Ürün kimliği: `savewhy_lifetime`. Fiyat uygulamada uydurulmaz; uygun Google Play teklifinin yerelleştirilmiş fiyatı gösterilir. Mağaza bağlantısı/ürün eksikse Satın al düğmesi kapalı ve uygun durum mesajı görünür. Debug/Colab APK'sında sahte sınırsız erişim verilmez.

## Gerçek ödemeyi etkinleştirme

Bu depoda Google Play Billing Library **8.0.0** entegrasyonu vardır; henüz Play Console hesabı/ürünü bağlı değildir. Gerçek satış için:

1. Play Console'da paket kimliği `com.savewhy.app` olan uygulama ve `savewhy_lifetime` tek seferlik ürünü oluştur. Tek kalıcı satın alma seçeneği kullan; abonelik veya tüketilen ürün olarak tanımlama. Fiyat/ülkeleri mağazada belirle.
2. Uygulamanın **lisans/faturalandırma RSA public key** değerini derleme ortamında Gradle özelliği olarak ver: `SAVEWHY_PLAY_PUBLIC_KEY`. Bu bir public key'dir; private key veya Google servis hesabı anahtarı APK'ya konmaz.
3. Kararlı imzalı AAB/APK'yı internal testing'e yükle, lisans test kullanıcılarını tanımla ve Play'den kurulan sürümde satın alma/iptal/bekleyen ödeme/geri yükleme denemelerini yap.
4. Release signing ve mağaza ürün aktivasyonu tamamlanmadan satın almanın canlı olduğu söylenmez. Colab debug APK'sı arayüz/ücretsiz sınır incelemesi içindir.

## Doğrulama ve erişim

Sadece PURCHASED durumundaki, doğru paket/ürün/token içeren, RSA-SHA1 imzası public key ile doğrulanan makbuz kabul edilir. Bekleyen veya iptal edilen işlem erişim vermez. Yeni satın alma önce acknowledge edilir; başarılı onaydan sonra imzalı makbuz cihazda saklanır. Sınırsızlık bir preferences boolean'ı veya kart yedeğindeki alanla açılmaz. Satın alma yedeğe eklenmez; aynı Google Play hesabındaki sahiplik sorgusuyla geri yüklenir.

Uygulama açılırken ve geri yüklemede Play sahipliği sorgulanır. Başarılı sorguda ürün yoksa yerel makbuz kaldırılır. Bağlantı hatasında önceden doğrulanmış makbuzla çevrimdışı kullanım korunur. Bir tüketilmeyen ürün consume edilmez.

**Güvenlik sınırı:** Bu temel çevrimdışı modelde doğrulama istemcidedir. Root edilmiş/değiştirilmiş istemciye karşı tam koruma sağlamaz; çevrimdışı cihazda iade anında anlık iptal garanti edilmez. Google'ın daha güçlü sunucu tarafı Play Developer API doğrulama önerisi ayrıca değerlendirilebilir. Bu sürümde AI sunucusu veya sürekli AI maliyeti yoktur.

## Destek ve geri bildirim

Ayarlar → İletişim ve destek, `tr.developer@hotmail.com` adresine `savewhy - Destek` / İngilizce eşdeğer konu başlığıyla e-posta taslağı açar. Konu ve gövde hem mailto hem Intent extras içindedir. Uygulama e-postayı kendiliğinden göndermez.

Ayarlar → Uygulamadan memnun musun? her zaman erişilebilir. Beş yeni kayıttan sonra bir kez, aktif ana ekranda nazik bir memnuniyet sorusu gösterilir. Memnunum ve Geliştirilebilir yanıtlarının ikisi de aynı isteğe bağlı metin/e-posta akışına gider; mağaza puanlama yönlendirmesi veya olumlu yanıt filtresi yoktur. Şimdi değil seçeneği vardır. Yanıt yerelde tutulur, metin yalnızca kullanıcının göndereceği taslakta paylaşılır. Konu `savewhy - Geri bildirim` ile başlar. Otomatik kişisel/cihaz verisi gönderilmez; sürüm numarası taslakta görünür.

## Görseller

Ana ekran kenarları 16 dp'ye hizalanır; kart iç boşluğu 8 dp, kartlar arası 6 dp'dir. Küçük görsel 76×56 dp'dir. Launcher/adaptive ikon ve Android 12 sistem splash ikonu, aynı mint cam yer imi görselini kullanır. Splash'taki görünen Devam etmek için dokun yazısı kaldırılmıştır; dokunarak geçiş erişilebilir eylem olarak kalır. Copy Right 2026 güvenli alt alanda gösterilir.

Kaynaklar:
- https://developer.android.com/google/play/billing/integrate
- https://developer.android.com/google/play/billing/security
