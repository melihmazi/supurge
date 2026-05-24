package com.supurge.controller;

import com.supurge.model.*;

import java.util.*;

/**
 * Robotun hareket algoritmalarını yöneten controller.
 *
 * Tüm algoritmalar için ortak akıllı kapsama katmanı:
 *   1. Mevcut hücre kirli → temizle (TemizlemeKontrolcu halleder)
 *   2. Komşuda kirli hücre var → oraya git
 *   3. Kirli hücre yok ama temizlenmemiş var → A* ile en yakınına git
 *   4. Her şey temiz → robotu durdur
 *
 * Spiral / Duvar Takip modları bu akıllı katmanın üstünde çalışır:
 * temizlenmiş alanlarda geometrik desen uygular, ama takılınca
 * veya temizlenmemiş hücre bulunca akıllı katmana geçer.
 */
public class HareketKontrolcu {

    private final Oda oda;
    private final Robot robot;
    private final BataryaKontrolcu bataryaKontrolcu;
    private final YolBulmaKontrolcu yolBulma;
    private final Random rastgele = new Random();

    // Spiral için
    private int spiralAdim      = 0;
    private int spiralBoyut     = 1;
    private int spiralTurSayaci = 0;

    // Takılma tespiti
    private int takiliKalma = 0;
    private int sonX = -1, sonY = -1;

    // Aktif A* yolu (hedefe giderken adım adım tüketilir)
    private List<int[]> aktifYol = null;
    private int aktifYolAdimi    = 0;

    public HareketKontrolcu(Oda oda, Robot robot, BataryaKontrolcu bataryaKontrolcu) {
        this.oda = oda;
        this.robot = robot;
        this.bataryaKontrolcu = bataryaKontrolcu;
        this.yolBulma = new YolBulmaKontrolcu(oda);
    }

    // =========================================================
    // Ana hareket metodu
    // =========================================================
    public boolean hareketEt() {
        // Takılma tespiti
        if (robot.getX() == sonX && robot.getY() == sonY) {
            takiliKalma++;
            if (takiliKalma >= 8) {
                takiliKalma = 0;
                aktifYol = null;
                sifirla();
            }
        } else {
            takiliKalma = 0;
            sonX = robot.getX();
            sonY = robot.getY();
        }

        // 1. Komşuda kirli hücre varsa oraya git (tüm algoritmalar için)
        Yon kirliYon = kirliKomsuBul();
        if (kirliYon != null) {
            aktifYol = null;
            return git(kirliYon);
        }

        // 2. Aktif A* yolu varsa devam et
        if (aktifYol != null && aktifYolAdimi < aktifYol.size()) {
            int[] hedef = aktifYol.get(aktifYolAdimi);
            // Zaten bu konumdaysak bir sonraki adıma geç
            if (hedef[0] == robot.getX() && hedef[1] == robot.getY()) {
                aktifYolAdimi++;
                if (aktifYolAdimi >= aktifYol.size()) {
                    aktifYol = null;
                    return false;
                }
                hedef = aktifYol.get(aktifYolAdimi);
            }
            // Bir sonraki adıma git (adimi burada artır, git() çağrısından önce)
            int dx = hedef[0] - robot.getX();
            int dy = hedef[1] - robot.getY();
            Yon yon = yonBul(dx, dy);
            if (yon != null && !oda.engelMi(hedef[0], hedef[1])) {
                aktifYolAdimi++; // önce artır, sonra git
                return git(yon);
            } else {
                // Yol geçersiz oldu (yeni engel), yeniden hesapla
                aktifYol = null;
            }
        }

        // 3. Algoritmaya göre hareket
        boolean hareket = switch (robot.getAlgoritma()) {
            case RASTGELE    -> rastgeleHareketEt();
            case SPIRAL      -> spiralHareketEt();
            case DUVAR_TAKIP -> duvarTakipHareketEt();
        };

        // 4. Hareket edemediyse → en yakın temizlenmemiş hücreye A* ile git
        if (!hareket) {
            return enYakinTemizlenmemiseGit();
        }

        return hareket;
    }

    // =========================================================
    // En yakın temizlenmemiş hücreye A* ile git
    // =========================================================
    private boolean enYakinTemizlenmemiseGit() {
        Hucre hedef = enYakinTemizlenmemisHucre();
        if (hedef == null) {
            // Tüm alan temizlendi — robotu durdur
            robot.setCalisiyor(false);
            return false;
        }

        List<int[]> yol = yolBulma.aYildizYolBul(
                robot.getX(), robot.getY(), hedef.getX(), hedef.getY());

        if (yol == null || yol.size() < 2) {
            // Bu hücreye ulaşılamıyor, bir sonrakini dene
            return rastgeleHareketEt();
        }

        aktifYol = yol;
        aktifYolAdimi = 1; // 0. eleman mevcut konum

        if (aktifYolAdimi >= aktifYol.size()) {
            aktifYol = null;
            return false;
        }
        int[] sonrakiAdim = aktifYol.get(aktifYolAdimi);
        int dx = sonrakiAdim[0] - robot.getX();
        int dy = sonrakiAdim[1] - robot.getY();
        Yon yon = yonBul(dx, dy);
        if (yon != null) {
            aktifYolAdimi++;
            return git(yon);
        }
        return false;
    }

    // =========================================================
    // En yakın temizlenmemiş hücreyi BFS ile bul
    // =========================================================
    private Hucre enYakinTemizlenmemisHucre() {
        Queue<int[]> kuyruk = new LinkedList<>();
        Set<String> ziyaret = new HashSet<>();
        kuyruk.add(new int[]{robot.getX(), robot.getY()});
        ziyaret.add(robot.getX() + "," + robot.getY());

        int[][] yonler = {{0,-1},{0,1},{1,0},{-1,0}};

        while (!kuyruk.isEmpty()) {
            int[] mevcut = kuyruk.poll();
            Hucre h = oda.getHucre(mevcut[0], mevcut[1]);
            // Kirli hücre veya hiç ziyaret edilmemiş hücre → hedef
            if (h != null && !h.isEngel() && !h.isSarjIstasyonu()) {
                if (h.isKirli() || !h.isZiyaretEdildi()) {
                    return h;
                }
            }
            for (int[] yon : yonler) {
                int nx = mevcut[0] + yon[0];
                int ny = mevcut[1] + yon[1];
                String k = nx + "," + ny;
                if (!oda.engelMi(nx, ny) && !ziyaret.contains(k)) {
                    ziyaret.add(k);
                    kuyruk.add(new int[]{nx, ny});
                }
            }
        }
        return null; // tüm ulaşılabilir hücreler ziyaret edildi
    }

    // =========================================================
    // Tüm ulaşılabilir hücreler temiz mi?
    // =========================================================
    private boolean tumHucrelerTemizMi() {
        for (int y = 0; y < oda.getYukseklik(); y++) {
            for (int x = 0; x < oda.getGenislik(); x++) {
                Hucre h = oda.getHucre(x, y);
                if (h != null && !h.isEngel() && !h.isSarjIstasyonu()) {
                    // Kirli hücre varsa veya hiç ziyaret edilmemiş hücre varsa bitmedi
                    if (h.isKirli() || !h.isZiyaretEdildi()) return false;
                }
            }
        }
        return true;
    }

    // =========================================================
    // Komşuda kirli hücre var mı?
    // =========================================================
    private Yon kirliKomsuBul() {
        for (Yon yon : Yon.values()) {
            int nx = robot.getX() + yon.getDx();
            int ny = robot.getY() + yon.getDy();
            Hucre h = oda.getHucre(nx, ny);
            if (h != null && h.isKirli()) return yon;
        }
        return null;
    }

    // =========================================================
    // Rastgele hareket — temizlenmemiş öncelikli
    // =========================================================
    private boolean rastgeleHareketEt() {
        List<Yon> gecerli = gecerliYonleriGetir();
        if (gecerli.isEmpty()) return false;

        // Temizlenmemiş komşuya öncelik ver (ziyaret edilmemiş veya kirli)
        List<Yon> temizlenmemis = new ArrayList<>();
        for (Yon yon : gecerli) {
            Hucre h = oda.getHucre(robot.getX() + yon.getDx(), robot.getY() + yon.getDy());
            if (h != null && (!h.isZiyaretEdildi() || h.isKirli())) temizlenmemis.add(yon);
        }
        if (!temizlenmemis.isEmpty()) {
            return git(temizlenmemis.get(rastgele.nextInt(temizlenmemis.size())));
        }

        return git(gecerli.get(rastgele.nextInt(gecerli.size())));
    }

    // =========================================================
    // Spiral hareket
    // =========================================================
    private boolean spiralHareketEt() {
        Yon mevcutYon = robot.getYon();
        int yeniX = robot.getX() + mevcutYon.getDx();
        int yeniY = robot.getY() + mevcutYon.getDy();

        if (!oda.engelMi(yeniX, yeniY)) {
            git(mevcutYon);
            spiralAdim++;
            if (spiralAdim >= spiralBoyut) {
                spiralAdim = 0;
                spiralTurSayaci++;
                if (spiralTurSayaci % 2 == 0) spiralBoyut++;
                robot.setYon(sagaYon(robot.getYon()));
            }
            return true;
        }

        // Engel: yön değiştir
        Yon[] denemeSirasi = {
            sagaYon(mevcutYon),
            solaYon(mevcutYon),
            geriYon(mevcutYon)
        };
        for (Yon deneme : denemeSirasi) {
            int nx = robot.getX() + deneme.getDx();
            int ny = robot.getY() + deneme.getDy();
            if (!oda.engelMi(nx, ny)) {
                spiralAdim = 0;
                return git(deneme);
            }
        }

        // Tamamen sıkışmış
        sifirla();
        return false;
    }

    // =========================================================
    // Duvar takip (sol el kuralı)
    // =========================================================
    private boolean duvarTakipHareketEt() {
        Yon[] sira = {
            solaYon(robot.getYon()),
            robot.getYon(),
            sagaYon(robot.getYon()),
            geriYon(robot.getYon())
        };
        for (Yon deneme : sira) {
            int nx = robot.getX() + deneme.getDx();
            int ny = robot.getY() + deneme.getDy();
            if (!oda.engelMi(nx, ny)) {
                return git(deneme);
            }
        }
        return false;
    }

    // =========================================================
    // Yardımcılar
    // =========================================================
    private boolean git(Yon yon) {
        robot.setYon(yon);
        robot.hareketEt(robot.getX() + yon.getDx(), robot.getY() + yon.getDy());
        bataryaKontrolcu.hareketTuketimUygula();
        return true;
    }

    private Yon yonBul(int dx, int dy) {
        for (Yon yon : Yon.values()) {
            if (yon.getDx() == dx && yon.getDy() == dy) return yon;
        }
        return null;
    }

    private Yon sagaYon(Yon yon) {
        return switch (yon) {
            case KUZEY -> Yon.DOGU;
            case DOGU  -> Yon.GUNEY;
            case GUNEY -> Yon.BATI;
            case BATI  -> Yon.KUZEY;
        };
    }

    private Yon solaYon(Yon yon) {
        return switch (yon) {
            case KUZEY -> Yon.BATI;
            case BATI  -> Yon.GUNEY;
            case GUNEY -> Yon.DOGU;
            case DOGU  -> Yon.KUZEY;
        };
    }

    private Yon geriYon(Yon yon) {
        return switch (yon) {
            case KUZEY -> Yon.GUNEY;
            case GUNEY -> Yon.KUZEY;
            case DOGU  -> Yon.BATI;
            case BATI  -> Yon.DOGU;
        };
    }

    private List<Yon> gecerliYonleriGetir() {
        List<Yon> liste = new ArrayList<>();
        for (Yon yon : Yon.values()) {
            int nx = robot.getX() + yon.getDx();
            int ny = robot.getY() + yon.getDy();
            if (!oda.engelMi(nx, ny)) liste.add(yon);
        }
        return liste;
    }

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
