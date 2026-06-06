package com.supurge.view;

import com.supurge.model.SimulasyonDurumu;
import com.supurge.model.Yon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Sağ bilgi paneli - robot durumu, batarya, istatistikler.
 */
public class BilgiPaneli extends VBox {

    private Label konumDeger;
    private Label yonDeger;
    private Label bataryaDeger;
    private ProgressBar bataryaCubugu;
    private Label bataryaDurumDeger;
    private Label temizlenenDeger;
    private Label kalanDeger;
    private Label kirSayisiDeger;
    private Label sureDeger;
    private Label robotDurumDeger;  // Çalışıyor / Duraklatıldı / Şarj Dönüşü / Tamamlandı

    public BilgiPaneli() {
        setPadding(new Insets(12));
        setSpacing(8);
        setPrefWidth(200);
        setStyle("-fx-background-color: #1e2235;");

        getChildren().addAll(
            bolumBasligi("Robot Durumu"),
            ayrac(),
            satirOlustur("Durum",        robotDurumDeger = durumEtiketi("Bekliyor")),
            satirOlustur("Konum (x, y)", konumDeger    = degerEtiketi("(0, 0)")),
            satirOlustur("Yön",          yonDeger      = degerEtiketi("Doğu →")),
            bataryaBolumu(),
            ayrac(),
            bolumBasligi("İstatistikler"),
            ayrac(),
            satirOlustur("Temizlenen",   temizlenenDeger = degerEtiketi("0%")),
            satirOlustur("Kalan Kir",    kalanDeger      = degerEtiketi("0")),
            satirOlustur("Toplam Kir",   kirSayisiDeger  = degerEtiketi("0")),
            satirOlustur("Süre",         sureDeger       = degerEtiketi("00:00"))
        );
    }

    public void guncelle(SimulasyonDurumu durum) {
        if (durum == null) return;

        konumDeger.setText(String.format("(%d, %d)", durum.getRobotX(), durum.getRobotY()));
        yonDeger.setText(yonMetni(durum.getYon()));

        // Robot durum etiketi
        if (durum.isTamamlandi()) {
            robotDurumDeger.setText("Tamamlandı");
            robotDurumDeger.setTextFill(Color.rgb(46, 204, 113));
        } else if (durum.isSarjaDonuyorMu()) {
            robotDurumDeger.setText("Şarj Dönüşü");
            robotDurumDeger.setTextFill(Color.rgb(230, 126, 34));
        } else if (durum.isCalisiyor()) {
            robotDurumDeger.setText("▶ Çalışıyor");
            robotDurumDeger.setTextFill(Color.rgb(46, 204, 113));
        } else {
            robotDurumDeger.setText("Bekliyor");
            robotDurumDeger.setTextFill(Color.rgb(140, 150, 170));
        }

        double bat = durum.getBataryaYuzdesi();
        bataryaDeger.setText(String.format("%.0f%%", bat));
        bataryaCubugu.setProgress(bat / 100.0);

        if (bat <= 10) {
            bataryaCubugu.setStyle("-fx-accent: #e74c3c;");
            bataryaDurumDeger.setText("KRİTİK");
            bataryaDurumDeger.setTextFill(Color.rgb(231, 76, 60));
        } else if (bat <= 20) {
            bataryaCubugu.setStyle("-fx-accent: #e67e22;");
            bataryaDurumDeger.setText("DÜŞÜK");
            bataryaDurumDeger.setTextFill(Color.rgb(230, 126, 34));
        } else if (bat <= 50) {
            bataryaCubugu.setStyle("-fx-accent: #f1c40f;");
            bataryaDurumDeger.setText("ORTA");
            bataryaDurumDeger.setTextFill(Color.rgb(241, 196, 15));
        } else {
            bataryaCubugu.setStyle("-fx-accent: #2ecc71;");
            bataryaDurumDeger.setText("İYİ");
            bataryaDurumDeger.setTextFill(Color.rgb(46, 204, 113));
        }

        temizlenenDeger.setText(String.format("%.0f%%", durum.temizlenenYuzde()));
        kalanDeger.setText(String.valueOf(durum.getKalanKirliHucre()));
        kirSayisiDeger.setText(String.valueOf(durum.getToplamTemizlenenKir()));

        long sure = durum.getGecenSure();
        sureDeger.setText(String.format("%02d:%02d", sure / 60, sure % 60));
    }

    // ---- Yardımcılar ----

    private VBox bataryaBolumu() {
        bataryaDeger = degerEtiketi("100%");
        bataryaDurumDeger = new Label("İYİ");
        bataryaDurumDeger.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        bataryaDurumDeger.setTextFill(Color.rgb(46, 204, 113));

        HBox satir = new HBox(6);
        satir.setAlignment(Pos.CENTER_LEFT);
        Label etiket = new Label("Batarya");
        etiket.setFont(Font.font("Arial", 11));
        etiket.setTextFill(Color.rgb(140, 150, 170));
        etiket.setMinWidth(70);
        satir.getChildren().addAll(etiket, bataryaDeger, bataryaDurumDeger);

        bataryaCubugu = new ProgressBar(1.0);
        bataryaCubugu.setMaxWidth(Double.MAX_VALUE);
        bataryaCubugu.setPrefHeight(8);
        bataryaCubugu.setStyle("-fx-accent: #2ecc71; -fx-background-color: #2a2f45;");

        VBox kutu = new VBox(4, satir, bataryaCubugu);
        return kutu;
    }

    private HBox satirOlustur(String etiketMetin, Label degerLabel) {
        Label etiket = new Label(etiketMetin);
        etiket.setFont(Font.font("Arial", 11));
        etiket.setTextFill(Color.rgb(140, 150, 170));
        etiket.setMinWidth(80);

        HBox satir = new HBox(6, etiket, degerLabel);
        satir.setAlignment(Pos.CENTER_LEFT);
        return satir;
    }

    private Label durumEtiketi(String metin) {
        Label l = new Label(metin);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        l.setTextFill(Color.rgb(140, 150, 170));
        return l;
    }

    private Label degerEtiketi(String metin) {
        Label l = new Label(metin);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        l.setTextFill(Color.WHITE);
        return l;
    }

    private Label bolumBasligi(String metin) {
        Label l = new Label(metin);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        l.setTextFill(Color.WHITE);
        return l;
    }

    private Separator ayrac() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color: #2a2f45;");
        return s;
    }

    private String yonMetni(Yon yon) {
        if (yon == null) return "-";
        return switch (yon) {
            case KUZEY -> "Kuzey ↑";
            case GUNEY -> "Güney ↓";
            case DOGU  -> "Doğu →";
            case BATI  -> "Batı ←";
        };
    }
}
