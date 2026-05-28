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
    private boolean manuelSarjDonusu = false; // kullanıcı butona bastıysa true
    private boolean temizlikBittiMi = false;  // YENİ: Otonom dönüş kontrol bayrağı
    private boolean kullaniciDuraklattiMi = false;

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
        kullaniciDuraklattiMi = false; // Kullanıcı başlattı, duraklatma modu iptal
        robot.setCalisiyor(true);
        if (baslangicZamani == 0) baslangicZamani = System.currentTimeMillis();
        bataryaKontrolcu.istasyondanAyril();
        sarjaDonuyorMu = false;
    }

    public void duraklat() {
        kullaniciDuraklattiMi = true; // Sisteme kullanıcının manuel müdahale ettiğini söyle
        robot.setCalisiyor(false);
    }

    public void sifirla() {
        oda.sifirla();
        robot.setX(oda.getSarjIstasyonuX());
        robot.setY(oda.getSarjIstasyonuY());
        robot.setBatarya(100);
        robot.setYon(Yon.DOGU);
        robot.setHiz(1.0);
        robot.setCalisiyor(false);
        robot.yoluTemizle();
        baslangicZamani = 0;
        sarjaDonuyorMu = false;
        manuelSarjDonusu = false;
        temizlikBittiMi = false;
        kullaniciDuraklattiMi = false; // Sıfırlamada bu bayrağı da temizle

        hareketKontrolcu.sifirla();
        temizlemeKontrolcu.sifirla();
        durumGuncelle();
    }

    // =========================================================
    // Ana Simülasyon Döngüsü (JavaFX AnimationTimer'dan çağrılır)
    // =========================================================

    /**
     * Her simülasyon tick'inde çağrılır.
     */
    public void adimAt() {
        if (kullaniciDuraklattiMi) {
            return;
        }
        // YENİ MANTIK: Robot çalışmıyorsa (algoritma gidecek yer bulamayıp motoru durdurduysa)
        if (!robot.isCalisiyor()) {
            if (robot.getX() != oda.getSarjIstasyonuX() || robot.getY() != oda.getSarjIstasyonuY()) {
                if (oda.temizlenenHucreSayisi() > 0 && !sarjaDonuyorMu) {
                    temizlikBittiMi = true;
                    sarjaDonuyorMu = true;
                    robot.setCalisiyor(true); // Dönüş yolculuğu için motorları ateşle
                }
            }
            return;
        }

        // 1. Şarj istasyonuna dönüş modu — temizleme yapmadan direkt git
        if (sarjaDonuyorMu || bataryaKontrolcu.donmesiGerekiyorMu()) {
            sarjaDonuyorMu = true;
            sarjIstasyonunaAdimAt();
            durumGuncelle();
            return;
        }

        // 2. Batarya kritik ve istasyona dönemiyorsa dur
        if (bataryaKontrolcu.kritikMi()) {
            robot.setCalisiyor(false);
            durumGuncelle();
            return;
        }

        // 3. Mevcut hücreyi temizle
        boolean temizleniyor = temizlemeKontrolcu.temizle();

        // 4. Temizleme bitmişse hareket et
        if (!temizleniyor) {
            hareketKontrolcu.hareketEt();
        }

        // 5. Hareket yolu çok uzadıysa kırp (bellek tasarrufu)
        if (robot.getHareketYolu().size() > 500) {
            robot.getHareketYolu().subList(0, 250).clear();
        }

        durumGuncelle();
    }

    // =========================================================
    // Şarj İstasyonu Dönüşü
    // =========================================================

    public void sarjIstasyonunaDon() {
        sarjaDonuyorMu = true;
        manuelSarjDonusu = true;
        robot.setCalisiyor(true);
        if (baslangicZamani == 0) baslangicZamani = System.currentTimeMillis();
        durumGuncelle();
    }

    private void sarjIstasyonunaAdimAt() {
        int hedefX = oda.getSarjIstasyonuX();
        int hedefY = oda.getSarjIstasyonuY();

        // Zaten istasyondaysa şarj et ve bekle
        if (robot.getX() == hedefX && robot.getY() == hedefY) {
            bataryaKontrolcu.sarjEt();
            sarjaDonuyorMu = false;
            temizlemeKontrolcu.sayaciSifirla();
            hareketKontrolcu.sifirla();

            // YENİ MANTIK: Neden döndük?
            if (temizlikBittiMi) {
                robot.setCalisiyor(false); // Temizlik bitti, uyku moduna geç
                temizlikBittiMi = false;
            } else if (!manuelSarjDonusu) {
                robot.setCalisiyor(true);  // Şarj için döndüyse dolunca işe geri dön
            } else {
                robot.setCalisiyor(false); // Kullanıcı butona bastıysa bekle
                manuelSarjDonusu = false;
            }
            return;
        }

        // A* ile bir adım ilerle
        List<int[]> yol = yolBulmaKontrolcu.aYildizYolBul(
                robot.getX(), robot.getY(), hedefX, hedefY
        );

        if (yol != null && yol.size() > 1) {
            int[] sonrakiAdim = yol.get(1);
            robot.hareketEt(sonrakiAdim[0], sonrakiAdim[1]);
            bataryaKontrolcu.hareketTuketimUygula();
        }
    }

    // =========================================================
    // Kullanıcı Eylemleri
    // =========================================================

    public void kirEkle(int x, int y, KirTuru kirTuru) {
        temizlemeKontrolcu.kirEkle(x, y, kirTuru);
        durumGuncelle();
    }

    public void engelEkle(int x, int y, int mobilyaTuru) {
        oda.engelEkle(x, y, mobilyaTuru);
    }

    public void hizAyarla(double hiz) {
        robot.setHiz(hiz);
    }

    public void algoritmaAyarla(TemizlemeAlgoritması algoritma) {
        robot.setAlgoritma(algoritma);
        hareketKontrolcu.sifirla();
    }

    public void bataryaAyarla(double yuzde) {
        bataryaKontrolcu.bataryaAyarla(yuzde);
        if (yuzde > bataryaKontrolcu.getDusukBataryaEsigi()) {
            sarjaDonuyorMu = false;
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
        durum.setCalisiyor(robot.isCalisiyor());

        // YENİ MANTIK: Sadece robot yuvaya başarıyla park ettiğinde ve temizlik yaptığında "Tamamlandı" de.
        boolean tumTemiz = !robot.isCalisiyor()
                && robot.getX() == oda.getSarjIstasyonuX()
                && robot.getY() == oda.getSarjIstasyonuY()
                && oda.temizlenenHucreSayisi() > 0;

        durum.setTamamlandi(tumTemiz);
        if (baslangicZamani > 0)
            durum.setGecenSure((System.currentTimeMillis() - baslangicZamani) / 1000);
    }

    public Oda getOda()                       { return oda; }
    public Robot getRobot()                   { return robot; }
    public SimulasyonDurumu getDurum()        { return durum; }
    public boolean isSarjaDonuyorMu()         { return sarjaDonuyorMu; }
    public BataryaKontrolcu getBataryaKontrolcu() { return bataryaKontrolcu; }
}