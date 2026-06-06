package com.supurge.controller;

import com.supurge.model.*;

import java.util.*;

public class HareketKontrolcu {

    // Nerede olduğunu, kim olduğunu ve şarjı
    private final Oda oda;
    private final Robot robot;
    private final BataryaKontrolcu bataryaKontrolcu;
    private final YolBulmaKontrolcu yolBulma; // BFS ve A* algoritmalarımız burda
    private final Random rastgele = new Random();

    // Spiral modu için hafıza değişkenleri
    private int spiralAdim      = 0; // Şu an o çizgide kaç adım attı?
    private int spiralBoyut     = 1; // Çizgi ne kadar uzun olacak?
    private int spiralTurSayaci = 0; // Kaç kere döndü?

    // Robot iki koltuk arasına falan sıkışırsa diye
    private int takiliKalma = 0;
    private int sonX = -1, sonY = -1;

    // A* algoritmasının çizdiği GPS rotası
    private List<int[]> aktifYol = null;
    private int aktifYolAdimi    = 0;    // Rotanın kaçıncı adımındayız?

    public HareketKontrolcu(Oda oda, Robot robot, BataryaKontrolcu bataryaKontrolcu) {
        this.oda = oda;
        this.robot = robot;
        this.bataryaKontrolcu = bataryaKontrolcu;
        this.yolBulma = new YolBulmaKontrolcu(oda); // Yol bulucuyu başlatıyoruz
    }


    // Timer her tetiklendiğinde (her adımda) çalışan ana metot
    public boolean hareketEt() {
        // 0. ADIM: Robot duvara falan mı kafa atıyor kontrolü (Takılma tespiti)
        if (robot.getX() == sonX && robot.getY() == sonY) {
            takiliKalma++;
            // 8 turdur aynı yerdeyse sistemi sıfırla (Paniği önle)
            if (takiliKalma >= 8) {
                takiliKalma = 0;
                aktifYol = null;
                sifirla();
            }
        } else {
            // Hareket ediyorsa sıkıntı yok, sayacı sıfırla ve yeni konumu kaydet
            takiliKalma = 0;
            sonX = robot.getX();
            sonY = robot.getY();
        }

        //Yanımda, sağımda solumda kir var mı? Varsa direkt yapış!
        Yon kirliYon = kirliKomsuBul();
        if (kirliYon != null) {
            aktifYol = null; // A* rotasını falan boşver, önce bunu temizle
            return git(kirliYon);
        }

        // Elimde çizilmiş bir A* rotası varsa, o yoldan devam et
        if (aktifYol != null && aktifYolAdimi < aktifYol.size()) {
            int[] hedef = aktifYol.get(aktifYolAdimi);
            // Rotadaki o adıma geldiysek, listedeki bir sonraki adıma geç
            if (hedef[0] == robot.getX() && hedef[1] == robot.getY()) {
                aktifYolAdimi++;
                if (aktifYolAdimi >= aktifYol.size()) {
                    aktifYol = null; // Yol bitti
                    return false;
                }
                hedef = aktifYol.get(aktifYolAdimi);
            }

            // Hangi yöne gideceğimizi hesapla (Örn: X artıyorsa Doğu'ya git)
            int dx = hedef[0] - robot.getX();
            int dy = hedef[1] - robot.getY();
            Yon yon = yonBul(dx, dy);

            // Eğer önümüze sonradan bir engel çıkmadıysa yola devam
            if (yon != null && !oda.engelMi(hedef[0], hedef[1])) {
                aktifYolAdimi++;
                return git(yon);
            } else {
                aktifYol = null; // Yol kapandı, rotayı çöpe at yeniden hesaplarız
            }
        }

        //Ne yanımda kir var, ne de elimde rota. Haritayı tara BFS
        Hucre hedefHucre = enYakinTemizlenmemisHucre();
        if (hedefHucre == null) {
            robot.setCalisiyor(false);
            return false;
        }

        // Bulduğumuz pislik hemen dibimizde mi yoksa uzakta mı?
        boolean hedefKomsu = Math.abs(hedefHucre.getX() - robot.getX())
                + Math.abs(hedefHucre.getY() - robot.getY()) == 1;

        // Uzaktaysa araya kendi algoritmamızı sokalım
        if (!hedefKomsu) {
            // Kullanıcı arayüzden hangi modu (Rastgele, Spiral vs.) seçtiyse onu çalıştır
            boolean hareket = switch (robot.getAlgoritma()) {
                case RASTGELE    -> rastgeleHareketEt();
                case SPIRAL      -> spiralHareketEt();
                case DUVAR_TAKIP -> duvarTakipHareketEt();
            };

            // Eğer kendi algoritmamız bizi temiz bir yere götürüyorsa bunu boşver
            if (hareket) {
                Hucre mevcutHucre = oda.getHucre(robot.getX(), robot.getY());
                if (mevcutHucre != null && (!mevcutHucre.isZiyaretEdildi() || mevcutHucre.isKirli())) {
                    return true; // Girilmemiş yere girdik
                }
                // Girilmiş yerlerde dolanmayalım, direkt A* ile hedefe gidelim
                return enYakinTemizlenmemiseGit();
            }
            // Algoritma kilitlendiyse yine A* kurtarır
            return enYakinTemizlenmemiseGit();
        }

        // Hedef komşumuzdaysa A* ile rota çizmeye gerek yok, direkt o yöne adım at
        int dx = hedefHucre.getX() - robot.getX();
        int dy = hedefHucre.getY() - robot.getY();
        Yon yon = yonBul(dx, dy);
        if (yon != null) return git(yon);
        return false;
    }

    // Hedef belli, Oraya A* ile rota çiz
    private boolean enYakinTemizlenmemiseGit() {
        Hucre hedef = enYakinTemizlenmemisHucre(); // Önce BFS ile hedefi bul
        if (hedef == null) {
            robot.setCalisiyor(false); // Hedef yoksa işlem bitti
            return false;
        }

        // A* Algoritması ile şu anki x,y'den al, hedefin x,y'sine bana yol listesi ver
        List<int[]> yol = yolBulma.aYildizYolBul(
                robot.getX(), robot.getY(), hedef.getX(), hedef.getY());

        // Yol çizilemiyorsa sıkışmışızdır rastgele çıkmaya çalış
        if (yol == null || yol.size() < 2) {
            return rastgeleHareketEt();
        }

        aktifYol = yol;
        aktifYolAdimi = 1; // İlk adım zaten bulunduğumuz yer 1'den başla

        if (aktifYolAdimi >= aktifYol.size()) {
            aktifYol = null;
            return false;
        }

        // Rotadaki ilk adımı at
        int[] sonrakiAdim = aktifYol.get(aktifYolAdimi);
        int dx = sonrakiAdim[0] - robot.getX();
        int dy = sonrakiAdim[1] - robot.getY();
        Yon yon = yonBul(dx, dy);

        if (yon != null) {
            boolean adimBasarili = git(yon);
            if (adimBasarili) {
                aktifYolAdimi++;
                return true;
            } else {
                aktifYol = null; // Giderken eşyaya falan çarptık, rotayı iptal et
                return false;
            }
        }
        return false;
    }

    // BFS ile ilk kiri  bulma
    private Hucre enYakinTemizlenmemisHucre() {
        Queue<int[]> kuyruk = new LinkedList<>(); // İşlenecek hücreler kuyruğu
        Set<String> ziyaret = new HashSet<>();    // Aynı yere 2 kere bakmamak için

        kuyruk.add(new int[]{robot.getX(), robot.getY()});
        ziyaret.add(robot.getX() + "," + robot.getY());

        int[][] yonler = {{0,-1},{0,1},{1,0},{-1,0}}; // kuzey, güney, doğu, batı

        while (!kuyruk.isEmpty()) {
            int[] mevcut = kuyruk.poll();
            Hucre h = oda.getHucre(mevcut[0], mevcut[1]);

            // Eğer baktığımız yer mobilya/şarj ünitesi değilse ve kirliyse hedef bura
            if (h != null && !h.isEngel() && !h.isSarjIstasyonu()) {
                if (h.isKirli() || !h.isZiyaretEdildi()) {
                    return h;
                }
            }

            // Bulamadıysak komşu hücreleri kuyruğa ekle
            for (int[] yon : yonler) {
                int nx = mevcut[0] + yon[0];
                int ny = mevcut[1] + yon[1];
                String k = nx + "," + ny;
                // Sınırların dışına çıkma ve daha önce baktığın yere bakma
                if (!oda.engelMi(nx, ny) && !ziyaret.contains(k)) {
                    ziyaret.add(k);
                    kuyruk.add(new int[]{nx, ny});
                }
            }
        }
        return null; // Döngü bitti ama kir bulamadıysa oda temiz
    }

    // Odanın bitip bitmediğini kontrol etme
    private boolean tumHucrelerTemizMi() {
        // Çift for döngüsüyle tüm matrisi (ızgarayı) geziyoruz
        for (int y = 0; y < oda.getYukseklik(); y++) {
            for (int x = 0; x < oda.getGenislik(); x++) {
                Hucre h = oda.getHucre(x, y);
                if (h != null && !h.isEngel() && !h.isSarjIstasyonu()) {
                    if (h.isKirli() || !h.isZiyaretEdildi()) return false; // Kir bulduk, iş bitmedi
                }
            }
        }
        return true; // Hiç false dönmediyse her yer temiz
    }

    // Etrafımızdaki 4 karede kir var mı?
    private Yon kirliKomsuBul() {
        for (Yon yon : Yon.values()) { // kuzey, güney, doğu, batı döner
            int nx = robot.getX() + yon.getDx();
            int ny = robot.getY() + yon.getDy();
            Hucre h = oda.getHucre(nx, ny);

            // Hem kirli olacak hemde mobilyanın altı falan olmayacak
            if (h != null && h.isKirli() && !h.isEngel()) return yon;
        }
        return null;
    }

    // rastgele modu-> Gidilebilecek yönlerden birini salla
    private boolean rastgeleHareketEt() {
        List<Yon> gecerli = gecerliYonleriGetir(); // Duvar olmayan yönleri al
        if (gecerli.isEmpty()) return false;

        // Tamamen rastgele gitme önce girmediğin yerler var mı ona bak
        List<Yon> temizlenmemis = new ArrayList<>();
        for (Yon yon : gecerli) {
            Hucre h = oda.getHucre(robot.getX() + yon.getDx(), robot.getY() + yon.getDy());
            if (h != null && (!h.isZiyaretEdildi() || h.isKirli())) temizlenmemis.add(yon);
        }

        // Girilmemiş yer varsa onlardan birini rastgele seç
        if (!temizlenmemis.isEmpty()) {
            return git(temizlenmemis.get(rastgele.nextInt(temizlenmemis.size())));
        }

        // Her yer girilmişse tamamen rastgele seç
        return git(gecerli.get(rastgele.nextInt(gecerli.size())));
    }

    // SPİRAL MODU: Merkezden dışa doğru dönerek büyü
    private boolean spiralHareketEt() {
        Yon mevcutYon = robot.getYon();
        int yeniX = robot.getX() + mevcutYon.getDx();
        int yeniY = robot.getY() + mevcutYon.getDy();

        // Önümüz boşsa dümdüz git
        if (!oda.engelMi(yeniX, yeniY)) {
            git(mevcutYon);
            spiralAdim++; // Adım sayacını artır

            // Eğer çizmemiz gereken sınır boyuna ulaştıysak
            if (spiralAdim >= spiralBoyut) {
                spiralAdim = 0; // Sayacı sıfırla
                spiralTurSayaci++; // Turu 1 artır

                // Her 2 dönüşte bir (sağ-sağ yapınca) yürüyeceğimiz yolu 1 birim uzat
                if (spiralTurSayaci % 2 == 0) spiralBoyut++;

                // Ve robotu sağa döndür
                robot.setYon(sagaYon(robot.getYon()));
            }
            return true;
        }

        // engele çarptıysak spiral bozulur, kaçacak yer ara
        Yon[] denemeSirasi = { sagaYon(mevcutYon), solaYon(mevcutYon), geriYon(mevcutYon) };
        for (Yon deneme : denemeSirasi) {
            int nx = robot.getX() + deneme.getDx();
            int ny = robot.getY() + deneme.getDy();
            if (!oda.engelMi(nx, ny)) {
                spiralAdim = 0; // Yeni yöne geçince sayacı sıfırla
                return git(deneme);
            }
        }

        // Hiçbir yere gidemiyorsak sıkışmışızdır
        sifirla();
        return false;
    }

    // DUVAR TAKİP (Sol El Kuralı Labirent Çözümü)
    private boolean duvarTakipHareketEt() {
        // Her zaman öncelik sola gitmektir. Sol kapalıysa düz, Düz kapalıysa sağ, o da kapalıysa geri
        Yon[] sira = {
                solaYon(robot.getYon()),
                robot.getYon(),
                sagaYon(robot.getYon()),
                geriYon(robot.getYon())
        };

        for (Yon deneme : sira) {
            int nx = robot.getX() + deneme.getDx();
            int ny = robot.getY() + deneme.getDy();
            if (!oda.engelMi(nx, ny)) { // Engel olmayan ilk yönü bulur bulmaz git
                return git(deneme);
            }
        }
        return false;
    }

    // FİZİKSEL HAREKET KISMI (Tekerlekleri Döndür)

    private boolean git(Yon yon) {
        int nx = robot.getX() + yon.getDx();
        int ny = robot.getY() + yon.getDy();

        // Gideceğin yerde mobilya vs. varsa hareketi iptal et
        if (oda.engelMi(nx, ny)) {
            aktifYol = null;
            return false;
        }

        // Önce robotun yönünü çevir
        robot.setYon(yon);
        // Sonra koordinatını güncelle
        robot.hareketEt(nx, ny);
        // Gittiği için şarjından düş -> BataryaKontrolcu'ye haber veriyoruz
        bataryaKontrolcu.hareketTuketimUygula();
        return true;
    }

    // Koordinat farkına (dx, dy) bakarak hangi yöne gittiğimizi bulan yardımcı
    private Yon yonBul(int dx, int dy) {
        for (Yon yon : Yon.values()) {
            if (yon.getDx() == dx && yon.getDy() == dy) return yon;
        }
        return null;
    }

    // Mevcut yöne göre sağın neresi olduğunu hesaplar
    private Yon sagaYon(Yon yon) {
        return switch (yon) {
            case KUZEY -> Yon.DOGU;
            case DOGU  -> Yon.GUNEY;
            case GUNEY -> Yon.BATI;
            case BATI  -> Yon.KUZEY;
        };
    }

    // Mevcut yöne göre solun neresi olduğunu hesaplar
    private Yon solaYon(Yon yon) {
        return switch (yon) {
            case KUZEY -> Yon.BATI;
            case BATI  -> Yon.GUNEY;
            case GUNEY -> Yon.DOGU;
            case DOGU  -> Yon.KUZEY;
        };
    }

    // Mevcut yöne göre arkanın neresi olduğunu hesaplar
    private Yon geriYon(Yon yon) {
        return switch (yon) {
            case KUZEY -> Yon.GUNEY;
            case GUNEY -> Yon.KUZEY;
            case DOGU  -> Yon.BATI;
            case BATI  -> Yon.DOGU;
        };
    }

    // Robotun 4 tarafına bakar, engel olmayan (gidilebilir) yönleri liste halinde verir
    private List<Yon> gecerliYonleriGetir() {
        List<Yon> liste = new ArrayList<>();
        for (Yon yon : Yon.values()) {
            int nx = robot.getX() + yon.getDx();
            int ny = robot.getY() + yon.getDy();
            if (!oda.engelMi(nx, ny)) liste.add(yon);
        }
        return liste;
    }

    // Robot hata yaparsa/sıkışırsa hafızasını sildiğimiz yer
    public void sifirla() {
        spiralAdim      = 0;
        spiralBoyut     = 1;
        spiralTurSayaci = 0;
        takiliKalma     = 0;
        sonX = -1;
        sonY = -1;
        aktifYol = null;
        aktifYolAdimi = 0;
    }
}