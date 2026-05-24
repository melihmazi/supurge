package com.supurge.controller;

import com.supurge.model.Oda;

import java.util.*;

/**
 * BFS ve A* yol bulma algoritmalarını içeren controller.
 * Robotun şarj istasyonuna en kısa yoldan dönmesini sağlar.
 */
public class YolBulmaKontrolcu {

    private final Oda oda;

    public YolBulmaKontrolcu(Oda oda) {
        this.oda = oda;
    }

    /**
     * BFS ile iki nokta arasındaki en kısa yolu bulur.
     * @return koordinat listesi (başlangıç dahil), yol yoksa null
     */
    public List<int[]> bfsYolBul(int baslangicX, int baslangicY, int hedefX, int hedefY) {
        // Başlangıç = hedef
        if (baslangicX == hedefX && baslangicY == hedefY) {
            List<int[]> tek = new ArrayList<>();
            tek.add(new int[]{baslangicX, baslangicY});
            return tek;
        }

        Queue<List<int[]>> kuyruk = new LinkedList<>();
        Set<String> ziyaretEdilen = new HashSet<>();

        List<int[]> baslangicYol = new ArrayList<>();
        baslangicYol.add(new int[]{baslangicX, baslangicY});
        kuyruk.add(baslangicYol);
        ziyaretEdilen.add(anahtar(baslangicX, baslangicY));

        int[][] yonler = {{0, -1}, {0, 1}, {1, 0}, {-1, 0}};

        while (!kuyruk.isEmpty()) {
            List<int[]> mevcutYol = kuyruk.poll();
            int[] son = mevcutYol.get(mevcutYol.size() - 1);

            for (int[] yon : yonler) {
                int nx = son[0] + yon[0];
                int ny = son[1] + yon[1];
                String k = anahtar(nx, ny);

                if (!oda.engelMi(nx, ny) && !ziyaretEdilen.contains(k)) {
                    ziyaretEdilen.add(k);
                    List<int[]> yeniYol = new ArrayList<>(mevcutYol);
                    yeniYol.add(new int[]{nx, ny});

                    if (nx == hedefX && ny == hedefY) return yeniYol;
                    kuyruk.add(yeniYol);
                }
            }
        }
        return null; // yol bulunamadı
    }

    /**
     * A* ile iki nokta arasındaki en kısa yolu bulur.
     * Manhattan mesafesi heuristik olarak kullanılır.
     * @return koordinat listesi (başlangıç dahil), yol yoksa null
     */
    public List<int[]> aYildizYolBul(int baslangicX, int baslangicY, int hedefX, int hedefY) {
        PriorityQueue<int[]> acikListe = new PriorityQueue<>(Comparator.comparingInt(a -> a[2]));
        Map<String, String> onceki = new HashMap<>();
        Map<String, Integer> gMaliyet = new HashMap<>();

        String baslangicAnahtar = anahtar(baslangicX, baslangicY);
        acikListe.add(new int[]{baslangicX, baslangicY, 0});
        gMaliyet.put(baslangicAnahtar, 0);
        onceki.put(baslangicAnahtar, null);

        int[][] yonler = {{0, -1}, {0, 1}, {1, 0}, {-1, 0}};

        while (!acikListe.isEmpty()) {
            int[] mevcut = acikListe.poll();
            int cx = mevcut[0], cy = mevcut[1];

            if (cx == hedefX && cy == hedefY) {
                return yoluYeniden(onceki, hedefX, hedefY);
            }

            for (int[] yon : yonler) {
                int nx = cx + yon[0];
                int ny = cy + yon[1];
                if (oda.engelMi(nx, ny)) continue;

                String komsuAnahtar = anahtar(nx, ny);
                int yeniG = gMaliyet.get(anahtar(cx, cy)) + 1;

                if (!gMaliyet.containsKey(komsuAnahtar) || yeniG < gMaliyet.get(komsuAnahtar)) {
                    gMaliyet.put(komsuAnahtar, yeniG);
                    int f = yeniG + manhattan(nx, ny, hedefX, hedefY);
                    acikListe.add(new int[]{nx, ny, f});
                    onceki.put(komsuAnahtar, anahtar(cx, cy));
                }
            }
        }
        return null;
    }

    // ---- Yardımcı Metodlar ----

    private List<int[]> yoluYeniden(Map<String, String> onceki, int hedefX, int hedefY) {
        LinkedList<int[]> yol = new LinkedList<>();
        String mevcut = anahtar(hedefX, hedefY);
        while (mevcut != null) {
            String[] parca = mevcut.split(",");
            yol.addFirst(new int[]{Integer.parseInt(parca[0]), Integer.parseInt(parca[1])});
            mevcut = onceki.get(mevcut);
        }
        return yol;
    }

    private int manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private String anahtar(int x, int y) {
        return x + "," + y;
    }
}
