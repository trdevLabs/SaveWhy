# Cihazda kabul kontrolü

Android 8+ üzerinde, hem normal açılışta hem uygulama zaten açıkken dene.

- [ ] Instagram, TikTok ve YouTube'dan Paylaş → SaveWhy ile gerçek bağlantıları gönder. Uygulamaların paylaşım seçenekleri sürüme göre değişebilir.
- [ ] Başlık ve neden ekle; her niyetle bir kart kaydet. Aynı bağlantının farklı niyetle kaydedilebildiğini doğrula.
- [ ] Aynı bağlantıyı aynı niyetle takip parametreli ve parametresiz paylaş; tekrarın engellendiğini doğrula.
- [ ] Kaydetmeden ekranı döndür; bağlantı, başlık, niyet, neden ve adımlar korunmalı. Yeni paylaşım açık formu kapatmadan önce seçim sunmalı.
- [ ] Niyet şablonu ekle, değiştir, kendi adımlarını yaz. Var olan adımların üzerine şablon koyarken onay sorulmalı.
- [ ] Kartı aç; bir adımı tamamla, formda başka bir adımı düzenle. Metni değişmeyen adımın tamamlanma durumu korunmalı.
- [ ] Uygulamayı kapatıp aç; kartlar ve adımların tamamlanma durumları korunmalı.
- [ ] `icerik`, `öğren` ve bir adımın metniyle arama yap. Seçili filtre belirgin görünmeli.
- [ ] Kartı tamamla → Tamamlanan filtresine geç → Geri al. Sayaçlar değişmeli.
- [ ] Kaynak bağlantıyı aç; silme onayını önce iptal et, sonra kartı sil.
- [ ] JSON yedeği oluştur. Aynı yedeği tekrar içe aktar; kayıtlar çoğalmamalı.
- [ ] Yedeği başka bir cihaz/temiz kurulumda içe aktar; başlık, niyet, neden, adımlar ve durumlar korunmalı.
- [ ] Geçersiz veya desteklenmeyen sürümde JSON seç; mevcut kayıtlar korunmalı. Dosya seçicisini iptal et; veri değişmemeli.
- [ ] Küçük ekranda ve büyük yazı boyutuyla kart detayları ve kayıt formu erişilebilir olmalı.

Birim testleri veri işlemlerini doğrular; bu liste paylaşım uygulamalarını, dosya sağlayıcılarını ve Android yaşam döngüsünü gerçek cihazda doğrulamak içindir.
