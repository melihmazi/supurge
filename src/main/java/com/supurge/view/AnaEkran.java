package com.supurge.view;

import com.supurge.controller.SimulasyonKontrolcu;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Ana ekran - tüm panelleri bir araya getirir.
 * Koyu tema, başlık barı ve alt istatistik barı içerir.
 */
public class AnaEkran {

    private static final int ODA_GENISLIK  = 20;
    private static final int ODA_YUKSEKLIK = 14;
    private static final long ADIM_SURESI_NS = 200_000_000L;

    private SimulasyonKontrolcu kontrolcu;
    private OdaGorunumu odaGorunumu;
    private KontrolPaneli kontrolPaneli;
    private BilgiPaneli bilgiPaneli;
    private IstatistikBari istatistikBari;
    private long sonAdimZamani = 0;
    private boolean tamamlandiGosterildi = false;

    public void baslat(Stage sahne) {
        kontrolcu = new SimulasyonKontrolcu(ODA_GENISLIK, ODA_YUKSEKLIK);

        odaGorunumu   = new OdaGorunumu(kontrolcu.getOda(), kontrolcu.getRobot());
        kontrolPaneli = new KontrolPaneli(kontrolcu);
        bilgiPaneli   = new BilgiPaneli();
        istatistikBari = new IstatistikBari();

        // İlk çizim
        odaGorunumu.guncelle(kontrolcu.getDurum());
        bilgiPaneli.guncelle(kontrolcu.getDurum());
        istatistikBari.guncelle(kontrolcu.getDurum());

        // Canvas tıklama
        odaGorunumu.setOnMouseClicked(event -> {
            int x = (int) ((event.getX() - odaGorunumu.getKenarBoslugu()) / odaGorunumu.getHucreBoyutu());
            int y = (int) ((event.getY() - odaGorunumu.getKenarBoslugu()) / odaGorunumu.getHucreBoyutu());
            if (x < 0 || y < 0 || x >= ODA_GENISLIK || y >= ODA_YUKSEKLIK) return;

            if (kontrolPaneli.isKirEkleModu()) {
                kontrolcu.kirEkle(x, y, kontrolPaneli.getSeciliKirTuru());
            } else if (kontrolPaneli.isEngelEkleModu()) {
                kontrolcu.engelEkle(x, y, kontrolPaneli.getSeciliMobilyaTuru());
            }
            odaGorunumu.guncelle(kontrolcu.getDurum());
        });

        // Başlık barı
        final HBox baslikBari = baslikBariOlustur();
        // Orta alan: sol panel + canvas + sağ panel
        HBox ortaAlan = new HBox(0);
        ortaAlan.getChildren().addAll(kontrolPaneli, odaGorunumu, bilgiPaneli);
        HBox.setHgrow(odaGorunumu, Priority.ALWAYS);

        // Ana düzen
        VBox kokDuzen = new VBox(0);
        kokDuzen.getChildren().addAll(baslikBari, ortaAlan, istatistikBari);
        kokDuzen.setStyle("-fx-background-color: #1a1d2e;");
        VBox.setVgrow(ortaAlan, Priority.ALWAYS);

        // AnimationTimer
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                double hiz = kontrolcu.getDurum().getRobotHiz();
                long adimSuresi = hiz > 0 ? (long)(ADIM_SURESI_NS / hiz) : ADIM_SURESI_NS;
                if (now - sonAdimZamani >= adimSuresi) {
                    kontrolcu.adimAt();
                    odaGorunumu.guncelle(kontrolcu.getDurum());
                    bilgiPaneli.guncelle(kontrolcu.getDurum());
                    istatistikBari.guncelle(kontrolcu.getDurum());

                    // Tamamlanma bildirimi
                    if (kontrolcu.getDurum().isTamamlandi() && !tamamlandiGosterildi) {
                        tamamlandiGosterildi = true;
                        baslikBariTamamlandiGoster(baslikBari);
                    }
                    // Sıfırlandıysa bildirimi temizle
                    // Sıfırlandıysa bildirimi temizle
                    if (!kontrolcu.getDurum().isTamamlandi() && !kontrolcu.getDurum().isCalisiyor()) {
                        if (tamamlandiGosterildi) {
                            tamamlandiGosterildi = false;

                            // YENİ HALİ: 3. indeksten sonraki tüm etiketleri temizle
                            while (baslikBari.getChildren().size() > 3) {
                                baslikBari.getChildren().remove(3);
                            }
                        }
                    }

                    sonAdimZamani = now;
                }
            }
        }.start();

        // --- GÜNCELLENEN KAYDIRMA ÇUBUĞU (SCROLLPANE) KISMI ---

        ScrollPane scrollPane = new ScrollPane(kokDuzen);

        // İçeriği ekrana sığmaya zorlayan ayarları kaldırdık, yerine kendi boyutunda kalmasını sağladık
        // Kaydırma çubuklarının ihtiyaç anında hem yatay hem dikey olarak çıkmasını garanti altına alıyoruz:
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Yatay (Enine) Kaydırma
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Dikey (Dikine) Kaydırma

        scrollPane.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");

        Scene gorunum = new Scene(scrollPane, 1200, 700);
        sahne.setTitle("Robot Süpürge Simülasyonu");
        sahne.setScene(gorunum);
        sahne.setResizable(true);
        sahne.show();

        // -----------------------------------------------------
    }

    private HBox baslikBariOlustur() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setStyle("-fx-background-color: #12152a;");

        Label ikon   = new Label("🤖");
        ikon.setFont(Font.font(24));

        Label baslik = new Label("Robot Süpürge Simülasyonu");
        baslik.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        baslik.setTextFill(Color.WHITE);

        Label yildiz = new Label("✨");
        yildiz.setFont(Font.font(18));

        bar.getChildren().addAll(ikon, baslik, yildiz);
        return bar;
    }

    /** Tamamlanma durumunda başlık barına yeşil bildirim ve duruma göre uyarı ekler. */
    private void baslikBariTamamlandiGoster(HBox bar) {
        // 1. Standart "Tamamlandı" etiketi
        Label tamamlandi = new Label("Temizlik Tamamlandı");
        tamamlandi.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        tamamlandi.setTextFill(Color.rgb(46, 204, 113));
        tamamlandi.setStyle("-fx-background-color: #1a3a2a; -fx-background-radius: 6; -fx-padding: 4 10;");

        if (bar.getChildren().size() < 4) {
            bar.getChildren().add(tamamlandi);
        }

        // 2. Ulaşılamayan Kir Kontrolü (Ek Puan Özelliği)
        // DİKKAT: "getKalanKirSayisi()" metodunun adı Model sınıfınızda farklı olabilir
        // (Örn: getKalanAlan(), getTemizlenmeyenHucreSayisi() vb.). Hata verirse kendi metodunuzun adını yazın.
        if (kontrolcu.getDurum().getKalanKirliHucre() > 0) {
            Label ulasilamayan = new Label("Ulaşılamayan Alanda Kir Tespit Edildi!");
            ulasilamayan.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            ulasilamayan.setTextFill(Color.rgb(255, 152, 0)); // Turuncu uyarı rengi
            ulasilamayan.setStyle("-fx-background-color: #4a2a0a; -fx-background-radius: 6; -fx-padding: 4 10;");

            bar.getChildren().add(ulasilamayan);
        }
    }
}