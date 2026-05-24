package com.supurge.view;

import com.supurge.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Oda grid yapısını JavaFX Canvas üzerinde çizen View sınıfı.
 *
 * OOP/MVC notu:
 * - Çizim için Oda ve Robot'a salt okunur erişim gereklidir (grid verisi ve yol).
 * - Bu sınıf hiçbir zaman model nesnesini değiştirmez, yalnızca okur.
 * - Durum bilgisi (konum, yön vb.) SimulasyonDurumu üzerinden gelir.
 */
public class OdaGorunumu extends Canvas {

    private static final int HUCRE_BOYUTU = 40;

    // Salt okunur model referansları — sadece çizim için
    private final Oda oda;
    private final Robot robot;

    public OdaGorunumu(Oda oda, Robot robot) {
        super(oda.getGenislik() * HUCRE_BOYUTU, oda.getYukseklik() * HUCRE_BOYUTU);
        this.oda = oda;
        this.robot = robot;
        yenidenCiz();
    }

    /**
     * SimulasyonDurumu ile tetiklenen güncelleme — AnaEkran bu metodu çağırır.
     * Durum nesnesi şu an çizimde kullanılmıyor ama ileride
     * (örn. şarj dönüş animasyonu) buradan okunabilir.
     */
    public void guncelle(SimulasyonDurumu durum) {
        yenidenCiz();
    }

    /** Tüm sahneyi yeniden çizer */
    public void yenidenCiz() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        hucreleriCiz(gc);
        yoluCiz(gc);
        robotuCiz(gc);
    }

    private void hucreleriCiz(GraphicsContext gc) {
        Hucre[][] grid = oda.getGrid();
        for (int y = 0; y < oda.getYukseklik(); y++) {
            for (int x = 0; x < oda.getGenislik(); x++) {
                Hucre h = grid[y][x];
                double px = x * HUCRE_BOYUTU;
                double py = y * HUCRE_BOYUTU;

                gc.setFill(hucreRengi(h));
                gc.fillRect(px, py, HUCRE_BOYUTU, HUCRE_BOYUTU);

                gc.setStroke(Color.LIGHTGRAY);
                gc.strokeRect(px, py, HUCRE_BOYUTU, HUCRE_BOYUTU);

                if (h.isSarjIstasyonu()) {
                    gc.setFill(Color.DARKGOLDENROD);
                    gc.fillText("⚡", px + 12, py + 25);
                }
            }
        }
    }

    private Color hucreRengi(Hucre h) {
        if (h.isEngel())        return Color.SADDLEBROWN;
        if (h.isSarjIstasyonu()) return Color.GOLD;
        if (h.isTemizlendi())   return Color.LIGHTBLUE;
        if (h.isKirli())        return kirRengi(h.getKirTuru());
        return Color.WHITE;
    }

    private void yoluCiz(GraphicsContext gc) {
        List<int[]> yol = robot.getHareketYolu();
        gc.setStroke(Color.CORNFLOWERBLUE);
        gc.setLineWidth(1.5);
        for (int[] nokta : yol) {
            double px = nokta[0] * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0;
            double py = nokta[1] * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0;
            gc.strokeOval(px - 2, py - 2, 4, 4);
        }
        gc.setLineWidth(1);
    }

    private void robotuCiz(GraphicsContext gc) {
        double px = robot.getX() * HUCRE_BOYUTU;
        double py = robot.getY() * HUCRE_BOYUTU;
        gc.setFill(Color.DARKSLATEGRAY);
        gc.fillOval(px + 4, py + 4, HUCRE_BOYUTU - 8, HUCRE_BOYUTU - 8);
        gc.setFill(Color.WHITE);
        gc.fillText("R", px + 14, py + 25);
    }

    private Color kirRengi(KirTuru kirTuru) {
        return switch (kirTuru) {
            case TOZ  -> Color.LIGHTYELLOW;
            case SIVI -> Color.LIGHTCYAN;
            case LEKE -> Color.LIGHTCORAL;
        };
    }

    public int getHucreBoyutu() { return HUCRE_BOYUTU; }
}
