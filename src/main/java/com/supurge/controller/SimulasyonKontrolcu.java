package com.supurge.controller;

import com.supurge.model.KirTuru;
import com.supurge.model.Oda;
import com.supurge.model.Robot;
import com.supurge.model.SimulasyonDurumu;
import com.supurge.model.TemizlemeAlgoritması;
import com.supurge.model.Yon;
import java.util.List;
import java.util.LinkedList; // YENİ EKLENDİ
import java.util.Queue;      // YENİ EKLENDİ

public class SimulasyonKontrolcu {
    private final Oda oda;
    private Robot robot;
    private final SimulasyonDurumu durum;
    private HareketKontrolcu hareketKontrolcu;
    private TemizlemeKontrolcu temizlemeKontrolcu;
    private YolBulmaKontrolcu yolBulmaKontrolcu;
    private BataryaKontrolcu bataryaKontrolcu;
    private long baslangicZamani = 0L;
    private boolean sarjaDonuyorMu = false;
    private boolean manuelSarjDonusu = false;
    private boolean temizlikBittiMi = false;
    private boolean kullaniciDuraklattiMi = false;

    public SimulasyonKontrolcu(int odaGenislik, int odaYukseklik) {
        this.oda = new Oda(odaGenislik, odaYukseklik);
        this.robot = new Robot(this.oda.getSarjIstasyonuX(), this.oda.getSarjIstasyonuY());
        this.durum = new SimulasyonDurumu();
        this.altKontrolculariBaşlat();
        this.durumGuncelle();
    }

    private void altKontrolculariBaşlat() {
        this.bataryaKontrolcu = new BataryaKontrolcu(this.robot);
        this.hareketKontrolcu = new HareketKontrolcu(this.oda, this.robot, this.bataryaKontrolcu);
        this.temizlemeKontrolcu = new TemizlemeKontrolcu(this.oda, this.robot);
        this.yolBulmaKontrolcu = new YolBulmaKontrolcu(this.oda);
    }

    public void baslat() {
        this.kullaniciDuraklattiMi = false;
        this.robot.setCalisiyor(true);
        if (this.baslangicZamani == 0L) {
            this.baslangicZamani = System.currentTimeMillis();
        }

        this.bataryaKontrolcu.istasyondanAyril();
        this.sarjaDonuyorMu = false;
    }

    public void duraklat() {
        this.kullaniciDuraklattiMi = true;
        this.robot.setCalisiyor(false);
    }

    public void sifirla() {
        this.oda.sifirla();
        this.robot.setX(this.oda.getSarjIstasyonuX());
        this.robot.setY(this.oda.getSarjIstasyonuY());
        this.robot.setBatarya((double)100.0F);
        this.robot.setYon(Yon.DOGU);
        this.robot.setHiz((double)1.0F);
        this.robot.setCalisiyor(false);
        this.robot.yoluTemizle();
        this.baslangicZamani = 0L;
        this.sarjaDonuyorMu = false;
        this.manuelSarjDonusu = false;
        this.temizlikBittiMi = false;
        this.kullaniciDuraklattiMi = false;
        this.hareketKontrolcu.sifirla();
        this.temizlemeKontrolcu.sifirla();
        this.durumGuncelle();
    }

    public void adimAt() {
        if (!this.kullaniciDuraklattiMi) {
            if (this.robot.isCalisiyor()) {
                if (!this.sarjaDonuyorMu && !this.bataryaKontrolcu.donmesiGerekiyorMu()) {
                    if (this.bataryaKontrolcu.kritikMi()) {
                        this.robot.setCalisiyor(false);
                        this.durumGuncelle();
                    } else {
                        boolean temizleniyor = this.temizlemeKontrolcu.temizle();
                        if (!temizleniyor) {
                            this.hareketKontrolcu.hareketEt();
                        }

                        if (this.robot.getHareketYolu().size() > 500) {
                            this.robot.getHareketYolu().subList(0, 250).clear();
                        }

                        this.durumGuncelle();
                    }
                } else {
                    this.sarjaDonuyorMu = true;
                    this.sarjIstasyonunaAdimAt();
                    this.durumGuncelle();
                }
            } else {
                if ((this.robot.getX() != this.oda.getSarjIstasyonuX() || this.robot.getY() != this.oda.getSarjIstasyonuY()) && this.oda.temizlenenHucreSayisi() > 0 && !this.sarjaDonuyorMu) {
                    this.temizlikBittiMi = true;
                    this.sarjaDonuyorMu = true;
                    this.robot.setCalisiyor(true);
                }

            }
        }
    }

    public void sarjIstasyonunaDon() {
        this.sarjaDonuyorMu = true;
        this.manuelSarjDonusu = true;
        this.robot.setCalisiyor(true);
        if (this.baslangicZamani == 0L) {
            this.baslangicZamani = System.currentTimeMillis();
        }

        this.durumGuncelle();
    }

    private void sarjIstasyonunaAdimAt() {
        int hedefX = this.oda.getSarjIstasyonuX();
        int hedefY = this.oda.getSarjIstasyonuY();
        if (this.robot.getX() == hedefX && this.robot.getY() == hedefY) {
            this.bataryaKontrolcu.sarjEt();
            this.sarjaDonuyorMu = false;
            this.temizlemeKontrolcu.sayaciSifirla();
            this.hareketKontrolcu.sifirla();
            if (this.temizlikBittiMi) {
                this.robot.setCalisiyor(false);
                this.temizlikBittiMi = false;
            } else if (!this.manuelSarjDonusu) {
                this.robot.setCalisiyor(true);
            } else {
                this.robot.setCalisiyor(false);
                this.manuelSarjDonusu = false;
            }

        } else {
            List<int[]> yol = this.yolBulmaKontrolcu.aYildizYolBul(this.robot.getX(), this.robot.getY(), hedefX, hedefY);
            if (yol != null && yol.size() > 1) {
                int[] sonrakiAdim = (int[])yol.get(1);
                this.robot.hareketEt(sonrakiAdim[0], sonrakiAdim[1]);
                this.bataryaKontrolcu.hareketTuketimUygula();
            }

        }
    }

    public void kirEkle(int x, int y, KirTuru kirTuru) {
        this.temizlemeKontrolcu.kirEkle(x, y, kirTuru);
        this.durumGuncelle();
    }

    public void engelEkle(int x, int y, int mobilyaTuru) {
        this.oda.engelEkle(x, y, mobilyaTuru);
    }

    public void hizAyarla(double hiz) {
        this.robot.setHiz(hiz);
    }

    public void algoritmaAyarla(TemizlemeAlgoritması algoritma) {
        this.robot.setAlgoritma(algoritma);
        this.hareketKontrolcu.sifirla();
    }

    public void bataryaAyarla(double yuzde) {
        this.bataryaKontrolcu.bataryaAyarla(yuzde);
        if (yuzde > this.bataryaKontrolcu.getDusukBataryaEsigi()) {
            this.sarjaDonuyorMu = false;
        }

        this.durumGuncelle();
    }

    private void durumGuncelle() {
        this.durum.setRobotX(this.robot.getX());
        this.durum.setRobotY(this.robot.getY());
        this.durum.setYon(this.robot.getYon());
        this.durum.setBataryaYuzdesi(this.robot.getBataryaYuzdesi());
        this.durum.setToplamHucre(this.oda.toplamHucreSayisi());
        this.durum.setTemizlenenHucre(this.oda.temizlenenHucreSayisi());
        this.durum.setKalanKirliHucre(this.oda.kirliHucreleriGetir().size());
        this.durum.setToplamTemizlenenKir(this.temizlemeKontrolcu.getToplamTemizlenenKir());
        this.durum.setRobotHiz(this.robot.getHiz());
        this.durum.setSarjaDonuyorMu(this.sarjaDonuyorMu);
        this.durum.setBataryaDurumu(this.bataryaKontrolcu.bataryaDurumMetni());
        this.durum.setCalisiyor(this.robot.isCalisiyor());
        boolean tumTemiz = !this.robot.isCalisiyor() && this.robot.getX() == this.oda.getSarjIstasyonuX() && this.robot.getY() == this.oda.getSarjIstasyonuY() && this.oda.temizlenenHucreSayisi() > 0;
        this.durum.setTamamlandi(tumTemiz);
        if (this.baslangicZamani > 0L) {
            this.durum.setGecenSure((System.currentTimeMillis() - this.baslangicZamani) / 1000L);
        }

    }

    public Oda getOda() {
        return this.oda;
    }

    public Robot getRobot() {
        return this.robot;
    }

    public SimulasyonDurumu getDurum() {
        return this.durum;
    }

    public boolean isSarjaDonuyorMu() {
        return this.sarjaDonuyorMu;
    }

    public BataryaKontrolcu getBataryaKontrolcu() {
        return this.bataryaKontrolcu;
    }

    // --- YENİ EKLENEN BONUS ÖZELLİK: Ulaşılamayan Alan (Ölü Bölge) Tespiti ---
    public boolean ulasilamayanAlanVarMi() {
        int genislik = oda.getGenislik();
        int yukseklik = oda.getYukseklik();

        boolean[][] ziyaretEdildi = new boolean[genislik][yukseklik];
        Queue<int[]> kuyruk = new LinkedList<>();

        // Tarama, robotun her zaman erişebildiği "Şarj İstasyonundan" başlar
        int baslangicX = oda.getSarjIstasyonuX();
        int baslangicY = oda.getSarjIstasyonuY();

        kuyruk.add(new int[]{baslangicX, baslangicY});
        ziyaretEdildi[baslangicX][baslangicY] = true;

        // Dört ana yön: Sağ, Sol, Aşağı, Yukarı
        int[][] yonler = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        // BFS (Sığ Öncelikli Arama) ile taşkın dolgu (flood fill) yapıyoruz
        while (!kuyruk.isEmpty()) {
            int[] anlik = kuyruk.poll();
            int x = anlik[0];
            int y = anlik[1];

            for (int[] yon : yonler) {
                int yeniX = x + yon[0];
                int yeniY = y + yon[1];

                // Sınırlar içindeyse, daha önce bakılmadıysa ve o hücrede ENGEL YOKSA
                if (oda.gecerliMi(yeniX, yeniY) && !ziyaretEdildi[yeniX][yeniY] && !oda.engelMi(yeniX, yeniY)) {
                    ziyaretEdildi[yeniX][yeniY] = true;
                    kuyruk.add(new int[]{yeniX, yeniY});
                }
            }
        }

        // Tarama bitti. Şimdi tüm odayı baştan sona kontrol ediyoruz.
        for (int y = 0; y < yukseklik; y++) {
            for (int x = 0; x < genislik; x++) {
                // Eğer bir hücrede ENGEL YOKSA ama BFS ile oraya ZİYARET EDİLEMEDİYSE:
                if (!oda.engelMi(x, y) && !ziyaretEdildi[x][y]) {
                    return true; // Ulaşılamayan boş bir alan bulduk!
                }
            }
        }

        return false; // Tüm boş alanlara başarıyla ulaşılabiliyor.
    }
}