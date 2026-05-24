package com.supurge.controller;

import com.supurge.model.*;

/**
 * Temizleme işlemlerini yöneten controller.
 * Kir türüne göre farklı temizleme süresi ve batarya tüketimi uygular.
 */
public class TemizlemeKontrolcu {

    private final Oda oda;
    private final Robot robot;

    // Mevcut hücredeki temizleme ilerleme sayacı
    private int temizlemeAdimi = 0;
    // Toplam temizlenen kir sayısı (istatistik)
    private int toplamTemizlenenKir = 0;

    public TemizlemeKontrolcu(Oda oda, Robot robot) {
        this.oda = oda;
        this.robot = robot;
    }

    /**
     * Robotun bulunduğu hücreyi temizlemeye çalışır.
     * @return hücre kirli ve temizleme devam ediyorsa true,
     *         hücre zaten temizse false (hareket edebilir)
     */
    public boolean temizle() {
        Hucre hucre = oda.getHucre(robot.getX(), robot.getY());
        if (hucre == null) return false;

        if (hucre.isKirli()) {
            temizlemeAdimi++;
            // Kir türüne göre ek batarya tüket
            double ekTuketim = hucre.getKirTuru().getBataryaTuketimi() * 0.1;
            robot.bataryaAzalt(ekTuketim);

            // Temizleme tamamlandı mı?
            if (temizlemeAdimi >= hucre.getKirTuru().getTemizlemeSuresi()) {
                hucreTemizle(hucre);
                return false; // temizlendi, artık hareket edebilir
            }
            return true; // hâlâ temizleniyor, bekle
        }

        // Kirli değil ama ziyaret edilmemiş
        if (!hucre.isTemizlendi()) {
            hucre.setTemizlendi(true);
        }
        temizlemeAdimi = 0;
        return false;
    }

    /**
     * Belirli bir hücreye kir ekler.
     */
    public void kirEkle(int x, int y, KirTuru kirTuru) {
        oda.kirEkle(x, y, kirTuru);
    }

    /**
     * Temizleme sayacını sıfırlar (robot yeni hücreye geçtiğinde).
     */
    public void sayaciSifirla() {
        temizlemeAdimi = 0;
    }

    /**
     * Tüm temizleme durumunu sıfırlar.
     */
    public void sifirla() {
        temizlemeAdimi = 0;
        toplamTemizlenenKir = 0;
    }

    public int getToplamTemizlenenKir() {
        return toplamTemizlenenKir;
    }

    public int getTemizlemeAdimi() {
        return temizlemeAdimi;
    }

    // ---- Özel Metodlar ----

    private void hucreTemizle(Hucre hucre) {
        hucre.setKirTuru(null);
        hucre.setTemizlendi(true);
        temizlemeAdimi = 0;
        toplamTemizlenenKir++;
    }
}
