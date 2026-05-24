package com.supurge.view;

import com.supurge.controller.SimulasyonKontrolcu;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Ana ekran - tüm panelleri bir araya getirir.
 */
public class AnaEkran {

    private static final int ODA_GENISLIK = 20;
    private static final int ODA_YUKSEKLIK = 14;
    private static final long ADIM_SURESI_NS = 200_000_000L; // 200ms

    private SimulasyonKontrolcu kontrolcu;
    private OdaGorunumu odaGorunumu;
    private KontrolPaneli kontrolPaneli;
    private BilgiPaneli bilgiPaneli;
    private AnimationTimer zamanlayici;
    private long sonAdimZamani = 0;

    public void baslat(Stage sahne) {
        kontrolcu = new SimulasyonKontrolcu(ODA_GENISLIK, ODA_YUKSEKLIK);

        odaGorunumu = new OdaGorunumu(kontrolcu.getOda(), kontrolcu.getRobot());
        kontrolPaneli = new KontrolPaneli(kontrolcu);
        bilgiPaneli = new BilgiPaneli();

        // İlk çizim
        odaGorunumu.guncelle(kontrolcu.getDurum());
        bilgiPaneli.guncelle(kontrolcu.getDurum());

        // Tıklama ile kir/engel ekleme
        odaGorunumu.setOnMouseClicked(event -> {
            int x = (int) (event.getX() / odaGorunumu.getHucreBoyutu());
            int y = (int) (event.getY() / odaGorunumu.getHucreBoyutu());
            if (kontrolPaneli.isKirEkleModu()) {
                kontrolcu.kirEkle(x, y, kontrolPaneli.getSeciliKirTuru());
            } else if (kontrolPaneli.isEngelEkleModu()) {
                kontrolcu.engelEkle(x, y);
            }
            odaGorunumu.yenidenCiz();
        });

        // Layout
        BorderPane kokDuzen = new BorderPane();
        kokDuzen.setLeft(kontrolPaneli);
        kokDuzen.setCenter(odaGorunumu);
        kokDuzen.setRight(bilgiPaneli);
        kokDuzen.setPadding(new Insets(8));
        kokDuzen.setStyle("-fx-background-color: #121212;");

        // AnimationTimer - simülasyon döngüsü
        zamanlayici = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Hız bilgisi doğrudan Robot'tan değil, SimulasyonDurumu'ndan alınır (MVC)
                double hiz = kontrolcu.getDurum().getRobotHiz();
                long adimSuresi = hiz > 0 ? (long) (ADIM_SURESI_NS / hiz) : ADIM_SURESI_NS;
                if (now - sonAdimZamani >= adimSuresi) {
                    kontrolcu.adimAt();
                    odaGorunumu.guncelle(kontrolcu.getDurum());
                    bilgiPaneli.guncelle(kontrolcu.getDurum());
                    sonAdimZamani = now;
                }
            }
        };
        zamanlayici.start();

        Scene gorunum = new Scene(kokDuzen);
        sahne.setTitle("🤖 Robot Süpürge Simülasyonu");
        sahne.setScene(gorunum);
        sahne.setResizable(true);
        sahne.show();
    }
}
