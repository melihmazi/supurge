package com.supurge.view;

import com.supurge.controller.SimulasyonKontrolcu;
import com.supurge.model.KirTuru;
import com.supurge.model.TemizlemeAlgoritması;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Sol kontrol paneli - resme uygun koyu tema.
 */
public class KontrolPaneli extends VBox {

    private final SimulasyonKontrolcu kontrolcu;
    private ToggleGroup kirTuruGrubu;
    private ToggleGroup algoritmaGrubu;
    private boolean kirEkleModu   = false;
    private boolean engelEkleModu = false;

    public KontrolPaneli(SimulasyonKontrolcu kontrolcu) {
        this.kontrolcu = kontrolcu;
        setPadding(new Insets(12));
        setSpacing(10);
        setPrefWidth(210);
        setStyle("-fx-background-color: #1e2235;");

        getChildren().addAll(
            bolumBasligi("🔧 Araçlar"),
            kirBolumu(),
            engelBolumu(),
            hizBolumu(),
            algoritmaBolumu(),
            robotDurumuBolumu(),
            kontrolButonlari()
        );
    }

    // ---- Bölüm başlığı ----
    private Label bolumBasligi(String metin) {
        Label l = new Label(metin);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        l.setTextFill(Color.WHITE);
        return l;
    }

    private Label kucukEtiket(String metin) {
        Label l = new Label(metin);
        l.setFont(Font.font("Arial", 11));
        l.setTextFill(Color.rgb(160, 170, 190));
        return l;
    }

    // ---- Kir ekleme bölümü ----
    private VBox kirBolumu() {
        Button kirEkleBtn = stilliButon("🧹  Kir Ekle", "#3a7bd5", "#2d6abf");
        kirEkleBtn.setOnAction(e -> {
            kirEkleModu = !kirEkleModu;
            engelEkleModu = false;
            kirEkleBtn.setStyle(kirEkleModu
                ? "-fx-background-color: #5a9bf5; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12;"
                : "-fx-background-color: #3a7bd5; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12;");
        });

        kirTuruGrubu = new ToggleGroup();

        ToggleButton toz  = kirToggle("🧹 Toz",  KirTuru.TOZ);
        ToggleButton sivi = kirToggle("💧 Sıvı", KirTuru.SIVI);
        ToggleButton leke = kirToggle("🌀 Leke", KirTuru.LEKE);
        toz.setSelected(true);

        HBox kirSecim = new HBox(4, toz, sivi, leke);

        return bolum(kucukEtiket("Kir Türü"), kirEkleBtn, kirSecim);
    }

    private ToggleButton kirToggle(String metin, KirTuru kirTuru) {
        ToggleButton tb = new ToggleButton(metin);
        tb.setToggleGroup(kirTuruGrubu);
        tb.setUserData(kirTuru);
        tb.setFont(Font.font("Arial", 10));
        tb.setStyle("-fx-background-color: #2a2f45; -fx-text-fill: #aabbcc; -fx-background-radius: 4;");
        tb.selectedProperty().addListener((obs, o, n) ->
            tb.setStyle(n
                ? "-fx-background-color: #3a7bd5; -fx-text-fill: white; -fx-background-radius: 4;"
                : "-fx-background-color: #2a2f45; -fx-text-fill: #aabbcc; -fx-background-radius: 4;"));
        return tb;
    }

    // ---- Engel ekleme ----
    private VBox engelBolumu() {
        Button engelBtn = stilliButon("🛋  Mobilya Ekle", "#2a7a4a", "#1f6038");
        engelBtn.setOnAction(e -> {
            engelEkleModu = !engelEkleModu;
            kirEkleModu = false;
            engelBtn.setStyle(engelEkleModu
                ? "-fx-background-color: #3aaa6a; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12;"
                : "-fx-background-color: #2a7a4a; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12;");
        });
        return bolum(engelBtn);
    }

    // ---- Hız ----
    private VBox hizBolumu() {
        Slider hiz = new Slider(0.5, 3.0, 1.0);
        hiz.setShowTickLabels(false);
        hiz.setStyle("-fx-control-inner-background: #2a2f45;");

        Label hizDeger = new Label("1.0x");
        hizDeger.setTextFill(Color.rgb(100, 180, 255));
        hizDeger.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        hiz.valueProperty().addListener((obs, o, n) -> {
            kontrolcu.hizAyarla(n.doubleValue());
            hizDeger.setText(String.format("%.1fx", n.doubleValue()));
        });

        HBox satir = new HBox(8, hiz, hizDeger);
        satir.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(hiz, Priority.ALWAYS);

        return bolum(bolumBasligi("⚡ Robot Hızı"), satir);
    }

    // ---- Algoritma ----
    private VBox algoritmaBolumu() {
        algoritmaGrubu = new ToggleGroup();

        RadioButton rastgele   = algoritmaRadio("Rastgele",    TemizlemeAlgoritması.RASTGELE);
        RadioButton spiral     = algoritmaRadio("Spiral",      TemizlemeAlgoritması.SPIRAL);
        RadioButton duvarTakip = algoritmaRadio("Duvar Takip", TemizlemeAlgoritması.DUVAR_TAKIP);
        spiral.setSelected(true);

        algoritmaGrubu.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n != null) kontrolcu.algoritmaAyarla((TemizlemeAlgoritması) n.getUserData());
        });

        return bolum(bolumBasligi("⚙ Temizlik Algoritması"), rastgele, spiral, duvarTakip);
    }

    private RadioButton algoritmaRadio(String metin, TemizlemeAlgoritması alg) {
        RadioButton rb = new RadioButton(metin);
        rb.setToggleGroup(algoritmaGrubu);
        rb.setUserData(alg);
        rb.setTextFill(Color.rgb(180, 190, 210));
        rb.setFont(Font.font("Arial", 12));
        return rb;
    }

    // ---- Robot durumu (batarya kaydırıcı) ----
    private VBox robotDurumuBolumu() {
        Slider batarya = new Slider(0, 100, 100);
        batarya.setShowTickLabels(true);
        batarya.setMajorTickUnit(25);
        batarya.setStyle("-fx-control-inner-background: #2a2f45;");
        batarya.valueProperty().addListener((obs, o, n) -> kontrolcu.bataryaAyarla(n.doubleValue()));

        return bolum(bolumBasligi("🔋 Batarya Ayarı"), batarya);
    }

    // ---- Kontrol butonları ----
    private VBox kontrolButonlari() {
        Button baslatBtn  = stilliButon("▶  Başlat",       "#27ae60", "#1e8449");
        Button duraklatBtn = stilliButon("⏸  Duraklat",    "#2980b9", "#1f6fa0");
        Button sifirlaBtn  = stilliButon("⏹  Sıfırla",     "#c0392b", "#a93226");
        Button sarjDonBtn  = stilliButon("🏠  İstasyona Dön", "#e67e22", "#ca6f1e");

        baslatBtn.setOnAction(e  -> kontrolcu.baslat());
        duraklatBtn.setOnAction(e -> kontrolcu.duraklat());
        sifirlaBtn.setOnAction(e  -> kontrolcu.sifirla());
        sarjDonBtn.setOnAction(e  -> kontrolcu.sarjIstasyonunaDon());

        return bolum(bolumBasligi("🎮 Kontroller"),
                     baslatBtn, duraklatBtn, sifirlaBtn, sarjDonBtn);
    }

    // ---- Yardımcılar ----
    private Button stilliButon(String metin, String renk, String hoverRenk) {
        Button btn = new Button(metin);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        String temelStil = String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12;", renk);
        String hoverStil = String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12;", hoverRenk);
        btn.setStyle(temelStil);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStil));
        btn.setOnMouseExited(e  -> btn.setStyle(temelStil));
        return btn;
    }

    private VBox bolum(javafx.scene.Node... elemanlar) {
        VBox kutu = new VBox(5);
        kutu.getChildren().addAll(elemanlar);
        kutu.setPadding(new Insets(6, 0, 6, 0));
        kutu.setStyle("-fx-border-color: #2a2f45; -fx-border-width: 0 0 1 0;");
        return kutu;
    }

    public boolean isKirEkleModu()   { return kirEkleModu; }
    public boolean isEngelEkleModu() { return engelEkleModu; }

    public KirTuru getSeciliKirTuru() {
        Toggle t = kirTuruGrubu.getSelectedToggle();
        return t != null ? (KirTuru) t.getUserData() : KirTuru.TOZ;
    }
}
