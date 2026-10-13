# v0.4 — Cihaz geri bildirimleri ve yerel hatırlatmalar

## Detaylar

Etiketler TextInputLayout dışında, input kimliğine bağlı erişilebilir TextView olarak gösterilir. Çok satırlı açıklama ayrı satırdadır; iç EditText üzerinde ikinci hint yoktur. Başlık, not, yapılacaklar ve Bul sorgusu isteğe bağlıdır.

Yapılacaklar, içeriği kullanırken tamamlanacak küçük işlerdir. Video içeriği analiz edilmez. Genel niyet şablonları örnek listede gösterilir; kullanıcı seçtiği maddeleri mevcut listeye ekler. Aynı metin tekrar eklenmez ve mevcut maddeler/tamamlanma durumları silinmez.

## Hatırlatmalar

Hızlı kayıt panelinde detaylardan bağımsız Hatırlatıcı ekle düğmesi vardır. Bir saat sonra, akşam 20:00, yarın 09:00, cumartesi 10:00 ve tarih/saat seçimi sunulur. Akşam saati geçmişse düğme yarın 20:00 olduğunu açıklar. Sonra, birlikte veya boş zamanda tek başına bir tarih değildir; izinsiz otomatik zaman atanmaz.

Kullanıcının seçimi bir kerelik, cihaz içi AlarmManager bildirimi oluşturur. Android 13+ bildirim izni yalnızca hatırlatıcı seçildiğinde istenir. Reddedilirse hatırlatma eklenmez; kayıt yapılabilir. Bildirim kanalı veya uygulama bildirimleri kapalıysa ayarlara bağlantı gösterilir. Bildirim başka kişiye gönderilmez.

Android pil tasarrufu ve Doze nedeniyle **bildirim tam dakikasında garanti edilmez**. Exact alarm/özel alarm izni kullanılmaz. Bu sınır zaman seçiminde açıklanır. Cihaz yeniden başlayınca, paket güncellenince, saat/saat dilimi değişince bekleyen alarmlar yeniden kurulur. Geçmiş bekleyen zamanlar ilk fırsatta işlenir. Silme, tamamlanma ve hatırlatmayı kaldırma alarmı iptal eder. Bildirim açılırsa doğrudan ilgili kart gösterilir; normal splash beklenmez. Bildirim yayımlandığında tek seferlik zaman tüketilir.

Hatırlatma `reminderAt` UTC epoch milisaniye olarak kartla beraber saklanır; yedek sürümü 3'tür. v0.1, sürüm 1 ve 2 yedekleri okunur; tarihleri varsayılan 0 olur. İçe aktarma kabul edildikten sonra alarmlar yeniden kurulur. Eski uygulama sürümü yeni yedeği okuyamaz.

## Dil

Türkçe ve İngilizce UI kaynakları vardır. Varsayılan, sistem dilidir; desteklenmeyen diller İngilizce kaynaklara düşer. Ayarlar → Uygulama dili → Sistem dili / Türkçe / English. AndroidX AppCompat uygulama dilini saklar; Android 13+ sistem uygulama dili ayarıyla uyumludur. Bildirimler de uygulama dilini kullanır.

Depolanan niyet anahtarları eski kayıtlarla uyum için sabit kalır, kullanıcıya kaynaklardan çevrilerek gösterilir. Kullanıcı başlığı, özel notu ve yapılacaklar metni çevrilmez. Yeni hızlı neden veya örnek yapılacak seçimi o anda görünen dilde kaydedilir.

## Splash

2,2 saniye görünür; dokunarak hemen geçilir. Paylaşım ve bildirim açılışı doğrudan ana/kayıt ekranına gider. Arka planda yönlendirme yapılmaz. Yeniden oluşturma başlangıç zamanını korur.

## Sonraki aşama

Bu sürümde sunucu ve AI içerik analizi yoktur. Cihaz geri bildirimleri nedeniyle hatırlatmalar öne alınmıştır. AI öneri/önizleme/onay akışı ve ticari kullanıma uygun barındırma ayrı sonraki aşamadır.
