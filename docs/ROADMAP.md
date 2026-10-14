# SaveWhy ürün ve geliştirme planı

## Ürün kararı

SaveWhy, sosyal medyada kaydedilen bir içeriği kullanıcının amacı ve bir sonraki eylemiyle birlikte saklar. Ana akış: **Paylaş → niyete dokun → kaydet → zamanı gelince harekete geç**. Hızlı neden seçimi isteğe bağlıdır; başlık, özel not ve adımlar kaydı engellemez.

İlk hedef, günlük sosyal medya kullanıcıları ve içerik üreticileridir. Bu bir pazar doğrulaması iddiası değil, mevcut ürünü geliştirmek için seçilen kapsamdır. Her içeriğe uzun görev listesi eklenmez; örneğin bir videoyu yalnızca daha sonra izlemek de geçerli bir amaçtır.

## Yedi temel niyet

| Niyet | Kullanıcının amacı | Örnek | Tamamlama |
| --- | --- | --- | --- |
| Yap | Bir şeyi denemek veya uygulamak | Tarif, egzersiz, DIY | Yaptım |
| Git | Bir yeri veya etkinliği ziyaret etmek | Kafe, sergi, gezi noktası | Gittim |
| Al | Satın alma kararını vermek | Masa lambası, mikrofon | Karar verdim |
| Öğren | Bilgi veya beceri edinmek | Montaj tekniği, ders | Öğrendim |
| Üret | Fikri kendi içeriğine uyarlamak | Açılış, anlatım, video formatı | Ürettim |
| İzle | İçeriği daha sonra tüketmek | Belgesel, uzun video | İzledim |
| Bul | İçerikteki bir şeyi veya benzerini araştırmak | Ürün modeli, yer, kaynak | Buldum |

**Al / Bul ayrımı:** Al, bilinen bir seçeneği satın alıp almamayı değerlendirmektir. Bul, aranan şeyin kimliğini, kaynağını veya uygun alternatifini araştırmaktır. Aynı bağlantı farklı amaçlarla kaydedilebilir.

**İzle / Öğren ayrımı:** İzle, izleme isteğini saklar. Öğren, belirli bir öğrenme hedefi ve uygulama planı taşır. İzle için eylem adımları zorunlu değildir.

Yeni niyetleri başlangıçta çoğaltmayacağız. Ek ihtiyaçlar önce mevcut niyetlerin alanları ve etiketlerle karşılanacak; özel niyetler ancak kullanıcıların tekrar eden ihtiyacı görülürse değerlendirilecek.

## v0.5 kişisel seçenekler

Sade ana ekran, varsayılan 15 günlük Unutulanlar listesi, değiştirilebilir süre ve niyet/hızlı neden kişiselleştirmesi uygulanmıştır. Kullanıcı yeni niyet/neden tanımlar, adını değiştirir ve göster/gizle seçer. Yedek biçimi 4, son etkinlik ve kişiselleştirme aktarımını içerir. AI aşaması henüz uygulanmamıştır.

## Cihaz geri bildirimiyle güncel sıra

v0.4 kapsamında detay alanı okunabilirliği, seçilebilir yapılacak örnekleri, Türkçe/İngilizce dil desteği ve yerel hatırlatmalar öne alındı. AI içerik önerileri henüz eklenmedi; önce cihazda temel akış ve bildirim güvenilirliği doğrulanacak. Hatırlatma sürümüyle birlikte yedek biçimi 3 oldu; eski veriler korunur.

## Sürüm sırası

### v0.3 — Hızlı kayıt, modern arayüz ve niyet sistemi

- Paylaş menüsünden gelen URL ile niyet seçmek yeterli olsun: başlık ve not yazma zorunluluğu olmasın.
- Her niyet için dört hızlı neden sun; özel metinleri kapalı, isteğe bağlı detay alanına taşı.
- Paylaşılan başlığı kullan; başlık gelmediyse bağlantıdan kaynak ve içerik kimliği üret. Bu, içerik özeti değildir.
- Manrope fontu, vektör ikonlar, Material 3 kartlar/chipler/alt paneller ve geri alınabilir silme kullan.
- Varsayılan AMOLED temada arka plan tam siyah olsun; aydınlık ve sistem tercihi seçenekleri saklansın.
- Uzun listeleri RecyclerView + DiffUtil ile göster; arama süzmesini ana ekranın dışında çalıştır.
- Küçük ekranda Kaydet düğmesini panelin altında sabit tut; detaylar ayrı kaydırılsın.
- Yedi niyeti tek bir katalogdan yönet.
- Form başlıklarını, açıklamalarını, örneklerini ve tamamlama düğmelerini niyete göre değiştir.
- Bul kartına düzenlenebilir arama sorgusu ekle; kullanıcı dokunduğunda tarayıcıda arama aç.
- Kaydetme nedenini veya diğer özel notları arama sorgusuna otomatik ekleme.
- Bekleyen / Tümü / Tamamlanan filtrelerini açıkça ayır; niyetle ararken tamamlanan kayıtlar da bulunabilsin.
- Eski kayıtları ve v0.1/v0.2 yedeklerini kayıpsız oku. Yeni araştırma alanını yedekle.

Kabul: paylaşılan URL, niyet seçimi ve Kaydet düğmesiyle klavye açmadan kayıt oluşturulur; kullanıcı neden seçimini değiştirebilir; açık taslak ve tema tercihi yeniden açılışta korunur. İzle ve Bul ile paylaşım/kayıt/düzenleme/tamamlama akışı çalışır; arama sorgusu, ekran dönüşü ve yedekleme sırasında korunur. Otomatik nesne tanıma veya içerik çözümleme bu sürümde yoktur.

### Sonraki sürüm — Metinden AI eylem kartı

İlk AI girdisi, kullanıcının verdiği açıklama, metin veya transkript olacak. Sadece bir URL'den videoyu okuduğumuzu varsaymayacağız. Android istemcisi bir sunucuya bağlanacak; model anahtarı APK içinde tutulmayacak.

Niyete göre çıktılar:

- Yap: hazırlık, gereksinimler ve uygulanabilir adımlar.
- Git: kullanıcının verdiği adres/yer bilgisi, eksik bilgiler ve ziyaret planı. Güncel saatler doğrulanmadıysa kesin bilgi gibi gösterilmez.
- Al: kullanıcının ihtiyaçlarına göre karar ölçütleri ve karşılaştırma soruları. Fiyatlar kaynak olmadan uydurulmaz.
- Öğren: kısa özet, öğrenme hedefi ve uygulama alıştırması.
- Üret: açılış, içerik akışı, format ve kullanıcının kendi versiyonuna uyarlama.
- İzle: verilen metinden kısa açıklama ve izleme gerekçesi; transkript yoksa içerik özeti uydurulmaz.
- Bul: verilen ipuçlarından araştırma sorguları. Nesnenin kimliği veya eşleşmesi kanıt yoksa kesinleşmiş sayılmaz.

AI önerisi önce kullanıcıya gösterilir; kabul edilen içerik karta yazılır. Mevcut not ve adımlar kullanıcı kabulü olmadan değiştirilmez. Kaynak metin ve AI önerisi ayrılır; bağlantı başlığı veya arama sorgusu içerik kanıtı sayılmaz. Hata/çevrimdışı durumda elle kaydetme çalışmaya devam eder.

Bu aşamada Bul için bulunan bağlantı ve sonuç notu da karta geri kaydedilecek. Hizmet için kimlik doğrulama, istek sınırı ve maliyet sınırı, dış kullanıma açılmadan uygulanacak. Model sağlayıcı ve hosting kararı uygulama geliştirme aşamasında verilecek; henüz kurulmuş bir servis yoktur.

### v0.5 — Kayıtları anlamla bulma

"Hafta sonu deneyebileceğim şeyler" veya "videolarım için anlatım fikirleri" gibi sorgularla kayıt bulma. Sözcük araması korunur; semantik sonuçlar ayrıca belirtilir. Kayıtların anlamsal temsilini güncelleme/silme ve özel notların hangi servise gönderildiği ürün akışında açıkça ele alınır.

Kabul: aynı anlama gelen farklı sözcüklerle kayıt bulunur; yanlış pozitif sonuçlar elle değerlendirilir. Yalnızca tam kelime araması semantik arama olarak sunulmaz.

### İleri aşama — Hatırlatmaları genişletme

Kullanıcının seçtiği zamanda geri dönme, erteleme ve haftalık bekleyenler görünümü. İzle için izleme listesi; Git için kullanıcı tarafından onaylanmış adres üzerinden harita açma. Bildirimler kullanıcı tercihiyle açılır. Satın alma takibi ve fiyat uyarıları ayrıca doğrulanabilir bir veri kaynağı gerektirir; bu aşamanın otomatik parçası değildir.

### Beta ve mağaza hazırlığı

Gerçek cihazlarda paylaşım, ekran dönüşü, dosya sağlayıcıları, büyük yazı boyutu ve imzalı güncellemeleri doğrula. Küçük bir kullanıcı grubuyla kaydetme süresini ve kayıtların tekrar açılıp kullanılmasını değerlendir. Ardından kararlı release imzası, gizlilik metni, mağaza görselleri ve release AAB hazırla. Mağaza yayını ayrıca ele alınır; Colab debug APK'sı yayın paketi değildir.

## Öncelik ve başarı ölçütü

Öncelik: **güvenilir kayıt → amaca uygun kart → kayıtları geri bulma → harekete dönüştürme**. Bir sonraki aşamaya geçmeden önce o aşamanın kabul akışı tamamlanmalı; özellikle gerçek cihazda paylaşım henüz ayrıca doğrulanmalıdır.

Başarıyı yalnızca kaydedilen bağlantı sayısıyla değerlendirmeyeceğiz: kaydetme akışının tamamlanması, kartların yeniden açılması ve kullanıcı tarafından tamamlanan amaçlar da önemlidir. Şu anda analitik veri toplayan bir servis yoktur; bu ölçütler beta değerlendirme planıdır.

## Ücretli ürün hedefi

Hedef, hızlı ve güvenilir kaydetme ile kayıtları gerçekten kullanılabilir kılan bir uygulamadır. Bu hedefe ulaşmak için yalnızca yeni özellik eklemek yeterli değildir.

Yayın öncesi kapılar:

1. **Kullanım:** paylaş menüsünden klavye açmadan kayıt; isteğe bağlı hızlı neden; boş sonuç ve hata durumları; küçük ekranda erişilebilir düğmeler.
2. **Görsel kalite:** aydınlık ve AMOLED temada tutarlı bileşenler; okunabilir kontrast; Türkçe karakterleri destekleyen font; ikonların erişilebilir adları.
3. **Güvenilirlik:** eski veriler kaybolmaz; dosya hatası sıfırlama yapmaz; silme geri alınabilir; yedekleme ve imzalı güncelleme gerçek cihazlarda denenir.
4. **Ürün değeri:** AI önerileri ve niyete özel sonuç alanları doğrulanır; kullanıcı kendi kaydına tekrar dönüp kullanabiliyor olmalı. Otomatik fiyat/nesne tanıma gibi uygulanmamış özellikler satış vaadi olmaz.
5. **Ücretlendirme:** bulut hizmeti olmayan temel sürüm için tek seferlik satın alma seçeneği değerlendirilecek. Sürekli AI maliyeti oluşursa kullanım paketi veya abonelik ayrıca tasarlanacak. Tutar henüz belirlenmedi; ödeme ve erişim doğrulaması henüz uygulanmadı.
6. **Yayın:** mağaza hedefi, gizlilik/bilgi metinleri, kararlı release anahtarı, release AAB, beta geri bildirimi ve mağaza incelemesi. Yayına hazır olma iddiası bu kapılar geçildikten sonra yapılır.

Geliştirme sırası: **hızlı kullanım ve arayüz → AI kartının somut faydası → geri bulma ve hatırlatma → beta → ücretlendirme/yayın**. Başlık yazdırarak kaydın önüne engel koyan akışa geri dönülmeyecek.

## v0.6 ve güncel ürün kapsamı

Tek seferlik satılan, cihazda çalışan temel ürün korunur. Bulut AI temel sürüm planından çıkarılmıştır. Kompakt arayüz, tam ekran ayarlar ve yerel bildirim merkezi uygulanmıştır. Sonraki adım gerçek cihaz güvenilirliği, kararlı yayın imzası ve mağaza hazırlığıdır; sınırsız bulut AI vaadi verilmez.


## v0.8 yayın hazırlığı

20 kayıt ücretsiz kapasite ve tek seferlik tüketilmeyen SaveWhy Sınırsız INAPP ürünü planı uygulanmıştır. Play ürünü/fiyat/public key, kararlı release imzası ve mağaza testleri henüz tamamlanmalıdır. Destek ve memnuniyet geri bildirimi e-posta taslağı olarak çalışır. Bulut AI temel üründe yoktur.
