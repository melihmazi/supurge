package com.supurge.controller;

import com.supurge.model.*;

public class TemizlemeKontrolcu {

    private final Oda oda;
    private final Robot robot;

    // Leke 5 adımda çıkıyorsa şu an kaçıncı adımdayız sayacı
    private int temizlemeAdimi      = 0;

    // Ekrana basılacak toplam temizlik skoru
    private int toplamTemizlenenKir = 0;

    // Robot yeni bir kareye geçerse sayacı sıfırlayabilelim diye eski konumu tutuyoruz
    private int oncekiX = -1;
    private int oncekiY = -1;

    public TemizlemeKontrolcu(Oda oda, Robot robot) {
        this.oda   = oda;
        this.robot = robot;
    }

    public boolean temizle() {
        int cx = robot.getX();
        int cy = robot.getY();

        // Robot bir önceki turdan beri hareket etmiş mi
        if (cx != oncekiX || cy != oncekiY) {
            temizlemeAdimi = 0; // Geçtiyse sayacı sıfırla ki havayı fırçalamasın
            oncekiX = cx;
            oncekiY = cy;
        }

        Hucre hucre = oda.getHucre(cx, cy);
        if (hucre == null) return false; // Harita dışıysa direkt pas geç

        // Zeminde kir var mı?
        if (hucre.isKirli()) {
            temizlemeAdimi++; // Fırçayı 1 tur daha döndür

            // Fırça motoru çalıştığı için ekstra batarya yiyoruz (Zorlu kir = Çok batarya)
            robot.bataryaAzalt(hucre.getKirTuru().getBataryaTuketimi() * 0.1);

            // Yeterince fırçaladık mı?
            if (temizlemeAdimi >= hucre.getKirTuru().getTemizlemeSuresi()) {

                // Kir tamamen söküldü hücreyi temizle
                hucre.setKirTuru(null);
                hucre.setTemizlendi(true);
                hucre.setZiyaretEdildi(true);

                temizlemeAdimi = 0; // Bir sonraki kir için fırça sayacını sıfırla
                toplamTemizlenenKir++; // Skoru artır

                return false; // İşim bitti, tekerlekleri döndürüp yola devam edebiliriz
            }
            return true; // Kir daha çıkmadı beni bu karede tutmaya devam et
        }

        // Eğer zemin zaten temizse vakum motorunu hiç açma sadece buradan geçtim diye işaretle
        hucre.setZiyaretEdildi(true);
        return false; // İşlem yok yola devam
    }

    // Arayüzden manuel kir ekleme köprüsü
    public void kirEkle(int x, int y, KirTuru kirTuru) {
        oda.kirEkle(x, y, kirTuru);
    }

    // Şarj istasyonuna döndüğünde fırça durumunu resetlemek için
    public void sayaciSifirla() {
        temizlemeAdimi = 0;
        oncekiX = -1;
        oncekiY = -1;
    }

    // Arayüzdeki Sıfırla butonuna basıldığında her şeyi fabrika ayarına döndür
    public void sifirla() {
        temizlemeAdimi      = 0;
        toplamTemizlenenKir = 0;
        oncekiX = -1;
        oncekiY = -1;
    }

    // Getter metotları
    public int getToplamTemizlenenKir() {
        return toplamTemizlenenKir;
    }
    public int getTemizlemeAdimi(){
        return temizlemeAdimi;
    }
}