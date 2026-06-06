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
 * Üstten bakış perspektifi, gölge ve doku detaylarıyla gerçekçi mobilyalar.
 */
public class OdaGorunumu extends Canvas {

    private static final int HUCRE_BOYUTU  = 48;
    private static final int KENAR_BOSLUGU = 28;

    private final Oda   oda;
    private final Robot robot;

    public OdaGorunumu(Oda oda, Robot robot) {
        super(oda.getGenislik() * HUCRE_BOYUTU + KENAR_BOSLUGU,
              oda.getYukseklik() * HUCRE_BOYUTU + KENAR_BOSLUGU);
        this.oda   = oda;
        this.robot = robot;
        yenidenCiz();
    }

    public void guncelle(SimulasyonDurumu durum) { yenidenCiz(); }

    public void yenidenCiz() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        koordinatlarCiz(gc);
        hucreleriCiz(gc);
        yoluCiz(gc);
        robotuCiz(gc);
    }

    // =========================================================
    // Koordinat numaraları
    // =========================================================
    private void koordinatlarCiz(GraphicsContext gc) {
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        gc.setFill(Color.rgb(160, 160, 160));
        gc.setTextAlign(TextAlignment.CENTER);
        for (int x = 0; x < oda.getGenislik(); x++) {
            double px = KENAR_BOSLUGU + x * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0;
            gc.fillText(String.valueOf(x), px, 14);
        }
        gc.setTextAlign(TextAlignment.RIGHT);
        for (int y = 0; y < oda.getYukseklik(); y++) {
            double py = KENAR_BOSLUGU + y * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0 + 4;
            gc.fillText(String.valueOf(y), KENAR_BOSLUGU - 4, py);
        }
    }

    // =========================================================
    // Hücreler
    // =========================================================
    private void hucreleriCiz(GraphicsContext gc) {
        Hucre[][] grid = oda.getGrid();
        for (int y = 0; y < oda.getYukseklik(); y++) {
            for (int x = 0; x < oda.getGenislik(); x++) {
                Hucre h = grid[y][x];
                double px = KENAR_BOSLUGU + x * HUCRE_BOYUTU;
                double py = KENAR_BOSLUGU + y * HUCRE_BOYUTU;

                if (h.isEngel()) {
                    karoZeminCiz(gc, px, py, false);
                    mobilyaCiz(gc, px, py, x, y);
                } else if (h.isSarjIstasyonu()) {
                    karoZeminCiz(gc, px, py, false);
                    sarjIstasyonuCiz(gc, px, py);
                } else if (h.isZiyaretEdildi()) {
                    karoZeminCiz(gc, px, py, true);
                    if (h.isKirli()) kirCiz(gc, px, py, h.getKirTuru()); // kirli ama ziyaret edilmiş
                } else if (h.isKirli()) {
                    karoZeminCiz(gc, px, py, false);
                    kirCiz(gc, px, py, h.getKirTuru());
                } else {
                    karoZeminCiz(gc, px, py, false);
                }

                gc.setStroke(Color.rgb(200, 185, 160, 0.3));
                gc.setLineWidth(0.5);
                gc.strokeRect(px, py, HUCRE_BOYUTU, HUCRE_BOYUTU);
            }
        }
    }

    // =========================================================
    // Karo zemin
    // =========================================================
    private void karoZeminCiz(GraphicsContext gc, double px, double py, boolean temiz) {
        int H = HUCRE_BOYUTU;
        if (temiz) {
            gc.setFill(Color.rgb(190, 218, 238));
            gc.fillRect(px, py, H, H);
            gc.setFill(Color.rgb(170, 200, 225, 0.5));
        } else {
            gc.setFill(Color.rgb(230, 216, 190));
            gc.fillRect(px, py, H, H);
            gc.setFill(Color.rgb(215, 198, 168, 0.5));
        }
        int h2 = H / 2;
        gc.fillRect(px + 2,      py + 2,      h2 - 3, h2 - 3);
        gc.fillRect(px + h2 + 1, py + h2 + 1, h2 - 3, h2 - 3);
    }



    // =========================================================
    // Mobilya yönlendirici
    // =========================================================
    private void mobilyaCiz(GraphicsContext gc, double px, double py, int gx, int gy) {
        int tip = oda.getGrid()[gy][gx].getMobilyaTuru();
        switch (tip) {
            case 0 -> koltukCiz(gc, px, py);
            case 1 -> sehpaCiz(gc, px, py);
            case 2 -> masaCiz(gc, px, py);
            case 3 -> saksiCiz(gc, px, py);
            case 4 -> kitaplikCiz(gc, px, py);
            default -> sandalyeCiz(gc, px, py);
        }
    }

    // =========================================================
    // KOLTUK — üstten bakış, iki yastıklı kanepe görünümü
    // =========================================================
    private void koltukCiz(GraphicsContext gc, double px, double py) {
        int H = HUCRE_BOYUTU;
        // Zemin rengi (kumaş)
        Color ana   = Color.rgb(188, 152, 108);
        Color koyu  = Color.rgb(155, 120, 78);
        Color acik  = Color.rgb(215, 185, 145);
        Color golge = Color.rgb(120, 90, 55, 0.5);

        // Sırtlık (arka panel)
        gc.setFill(koyu);
        gc.fillRoundRect(px + 3, py + 2, H - 6, 13, 5, 5);
        // Sırtlık üst kenar vurgusu
        gc.setFill(acik);
        gc.fillRoundRect(px + 4, py + 3, H - 8, 4, 3, 3);

        // Sol kol
        gc.setFill(koyu);
        gc.fillRoundRect(px + 2, py + 12, 9, H - 18, 4, 4);
        gc.setFill(acik);
        gc.fillRoundRect(px + 3, py + 13, 5, 6, 2, 2);

        // Sağ kol
        gc.setFill(koyu);
        gc.fillRoundRect(px + H - 11, py + 12, 9, H - 18, 4, 4);
        gc.setFill(acik);
        gc.fillRoundRect(px + H - 10, py + 13, 5, 6, 2, 2);

        // Oturma alanı
        gc.setFill(ana);
        gc.fillRoundRect(px + 11, py + 12, H - 22, H - 18, 4, 4);

        // Sol yastık
        gc.setFill(acik);
        gc.fillRoundRect(px + 12, py + 14, (H - 24) / 2 - 1, H - 24, 4, 4);
        gc.setFill(golge);
        gc.fillRoundRect(px + 12 + (H - 24) / 2 - 2, py + 14, 2, H - 24, 1, 1);
        // Yastık dikiş çizgisi
        gc.setStroke(Color.rgb(140, 105, 65, 0.6));
        gc.setLineWidth(0.8);
        gc.strokeLine(px + 12 + (H - 24) / 4.0, py + 16,
                      px + 12 + (H - 24) / 4.0, py + H - 12);

        // Sağ yastık
        double yastikX = px + 12 + (H - 24) / 2 + 1;
        gc.setFill(acik);
        gc.fillRoundRect(yastikX, py + 14, (H - 24) / 2 - 1, H - 24, 4, 4);
        gc.setStroke(Color.rgb(140, 105, 65, 0.6));
        gc.strokeLine(yastikX + (H - 24) / 4.0, py + 16,
                      yastikX + (H - 24) / 4.0, py + H - 12);
        gc.setLineWidth(1);
    }

    // =========================================================
    // SEHPA — yuvarlak cam üstlü orta sehpa
    // =========================================================
    private void sehpaCiz(GraphicsContext gc, double px, double py) {
        int H = HUCRE_BOYUTU;
        double cx = px + H / 2.0;
        double cy = py + H / 2.0;

        // Ayaklar (4 köşe)
        gc.setFill(Color.rgb(80, 52, 25));
        gc.fillRoundRect(px + 6,      py + 6,      6, 6, 2, 2);
        gc.fillRoundRect(px + H - 12, py + 6,      6, 6, 2, 2);
        gc.fillRoundRect(px + 6,      py + H - 12, 6, 6, 2, 2);
        gc.fillRoundRect(px + H - 12, py + H - 12, 6, 6, 2, 2);

        // Masa yüzeyi (ahşap)
        gc.setFill(Color.rgb(160, 110, 58));
        gc.fillOval(cx - 14, cy - 14, 28, 28);

        // Ahşap desen halkası
        gc.setStroke(Color.rgb(130, 85, 38, 0.5));
        gc.setLineWidth(1);
        gc.strokeOval(cx - 10, cy - 10, 20, 20);
        gc.strokeOval(cx - 6,  cy - 6,  12, 12);


        // Kenar çizgisi
        gc.setStroke(Color.rgb(100, 65, 28, 0.8));
        gc.setLineWidth(1.5);
        gc.strokeOval(cx - 14, cy - 14, 28, 28);
        gc.setLineWidth(1);
    }

    // =========================================================
    // MASA — dikdörtgen çalışma masası, üstten bakış
    // =========================================================
    private void masaCiz(GraphicsContext gc, double px, double py) {
        int H = HUCRE_BOYUTU;

        // Masa gövdesi
        gc.setFill(Color.rgb(130, 85, 40));
        gc.fillRoundRect(px + 3, py + 3, H - 6, H - 6, 4, 4);

        // Ahşap doku çizgileri (yatay)
        gc.setStroke(Color.rgb(105, 65, 25, 0.4));
        gc.setLineWidth(0.8);
        for (int i = 1; i < 4; i++) {
            double ly = py + 3 + i * ((H - 6) / 4.0);
            gc.strokeLine(px + 5, ly, px + H - 5, ly);
        }

        // Masa kenar çerçevesi
        gc.setStroke(Color.rgb(85, 50, 18, 0.9));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(px + 3, py + 3, H - 6, H - 6, 4, 4);


        // Masa üstü nesne: küçük defter
        gc.setFill(Color.rgb(240, 240, 220));
        gc.fillRoundRect(px + 8, py + 10, 14, 10, 2, 2);
        gc.setStroke(Color.rgb(180, 180, 160, 0.8));
        gc.setLineWidth(0.6);
        gc.strokeLine(px + 10, py + 13, px + 20, py + 13);
        gc.strokeLine(px + 10, py + 16, px + 18, py + 16);

        // Kalem
        gc.setFill(Color.rgb(220, 80, 60));
        gc.fillRoundRect(px + H - 16, py + 8, 3, 16, 1, 1);
        gc.setFill(Color.rgb(255, 220, 100));
        gc.fillRect(px + H - 16, py + 8, 3, 4);
        gc.setLineWidth(1);
    }

    // =========================================================
    // SAKSI — büyük saksı bitkisi, üstten bakış
    // =========================================================
    private void saksiCiz(GraphicsContext gc, double px, double py) {
        int H = HUCRE_BOYUTU;
        double cx = px + H / 2.0;
        double cy = py + H / 2.0;

        // Saksı gövdesi (terracotta)
        gc.setFill(Color.rgb(175, 85, 45));
        gc.fillOval(cx - 13, cy - 8, 26, 20);

        // Saksı iç (toprak)
        gc.setFill(Color.rgb(85, 52, 25));
        gc.fillOval(cx - 10, cy - 5, 20, 14);

        // Saksı kenar vurgusu
        gc.setStroke(Color.rgb(140, 65, 30, 0.8));
        gc.setLineWidth(1.5);
        gc.strokeOval(cx - 13, cy - 8, 26, 20);

        // Yapraklar (üstten bakış — daire şeklinde yayılmış)
        Color yaprakAna  = Color.rgb(55, 140, 65);
        Color yaprakKoyu = Color.rgb(35, 105, 45);

        // 6 yaprak, etrafına yayılmış
        double[][] yapraklar = {
            {cx - 14, cy - 16, 12, 14},
            {cx + 2,  cy - 18, 12, 14},
            {cx + 10, cy - 8,  14, 12},
            {cx + 8,  cy + 4,  12, 12},
            {cx - 6,  cy + 6,  12, 12},
            {cx - 16, cy - 2,  14, 12}
        };
        for (double[] y : yapraklar) {
            gc.setFill(yaprakAna);
            gc.fillOval(y[0], y[1], y[2], y[3]);
        }
    }

    // =========================================================
    // KİTAPLIK — dolu raf, üstten bakış
    // =========================================================
    private void kitaplikCiz(GraphicsContext gc, double px, double py) {
        int H = HUCRE_BOYUTU;

        // Raf gövdesi (koyu ahşap)
        gc.setFill(Color.rgb(88, 58, 28));
        gc.fillRoundRect(px + 2, py + 2, H - 4, H - 4, 3, 3);

        // Raf bölücü (orta yatay çizgi)
        gc.setFill(Color.rgb(65, 40, 15));
        gc.fillRect(px + 2, py + H / 2 - 2, H - 4, 4);

        // Raf kenar çerçevesi
        gc.setStroke(Color.rgb(50, 30, 10, 0.9));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(px + 2, py + 2, H - 4, H - 4, 3, 3);

        // Üst raf kitapları
        Color[] ust = {
            Color.rgb(195, 55, 55),   // kırmızı
            Color.rgb(55, 105, 195),  // mavi
            Color.rgb(55, 160, 75),   // yeşil
            Color.rgb(210, 165, 35),  // sarı
            Color.rgb(155, 55, 165),  // mor
            Color.rgb(55, 165, 165),  // turkuaz
        };
        int rafBaslangic = (int)(px + 4);
        int rafBitis     = (int)(px + H - 4);
        int rafGenislik  = rafBitis - rafBaslangic;
        int kitapW       = rafGenislik / ust.length;
        for (int i = 0; i < ust.length; i++) {
            int kx = rafBaslangic + i * kitapW;
            int ky = (int)(py + 4);
            int kh = H / 2 - 8;
            // Kitap gövdesi
            gc.setFill(ust[i]);
            gc.fillRect(kx + 1, ky, kitapW - 2, kh);
            // Kitap sırt vurgusu
            gc.setFill(Color.rgb(255, 255, 255, 0.15));
            gc.fillRect(kx + 1, ky, 2, kh);
            // Kitap alt gölgesi
            gc.setFill(Color.rgb(0, 0, 0, 0.2));
            gc.fillRect(kx + 1, ky + kh - 2, kitapW - 2, 2);
        }

        // Alt raf kitapları (farklı boyutlar)
        Color[] alt = {
            Color.rgb(210, 100, 40),  // turuncu
            Color.rgb(100, 55, 185),  // mor
            Color.rgb(185, 185, 55),  // sarı-yeşil
            Color.rgb(55, 140, 185),  // açık mavi
            Color.rgb(185, 55, 100),  // pembe
        };
        int altKitapW = rafGenislik / alt.length;
        for (int i = 0; i < alt.length; i++) {
            int kx = rafBaslangic + i * altKitapW;
            int ky = (int)(py + H / 2 + 2);
            int kh = H / 2 - 8;
            int yukseklik = (i % 2 == 0) ? kh : kh - 3;
            gc.setFill(alt[i]);
            gc.fillRect(kx + 1, ky + (kh - yukseklik), altKitapW - 2, yukseklik);
            gc.setFill(Color.rgb(255, 255, 255, 0.15));
            gc.fillRect(kx + 1, ky + (kh - yukseklik), 2, yukseklik);
        }
        gc.setLineWidth(1);
    }

    // =========================================================
    // SANDALYE — üstten bakış, 4 ayaklı
    // =========================================================
    private void sandalyeCiz(GraphicsContext gc, double px, double py) {
        int H = HUCRE_BOYUTU;
        Color ahsap  = Color.rgb(160, 115, 60);
        Color koyu   = Color.rgb(120, 82, 35);
        Color acik   = Color.rgb(195, 155, 95);

        // 4 ayak
        gc.setFill(koyu);
        gc.fillRoundRect(px + 5,      py + H - 12, 5, 5, 2, 2);
        gc.fillRoundRect(px + H - 10, py + H - 12, 5, 5, 2, 2);
        gc.fillRoundRect(px + 5,      py + 14,     5, 5, 2, 2);
        gc.fillRoundRect(px + H - 10, py + 14,     5, 5, 2, 2);

        // Oturma yüzeyi
        gc.setFill(ahsap);
        gc.fillRoundRect(px + 7, py + 15, H - 14, H - 22, 5, 5);

        // Oturma yüzeyi doku
        gc.setStroke(Color.rgb(130, 90, 40, 0.4));
        gc.setLineWidth(0.7);
        gc.strokeLine(px + 9,      py + 20, px + H - 9, py + 20);
        gc.strokeLine(px + 9,      py + 26, px + H - 9, py + 26);
        gc.strokeLine(px + 9,      py + 32, px + H - 9, py + 32);


        // Sırtlık (arka)
        gc.setFill(koyu);
        gc.fillRoundRect(px + 7, py + 3, H - 14, 13, 4, 4);
        gc.setFill(acik);
        gc.fillRoundRect(px + 9, py + 5, H - 18, 4, 2, 2);

        // Sırtlık orta çubuk
        gc.setFill(ahsap);
        gc.fillRoundRect(px + H / 2 - 2, py + 5, 4, 10, 2, 2);
        gc.setLineWidth(1);
    }

    // =========================================================
    // Şarj istasyonu
    // =========================================================
    private void sarjIstasyonuCiz(GraphicsContext gc, double px, double py) {
        int H = HUCRE_BOYUTU;
        double cx = px + H / 2.0;
        double cy = py + H / 2.0;

        // Koyu kutu
        gc.setFill(Color.rgb(22, 26, 35));
        gc.fillRoundRect(px + 4, py + 4, H - 8, H - 8, 8, 8);

        // Dış yeşil halka
        gc.setStroke(Color.rgb(0, 210, 90, 0.85));
        gc.setLineWidth(2.5);
        gc.strokeOval(cx - 12, cy - 12, 24, 24);

        // İç yeşil daire
        gc.setFill(Color.rgb(0, 185, 75));
        gc.fillOval(cx - 9, cy - 9, 18, 18);

        // Şimşek
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("⚡", cx, cy + 5);
        gc.setLineWidth(1);
    }

    // =========================================================
    // Kir çizimi
    // =========================================================
    private void kirCiz(GraphicsContext gc, double px, double py, KirTuru kirTuru) {
        switch (kirTuru) {
            case TOZ -> {
                gc.setFill(Color.rgb(155, 145, 130, 0.85));
                double[] nx = {6, 14, 24, 10, 30, 20, 36, 8, 28, 18};
                double[] ny = {8, 18, 6, 30, 22, 36, 14, 38, 38, 14};
                for (int i = 0; i < nx.length; i++) {
                    double r = (i % 3 == 0) ? 2.8 : 1.6;
                    gc.fillOval(px + nx[i], py + ny[i], r * 2, r * 2);
                }
            }
            case SIVI -> {
                gc.setFill(Color.rgb(60, 120, 200, 0.75));
                gc.fillOval(px + 9,  py + 11, 11, 15);
                gc.fillOval(px + 25, py + 7,  9,  12);
                gc.fillOval(px + 17, py + 27, 13, 17);
                gc.setFill(Color.rgb(140, 190, 240, 0.5));
                gc.fillOval(px + 11, py + 13, 4, 5);
                gc.fillOval(px + 27, py + 9,  3, 4);
            }
            case LEKE -> {
                gc.setFill(Color.rgb(110, 62, 25, 0.8));
                gc.fillOval(px + 7,  py + 9,  20, 15);
                gc.fillOval(px + 19, py + 19, 15, 13);
                gc.fillOval(px + 11, py + 25, 17, 11);
                gc.setFill(Color.rgb(90, 48, 15, 0.5));
                gc.fillOval(px + 14, py + 14, 10, 8);
            }
        }
    }

    // =========================================================
    // Hareket yolu
    // =========================================================
    private void yoluCiz(GraphicsContext gc) {
        List<int[]> yol = robot.getHareketYolu();
        if (yol.size() < 2) return;

        gc.setStroke(Color.rgb(70, 150, 255, 0.6));
        gc.setLineWidth(1.8);
        gc.setLineDashes(6, 4);

        for (int i = 1; i < yol.size(); i++) {
            int[] prev = yol.get(i - 1);
            int[] curr = yol.get(i);
            double x1 = KENAR_BOSLUGU + prev[0] * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0;
            double y1 = KENAR_BOSLUGU + prev[1] * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0;
            double x2 = KENAR_BOSLUGU + curr[0] * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0;
            double y2 = KENAR_BOSLUGU + curr[1] * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0;
            gc.strokeLine(x1, y1, x2, y2);
            if (i % 4 == 0) okBasiCiz(gc, x1, y1, x2, y2);
        }
        gc.setLineDashes(null);
        gc.setLineWidth(1);
    }

    private void okBasiCiz(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double len = 7, ac = Math.PI / 6;
        double ax = x2 - len * Math.cos(angle - ac);
        double ay = y2 - len * Math.sin(angle - ac);
        double bx = x2 - len * Math.cos(angle + ac);
        double by = y2 - len * Math.sin(angle + ac);
        gc.setFill(Color.rgb(70, 150, 255, 0.8));
        gc.fillPolygon(new double[]{x2, ax, bx}, new double[]{y2, ay, by}, 3);
    }

    // =========================================================
    // Robot çizimi
    // =========================================================
    private void robotuCiz(GraphicsContext gc) {
        double px = KENAR_BOSLUGU + robot.getX() * HUCRE_BOYUTU;
        double py = KENAR_BOSLUGU + robot.getY() * HUCRE_BOYUTU;
        double cx = px + HUCRE_BOYUTU / 2.0;
        double cy = py + HUCRE_BOYUTU / 2.0;
        double r  = HUCRE_BOYUTU / 2.0 - 3;


        // Dış halka
        gc.setFill(Color.rgb(42, 48, 58));
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);

        // İç beyaz daire
        gc.setFill(Color.rgb(232, 236, 242));
        gc.fillOval(cx - r + 3, cy - r + 3, (r - 3) * 2, (r - 3) * 2);

        // Merkez düğme
        gc.setFill(Color.rgb(65, 75, 92));
        gc.fillOval(cx - 5, cy - 5, 10, 10);
        gc.setFill(Color.rgb(95, 110, 130));
        gc.fillOval(cx - 3, cy - 3, 6, 6);

        // Sensör yayı
        gc.setStroke(Color.rgb(42, 48, 58));
        gc.setLineWidth(2);
        gc.strokeArc(cx - r + 5, cy - r + 5, (r - 5) * 2, (r - 5) * 2,
                     30, 120, javafx.scene.shape.ArcType.OPEN);
        gc.setLineWidth(1);

        // Yön göstergesi
        Yon yon = robot.getYon();
        if (yon != null) {
            double dx = yon.getDx() * (r - 7);
            double dy = yon.getDy() * (r - 7);
            gc.setFill(Color.rgb(0, 210, 110));
            gc.fillOval(cx + dx - 3, cy + dy - 3, 6, 6);
        }

        // Batarya göstergesi
        double bat = robot.getBataryaYuzdesi();
        Color batRenk = bat > 50 ? Color.rgb(55, 195, 75)
                      : bat > 20 ? Color.rgb(255, 155, 0)
                      : Color.rgb(215, 40, 40);

        gc.setFill(Color.rgb(18, 20, 28, 0.85));
        gc.fillRoundRect(cx - 20, py - 20, 40, 16, 5, 5);
        gc.setFill(Color.rgb(45, 50, 62));
        gc.fillRoundRect(cx - 16, py - 17, 28, 10, 3, 3);
        gc.setFill(batRenk);
        gc.fillRoundRect(cx - 16, py - 17, (int)(28 * bat / 100.0), 10, 3, 3);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.format("%.0f%%", bat), cx, py - 9);
    }

    public int getHucreBoyutu()  { return HUCRE_BOYUTU; }
    public int getKenarBoslugu() { return KENAR_BOSLUGU; }
}
