package com.supurge.controller;

import com.supurge.model.Robot;

/**
 * Robotun batarya yönetimini kontrol eden sınıf.
 * Şarj, tüketim ve düşük batarya uyarısı işlemlerini yönetir.
 */
public class BataryaKontrolcu {

    private static final double DUSUK_BATARYA_ESIGI  = 20.0;
    private static final double KRITIK_BATARYA_ESIGI = 10.0;
    private static final double HAREKET_TUKETIMI     = 0.5;  // her adımda tüketim

    private final Robot robot;
    private boolean sarjIstasyonundaMi = false;

    public BataryaKontrolcu(Robot robot) {
        this.robot = robot;
    }

    /**
     * Her hareket adımında standart batarya tüketimini uygular.
     */
    public void hareketTuketimUygula() {
        robot.bataryaAzalt(HAREKET_TUKETIMI);
    }

    /**
     * Batarya düşük mü kontrol eder (şarj istasyonuna dönmeli mi?).
     */
    public boolean donmesiGerekiyorMu() {
        return robot.getBataryaYuzdesi() <= DUSUK_BATARYA_ESIGI;
    }

    /**
     * Batarya kritik seviyede mi (robot durmalı mı?).
     */
    public boolean kritikMi() {
        return robot.getBataryaYuzdesi() <= KRITIK_BATARYA_ESIGI;
    }

    /**
     * Robotu şarj eder (şarj istasyonuna ulaştığında çağrılır).
     */
    public void sarjEt() {
        robot.bataryaSarjEt();
        sarjIstasyonundaMi = true;
    }

    /**
     * Şarj istasyonundan ayrılır.
     */
    public void istasyondanAyril() {
        sarjIstasyonundaMi = false;
    }

    /**
     * Bataryayı manuel olarak belirli bir yüzdeye ayarlar.
     * @param yuzde 0-100 arası değer
     */
    public void bataryaAyarla(double yuzde) {
        robot.setBatarya(yuzde);
    }

    /**
     * Batarya durumunu metin olarak döner (UI için).
     */
    public String bataryaDurumMetni() {
        double yuzde = robot.getBataryaYuzdesi();
        if (yuzde <= KRITIK_BATARYA_ESIGI) return "KRİTİK";
        if (yuzde <= DUSUK_BATARYA_ESIGI) return "DÜŞÜK";
        if (yuzde <= 50) return "ORTA";
        return "İYİ";
    }

    public boolean isSarjIstasyonundaMi() {
        return sarjIstasyonundaMi;
    }

    public double getDusukBataryaEsigi() {
        return DUSUK_BATARYA_ESIGI;
    }
}
