package com.supurge.model;

/* kir türlerini ve özellkilerini tanımlar
    her türün temizleme süresi ve batarya tüketimi farklıdır. */

public enum KirTuru {
    TOZ(1, 0.5),    // Toz, hızlı temizleme - az batarya
    SIVI(3, 1.5),   // Sıvı, orta süre - orta batarya
    LEKE(5, 2.5);   // Leke, yavaş temizlenir - çok batarya

    private final int temizlemeSuresi;      //temizleme adımı
    private final double bataryaTuketimi;   //batarya tüketimi

    KirTuru(int temizlemeSuresi, double bataryaTuketimi) {
        this.temizlemeSuresi = temizlemeSuresi;
        this.bataryaTuketimi = bataryaTuketimi;
    }

    public int getTemizlemeSuresi() { return temizlemeSuresi; }
    public double getBataryaTuketimi() { return bataryaTuketimi; }
}
