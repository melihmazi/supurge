package com.supurge.model;

/**
 * Odanın grid yapısındaki her bir hücreyi temsil eder.
 */
public class Hucre {

    private int x;
    private int y;
    private boolean engel;          // mobilya/duvar var mı
    private boolean temizlendi;     // temizlendi mi
    private KirTuru kirTuru;        // kir türü (null ise temiz)
    private boolean sarjIstasyonu;  // şarj istasyonu mu

    public Hucre(int x, int y) {
        this.x = x;
        this.y = y;
        this.engel = false;
        this.temizlendi = false;
        this.kirTuru = null;
        this.sarjIstasyonu = false;
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

    public KirTuru getKirTuru() { return kirTuru; }
    public void setKirTuru(KirTuru kirTuru) { this.kirTuru = kirTuru; }

    public boolean isSarjIstasyonu() { return sarjIstasyonu; }
    public void setSarjIstasyonu(boolean sarjIstasyonu) { this.sarjIstasyonu = sarjIstasyonu; }
}
