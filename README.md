# Robot Süpürge Simülasyonu

BZ 214 Görsel Programlama dersi projesi olarak geliştirilmiştir.
Projede Java kullanılmış olup, arayüz tasarımı için JavaFX tercih edilmiştir. 
Kodlar okunabilirliği artırmak amacıyla MVC mantığına göre üç ana parçaya (Model, View, Controller) ayrılmıştır.


## Gereksinimler

- Java JDK  23.0.2  [oracle.com/java](https://www.oracle.com/java/technologies/downloads/) 

- Apache Maven  3.8+  [maven.apache.org](https://maven.apache.org/download.cgi) 

- JavaFX ayrıca kurulmasına gerek yok, Maven otomatik indirir (JavaFX 17.0.6).

##  Nasıl Çalıştırılır?

Projeyi bilgisayarınızda çalıştırabilmek için Java (JDK 23.0.2) ve 
Maven'ın kurulu olması yeterli. JavaFX kütüphaneleriyle ekstra uğraşmanıza
gerek yok, Maven ilk çalıştırmada gerekli tüm paketleri kendisi indiriyor. 
Projeyi ayağa kaldırmak için terminali açıp önce git clone https://github.com/melihmazi/supurge.git
komutuyla dosyaları bilgisayarınıza çekin. Ardından cd supurge yazarak proje 
klasörünün içine girin ve son olarak mvn javafx:run komutunu çalıştırın. 
Simülasyon ekranı karşınıza gelecektir.

# Projeyi klonla ve derle, çalıştır
* git clone https://github.com/melihmazi/supurge.git
* cd supurge
* mvn javafx:run

İnternet bağlantısı ilk çalıştırmada bağımlılıkları indirir (~50 MB).

## Proje Yapısı (MVC)

- src
    - Controller
        - SimulasyonKontrolcu.java
        - HareketKontrolcu.java
        - TemizlemeKontrolcu.java
        - BataryaKontrolcu.java
        - YolBulmaKontrolcu.java
    - Model
        - Hucre.java
        - Oda.java
        - Robot.java
        - KirTuru.java
        - Yon.java
        - TemizlemeAlgoritması.java
        - SimulasyonDurumu.java
    - View
        - AnaEkran.java
        - OdaGorunumu.java
        - KontrolPaneli.java
        - BilgiPaneli.java
        - IstatistikBari.java
    - Main.java

### Teknik Özellikler
- JavaFX
- MVC tasarım mimarisi
- OOP prensipleri

## Özellikler

### Robotun Hareketi ve Algoritmalar
- Yol Bulma: Robot odayı kafasına göre değil, BFS ve A* algoritmalarını kullanarak geziyor. Her adımda en yakın temizlenmemiş hücreyi bulup rotayı oraya çiziyor.

- 3 Farklı Mod: Temizlik için 3 tane mantık yazdık: Rastgele, Spiral ve Duvar Takibi (klasik sol el kuralı).

- Sonsuz Döngü Koruması: Haritada girilmedik yer kalmadığında robot boş boş dönmesin diye sistemi otomatik durduruyoruz.

### Temizlik ve Kir Zorlukları
Odada 3 farklı kir tipi var ve her birinin temizlenme süresi/şarj tüketimi farklı:

Toz -> 1 adım (Hemen alıyor), Düşük 

Sıvı -> 3 adım (Uğraştırıyor),  Orta 

Leke -> 5 adım (Zorlu),  Yüksek 

### Batarya Olayı
* Otomatik Şarj (%20): Batarya %20'ye düşünce robot temizliği salıp A* algoritmasıyla en kısa yoldan şarj istasyonuna dönüyor. Şarjı fullenince kaldığı yerden devam ediyor.

* Şarjın Bitmesi (%10): Eğer şarj %10'a düşerse ve robot hala istasyona yetişemediyse olduğu yerde kapanıyor.

* Manuel Dönüş: Arayüze bir de "İstasyona Dön" butonu koyduk. Basarsan temizliği falan bırakıp direkt yuvasına gidip bekliyor.

### Engeller (Mobilyalar)
Sol panelden eşya seçip haritada (canvas) tıkladığımız yere mobilya ekleyebiliyoruz. Robot bunlara çarpmadan etrafından dolanıyor. Eklediğimiz eşyalar:
Koltuk,  Sehpa,  Masa,  Saksı,  Kitaplık,  Sandalye

### Arayüz Butonları
Buton Ne İşe Yarıyor?

Başlat -> Simülasyonu başlatıyor  duraklatıldıysa devam ettiriyor.

Duraklat -> Robotu olduğu yerde donduruyor.

Sıfırla -> Tüm haritayı ve ayarları siliyor, baştan başlatıyor.

İstasyona Dön -> Robotu direkt şarj istasyonuna çağırıyor.

## Kod Yapısı (MVC)

Projede spagetti kod olmasın ve sonradan ekleme yaparken patlamayalım diye katı bir MVC mimarisi kurduk:

* Model: Sadece verileri tutuyor. Robotun koordinatları, şarj durumu, odanın grid yapısı falan burada. İş mantığı (logic) kesinlikle içermiyor.

* Controller: Bütün olay burada dönüyor. Hareket algoritmaları, batarya hesabı, A*/BFS yol bulma hesaplamaları hepsi Controller klasöründe. View'a sadece gerekli verileri paslıyor.

* View: Sadece JavaFX ekran tasarımları ve canvas çizimleri. Doğrudan Model'e erişemiyor, butona tıklanma gibi durumları sadece Controller'a iletiyor.

