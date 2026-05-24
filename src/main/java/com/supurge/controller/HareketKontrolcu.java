package com.supurge.controller;

import com.supurge.model.*;

import java.util.*;
/**
 * Robotun hareket algoritmalarını yöneten controller.
 * Rastgele, Spiral ve Duvar Takip algoritmalarını uygular.
 */
public class HareketKontrolcu {

    private final Oda oda;
    private final Robot robot;
    private final BataryaKontrolcu bataryaKontrolcu;
    private final Random rastgele = new Random();

    // Spiral algoritması için adım sayacı
    private int spiralAdim = 0;
    private int spiralBoyut = 1;
    private int spiralTurSayaci = 0;

    public HareketKontrolcu(Oda oda, Robot robot, BataryaKontrolcu bataryaKontrolcu) {
        this.oda = oda;
        this.robot = robot;
        this.bataryaKontrolcu = bataryaKontrolcu;
    }

    /**
     * Seçili algoritmaya göre bir adım hareket ettirir.
     * Her başarılı harekette batarya tüketimi uygulanır.
     * @return hareket gerçekleştiyse true
     */
    public boolean hareketEt() {
        boolean hareket = switch (robot.getAlgoritma()) {
            case RASTGELE    -> rastgeleHareketEt();
            case SPIRAL      -> spiralHareketEt();
            case DUVAR_TAKIP -> duvarTakipHareketEt();
        };
        if (hareket) bataryaKontrolcu.hareketTuketimUygula();
        return hareket;
    }

    /**
     * Rastgele hareket: geçerli komşu hücrelerden birini seçer.
     * Kirli hücrelere öncelik verir.
     */
    private boolean rastgeleHareketEt() {
        List<Yon> gecerliYonler = gecerliYonleriGetir();
        if (gecerliYonler.isEmpty()) return false;

        // Kirli hücreye öncelik ver
        for (Yon yon : gecerliYonler) {
            int nx = robot.getX() + yon.getDx();
            int ny = robot.getY() + yon.getDy();
            Hucre h = oda.getHucre(nx, ny);
            if (h != null && h.isKirli()) {
                robot.setYon(yon);
                robot.hareketEt(nx, ny);
                return true;
            }
        }

        // Kirli yoksa rastgele git
        Yon secilen = gecerliYonler.get(rastgele.nextInt(gecerliYonler.size()));
        robot.setYon(secilen);
        robot.hareketEt(robot.getX() + secilen.getDx(), robot.getY() + secilen.getDy());
        return true;
    }

    /**
     * Spiral hareket: giderek genişleyen spiral çizer.
     * Engele çarparsa sağa döner.
     */
    private boolean spiralHareketEt() {
        int yeniX = robot.getX() + robot.getYon().getDx();
        int yeniY = robot.getY() + robot.getYon().getDy();

        if (!oda.engelMi(yeniX, yeniY)) {
            robot.hareketEt(yeniX, yeniY);
            spiralAdim++;

            // Belirli adımda yön değiştir (spiral genişlemesi)
            if (spiralAdim >= spiralBoyut) {
                spiralAdim = 0;
                spiralTurSayaci++;
                if (spiralTurSayaci % 2 == 0) spiralBoyut++;
                sagaDon();
            }
            return true;
        } else {
            // Engele çarptı, sağa dön
            sagaDon();
            spiralAdim = 0;
            // Yeni yönde de engel varsa rastgele dene
            int nx = robot.getX() + robot.getYon().getDx();
            int ny = robot.getY() + robot.getYon().getDy();
            if (!oda.engelMi(nx, ny)) {
                robot.hareketEt(nx, ny);
                return true;
            }
            return rastgeleHareketEt();
        }
    }

    /**
     * Duvar takip (sol el kuralı):
     * Önce sola dön, düz git, sağa dön, geri dön sırasıyla dener.
     */
    private boolean duvarTakipHareketEt() {
        // Sol el kuralı: sol → düz → sağ → geri
        Yon sol  = solaYon(robot.getYon());
        Yon duz  = robot.getYon();
        Yon sag  = sagaYon(robot.getYon());
        Yon geri = geriYon(robot.getYon());

        for (Yon deneme : new Yon[]{sol, duz, sag, geri}) {
            int nx = robot.getX() + deneme.getDx();
            int ny = robot.getY() + deneme.getDy();
            if (!oda.engelMi(nx, ny)) {
                robot.setYon(deneme);
                robot.hareketEt(nx, ny);
                return true;
            }
        }
        return false; // tamamen sıkışmış
    }

    // ---- Yardımcı Yön Metodları ----

    private void sagaDon() {
        robot.setYon(sagaYon(robot.getYon()));
    }

    private Yon sagaYon(Yon yon) {
        return switch (yon) {
            case KUZEY -> Yon.DOGU;
            case DOGU  -> Yon.GUNEY;
            case GUNEY -> Yon.BATI;
            case BATI  -> Yon.KUZEY;
        };
    }

    private Yon solaYon(Yon yon) {
        return switch (yon) {
            case KUZEY -> Yon.BATI;
            case BATI  -> Yon.GUNEY;
            case GUNEY -> Yon.DOGU;
            case DOGU  -> Yon.KUZEY;
        };
    }

    private Yon geriYon(Yon yon) {
        return switch (yon) {
            case KUZEY -> Yon.GUNEY;
            case GUNEY -> Yon.KUZEY;
            case DOGU  -> Yon.BATI;
            case BATI  -> Yon.DOGU;
        };
    }

    private List<Yon> gecerliYonleriGetir() {
        List<Yon> liste = new ArrayList<>();
        for (Yon yon : Yon.values()) {
            int nx = robot.getX() + yon.getDx();
            int ny = robot.getY() + yon.getDy();
            if (!oda.engelMi(nx, ny)) liste.add(yon);
        }
        return liste;
    }

    /** Spiral sayaçlarını sıfırlar */
    public void sifirla() {
        spiralAdim = 0;
        spiralBoyut = 1;
        spiralTurSayaci = 0;
    }
}
