package com.supurge.view;

import com.supurge.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;

/**
 * Oda grid yapısını JavaFX Canvas üzerinde çizen View sınıfı.
 * Görsel olarak zenginleştirilmiş: karo zemin, mobilya çizimleri,
 * koordinat numaraları, robot animasyonu.
 */
public class OdaGorunumu extends Canvas {

    private static final int HUCRE_BOYUTU = 48;
    private static final int KENAR_BOSLUGU = 28; // koordinat numaraları için

    private final Oda oda;
    private final Robot robot;

    public OdaGorunumu(Oda oda, Robot robot) {
        super(oda.getGenislik() * HUCRE_BOYUTU + KENAR_BOSLUGU,
              oda.getYukseklik() * HUCRE_BOYUTU + KENAR_BOSLUGU);
        this.oda = oda;
        this.robot = robot;
        yenidenCiz();
    }

    public void guncelle(SimulasyonDurumu durum) {
        yenidenCiz();
    }

    public void yenidenCiz() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        koordinatlarCiz(gc);
        hucreleriCiz(gc);
        yoluCiz(gc);
        robotuCiz(gc);
    }

    // ---- Koordinat numaraları (üst + sol) ----
    private void koordinatlarCiz(GraphicsContext gc) {
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        gc.setFill(Color.rgb(180, 180, 180));
        gc.setTextAlign(TextAlignment.CENTER);

        // Üst: sütun numaraları
        for (int x = 0; x < oda.getGenislik(); x++) {
            double px = KENAR_BOSLUGU + x * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0;
            gc.fillText(String.valueOf(x), px, 14);
        }
        // Sol: satır numaraları
        gc.setTextAlign(TextAlignment.RIGHT);
        for (int y = 0; y < oda.getYukseklik(); y++) {
            double py = KENAR_BOSLUGU + y * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0 + 4;
            gc.fillText(String.valueOf(y), KENAR_BOSLUGU - 4, py);
        }
    }

    // ---- Hücreler ----
    private void hucreleriCiz(GraphicsContext gc) {
        Hucre[][] grid = oda.getGrid();
        for (int y = 0; y < oda.getYukseklik(); y++) {
            for (int x = 0; x < oda.getGenislik(); x++) {
                Hucre h = grid[y][x];
                double px = KENAR_BOSLUGU + x * HUCRE_BOYUTU;
                double py = KENAR_BOSLUGU + y * HUCRE_BOYUTU;

                if (h.isEngel()) {
                    mobilyaCiz(gc, px, py);
                } else if (h.isSarjIstasyonu()) {
                    karoZeminCiz(gc, px, py, false);
                    sarjIstasyonuCiz(gc, px, py);
                } else if (h.isTemizlendi()) {
                    karoZeminCiz(gc, px, py, true);
                } else if (h.isKirli()) {
                    karoZeminCiz(gc, px, py, false);
                    kirCiz(gc, px, py, h.getKirTuru());
                } else {
                    karoZeminCiz(gc, px, py, false);
                }

                // Grid çizgisi
                gc.setStroke(Color.rgb(200, 185, 160, 0.4));
                gc.setLineWidth(0.5);
                gc.strokeRect(px, py, HUCRE_BOYUTU, HUCRE_BOYUTU);
            }
        }
    }

    // Karo zemin deseni
    private void karoZeminCiz(GraphicsContext gc, double px, double py, boolean temiz) {
        if (temiz) {
            // Temizlenmiş hücre - açık mavi ton
            gc.setFill(Color.rgb(200, 225, 245));
            gc.fillRect(px, py, HUCRE_BOYUTU, HUCRE_BOYUTU);
            gc.setFill(Color.rgb(185, 210, 230, 0.5));
            gc.fillRect(px + 2, py + 2, HUCRE_BOYUTU / 2 - 3, HUCRE_BOYUTU / 2 - 3);
            gc.fillRect(px + HUCRE_BOYUTU / 2 + 1, py + HUCRE_BOYUTU / 2 + 1,
                        HUCRE_BOYUTU / 2 - 3, HUCRE_BOYUTU / 2 - 3);
        } else {
            // Normal karo - bej/krem rengi
            gc.setFill(Color.rgb(235, 220, 195));
            gc.fillRect(px, py, HUCRE_BOYUTU, HUCRE_BOYUTU);
            // Karo iç deseni
            gc.setFill(Color.rgb(225, 208, 180, 0.6));
            gc.fillRect(px + 2, py + 2, HUCRE_BOYUTU / 2 - 3, HUCRE_BOYUTU / 2 - 3);
            gc.fillRect(px + HUCRE_BOYUTU / 2 + 1, py + HUCRE_BOYUTU / 2 + 1,
                        HUCRE_BOYUTU / 2 - 3, HUCRE_BOYUTU / 2 - 3);
        }
    }

    // Mobilya çizimi - türe göre farklı görünüm
    private void mobilyaCiz(GraphicsContext gc, double px, double py) {
        // Ahşap rengi arka plan
        gc.setFill(Color.rgb(139, 100, 60));
        gc.fillRect(px, py, HUCRE_BOYUTU, HUCRE_BOYUTU);

        // Mobilya detayı - koyu ahşap desen
        gc.setFill(Color.rgb(120, 82, 45));
        gc.fillRect(px + 3, py + 3, HUCRE_BOYUTU - 6, HUCRE_BOYUTU - 6);

        // Parlama efekti
        gc.setFill(Color.rgb(180, 140, 90, 0.3));
        gc.fillRect(px + 5, py + 5, HUCRE_BOYUTU / 3, HUCRE_BOYUTU / 3);

        // Mobilya ikonu
        gc.setFill(Color.rgb(200, 170, 120));
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("🛋", px + HUCRE_BOYUTU / 2.0, py + HUCRE_BOYUTU / 2.0 + 6);
    }

    // Şarj istasyonu çizimi
    private void sarjIstasyonuCiz(GraphicsContext gc, double px, double py) {
        // Siyah kutu
        gc.setFill(Color.rgb(30, 30, 35));
        gc.fillRoundRect(px + 4, py + 4, HUCRE_BOYUTU - 8, HUCRE_BOYUTU - 8, 6, 6);

        // Yeşil şarj ışığı
        gc.setFill(Color.rgb(0, 220, 100));
        gc.fillOval(px + HUCRE_BOYUTU / 2.0 - 10, py + HUCRE_BOYUTU / 2.0 - 10, 20, 20);

        // Şimşek ikonu
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("⚡", px + HUCRE_BOYUTU / 2.0, py + HUCRE_BOYUTU / 2.0 + 5);
    }

    // Kir çizimi - türe göre farklı nokta deseni
    private void kirCiz(GraphicsContext gc, double px, double py, KirTuru kirTuru) {
        switch (kirTuru) {
            case TOZ -> {
                // Küçük gri noktalar
                gc.setFill(Color.rgb(150, 140, 130, 0.8));
                double[] noktaX = {8, 15, 25, 12, 30, 20, 35, 10, 28};
                double[] noktaY = {10, 20, 8, 32, 25, 38, 15, 40, 40};
                for (int i = 0; i < noktaX.length; i++) {
                    double r = (i % 3 == 0) ? 2.5 : 1.5;
                    gc.fillOval(px + noktaX[i], py + noktaY[i], r * 2, r * 2);
                }
            }
            case SIVI -> {
                // Mavi damlalar
                gc.setFill(Color.rgb(70, 130, 200, 0.7));
                gc.fillOval(px + 10, py + 12, 10, 14);
                gc.fillOval(px + 26, py + 8, 8, 11);
                gc.fillOval(px + 18, py + 28, 12, 16);
                gc.setFill(Color.rgb(120, 170, 230, 0.5));
                gc.fillOval(px + 12, py + 14, 4, 5);
            }
            case LEKE -> {
                // Kahverengi leke
                gc.setFill(Color.rgb(120, 70, 30, 0.75));
                gc.fillOval(px + 8, py + 10, 18, 14);
                gc.fillOval(px + 20, py + 20, 14, 12);
                gc.fillOval(px + 12, py + 26, 16, 10);
            }
        }
    }

    // ---- Hareket yolu ----
    private void yoluCiz(GraphicsContext gc) {
        List<int[]> yol = robot.getHareketYolu();
        if (yol.size() < 2) return;

        gc.setStroke(Color.rgb(80, 160, 255, 0.7));
        gc.setLineWidth(2.0);
        gc.setLineDashes(6, 4);

        for (int i = 1; i < yol.size(); i++) {
            int[] onceki = yol.get(i - 1);
            int[] mevcut = yol.get(i);
            double x1 = KENAR_BOSLUGU + onceki[0] * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0;
            double y1 = KENAR_BOSLUGU + onceki[1] * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0;
            double x2 = KENAR_BOSLUGU + mevcut[0] * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0;
            double y2 = KENAR_BOSLUGU + mevcut[1] * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0;
            gc.strokeLine(x1, y1, x2, y2);

            // Ok başı
            if (i % 3 == 0) {
                okBasiCiz(gc, x1, y1, x2, y2);
            }
        }
        gc.setLineDashes(null);
        gc.setLineWidth(1);
    }

    private void okBasiCiz(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double okBoyutu = 7;
        double okAcisi = Math.PI / 6;

        double ax = x2 - okBoyutu * Math.cos(angle - okAcisi);
        double ay = y2 - okBoyutu * Math.sin(angle - okAcisi);
        double bx = x2 - okBoyutu * Math.cos(angle + okAcisi);
        double by = y2 - okBoyutu * Math.sin(angle + okAcisi);

        gc.setFill(Color.rgb(80, 160, 255, 0.8));
        gc.fillPolygon(new double[]{x2, ax, bx}, new double[]{y2, ay, by}, 3);
    }

    // ---- Robot çizimi ----
    private void robotuCiz(GraphicsContext gc) {
        double px = KENAR_BOSLUGU + robot.getX() * HUCRE_BOYUTU;
        double py = KENAR_BOSLUGU + robot.getY() * HUCRE_BOYUTU;
        double cx = px + HUCRE_BOYUTU / 2.0;
        double cy = py + HUCRE_BOYUTU / 2.0;
        double r = HUCRE_BOYUTU / 2.0 - 4;

        // Gölge
        gc.setFill(Color.rgb(0, 0, 0, 0.25));
        gc.fillOval(cx - r + 3, cy - r + 3, r * 2, r * 2);

        // Dış halka - koyu gri
        gc.setFill(Color.rgb(50, 55, 65));
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);

        // İç daire - açık gri
        gc.setFill(Color.rgb(230, 235, 240));
        gc.fillOval(cx - r + 3, cy - r + 3, (r - 3) * 2, (r - 3) * 2);

        // Merkez düğme
        gc.setFill(Color.rgb(80, 90, 100));
        gc.fillOval(cx - 5, cy - 5, 10, 10);

        // Sensör noktaları
        gc.setFill(Color.rgb(50, 55, 65));
        gc.fillOval(cx - r + 5, cy - 3, 5, 6);
        gc.fillOval(cx + r - 10, cy - 3, 5, 6);

        // Yön göstergesi
        Yon yon = robot.getYon();
        if (yon != null) {
            double dx = yon.getDx() * (r - 6);
            double dy = yon.getDy() * (r - 6);
            gc.setFill(Color.rgb(0, 200, 120));
            gc.fillOval(cx + dx - 3, cy + dy - 3, 6, 6);
        }

        // Batarya göstergesi üstte
        double batarya = robot.getBataryaYuzdesi();
        String bataryaMetin = String.format("%.0f%%", batarya);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.setTextAlign(TextAlignment.CENTER);

        // Batarya arka plan
        gc.setFill(Color.rgb(30, 30, 30, 0.75));
        gc.fillRoundRect(cx - 18, py - 18, 36, 14, 4, 4);

        // Batarya rengi
        Color bataryaRenk = batarya > 50 ? Color.rgb(80, 200, 80)
                          : batarya > 20 ? Color.rgb(255, 165, 0)
                          : Color.rgb(220, 50, 50);
        gc.setFill(bataryaRenk);
        gc.fillText(bataryaMetin, cx, py - 7);
    }

    public int getHucreBoyutu() { return HUCRE_BOYUTU; }
    public int getKenarBoslugu() { return KENAR_BOSLUGU; }
}
