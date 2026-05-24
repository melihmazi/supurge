package com.supurge.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Odanın grid yapısını temsil eden model sınıfı.
 */
public class Oda {

    private int genislik;
    private int yukseklik;
    private Hucre[][] grid;
    private int sarjIstasyonuX;
    private int sarjIstasyonuY;

    public Oda(int genislik, int yukseklik) {
        this.genislik = genislik;
        this.yukseklik = yukseklik;
        this.grid = new Hucre[yukseklik][genislik];
        gridOlustur();
        // Şarj istasyonu varsayılan olarak sol üst köşe
        this.sarjIstasyonuX = 0;
        this.sarjIstasyonuY = 0;
        grid[0][0].setSarjIstasyonu(true);
    }

    private void gridOlustur() {
        for (int y = 0; y < yukseklik; y++) {
            for (int x = 0; x < genislik; x++) {
                grid[y][x] = new Hucre(x, y);
            }
        }
    }

    public Hucre getHucre(int x, int y) {
        if (gecerliMi(x, y)) return grid[y][x];
        return null;
    }

    public boolean gecerliMi(int x, int y) {
        return x >= 0 && x < genislik && y >= 0 && y < yukseklik;
    }

    public boolean engelMi(int x, int y) {
        if (!gecerliMi(x, y)) return true; // sınır dışı = engel
        return grid[y][x].isEngel();
    }

    public void engelEkle(int x, int y) {
        if (gecerliMi(x, y)) grid[y][x].setEngel(true);
    }

    public void kirEkle(int x, int y, KirTuru kirTuru) {
        if (gecerliMi(x, y) && !grid[y][x].isEngel()) {
            grid[y][x].setKirTuru(kirTuru);
            grid[y][x].setTemizlendi(false);
        }
    }

    public int toplamHucreSayisi() {
        int sayac = 0;
        for (int y = 0; y < yukseklik; y++)
            for (int x = 0; x < genislik; x++)
                if (!grid[y][x].isEngel()) sayac++;
        return sayac;
    }

    public int temizlenenHucreSayisi() {
        int sayac = 0;
        for (int y = 0; y < yukseklik; y++)
            for (int x = 0; x < genislik; x++)
                if (grid[y][x].isTemizlendi()) sayac++;
        return sayac;
    }

    public List<Hucre> kirliHucreleriGetir() {
        List<Hucre> kirlilar = new ArrayList<>();
        for (int y = 0; y < yukseklik; y++)
            for (int x = 0; x < genislik; x++)
                if (grid[y][x].isKirli()) kirlilar.add(grid[y][x]);
        return kirlilar;
    }

    public void sifirla() {
        gridOlustur();
        grid[sarjIstasyonuY][sarjIstasyonuX].setSarjIstasyonu(true);
    }

    // Getters
    public int getGenislik() { return genislik; }
    public int getYukseklik() { return yukseklik; }
    public Hucre[][] getGrid() { return grid; }
    public int getSarjIstasyonuX() { return sarjIstasyonuX; }
    public int getSarjIstasyonuY() { return sarjIstasyonuY; }
}
