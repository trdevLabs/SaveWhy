package com.savewhy.app;

import java.util.*;

/** One source of truth for intent labels, guidance and actions. */
public final class IntentCatalog {
    public static final class Spec {
        public final String id, label, description, titleHint, noteHint, completedLabel, template;
        private Spec(String id, String label, String description, String titleHint,
                     String noteHint, String completedLabel, String template) {
            this.id=id; this.label=label; this.description=description; this.titleHint=titleHint;
            this.noteHint=noteHint; this.completedLabel=completedLabel; this.template=template;
        }
    }
    private static final List<Spec> ALL = Collections.unmodifiableList(Arrays.asList(
        new Spec("do","Yap","Bir şeyi dene, uygula veya hazırla.","Örn. Hafta sonu deneyeceğim tarif",
            "Ne yapmak istiyorsun? Bunu ne zaman veya hangi durumda kullanacaksın?","Yaptım",
            "Gerekenleri listele\nEksikleri hazırla\nDene ve sonucu not et"),
        new Spec("go","Git","Bir mekânı veya etkinliği ziyaret et.","Örn. Gitmek istediğim kahveci",
            "Nereye, kiminle ve ne için gitmek istiyorsun?","Gittim",
            "Adres ve çalışma saatlerini kontrol et\nGitmek için bir gün seç\nZiyaret sonrası not ekle"),
        new Spec("buy","Al","Satın almayı değerlendir.","Örn. Çalışma masası için lamba",
            "Bu ürün hangi ihtiyacını karşılayacak? Bütçen veya seçim ölçütün ne?","Karar verdim",
            "İhtiyacını ve bütçeni yaz\nFiyat ve alternatifleri karşılaştır\nSatın alma kararını ver"),
        new Spec("learn","Öğren","Bir bilgiyi veya beceriyi edin.","Örn. Öğrenmek istediğim montaj tekniği",
            "Ne öğrenmek ve nerede uygulamak istiyorsun?","Öğrendim",
            "Öğrenmek istediğin noktayı yaz\nİçerikten not çıkar\nBir örnekle uygula"),
        new Spec("create","Üret","Bir fikri kendi içeriğine dönüştür.","Örn. Kendi videom için anlatım fikri",
            "Hangi kısmından ilham aldın? Kendi içeriğinde nasıl kullanacaksın?","Ürettim",
            "Açılış cümlesini ve formatı not et\nKendi açını ve hedef kitleni belirle\nAkış ve eylem çağrısını yaz\nKendi versiyonunu üret"),
        new Spec("watch","İzle","Bir videoyu veya içeriği daha sonra izle.","Örn. Akşam izleyeceğim kısa belgesel",
            "Neden izlemek istiyorsun? Ne zaman izlemek sana uygun?","İzledim",
            "İçeriği izle"),
        new Spec("find","Bul","İçerikteki bir şeyi veya benzerini araştır.","Örn. Videodaki lambanın modelini bul",
            "Tam olarak neyi bulmak istiyorsun? Bildiğin ipuçları neler?","Buldum",
            "Aradığın şeyi ve ipuçlarını netleştir\nKaynak açıklamasını ve yorumları kontrol et\nUygun bir sorguyla web'de ara\nBulduğun sonucu kaynakla karşılaştır")
    ));
    private IntentCatalog() {}
    public static List<Spec> all() { return ALL; }
    public static Spec get(String label) {
        for (Spec spec:ALL) if (spec.label.equals(label)) return spec;
        if(Choices.custom(label))return new Spec(label,Choices.fallback(label),"Kendi niyetinle kaydet.","","İstersen özel bir not ekle.","Tamamlandı","");
        throw new IllegalArgumentException("Geçersiz niyet.");
    }
    public static String[] labels() {
        String[] labels=new String[ALL.size()];
        for(int i=0;i<ALL.size();i++) labels[i]=ALL.get(i).label;
        return labels;
    }
    public static List<String> filters() {
        List<String> names=new ArrayList<>(); names.add("Bekleyen"); names.add("Tümü");
        names.addAll(Arrays.asList(labels())); names.add("Tamamlanan"); return names;
    }
}
