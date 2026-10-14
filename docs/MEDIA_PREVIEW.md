# v0.9 — Tek pencere ve sosyal küçük görseller

Launcher, ACTION_SEND ve bildirim bağlantıları aynı `singleTask` MainActivity üzerinden açılır. `documentLaunchMode=never` ayrı belge görevlerini engeller. Splash artık dışarıdan açılabilen launcher değildir; soğuk launcher açılışında mevcut MainActivity üzerinde görünür ve kapanır. Yeni paylaşım `onNewIntent` ile işlenir; açık taslak varsa önceki taslak için mevcut seçim korunur.

Liste kartlarında 76×56 dp küçük görsel, 14 sp iki satırlık başlık, niyet rozeti ve kaynak adı vardır. Detayda aynı gerçek küçük görsel ve dış bağlantı açma bulunur. YouTube dahil tüm oynatma kaynak uygulamada/tarayıcıdadır; WebView veya video dosyası indirme yoktur.

## Görsel kaynakları

- YouTube: doğrulanmış video kimliği ile sabit HTTPS i.ytimg.com küçük görseli.
- Instagram/Facebook: herkese açık sayfanın `og:image` / `twitter:image` verisi.
- TikTok: resmi oEmbed `thumbnail_url`; kısa bağlantı çözümü ve Open Graph yedeği.
- X/Twitter/Pinterest/Threads: aynı herkese açık metadata akışı.

Bu yöntem her içerikte görsel garantisi vermez. Özel/silinmiş içerik, giriş duvarı, platform engeli ve ağ hatası otomatik önizlemeyi engelleyebilir. Oturum çerezleri, erişim token'ları veya koruma aşma kullanılmaz. Gerçek görsel alınamazsa kaynak simgesi gösterilir. Detayda **Küçük görsel seç** ile kullanıcı sistem dosya seçicisinden bir fotoğraf seçebilir; böylece erişimi kapalı içeriklere de görsel atanabilir. Galeri izni istenmez; seçilen dosya arka planda küçültülüp yeniden JPEG olarak kaydedilir. Kullanıcı tarafından seçilen görüntü otomatik önizlemeye üstün gelir.

İstekler yalnızca izinli sosyal alan adları ve görüntü CDN'lerine HTTPS üzerinden gider. Her yönlendirmede alan adı kontrol edilir; localhost/özel/link-local IP'ler ve özel portlar reddedilir. Çerez, not veya başlık gönderilmez; önizleme alabilmek için ilgili bağlantının kendisi kaynak platforma iletilir. En fazla dört yönlendirme, istek başına 3 saniye bağlantı / 5 saniye okuma zaman aşımı, 1 MB ağ yanıtı, 8192×8192 kaynak boyutu, yaklaşık 640 px çözünürlük ve 8 MB bellek önbelleği vardır. Başarısız önizleme beş dakika tekrar denenmez. Otomatik disk önbelleği 40 görseldir; seçilmiş görseller uygulamanın özel dosyalarında kalır. Seçilmiş görseller JSON yedeğinde yer almaz ve uygulama silinirse kaybolur.

AI servisi veya ücretli önizleme sunucusu kullanılmaz. Normal kayıt ve hatırlatmalar çevrimdışı çalışır; otomatik önizleme internet ister. Platformlardan canlı başarı garantisi verilmez; otomatik testler metadata ayrıştırma, güvenli URL/IP kontrolü, dört platformun önbellek bağlama akışı, seçilen görselin kalıcılığı ve ortak dış bağlantı davranışını doğrular.

Kaynaklar:
- https://developers.tiktok.com/docs/en/embed-videos
- https://developer.android.com/guide/components/activities/tasks-and-back-stack
