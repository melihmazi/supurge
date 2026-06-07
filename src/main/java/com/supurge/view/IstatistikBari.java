package com.supurge.view;

import com.supurge.model.SimulasyonDurumu;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Alt istatistik barı - Toplam Alan, Temizlenen Alan, Kalan Alan, Süre, Toplanan Toz.
 * Resme uygun koyu tema.
 */
public class IstatistikBari extends HBox {

    private Label toplamAlanDeger;
    private Label temizlenenAlanDeger;
    private Label kalanAlanDeger;
    private Label gecenSureDeger;
    private Label toplamKirDeger;

    public IstatistikBari() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(10, 20, 10, 20));
        setSpacing(0);
        setStyle("-fx-background-color: #12152a; -fx-border-color: #2a2f45; -fx-border-width: 1 0 0 0;");

        toplamAlanDeger    = degerEtiketi("0 m²");
        temizlenenAlanDeger = degerEtiketi("0 m² (0%)");
        kalanAlanDeger     = degerEtiketi("0 m² (0%)");
        gecenSureDeger     = degerEtiketi("00:00");
        toplamKirDeger     = degerEtiketi("0");

        getChildren().addAll(
            istatistikKutusu("", "#4a9eff", "Toplam Alan",    toplamAlanDeger),
            ayirici(),
            istatistikKutusu("", "#2ecc71", "Temizlenen Alan", temizlenenAlanDeger),
            ayirici(),
            istatistikKutusu("", "#95a5a6", "Kalan Alan",     kalanAlanDeger),
            ayirici(),
            istatistikKutusu("", null,     "Geçen Süre",     gecenSureDeger),
            ayirici(),
            istatistikKutusu("", null,     "Toplanan Toz",   toplamKirDeger)
        );
    }

    public void guncelle(SimulasyonDurumu durum) {
        if (durum == null) return;

        int toplam     = durum.getToplamHucre();
        int temizlenen = durum.getTemizlenenHucre();
        int kalan      = toplam - temizlenen;
        double yuzde   = durum.temizlenenYuzde();

        toplamAlanDeger.setText(toplam + " m²");
        temizlenenAlanDeger.setText(String.format("%d m² (%.0f%%)", temizlenen, yuzde));
        kalanAlanDeger.setText(String.format("%d m² (%.0f%%)", kalan, 100 - yuzde));

        long sure = durum.getGecenSure();
        gecenSureDeger.setText(String.format("%02d:%02d", sure / 60, sure % 60));
        toplamKirDeger.setText(durum.getToplamTemizlenenKir() + "");
    }

    // ---- Yardımcılar ----

    private HBox istatistikKutusu(String ikonMetin, String ikonRenk, String baslik, Label deger) {
        HBox kutu = new HBox(8);
        kutu.setAlignment(Pos.CENTER);
        kutu.setPadding(new Insets(0, 20, 0, 20));

        Label ikon = new Label(ikonMetin);
        ikon.setFont(Font.font(16));
        if (ikonRenk != null) {
            ikon.setTextFill(Color.web(ikonRenk));
        }

        Label baslikEtiket = new Label(baslik);
        baslikEtiket.setFont(Font.font("Arial", 11));
        baslikEtiket.setTextFill(Color.rgb(140, 150, 170));

        VBox metin = new VBox(2, baslikEtiket, deger);
        kutu.getChildren().addAll(ikon, metin);

        HBox.setHgrow(kutu, Priority.ALWAYS);
        return kutu;
    }

    private Label degerEtiketi(String metin) {
        Label l = new Label(metin);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        l.setTextFill(Color.WHITE);
        return l;
    }

    private Region ayirici() {
        Region r = new Region();
        r.setStyle("-fx-background-color: #2a2f45;");
        r.setPrefWidth(1);
        r.setPrefHeight(30);
        return r;
    }
}
