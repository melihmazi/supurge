package com.supurge.model;

/* simülasyonun anlık durumunu tutar
controller ile View arasında veri taşır
view yalnızca bu sınıfı okur, model nesnelerine doğrudan erişmez
 */
public class SimulasyonDurumu {

    private int robotX;
    private int robotY;
    private Yon yon;
    private double bataryaYuzdesi;
    private int toplamHucre;
    private int temizlenenHucre;
    private int kalanKirliHucre;
    private long gecenSure;
    private int toplamTemizlenenKir;
    private double robotHiz;          // AnaEkran'ın hız hesabı için
    private boolean sarjaDonuyorMu;   //uyarı göstermek için
    private String bataryaDurumu;     //iyi, orta, düşük, kritik
    private boolean calisiyor;        // robot çalışıyor mu
    private boolean tamamlandi;       // tüm alan temizlendi mi

    public SimulasyonDurumu() {}

    public double temizlenenYuzde() {
        if (toplamHucre == 0) return 0;
        return (temizlenenHucre * 100.0) / toplamHucre;
    }


    public int getRobotX() { return robotX; }
    public void setRobotX(int robotX) { this.robotX = robotX; }

    public int getRobotY() { return robotY; }
    public void setRobotY(int robotY) { this.robotY = robotY; }

    public Yon getYon() { return yon; }
    public void setYon(Yon yon) { this.yon = yon; }

    public double getBataryaYuzdesi() { return bataryaYuzdesi; }
    public void setBataryaYuzdesi(double bataryaYuzdesi) { this.bataryaYuzdesi = bataryaYuzdesi; }

    public int getToplamHucre() { return toplamHucre; }
    public void setToplamHucre(int toplamHucre) { this.toplamHucre = toplamHucre; }

    public int getTemizlenenHucre() { return temizlenenHucre; }
    public void setTemizlenenHucre(int temizlenenHucre) { this.temizlenenHucre = temizlenenHucre; }

    public int getKalanKirliHucre() { return kalanKirliHucre; }
    public void setKalanKirliHucre(int kalanKirliHucre) { this.kalanKirliHucre = kalanKirliHucre; }

    public long getGecenSure() { return gecenSure; }
    public void setGecenSure(long gecenSure) { this.gecenSure = gecenSure; }

    public int getToplamTemizlenenKir() { return toplamTemizlenenKir; }
    public void setToplamTemizlenenKir(int toplamTemizlenenKir) { this.toplamTemizlenenKir = toplamTemizlenenKir; }

    public double getRobotHiz() { return robotHiz; }
    public void setRobotHiz(double robotHiz) { this.robotHiz = robotHiz; }

    public boolean isSarjaDonuyorMu() { return sarjaDonuyorMu; }
    public void setSarjaDonuyorMu(boolean sarjaDonuyorMu) { this.sarjaDonuyorMu = sarjaDonuyorMu; }

    public String getBataryaDurumu() { return bataryaDurumu; }
    public void setBataryaDurumu(String bataryaDurumu) { this.bataryaDurumu = bataryaDurumu; }

    public boolean isCalisiyor() { return calisiyor; }
    public void setCalisiyor(boolean calisiyor) { this.calisiyor = calisiyor; }

    public boolean isTamamlandi() { return tamamlandi; }
    public void setTamamlandi(boolean tamamlandi) { this.tamamlandi = tamamlandi; }
}
