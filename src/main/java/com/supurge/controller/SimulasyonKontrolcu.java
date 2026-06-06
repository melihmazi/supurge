package com.supurge.controller;

import com.supurge.model.KirTuru;
import com.supurge.model.Oda;
import com.supurge.model.Robot;
import com.supurge.model.SimulasyonDurumu;
import com.supurge.model.TemizlemeAlgoritması;
import com.supurge.model.Yon;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

public class SimulasyonKontrolcu {

    // Sistemdeki fiziksel yapıtaşları
    private final Oda oda;
    private Robot robot;

    // MVC Mimarisi: View'a gönderilecek verilerin paketlendiği Data Transfer Object
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

    // Sistem ilk açıldığında nesneleri ürettiğimiz yer
    public SimulasyonKontrolcu(int odaGenislik, int odaYukseklik) {
        this.oda = new Oda(odaGenislik, odaYukseklik);
        this.robot = new Robot(this.oda.getSarjIstasyonuX(), this.oda.getSarjIstasyonuY());
        this.durum = new SimulasyonDurumu();
        this.altKontrolculariBaşlat();
        this.durumGuncelle();
    }

    // Alt beyinleri uyandırıp onlara kullanacakları odayı ve robotu veriyoruz
    private void altKontrolculariBaşlat() {
        this.bataryaKontrolcu = new BataryaKontrolcu(this.robot);
        this.hareketKontrolcu = new HareketKontrolcu(this.oda, this.robot, this.bataryaKontrolcu);
        this.temizlemeKontrolcu = new TemizlemeKontrolcu(this.oda, this.robot);
        this.yolBulmaKontrolcu = new YolBulmaKontrolcu(this.oda);
    }

    // Arayüzdeki başlat butonunun tetiklediği metot
    public void baslat() {
        this.kullaniciDuraklattiMi = false;
        this.robot.setCalisiyor(true);
        if (this.baslangicZamani == 0L) {
            this.baslangicZamani = System.currentTimeMillis(); // Kronometreyi başlat
        }
        this.bataryaKontrolcu.istasyondanAyril();
        this.sarjaDonuyorMu = false;
    }

    // Arayüzdeki duraklat butonunun tetiklediği metot
    public void duraklat() {
        this.kullaniciDuraklattiMi = true;
        this.robot.setCalisiyor(false);
    }

    // Arayüzdeki sıfırla butonu her şeyi fabrika ayarlarına döndürür.
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


    // Arayüzdeki timer saniyede onlarca kez burayı çağırır.
    // Tüm karar hiyerarşisi (Önce şarj -> Önce altını temizle -> Sonra yürü) burda

    public void adimAt() {
        if (!this.kullaniciDuraklattiMi) { // Duraklatılmadıysa oyun akar
            if (this.robot.isCalisiyor()) {

                // Şarj azaldıysa temizliği salıp eve dönme kontrolü
                if (!this.sarjaDonuyorMu && !this.bataryaKontrolcu.donmesiGerekiyorMu()) {

                    // Şarj %10'a indiyse sistemi tamamen kitle
                    if (this.bataryaKontrolcu.kritikMi()) {
                        this.robot.setCalisiyor(false);
                        this.durumGuncelle();
                    } else {
                        // Önce altındaki kiri temizlemeyi dene
                        boolean temizleniyor = this.temizlemeKontrolcu.temizle();
                        // Altında kir yoksa bir adım ileri git
                        if (!temizleniyor) {
                            this.hareketKontrolcu.hareketEt();
                        }

                        if (this.robot.getHareketYolu().size() > 500) {
                            this.robot.getHareketYolu().subList(0, 250).clear();
                        }
                        this.durumGuncelle();
                    }
                } else {
                    // Batarya düştü A* ile istasyona doğru bir adım at
                    this.sarjaDonuyorMu = true;
                    this.sarjIstasyonunaAdimAt();
                    this.durumGuncelle();
                }
            } else {
                // Temizlik bitti ama şarj istasyonunda değilsek eve dön komutu ver
                if ((this.robot.getX() != this.oda.getSarjIstasyonuX() || this.robot.getY() != this.oda.getSarjIstasyonuY()) && this.oda.temizlenenHucreSayisi() > 0 && !this.sarjaDonuyorMu) {
                    this.temizlikBittiMi = true;
                    this.sarjaDonuyorMu = true;
                    this.robot.setCalisiyor(true);
                }
            }
        }
    }

    // Arayüzdeki istasyona dön butonuna basıldığında manuel çağrılır
    public void sarjIstasyonunaDon() {
        this.sarjaDonuyorMu = true;
        this.manuelSarjDonusu = true;
        this.robot.setCalisiyor(true);
        if (this.baslangicZamani == 0L) {
            this.baslangicZamani = System.currentTimeMillis();
        }
        this.durumGuncelle();
    }

    // İstasyona dönerken A* algoritmasından çizilen rotanın ilk adımını atar
    private void sarjIstasyonunaAdimAt() {
        int hedefX = this.oda.getSarjIstasyonuX();
        int hedefY = this.oda.getSarjIstasyonuY();

        // Yuvaya vardık mı?
        if (this.robot.getX() == hedefX && this.robot.getY() == hedefY) {
            this.bataryaKontrolcu.sarjEt(); // Bataryayı 100 yap
            this.sarjaDonuyorMu = false;
            this.temizlemeKontrolcu.sayaciSifirla();
            this.hareketKontrolcu.sifirla();

            // Neden dönmüştük ona göre aksiyon al (Temizlik bittiyse kapat, bitmediyse temizliğe geri dön)
            if (this.temizlikBittiMi) {
                this.robot.setCalisiyor(false);
                this.temizlikBittiMi = false;
            } else if (!this.manuelSarjDonusu) {
                this.robot.setCalisiyor(true); // Şarjı fulledi, işe geri dön
            } else {
                this.robot.setCalisiyor(false);
                this.manuelSarjDonusu = false;
            }

        } else {
            // Henüz varmadık, A* ile hedef istasyonun yolunu çizdir ve sıradaki adımı at
            List<int[]> yol = this.yolBulmaKontrolcu.aYildizYolBul(this.robot.getX(), this.robot.getY(), hedefX, hedefY);
            if (yol != null && yol.size() > 1) {
                int[] sonrakiAdim = (int[])yol.get(1);
                this.robot.hareketEt(sonrakiAdim[0], sonrakiAdim[1]);
                this.bataryaKontrolcu.hareketTuketimUygula(); // Yürürken de şarj yakar
            }
        }
    }

    public void kirEkle(int x, int y, KirTuru kirTuru) {
        this.temizlemeKontrolcu.kirEkle(x, y, kirTuru);
        this.durumGuncelle();
    }

    // Arayüzden mobilya eklendiğinde çağrılır
    public void engelEkle(int x, int y, int mobilyaTuru) {
        this.oda.engelEkle(x, y, mobilyaTuru);
    }

    public void hizAyarla(double hiz) {
        this.robot.setHiz(hiz);
    }

    public void algoritmaAyarla(TemizlemeAlgoritması algoritma) {
        this.robot.setAlgoritma(algoritma);
        this.hareketKontrolcu.sifirla(); // Algoritma değişince eski hafızayı sil
    }

    public void bataryaAyarla(double yuzde) {
        this.bataryaKontrolcu.bataryaAyarla(yuzde);
        if (yuzde > this.bataryaKontrolcu.getDusukBataryaEsigi()) {
            this.sarjaDonuyorMu = false;
        }
        this.durumGuncelle();
    }


    // Verileri toplayıp paketleyip View'a hazırladığımız yer
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

        // Robot kapalıysa, istasyondaysa ve temizlediği yer sıfırdan büyükse iş tamamen bitmiştir
        boolean tumTemiz = !this.robot.isCalisiyor() && this.robot.getX() == this.oda.getSarjIstasyonuX() && this.robot.getY() == this.oda.getSarjIstasyonuY() && this.oda.temizlenenHucreSayisi() > 0;
        this.durum.setTamamlandi(tumTemiz);

        // Geçen süreyi milisaniyeden saniyeye çevirip ekrana basmak için
        if (this.baslangicZamani > 0L) {
            this.durum.setGecenSure((System.currentTimeMillis() - this.baslangicZamani) / 1000L);
        }
    }

    public Oda getOda() { return this.oda; }
    public Robot getRobot() { return this.robot; }
    public SimulasyonDurumu getDurum() { return this.durum; }
    public boolean isSarjaDonuyorMu() { return this.sarjaDonuyorMu; }
    public BataryaKontrolcu getBataryaKontrolcu() { return this.bataryaKontrolcu; }

    public boolean ulasilamayanAlanVarMi() {
        int genislik = oda.getGenislik();
        int yukseklik = oda.getYukseklik();

        boolean[][] ziyaretEdildi = new boolean[genislik][yukseklik];
        Queue<int[]> kuyruk = new LinkedList<>();

        // Tarama, garanti ulaşılabilir tek yer olan şarj istasyonundan başlar
        int baslangicX = oda.getSarjIstasyonuX();
        int baslangicY = oda.getSarjIstasyonuY();

        kuyruk.add(new int[]{baslangicX, baslangicY});
        ziyaretEdildi[baslangicX][baslangicY] = true;

        int[][] yonler = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}; // Sağ, Sol, Aşağı, Yukarı

        // BFS ile  haritaya yayılıyoruz
        while (!kuyruk.isEmpty()) {
            int[] anlik = kuyruk.poll();
            int x = anlik[0];
            int y = anlik[1];

            for (int[] yon : yonler) {
                int yeniX = x + yon[0];
                int yeniY = y + yon[1];

                // Sınırlar içindeyse, bakılmadıysa ve o hücrede engel yoksz su oraya da aksın
                if (oda.gecerliMi(yeniX, yeniY) && !ziyaretEdildi[yeniX][yeniY] && !oda.engelMi(yeniX, yeniY)) {
                    ziyaretEdildi[yeniX][yeniY] = true;
                    kuyruk.add(new int[]{yeniX, yeniY});
                }
            }
        }
        for (int y = 0; y < yukseklik; y++) {
            for (int x = 0; x < genislik; x++) {
                // Eğer bir hücrede engel yoksa ama BFS dalgası oraya ziyaret etmediyse
                if (!oda.engelMi(x, y) && !ziyaretEdildi[x][y]) {
                    return true; // Ulaşılamayan, etrafı kapalı boş bir alan bulduk!
                }
            }
        }

        return false; // Tüm boş alanlara temiz bir şekilde ulaşılabiliyor
    }
}