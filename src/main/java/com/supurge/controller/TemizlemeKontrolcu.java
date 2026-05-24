package com.supurge.controller;

import com.supurge.model.*;

/**
 * Temizleme işlemlerini yöneten controller.
 */
public class TemizlemeKontrolcu {

    private final Oda oda;
    private final Robot robot;

    private int temizlemeAdimi      = 0;
    private int toplamTemizlenenKir = 0;

    // Önceki hücre takibi — robot hareket edince sayacı sıfırla
    private int oncekiX = -1;
    private int oncekiY = -1;

    public TemizlemeKontrolcu(Oda oda, Robot robot) {
        this.oda   = oda;
        this.robot = robot;
    }

    /**
     * Robotun bulunduğu hücreyi temizler.
     * Robot yeni bir hücreye geçtiyse sayaç otomatik sıfırlanır.
     *
     * @return true → hâlâ temizleniyor (hareket etme)
     *         false → temiz, hareket edebilir
     */
    public boolean temizle() {
        int cx = robot.getX();
        int cy = robot.getY();

        // Robot yeni hücreye geçtiyse sayacı sıfırla
        if (cx != oncekiX || cy != oncekiY) {
            temizlemeAdimi = 0;
            oncekiX = cx;
            oncekiY = cy;
        }

        Hucre hucre = oda.getHucre(cx, cy);
        if (hucre == null) return false;

        if (hucre.isKirli()) {
            temizlemeAdimi++;
            robot.bataryaAzalt(hucre.getKirTuru().getBataryaTuketimi() * 0.1);

            if (temizlemeAdimi >= hucre.getKirTuru().getTemizlemeSuresi()) {
                hucre.setKirTuru(null);
                hucre.setTemizlendi(true);   // kir temizlendi
                hucre.setZiyaretEdildi(true);
                temizlemeAdimi = 0;
                toplamTemizlenenKir++;
                return false;
            }
            return true; // hâlâ temizleniyor
        }

        // Kirli değil — sadece ziyaret edildi olarak işaretle (temizlendi DEĞİL)
        hucre.setZiyaretEdildi(true);
        return false;
    }

    public void kirEkle(int x, int y, KirTuru kirTuru) {
        oda.kirEkle(x, y, kirTuru);
    }
    public void sayaciSifirla() {
        temizlemeAdimi = 0;
        oncekiX = -1;
        oncekiY = -1;
    }

    public void sifirla() {
        temizlemeAdimi      = 0;
        toplamTemizlenenKir = 0;
        oncekiX = -1;
        oncekiY = -1;
    }

    public int getToplamTemizlenenKir() { return toplamTemizlenenKir; }
    public int getTemizlemeAdimi()      { return temizlemeAdimi; }
}
