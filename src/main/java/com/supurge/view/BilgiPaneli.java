package com.supurge.view;

import com.supurge.model.SimulasyonDurumu;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * Robot durumunu gösteren bilgi paneli (sağ/sol panel).
 */
public class BilgiPaneli extends VBox {

    private Label konumEtiketi;
    private Label yonEtiketi;
    private Label bataryaEtiketi;
    private ProgressBar bataryaCubugu;
    private Label temizlenenEtiketi;
    private Label kalanEtiketi;
    private Label sureEtiketi;
    private Label kirSayisiEtiketi;

    public BilgiPaneli() {
        setPadding(new Insets(10));
        setSpacing(8);
        setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");

        Label baslik = new Label("🤖 Robot Durumu");
        baslik.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: white;");

        konumEtiketi = etiketOlustur("Konum: (0, 0)");
        yonEtiketi = etiketOlustur("Yön: Doğu");
        bataryaEtiketi = etiketOlustur("Batarya: 100%");
        bataryaCubugu = new ProgressBar(1.0);
        bataryaCubugu.setPrefWidth(180);
        bataryaCubugu.setStyle("-fx-accent: #4caf50;");

        temizlenenEtiketi = etiketOlustur("Temizlenen: 0%");
        kalanEtiketi = etiketOlustur("Kalan Kir: 0");
        kirSayisiEtiketi = etiketOlustur("Toplam Temizlenen Kir: 0");
        sureEtiketi = etiketOlustur("Süre: 00:00");

        getChildren().addAll(
                baslik, new Separator(),
                konumEtiketi, yonEtiketi,
                bataryaEtiketi, bataryaCubugu,
                new Separator(),
                temizlenenEtiketi, kalanEtiketi, kirSayisiEtiketi, sureEtiketi
        );
    }

    /** Simülasyon durumuna göre paneli günceller */
    public void guncelle(SimulasyonDurumu durum) {
        konumEtiketi.setText(String.format("Konum: (%d, %d)", durum.getRobotX(), durum.getRobotY()));
        yonEtiketi.setText("Yön: " + yonTurkce(durum.getYon()));
        bataryaEtiketi.setText(String.format("Batarya: %.0f%%", durum.getBataryaYuzdesi()));
        bataryaCubugu.setProgress(durum.getBataryaYuzdesi() / 100.0);

        // Batarya rengini güncelle
        if (durum.getBataryaYuzdesi() <= 20) {
            bataryaCubugu.setStyle("-fx-accent: #f44336;");
        } else if (durum.getBataryaYuzdesi() <= 50) {
            bataryaCubugu.setStyle("-fx-accent: #ff9800;");
        } else {
            bataryaCubugu.setStyle("-fx-accent: #4caf50;");
        }

        temizlenenEtiketi.setText(String.format("Temizlenen: %.0f%%", durum.temizlenenYuzde()));
        kalanEtiketi.setText("Kalan Kir: " + durum.getKalanKirliHucre());
        kirSayisiEtiketi.setText("Toplam Temizlenen Kir: " + durum.getToplamTemizlenenKir());

        long sure = durum.getGecenSure();
        sureEtiketi.setText(String.format("Süre: %02d:%02d", sure / 60, sure % 60));
    }

    private Label etiketOlustur(String metin) {
        Label l = new Label(metin);
        l.setStyle("-fx-text-fill: #cccccc;");
        return l;
    }

    private String yonTurkce(com.supurge.model.Yon yon) {
        if (yon == null) return "-";
        return switch (yon) {
            case KUZEY -> "Kuzey ↑";
            case GUNEY -> "Güney ↓";
            case DOGU -> "Doğu →";
            case BATI -> "Batı ←";
        };
    }
}
