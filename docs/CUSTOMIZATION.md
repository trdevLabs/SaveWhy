# v0.5 — Sade ana ekran, Unutulanlar ve kişisel seçenekler

Ana ekranda slogan ve bekleyen/tamamlanan sayıları kaldırıldı. Arama açıklaması kutunun içindedir. Splash görseli SaveWhy adının solunda, 28 sp yazı boyutundan büyük olmayan kutuda ve kendi şeffaflığıyla kullanılır. Adımsız bekleyen kartlarda durum alt yazısı yoktur. Ayarlardan Font lisansı, Nasıl çalışır ve sürüm/açıklama satırı kaldırıldı; lisans dosyaları APK içinde korunur.

## Unutulanlar

Varsayılan eşik 15 gündür. Ayarlar → Unutulanlar ayarı üzerinden 1–365 gün seçilir. Son açma/düzenleme/adım işaretleme/kaynak açma/tamamlama işlemi kaydın son etkinlik tarihini yeniler. Arama veya listeyi görüntülemek etkinlik değildir. Otomatik hatırlatıcı teslimi de etkinlik tarihini değiştirmez.

Tamamlanmamış kayıt son işlem üzerinden eşikten daha uzun süre geçtiyse Unutulanlar sekmesine girer ve Bekleyen listesinden çıkar. Tümü ve ilgili niyet süzgecinden erişilebilir; kayıt silinmez. Tamamlanmış kayıtlar Unutulanlar'a girmez. Kartı açmak yeniden aktif eder. Eski kayıtların etkinlik tarihi yoksa oluşturma tarihi kullanılır.

## Niyetler ve hızlı nedenler

Ayarlar → Niyetlerini düzenle. Yerleşik seçenekler göster/gizle ve yeniden adlandırma ile yönetilir; Yeni niyet ekle ile bağımsız kişisel niyet oluşturulur. Her niyetin Hızlı nedenler düğmesi vardır. Orada yerleşik nedenler göster/gizle veya yeniden adlandırma ile yönetilir; Yeni neden ekle ile o niyete özel seçenek eklenir. Hızlı kayıt panelindeki düzenleme düğmeleri aynı ayarları açar ve geri dönüldüğünde taslak korunur.

İşaretli seçenekler hızlı kayıtta ve niyet filtrelerinde gösterilir. Gizleme mevcut kartı ve kaydetme nedenini değiştirmez. Gizli niyetli eski kart düzenlenirken kendi niyeti seçimde tutulur. Tüm niyetler gizlense bile Niyetlerini düzenle erişilebilir. Nedenler boşsa kayıt yazı yazmadan niyet adıyla saklanabilir.

Kişisel niyet ve neden adları kullanıcı metnidir; dil değiştirilince çevrilmez. Yerleşik etiketler kullanıcı tarafından yeniden adlandırılmadıkça mevcut Türkçe/İngilizce kaynaklarını kullanır. Yeni niyetler genel kaydet/aç/tamamla akışını kullanır; özel web araması Bul niyetinin işlevidir. Kişisel niyetlerde niyete özel hazır şablon yoktur; kullanıcı kendi yapılacaklarını ekleyebilir.

Anahtarlar yeniden adlandırma veya gizlemede değişmez. Kart yedeği sürüm 4, `lastActivityAt` tarihini içerir; önceki tüm SaveWhy sürümleri okunur. Dışa aktarılan yedek ayrıca kişisel seçenekler ve Unutulanlar eşiğini içerir. İçe aktarma yeni kişisel seçenekleri ekler; aynı anahtar varsa mevcut cihazdaki kişiselleştirme korunur. Eşik bu cihazda daha önce açıkça seçildiyse değiştirilmez. Yedek iki alanı içermiyorsa varsayılan/mevcut ayarlar korunur. Eski uygulamalar sürüm 4 yedeğini okuyamaz.
