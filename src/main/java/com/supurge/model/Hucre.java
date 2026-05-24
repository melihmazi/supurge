package com.supurge.model;

/**
 * Odanın grid yapısındaki her bir hücreyi temsil eder.
 */
public class Hucre {

    private int x;
    private int y;
    private boolean engel;          // mobilya/duvar var mı
    private boolean temizlendi;     // kirli hücre temizlendi mi (kir vardı ve silindi)
    private boolean ziyaretEdildi;  // robot bu hücreden geçti mi
    private KirTuru kirTuru;        // kir türü (null ise temiz)
    private boolean sarjIstasyonu;  // şarj istasyonu mu
    private int mobilyaTuru;        // mobilya türü (0-5)

    public Hucre(int x, int y) {
        this.x = x;
        this.y = y;
        this.engel = false;
        this.temizlendi = false;
        this.ziyaretEdildi = false;
        this.kirTuru = null;
        this.sarjIstasyonu = false;
        this.mobilyaTuru = 0;
    }

    public boolean isKirli() {
        return kirTuru != null;
    }

    // Getters & Setters
    public int getX() { return x; }
    public int getY() { return y; }

    public boolean isEngel() { return engel; }
    public void setEngel(boolean engel) { this.engel = engel; }

    public boolean isTemizlendi() { return temizlendi; }
    public void setTemizlendi(boolean temizlendi) { this.temizlendi = temizlendi; }

    public boolean isZiyaretEdildi() { return ziyaretEdildi; }
    public void setZiyaretEdildi(boolean ziyaretEdildi) { this.ziyaretEdildi = ziyaretEdildi; }

    /** Hücre işlendi mi? Kirli hücre temizlendiyse veya kirli değilse ziyaret edildi. */
    public boolean isIslendi() { return temizlendi || (ziyaretEdildi && kirTuru == null); }

    public KirTuru getKirTuru() { return kirTuru; }
    public void setKirTuru(KirTuru kirTuru) { this.kirTuru = kirTuru; }

    public boolean isSarjIstasyonu() { return sarjIstasyonu; }
    public void setSarjIstasyonu(boolean sarjIstasyonu) { this.sarjIstasyonu = sarjIstasyonu; }

    public int getMobilyaTuru() { return mobilyaTuru; }
    public void setMobilyaTuru(int mobilyaTuru) { this.mobilyaTuru = mobilyaTuru; }
}
