# v0.6 — Kompakt arayüz ve bildirim merkezi

Başlıkta küçük logo, SaveWhy, bildirim zili ve ayarlar ikonu yer alır. Tema seçimi ayarlara taşınmıştır. Arama ayrı sıradadır. Durum sekmeleri ve kişisel niyetler iki bağımsız yatay satırda gösterilir; tek etkin filtre korunur. Ana sayfada Nasıl çalışır düğmesi yoktur.

Birincil Kaydet düğmesi dolu yüzey kullanır. İkincil işlemler daha küçük yazı ve şeffaf yüzey kullanır; dokunma hedefleri en az 48 dp kalır. Chip görsel yüksekliği 32 dp, dokunma alanı 48 dp'dir. Hızlı kayıt başlığı kısaltılmış, uzun giriş ve alt bilgi kaldırılmıştır; niyet/neden seçimi, isteğe bağlı detaylar ve sabit Kaydet alanı korunur.

Ayarlar ayrı tam ekran sayfadır; geri okuyla veya Android geri tuşuyla ana sayfaya dönülür. Genel, Kayıtlar, Bildirimler, Yedekleme bölümleri bulunur. Ayarlar satırlarının başlığı, mevcut değeri/açıklaması ve simgesi vardır; büyük dolu buton listesi kullanılmaz. Dil ve tema değişimi sistem API'leriyle sürer; ekran yeniden oluşturulmasında sayfa korunur.

Bildirim merkezi, bu uygulamanın yerel hatırlatma olaylarını ve yaklaşan tarihleri gösterir. Diğer uygulamaların bildirimleri okunmaz. Okunmamış olay varsa zil sayacı görünür ve ikon bir defa kısa salınır. Sistem animasyonları kapalıysa salınım yapılmaz; arka plana gidince animasyon/tarama durur. Uygulama açıkken 30 saniyede bir, açıldığında hemen kontrol edilir.

Olay geçmişi cihazda saklanır, en fazla 200 olay tutulur. Aynı kayıt/zaman tekrar eklenmez. Merkezi açmak olayları okundu yapar. Geçmişi temizlemek kartları veya gelecek alarmları silmez. Silinmiş karta dokunulursa kayıt bulunamadığı açıklanır. Başlıklar kullanıcı metni olarak aynen gösterilir. Geçmiş JSON kart yedeğine dahil değildir.

Sistem bildirimi kapalı olsa da zamanı gelen hatırlatma yerel merkeze düşer ve bir kerelik tarihi tüketilir. Telefonun kilit ekranında görünmesi Android bildirim izni/kanalı ve pil sınırlamalarına bağlıdır. Yerel geçmiş bozuksa otomatik sıfırlanmaz. Sunucu veya AI servisi eklenmemiştir; tek seferlik satış modeli korunur.
