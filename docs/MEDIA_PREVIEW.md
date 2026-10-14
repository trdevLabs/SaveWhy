# v0.7 — Güvenli ekran alanı, kompakt kart ve YouTube oynatıcı

Yeni kök sayfa hazırlanırken son bilinen sistem çubuğu/ekran kesiti boşlukları hemen uygulanır; dinleyici sayfa pencereye eklenmeden kurulup yeniden inset dağıtımı istenir. Bildirimler ve ayarlar başlıkları durum çubuğunun altına taşmaz. Tema adı Karanlık / Dark'tır; AMOLED zeminin rengi siyah kalır.

Liste kartlarında 88×66 dp küçük görsel, 14 sp iki satırlık başlık, küçük niyet rozeti ve kaynak adı vardır. Uzun başlıklar listede kısaltılır; asıl metin değiştirilmez. Detay başlığı 17 sp, not 13 sp'dir.

YouTube watch, youtu.be, shorts, embed ve live bağlantılarındaki 11 karakterli kimlik doğrulanır. Küçük görsel yalnızca sabit HTTPS `i.ytimg.com` uç noktasından alınır. Rastgele kaynak URL'si, özel not veya başlık sunucuya gönderilmez. Ağ işleri arka plan iş parçacığındadır; yanıtta en fazla 1 MB ve 4096×4096 boyut kabul edilir. Bellek önbelleği 8 MB, cihaz önbelleği en fazla 40 küçük görseldir. İnternet yoksa mevcut önbellek veya kaynak simgesi gösterilir. Diğer platformlar için gerçek görsel varmış gibi sahte görsel kullanılmaz.

YouTube detayında resmi gömülü oynatıcı ancak kullanıcı Videoyu oynat'a dokununca yüklenir. Oynatıcı en az 200 dp yüksekliğindedir. Referer, YouTube'un Android WebView kılavuzuna göre uygulama kimliğinden (`https://com.savewhy.app`) ayarlanır. JavaScript yalnızca bu doğrulanmış oynatıcı için açıktır; JavaScript-native köprüsü, dosya/içerik erişimi ve cleartext trafik yoktur. Oynatıcı kontrolleri/markalaması değiştirilmez ve üzerlerine özel kontroller bindirilmez.

Panel kapatılınca veya uygulama arka plana gidince WebView durdurulur ve yok edilir; tekrar oynatmak için dokunmak gerekir. Kaynak uygulamada açma düğmesi her zaman vardır. Gömme kısıtı, özel/silinmiş video, internet veya WebView sorunu nedeniyle her video oynatılamayabilir. Instagram/TikTok vb. kaynaklar bu sürümde kaynak uygulamada açılır; video dosyası indirilmez veya korumalar aşılmaz.

Başlık paylaşım bilgisinden alınmaya devam eder; bu sürüm video içeriğini analiz etmez. Normal hızlı kaydetme ve kullanıcı verileri çevrimdışı çalışır. Görsel/CDN ve YouTube oynatma internet bağlantısı gerektirir; uygulama kendi AI sunucusunu kullanmaz.

Kaynaklar:
- https://developers.google.com/youtube/terms/required-minimum-functionality
- https://developers.google.com/youtube/player_parameters
- https://developer.android.com/reference/androidx/core/view/ViewCompat
