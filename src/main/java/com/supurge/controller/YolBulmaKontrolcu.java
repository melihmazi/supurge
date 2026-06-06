package com.supurge.controller;

import com.supurge.model.Oda;

import java.util.*;


public class YolBulmaKontrolcu {

    private final Oda oda;

    public YolBulmaKontrolcu(Oda oda) {
        this.oda = oda;
    }

    // BFS (Breadth-First Search)
    // her yöne eşit yayıldığı için yavaştır ama en kısa yolu garanti eder.

    public List<int[]> bfsYolBul(int baslangicX, int baslangicY, int hedefX, int hedefY) {
        // zaten hedefteyiz bir şey aramaya gerek yok
        if (baslangicX == hedefX && baslangicY == hedefY) {
            List<int[]> tek = new ArrayList<>();
            tek.add(new int[]{baslangicX, baslangicY});
            return tek;
        }

        // incelenecek yolların listesi (İlk giren ilk çıkar)
        Queue<List<int[]>> kuyruk = new LinkedList<>();

        // sonsuz döngüye girmemek için "buraya daha önce baktım" hafızası
        Set<String> ziyaretEdilen = new HashSet<>();

        // algoritmayı bulunduğumuz noktadan ateşliyoruz
        List<int[]> baslangicYol = new ArrayList<>();
        baslangicYol.add(new int[]{baslangicX, baslangicY});
        kuyruk.add(baslangicYol);
        ziyaretEdilen.add(anahtar(baslangicX, baslangicY));

        int[][] yonler = {{0, -1}, {0, 1}, {1, 0}, {-1, 0}}; // kuzey, güney, doğu, batı

        while (!kuyruk.isEmpty()) {
            List<int[]> mevcutYol = kuyruk.poll(); // kuyruktaki ilk ihtimali al
            int[] son = mevcutYol.get(mevcutYol.size() - 1); // o yolun son adımına bak

            // o son adımdan 4 yöne doğru dalgalan
            for (int[] yon : yonler) {
                int nx = son[0] + yon[0];
                int ny = son[1] + yon[1];
                String k = anahtar(nx, ny);

                // gideceğimiz yer duvar/mobilya değilse ve daha önce bakmadıysak
                if (!oda.engelMi(nx, ny) && !ziyaretEdilen.contains(k)) {
                    ziyaretEdilen.add(k); // İşaretle

                    // eski yolu kopyala, üstüne bu yeni adımı ekle
                    List<int[]> yeniYol = new ArrayList<>(mevcutYol);
                    yeniYol.add(new int[]{nx, ny});

                    // hedefi bulduk mu? bulduysak tüm bu çizilmiş rotayı teslim et
                    if (nx == hedefX && ny == hedefY) return yeniYol;

                    // bulamadıysak bu yeni ihtimali de kuyruğa ekle
                    kuyruk.add(yeniYol);
                }
            }
        }
        return null; // tüm harita tarandı ama hedefe giden bir yol yok
    }


    // A* (A-Star) Algoritması
    // Heuristik (sezgisel) fonksiyon kullanarak sadece hedefe yaklaşan yolları dener. BFS'den çok daha performanslıdır.

    public List<int[]> aYildizYolBul(int baslangicX, int baslangicY, int hedefX, int hedefY) {

        // Öncelikli Kuyruk: F skoru (toplam maliyeti) en düşül olan hücreyi otomatik olarak en öne alır.
        PriorityQueue<int[]> acikListe = new PriorityQueue<>(Comparator.comparingInt(a -> a[2]));

        Map<String, String> onceki = new HashMap<>();

        // Başlangıçtan bu noktaya kadar kaç adım attık
        Map<String, Integer> gMaliyet = new HashMap<>();

        String baslangicAnahtar = anahtar(baslangicX, baslangicY);
        // Dizi yapısı: [X koordinatı, Y koordinatı, F Skoru]
        acikListe.add(new int[]{baslangicX, baslangicY, 0});
        gMaliyet.put(baslangicAnahtar, 0);
        onceki.put(baslangicAnahtar, null);

        int[][] yonler = {{0, -1}, {0, 1}, {1, 0}, {-1, 0}};

        while (!acikListe.isEmpty()) {
            int[] mevcut = acikListe.poll(); // F skoru en iyi olan hücreyi al
            int cx = mevcut[0], cy = mevcut[1];

            // hedefi bulduk şimdi tozları takip ederek rotayı tersten çizcez
            if (cx == hedefX && cy == hedefY) {
                return yoluYeniden(onceki, hedefX, hedefY);
            }

            // Etraftaki 4 kareyi incele
            for (int[] yon : yonler) {
                int nx = cx + yon[0];
                int ny = cy + yon[1];

                // Mobilyanın içinden geçemezsin
                if (oda.engelMi(nx, ny)) continue;

                String komsuAnahtar = anahtar(nx, ny);

                // Komşuya geçmek için attığımız toplam adım sayısı
                int yeniG = gMaliyet.get(anahtar(cx, cy)) + 1;

                // Eğer komşuya daha önce hiç gitmediysek veya şuanki bulduğumuz yol, eski gittiğimiz yoldan daha kısaysa
                if (!gMaliyet.containsKey(komsuAnahtar) || yeniG < gMaliyet.get(komsuAnahtar)) {

                    gMaliyet.put(komsuAnahtar, yeniG); // Yeni en kısa rekoru kaydet

                    int f = yeniG + manhattan(nx, ny, hedefX, hedefY);

                    acikListe.add(new int[]{nx, ny, f}); // İhtimali kuyruğa at
                    onceki.put(komsuAnahtar, anahtar(cx, cy)); // Buraya cx,cy'den geldim diye kaydet
                }
            }
        }
        return null; // Yol yok
    }

    // Hedef bulunduktan sonra, 'onceki' haritasını kullanarak hedeften başlangıca doğru yolu çıkarır.
    private List<int[]> yoluYeniden(Map<String, String> onceki, int hedefX, int hedefY) {
        LinkedList<int[]> yol = new LinkedList<>(); // Başa ekleme yapacağımız için LinkedList
        String mevcut = anahtar(hedefX, hedefY);

        while (mevcut != null) {
            String[] parca = mevcut.split(",");
            yol.addFirst(new int[]{Integer.parseInt(parca[0]), Integer.parseInt(parca[1])}); // Listeye ekle
            mevcut = onceki.get(mevcut); // Bir önceki hücreye geç
        }
        return yol;
    }

    // Çapraz gidilemediği için X'lerin farkı ile Y'lerin farkını toplayarak kuş uçuşu tahmin yapar.
    private int manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    // Map ve Set'lerde koordinatları tutabilmek için X ve Y'yi "X,Y" şeklinde String'e çevirir.
    private String anahtar(int x, int y) {
        return x + "," + y;
    }
}