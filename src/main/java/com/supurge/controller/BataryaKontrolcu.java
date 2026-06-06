package com.supurge.controller;

import com.supurge.model.Robot;

 // Robotun batarya yönetimini kontrol eden sınıf.
 // Şarj, tüketim ve düşük batarya uyarısı işlemlerini yönetir.

public class BataryaKontrolcu {

    private static final double DUSUK_BATARYA_ESIGI  = 20.0; // şarj yüzde 20 ye inince şarj istasyonunu aramaya başlar
    private static final double KRITIK_BATARYA_ESIGI = 10.0; // şarj yüzde 10 a inince robotun olduğu yerde kalır
    private static final double HAREKET_TUKETIMI     = 0.5;  // her adımda şarj tüketimi yüzde 0.5 yer.

    private final Robot robot;
    private boolean sarjIstasyonundaMi = false;

    public BataryaKontrolcu(Robot robot) {
        this.robot = robot;
    }

    //robot her ilerlediğinde modeldeki bataryayı 0.5 azaltan metot
    public void hareketTuketimUygula() {
        robot.bataryaAzalt(HAREKET_TUKETIMI);
    }

    //şarj yüzde 20 ve altındaysa evet istasyona dön (true)
    public boolean donmesiGerekiyorMu() {
        return robot.getBataryaYuzdesi() <= DUSUK_BATARYA_ESIGI;
    }

    //şarj yüzde 10 veya altındaysa dur (true) diyen
    public boolean kritikMi() {
        return robot.getBataryaYuzdesi() <= KRITIK_BATARYA_ESIGI;
    }

    //istasyon varınca şarjı yüzde 100 yapan
    public void sarjEt() {
        robot.bataryaSarjEt();
        sarjIstasyonundaMi = true;
    }

    //tekrar temizliğe başlarken
    public void istasyondanAyril() {
        sarjIstasyonundaMi = false;
    }

    public void bataryaAyarla(double yuzde) {
        robot.setBatarya(yuzde);
    }

     // Batarya durumunu ekranda metin olarak gözükür
    public String bataryaDurumMetni() {
        double yuzde = robot.getBataryaYuzdesi();
        if (yuzde <= KRITIK_BATARYA_ESIGI) return "KRİTİK";
        if (yuzde <= DUSUK_BATARYA_ESIGI) return "DÜŞÜK";
        if (yuzde <= 50) return "ORTA";
        return "İYİ";
    }

    // Robotun durumunu dışarıdan başka class'ların kontrol edebilmesi için
    public boolean isSarjIstasyonundaMi() {
        return sarjIstasyonundaMi;
    }

    // %20 sınırını A* algoritması veya UI kısmında kullanmak gerekirse diye
    public double getDusukBataryaEsigi() {
        return DUSUK_BATARYA_ESIGI;
    }
}
