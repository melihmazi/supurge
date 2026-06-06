package com.supurge.model;

import java.util.ArrayList;
import java.util.List;

/* süpürge sınıfı temsil eden */
public class Robot {

    private int x;
    private int y;
    private double batarya;
    private static final double MAX_BATARYA = 100.0;
    private Yon yon;
    private double hiz;                             // hareket hızı
    private TemizlemeAlgoritması algoritma;
    private boolean calisiyor;
    private List<int[]> hareketYolu;                // geçilen hücrelerin koordinatları

    public Robot(int baslangicX, int baslangicY) {
        this.x = baslangicX;
        this.y = baslangicY;
        this.batarya = MAX_BATARYA;
        this.yon = Yon.DOGU;
        this.hiz = 1.0;
        this.algoritma = TemizlemeAlgoritması.SPIRAL;
        this.calisiyor = false;
        this.hareketYolu = new ArrayList<>();
    }

    public void bataryaAzalt(double miktar) {
        batarya = Math.max(0, batarya - miktar);
    }

    public void bataryaSarjEt() {
        batarya = MAX_BATARYA;
    }

    /* robotu belirtilen konuma taşır.
      batarya tüketimi BataryaKontrolcu tarafından yönetilir.
     */
    public void hareketEt(int yeniX, int yeniY) {
        this.x = yeniX;
        this.y = yeniY;
        hareketYolu.add(new int[]{yeniX, yeniY});
    }

    public double getBataryaYuzdesi() {
        return (batarya / MAX_BATARYA) * 100;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public double getBatarya() { return batarya; }
    public void setBatarya(double batarya) { this.batarya = Math.min(MAX_BATARYA, Math.max(0, batarya)); }

    public Yon getYon() { return yon; }
    public void setYon(Yon yon) { this.yon = yon; }

    public double getHiz() { return hiz; }
    public void setHiz(double hiz) { this.hiz = hiz; }

    public TemizlemeAlgoritması getAlgoritma() { return algoritma; }
    public void setAlgoritma(TemizlemeAlgoritması algoritma) { this.algoritma = algoritma; }

    public boolean isCalisiyor() { return calisiyor; }
    public void setCalisiyor(boolean calisiyor) { this.calisiyor = calisiyor; }

    public List<int[]> getHareketYolu() { return hareketYolu; }

    public void yoluTemizle() { hareketYolu.clear(); }
}
