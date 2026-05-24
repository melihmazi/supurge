package com.supurge.controller;

import com.supurge.model.*;

import java.util.List;
/**
 * Ana Controller - Orkestratör.
 * HareketKontrolcu, TemizlemeKontrolcu, YolBulmaKontrolcu ve BataryaKontrolcu'yu
 * koordine ederek simülasyonun akışını yönetir.
 * View yalnızca bu sınıfla iletişim kurar.
 */
public class SimulasyonKontrolcu {

    private final Oda oda;
    private Robot robot;
    private final SimulasyonDurumu durum;

    // Alt controller'lar
    private HareketKontrolcu hareketKontrolcu;
    private TemizlemeKontrolcu temizlemeKontrolcu;
    private YolBulmaKontrolcu yolBulmaKontrolcu;
    private BataryaKontrolcu bataryaKontrolcu;

    private long baslangicZamani = 0;
    private boolean sarjaDonuyorMu = false;

    public SimulasyonKontrolcu(int odaGenislik, int odaYukseklik) {
        this.oda = new Oda(odaGenislik, odaYukseklik);
        this.robot = new Robot(oda.getSarjIstasyonuX(), oda.getSarjIstasyonuY());
        this.durum = new SimulasyonDurumu();
        altKontrolculariBaşlat();
        durumGuncelle();
    }

    private void altKontrolculariBaşlat() {
        this.bataryaKontrolcu   = new BataryaKontrolcu(robot);
        this.hareketKontrolcu   = new HareketKontrolcu(oda, robot, bataryaKontrolcu);
        this.temizlemeKontrolcu = new TemizlemeKontrolcu(oda, robot);
        this.yolBulmaKontrolcu  = new YolBulmaKontrolcu(oda);
    }

    // =========================================================
    // Simülasyon Yaşam Döngüsü
    // =========================================================

    /** Simülasyonu başlatır veya devam ettirir. */
    public void baslat() {
        robot.setCalisiyor(true);
        if (baslangicZamani == 0) baslangicZamani = System.currentTimeMillis();
        bataryaKontrolcu.istasyondanAyril();
        sarjaDonuyorMu = false;
    }

    /** Simülasyonu duraklatır. */
    public void duraklat() {
        robot.setCalisiyor(false);
    }

    /** Simülasyonu tamamen sıfırlar. */
    public void sifirla() {
        oda.sifirla();
        // Robot nesnesini yeniden oluşturmak yerine mevcut robotu sıfırla
        // (View'daki referans bozulmasın)
        robot.setX(oda.getSarjIstasyonuX());
        robot.setY(oda.getSarjIstasyonuY());
        robot.setBatarya(100);
        robot.setYon(Yon.DOGU);
        robot.setHiz(1.0);
        robot.setCalisiyor(false);
        robot.yoluTemizle();
        baslangicZamani = 0;
        sarjaDonuyorMu = false;
        // Alt controller'ları sıfırla (robot referansı aynı kaldığı için yeniden oluşturmaya gerek yok)
        hareketKontrolcu.sifirla();
        temizlemeKontrolcu.sifirla();
        durumGuncelle();
    }

    // =========================================================
    // Ana Simülasyon Döngüsü (JavaFX AnimationTimer'dan çağrılır)
    // =========================================================

    /**
     * Her simülasyon tick'inde çağrılır.
     * Öncelik sırası: Batarya kontrolü → Şarj dönüşü → Temizleme → Hareket
     */
    public void adimAt() {
        if (!robot.isCalisiyor()) return;

        // 1. Batarya bitti mi?
        if (bataryaKontrolcu.kritikMi()) {
            robot.setCalisiyor(false);
            durumGuncelle();
            return;
        }

        // 2. Şarj istasyonuna dönüş modu
        if (sarjaDonuyorMu || bataryaKontrolcu.donmesiGerekiyorMu()) {
            sarjaDonuyorMu = true;
            sarjIstasyonunaAdimAt();
            durumGuncelle();
            return;
        }

        // 3. Mevcut hücreyi temizle
        boolean temizleniyor = temizlemeKontrolcu.temizle();

        // 4. Temizleme bitmişse hareket et
        if (!temizleniyor) {
            hareketKontrolcu.hareketEt();
        }

        durumGuncelle();
    }

    // =========================================================
    // Şarj İstasyonu Dönüşü
    // =========================================================

    /**
     * Kullanıcı isteğiyle veya düşük batarya durumunda şarj istasyonuna döner.
     * A* algoritması kullanır.
     */
    public void sarjIstasyonunaDon() {
        sarjaDonuyorMu = true;
        sarjIstasyonunaAdimAt();
        durumGuncelle();
    }

    /**
     * A* ile şarj istasyonuna bir adım atar.
     * Ulaşınca şarj eder ve normal moda döner.
     */
    private void sarjIstasyonunaAdimAt() {
        int hedefX = oda.getSarjIstasyonuX();
        int hedefY = oda.getSarjIstasyonuY();

        // Zaten istasyondaysa şarj et
        if (robot.getX() == hedefX && robot.getY() == hedefY) {
            bataryaKontrolcu.sarjEt();
            sarjaDonuyorMu = false;
            temizlemeKontrolcu.sayaciSifirla();
            return;
        }

        // A* ile bir adım ilerle
        List<int[]> yol = yolBulmaKontrolcu.aYildizYolBul(
                robot.getX(), robot.getY(), hedefX, hedefY
        );

        if (yol != null && yol.size() > 1) {
            int[] sonrakiAdim = yol.get(1);
            robot.hareketEt(sonrakiAdim[0], sonrakiAdim[1]);
        }
    }

    // =========================================================
    // Kullanıcı Eylemleri (View'dan çağrılır)
    // =========================================================

    /** Belirtilen hücreye kir ekler. */
    public void kirEkle(int x, int y, KirTuru kirTuru) {
        temizlemeKontrolcu.kirEkle(x, y, kirTuru);
        durumGuncelle();
    }

    /** Belirtilen hücreye engel (mobilya) ekler. */
    public void engelEkle(int x, int y) {
        oda.engelEkle(x, y);
    }

    /** Robot hızını ayarlar (0.5x - 3.0x). */
    public void hizAyarla(double hiz) {
        robot.setHiz(hiz);
    }

    /** Temizleme algoritmasını değiştirir. */
    public void algoritmaAyarla(TemizlemeAlgoritması algoritma) {
        robot.setAlgoritma(algoritma);
        hareketKontrolcu.sifirla(); // spiral sayaçlarını sıfırla
    }

    /** Bataryayı manuel olarak ayarlar (0-100). */
    public void bataryaAyarla(double yuzde) {
        bataryaKontrolcu.bataryaAyarla(yuzde);
        if (yuzde > bataryaKontrolcu.getDusukBataryaEsigi()) {
            sarjaDonuyorMu = false; // yeterli batarya varsa dönüş modunu kapat
        }
        durumGuncelle();
    }

    // =========================================================
    // Durum Güncelleme
    // =========================================================

    private void durumGuncelle() {
        durum.setRobotX(robot.getX());
        durum.setRobotY(robot.getY());
        durum.setYon(robot.getYon());
        durum.setBataryaYuzdesi(robot.getBataryaYuzdesi());
        durum.setToplamHucre(oda.toplamHucreSayisi());
        durum.setTemizlenenHucre(oda.temizlenenHucreSayisi());
        durum.setKalanKirliHucre(oda.kirliHucreleriGetir().size());
        durum.setToplamTemizlenenKir(temizlemeKontrolcu.getToplamTemizlenenKir());
        durum.setRobotHiz(robot.getHiz());
        durum.setSarjaDonuyorMu(sarjaDonuyorMu);
        durum.setBataryaDurumu(bataryaKontrolcu.bataryaDurumMetni());
        if (baslangicZamani > 0)
            durum.setGecenSure((System.currentTimeMillis() - baslangicZamani) / 1000);
    }

    // =========================================================
    // Getters (View erişimi için)
    // =========================================================

    public Oda getOda()                       { return oda; }
    public Robot getRobot()                   { return robot; }
    public SimulasyonDurumu getDurum()        { return durum; }
    public boolean isSarjaDonuyorMu()         { return sarjaDonuyorMu; }
    public BataryaKontrolcu getBataryaKontrolcu() { return bataryaKontrolcu; }
}
