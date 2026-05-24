# Robot Süpürge Simülasyonu

> This project was developed as part of the **BZ 214 Visual Programming** course.  
> Special thanks to the course instructor and contributors.

## Proje Yapısı (MVC)

```
src/main/java/com/supurge/
├── Main.java                              # Başlangıç noktası (JavaFX Application)
│
├── model/                                 # MODEL katmanı
│   ├── Hucre.java                         # Grid hücresi (engel, kir, şarj istasyonu)
│   ├── Oda.java                           # Oda grid yapısı
│   ├── Robot.java                         # Robot durumu (konum, batarya, yön)
│   ├── KirTuru.java                       # Enum: TOZ, SIVI, LEKE
│   ├── Yon.java                           # Enum: KUZEY, GUNEY, DOGU, BATI
│   ├── TemizlemeAlgoritması.java          # Enum: RASTGELE, SPIRAL, DUVAR_TAKIP
│   └── SimulasyonDurumu.java             # Anlık durum verisi (View'a taşınır)
│
├── controller/                            # CONTROLLER katmanı
│   └── SimulasyonKontrolcu.java          # İş mantığı + BFS yol bulma
│
└── view/                                  # VIEW katmanı (JavaFX)
    ├── AnaEkran.java                      # Ana pencere + AnimationTimer
    ├── OdaGorunumu.java                   # Canvas üzerinde oda çizimi
    ├── KontrolPaneli.java                 # Sol panel (kir, engel, hız, algoritma)
    └── BilgiPaneli.java                   # Sağ panel (robot durumu, batarya, süre)
```

## Özellikler

- **3 Kir Türü:** Toz (hızlı), Sıvı (orta), Leke (yavaş) - farklı temizleme süresi ve batarya tüketimi
- **3 Temizleme Algoritması:** Rastgele, Spiral, Duvar Takip
- **BFS Yol Bulma:** Batarya düşünce en kısa yoldan şarj istasyonuna döner
- **Gerçek Zamanlı Bilgi:** Konum, yön, batarya, temizlenen alan, süre
- **Tıklayarak Kir/Engel Ekleme:** Canvas üzerine tıkla

## Çalıştırma

JavaFX SDK gereklidir.

```bash
javac --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml \
      -d out $(find src -name "*.java")

java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml \
     -cp out com.supurge.Main
```
