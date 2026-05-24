# 🤖 Robot Süpürge Simülasyonu

> **BZ 214 Görsel Programlama** dersi kapsamında geliştirilmiştir.  
> Java 17 + JavaFX 17 ile MVC mimarisi kullanılarak yazılmıştır.

---

## 🛠 Gereksinimler

| Araç | Versiyon | İndirme |
|------|----------|---------|
| Java JDK | 23.0.2 | [oracle.com/java](https://www.oracle.com/java/technologies/downloads/) |
| Apache Maven | 3.8+ | [maven.apache.org](https://maven.apache.org/download.cgi) |

> JavaFX ayrıca kurulmasına gerek yok — Maven otomatik indirir (JavaFX 17.0.6).

---

## 🚀 Çalıştırma

```bash
# Projeyi klonla
git clone https://github.com/melihmazi/supurge.git
cd supurge

# Derle ve çalıştır
mvn javafx:run
```

İnternet bağlantısı ilk çalıştırmada bağımlılıkları indirir (~50 MB).

---

## 📁 Proje Yapısı (MVC)

```
robot-supurge/
├── pom.xml                                # Maven yapılandırması
│
└── src/main/java/com/supurge/
    │
    ├── Main.java                          # Uygulama başlangıç noktası
    │
    ├── model/                             # MODEL — veri ve iş kuralları
    │   ├── Hucre.java                     # Grid hücresi (engel, kir, ziyaret, şarj)
    │   ├── Oda.java                       # 20×14 grid yapısı
    │   ├── Robot.java                     # Robot durumu (konum, batarya, yön, hız)
    │   ├── KirTuru.java                   # Enum: TOZ | SIVI | LEKE
    │   ├── Yon.java                       # Enum: KUZEY | GUNEY | DOGU | BATI
    │   ├── TemizlemeAlgoritması.java      # Enum: RASTGELE | SPIRAL | DUVAR_TAKIP
    │   └── SimulasyonDurumu.java          # Controller→View veri taşıyıcısı (DTO)
    │
    ├── controller/                        # CONTROLLER — iş mantığı
    │   ├── SimulasyonKontrolcu.java       # Ana orkestratör, simülasyon döngüsü
    │   ├── HareketKontrolcu.java          # Hareket algoritmaları + A* kapsama
    │   ├── TemizlemeKontrolcu.java        # Kir temizleme mantığı
    │   ├── BataryaKontrolcu.java          # Batarya tüketimi ve şarj yönetimi
    │   └── YolBulmaKontrolcu.java         # BFS ve A* yol bulma algoritmaları
    │
    └── view/                              # VIEW — JavaFX arayüzü
        ├── AnaEkran.java                  # Ana pencere + AnimationTimer döngüsü
        ├── OdaGorunumu.java               # Canvas çizimi (oda, mobilyalar, robot)
        ├── KontrolPaneli.java             # Sol panel (kir, mobilya, hız, algoritma)
        ├── BilgiPaneli.java               # Sağ panel (robot durumu, batarya, istatistik)
        └── IstatistikBari.java            # Alt bar (toplam alan, süre, toplanan toz)
```

---

## ✨ Özellikler

### Robot Hareketi
- **Akıllı Kapsama:** Her adımda BFS ile en yakın ziyaret edilmemiş hücreyi bulur, A* ile oraya gider
- **3 Algoritma:** Rastgele, Spiral, Duvar Takip (sol el kuralı)
- **Döngü Yok:** Tüm hücreler ziyaret edilince robot durur

### Temizleme
| Kir Türü | Temizleme Süresi | Batarya Tüketimi |
|----------|-----------------|-----------------|
| 🧹 Toz   | 1 adım          | Düşük           |
| 💧 Sıvı  | 3 adım          | Orta            |
| 🌀 Leke  | 5 adım          | Yüksek          |

### Batarya & Şarj
- **%20'de otomatik dönüş:** Robot şarj istasyonuna A* ile döner, şarj olur, kaldığı yerden devam eder
- **Manuel dönüş:** "🏠 İstasyona Dön" butonu — temizleme yapmadan direkt döner, istasyonda bekler
- **%10 kritik:** Şarj istasyonuna ulaşamazsa durur

### Mobilyalar
Sol panelden 6 farklı mobilya seçip canvas'a tıklayarak eklenebilir:
`🛋 Koltuk` · `🪑 Sehpa` · `🪑 Masa` · `🪴 Saksı` · `📚 Kitaplık` · `🪑 Sandalye`

### Kontroller
| Buton | İşlev |
|-------|-------|
| ▶ Başlat | Simülasyonu başlatır / devam ettirir |
| ⏸ Duraklat | Robotu durdurur |
| ⏹ Sıfırla | Her şeyi sıfırlar |
| 🏠 İstasyona Dön | Robotu şarj istasyonuna gönderir |

---

## 🏗 Mimari

Proje katı **MVC** mimarisine uyar:

- **Model** → Sadece veri tutar, iş mantığı içermez
- **Controller** → Tüm iş mantığı burada, View'a `SimulasyonDurumu` DTO'su ile veri gönderir
- **View** → Sadece çizer ve kullanıcı olaylarını Controller'a iletir, Model'e doğrudan erişmez

---

## 👥 Katkıda Bulunanlar

- [@melihmazi](https://github.com/melihmazi)
- [@merts](https://github.com/merts)
