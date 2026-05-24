package com.supurge.view;

import com.supurge.controller.SimulasyonKontrolcu;
import com.supurge.model.KirTuru;
import com.supurge.model.TemizlemeAlgoritması;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * Kullanıcı kontrol paneli - sol panel.
 * Kir ekleme, engel ekleme, hız, algoritma ve kontrol butonları.
 */
public class KontrolPaneli extends VBox {

    private SimulasyonKontrolcu kontrolcu;
    private ToggleGroup kirTuruGrubu;
    private ToggleGroup algoritmaGrubu;
    private Slider hizKaydirici;
    private Slider bataryaKaydirici;

    // Tıklama modu
    private boolean kirEkleModu = false;
    private boolean engelEkleModu = false;

    public KontrolPaneli(SimulasyonKontrolcu kontrolcu) {
        this.kontrolcu = kontrolcu;
        setPadding(new Insets(10));
        setSpacing(8);
        setStyle("-fx-background-color: #1e1e1e;");
        setPrefWidth(200);

        getChildren().addAll(
                araçlarBasligi(),
                kirEkleBolumu(),
                engelEkleBolumu(),
                hizBolumu(),
                algoritmaBolumu(),
                bataryaBolumu(),
                kontrolButonlari()
        );
    }

    private Label baslikEtiketi(String metin) {
        Label l = new Label(metin);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #aaaaaa; -fx-font-size: 12;");
        return l;
    }

    private VBox araçlarBasligi() {
        Label l = new Label("🔧 Araçlar");
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14;");
        return new VBox(l);
    }

    private VBox kirEkleBolumu() {
        Button kirEkleBtn = new Button("🧹 Kir Ekle");
        kirEkleBtn.setMaxWidth(Double.MAX_VALUE);
        kirEkleBtn.setStyle("-fx-background-color: #3c3f41; -fx-text-fill: white;");
        kirEkleBtn.setOnAction(e -> {
            kirEkleModu = !kirEkleModu;
            engelEkleModu = false;
        });

        kirTuruGrubu = new ToggleGroup();
        RadioButton toz = new RadioButton("Toz");
        RadioButton sivi = new RadioButton("Sıvı");
        RadioButton leke = new RadioButton("Leke");
        toz.setToggleGroup(kirTuruGrubu);
        sivi.setToggleGroup(kirTuruGrubu);
        leke.setToggleGroup(kirTuruGrubu);
        toz.setSelected(true);
        toz.setUserData(KirTuru.TOZ);
        sivi.setUserData(KirTuru.SIVI);
        leke.setUserData(KirTuru.LEKE);
        toz.setStyle("-fx-text-fill: #cccccc;");
        sivi.setStyle("-fx-text-fill: #cccccc;");
        leke.setStyle("-fx-text-fill: #cccccc;");

        VBox kutu = new VBox(4, baslikEtiketi("Kir Türü"), kirEkleBtn, toz, sivi, leke);
        kutu.setPadding(new Insets(4, 0, 4, 0));
        return kutu;
    }

    private VBox engelEkleBolumu() {
        Button engelBtn = new Button("🛋 Mobilya Ekle");
        engelBtn.setMaxWidth(Double.MAX_VALUE);
        engelBtn.setStyle("-fx-background-color: #3c3f41; -fx-text-fill: white;");
        engelBtn.setOnAction(e -> {
            engelEkleModu = !engelEkleModu;
            kirEkleModu = false;
        });
        return new VBox(4, engelBtn);
    }

    private VBox hizBolumu() {
        hizKaydirici = new Slider(0.5, 3.0, 1.0);
        hizKaydirici.setShowTickLabels(true);
        hizKaydirici.setMajorTickUnit(0.5);
        hizKaydirici.valueProperty().addListener((obs, eski, yeni) ->
                kontrolcu.hizAyarla(yeni.doubleValue()));
        return new VBox(4, baslikEtiketi("⚡ Robot Hızı"), hizKaydirici);
    }

    private VBox algoritmaBolumu() {
        algoritmaGrubu = new ToggleGroup();
        RadioButton rastgele = new RadioButton("Rastgele");
        RadioButton spiral = new RadioButton("Spiral");
        RadioButton duvarTakip = new RadioButton("Duvar Takip");
        rastgele.setToggleGroup(algoritmaGrubu);
        spiral.setToggleGroup(algoritmaGrubu);
        duvarTakip.setToggleGroup(algoritmaGrubu);
        spiral.setSelected(true);
        rastgele.setUserData(TemizlemeAlgoritması.RASTGELE);
        spiral.setUserData(TemizlemeAlgoritması.SPIRAL);
        duvarTakip.setUserData(TemizlemeAlgoritması.DUVAR_TAKIP);

        for (Toggle t : algoritmaGrubu.getToggles())
            ((RadioButton) t).setStyle("-fx-text-fill: #cccccc;");

        algoritmaGrubu.selectedToggleProperty().addListener((obs, eski, yeni) -> {
            if (yeni != null) kontrolcu.algoritmaAyarla((TemizlemeAlgoritması) yeni.getUserData());
        });

        return new VBox(4, baslikEtiketi("⚙ Temizlik Algoritması"), rastgele, spiral, duvarTakip);
    }

    private VBox bataryaBolumu() {
        bataryaKaydirici = new Slider(0, 100, 100);
        bataryaKaydirici.setShowTickLabels(true);
        bataryaKaydirici.setMajorTickUnit(25);
        bataryaKaydirici.valueProperty().addListener((obs, eski, yeni) ->
                kontrolcu.bataryaAyarla(yeni.doubleValue()));
        return new VBox(4, baslikEtiketi("🔋 Batarya Ayarı"), bataryaKaydirici);
    }

    private VBox kontrolButonlari() {
        Button baslatBtn = new Button("▶ Başlat");
        Button duraklatBtn = new Button("⏸ Duraklat");
        Button sifirlaBtn = new Button("⏹ Sıfırla");
        Button sarjDonBtn = new Button("🏠 İstasyona Dön");

        baslatBtn.setMaxWidth(Double.MAX_VALUE);
        duraklatBtn.setMaxWidth(Double.MAX_VALUE);
        sifirlaBtn.setMaxWidth(Double.MAX_VALUE);
        sarjDonBtn.setMaxWidth(Double.MAX_VALUE);

        baslatBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
        duraklatBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        sifirlaBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        sarjDonBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");

        baslatBtn.setOnAction(e -> kontrolcu.baslat());
        duraklatBtn.setOnAction(e -> kontrolcu.duraklat());
        sifirlaBtn.setOnAction(e -> kontrolcu.sifirla());
        sarjDonBtn.setOnAction(e -> kontrolcu.sarjIstasyonunaDon());

        return new VBox(6, baslikEtiketi("🎮 Kontroller"),
                baslatBtn, duraklatBtn, sifirlaBtn, sarjDonBtn);
    }

    // Tıklama modunu dışarıdan sorgulama
    public boolean isKirEkleModu() { return kirEkleModu; }
    public boolean isEngelEkleModu() { return engelEkleModu; }

    public KirTuru getSeciliKirTuru() {
        Toggle secili = kirTuruGrubu.getSelectedToggle();
        return secili != null ? (KirTuru) secili.getUserData() : KirTuru.TOZ;
    }
}
