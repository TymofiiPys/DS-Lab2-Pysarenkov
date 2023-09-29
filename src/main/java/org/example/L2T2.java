package org.example;

public class L2T2 {
    private final double[] propertyPrice;
    private volatile static double bufferWtoTr = 0;
    private volatile static double bufferTrtoCounter = 0;
    private int w, tr, c;

    private static Object syncWtoTr = new Object();
    private static Object syncTrtoCounter = new Object();

    public L2T2(double[] propertyPrice){
        this.propertyPrice = propertyPrice;
    }

    class GoodsAccess {
        double buffer;
        boolean valueSet = false;
        synchronized double get() {
            while (!valueSet) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            valueSet = false;
            notify();
            return buffer;
        }

        synchronized void put(double d) {
            while (valueSet) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.buffer = d;
            valueSet = true;
            notify();
        }
    }

    class Producer implements Runnable {
        GoodsAccess g;

        Producer (GoodsAccess g){
            this.g = g;
        }
        public void run(){
            int i = 0;
            while(true){
                g.put(propertyPrice[i++]);
            }
        }
    }

    class ProducerConsumer implements Runnable {
        GoodsAccess g1;
        GoodsAccess g2;

        ProducerConsumer (GoodsAccess g1, GoodsAccess g2){
            this.g1 = g1;
            this.g2 = g2;
            new Thread(this, "Іванов").start();
        }
        public void run(){
            while(true){
                g2.put(g1.get());
            }
        }
    }

    class Consumer implements Runnable {
        GoodsAccess g;

        Consumer (GoodsAccess g){
            this.g = g;
        }
        public void run(){
            while(true){
                g.get();
            }
        }
    }

    private Thread getWarehouseThread(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (syncWtoTr) {
                    double[] takenProperty = propertyPrice;
                    while (w < takenProperty.length) {
                        if (w == 0) {
                            System.out.println("Іванов почав брати майно зі складу...");
                        }
                        while(!(w == tr)){
                            try {
                                syncWtoTr.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        bufferWtoTr = takenProperty[w];
                        System.out.println("Іванов взяв об'єкт №" + (w+1) + " зі складу");
                        w++;
                        syncWtoTr.notify();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, "Warehouse");
    }

    private Thread getTruckThread(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                double midiBuf;
                while (tr < propertyPrice.length) {
                    if (tr == 0) {
                        System.out.println("Петров почав завантаження майна у вантажівку...");
                    }
                    synchronized (syncWtoTr) {
                        while(!(tr < w)) {
                            try {
                                syncWtoTr.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        midiBuf = bufferWtoTr;
                        System.out.println("Петров взяв об'єкт №" + (tr+1) + " у Іванова");
                        syncWtoTr.notify();
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    synchronized (syncTrtoCounter){
                        while(!(tr == c)) {
                            try {
                                syncTrtoCounter.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        bufferTrtoCounter = midiBuf;
                        System.out.println("Петров загрузив об'єкт №" + (tr+1) + " у вантажівку");
                        tr++;
                        syncTrtoCounter.notify();
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, "Truck");
    }

    private Thread getCounterThread(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (syncTrtoCounter) {
                    double[] receivedProperty = new double[propertyPrice.length];
                    while (c < receivedProperty.length) {
                        if (c == 0) {
                            System.out.println("Нечипорчук почав рахувати вартість накраденого майна...");
                        }
                        while(!(c < tr)) {
                            try {
                                syncTrtoCounter.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        receivedProperty[c] = bufferTrtoCounter;
                        System.out.println("Нечипорчук додав об'єкт №" + (c+1) + " у список накраденого");
                        c++;
                        syncTrtoCounter.notify();
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    double sum = 0;
                    for (double prop :
                            receivedProperty) {
                        sum += prop;
                    }
                    System.out.println("Загальна вартість накраденого майна складає " +
                            (int)Math.floor(sum) + " гривень " + (int)((sum - Math.floor(sum)) * 100) + " копійок.");
                }
            }
        }, "Counter");
    }

    private double sumOf(double[] arr){
        double sum = 0;
        for (double d:
             arr) {
            sum += d;
        }
        return sum;
    }

    private void launchRobbery(){
        Thread ivanov = getWarehouseThread();
        Thread petrov = getTruckThread();
        Thread nechyporchuk = getCounterThread();
        ivanov.start();
        petrov.start();
        nechyporchuk.start();
        try {
            ivanov.join();
            petrov.join();
            nechyporchuk.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        double[] propertyPrice = new double[]{68.30, 20.21, 63.05, 51.16, 27.14,
                25.18, 42.85, 64.96, 48.15, 69.91,
                26.41, 87.73, 7.89, 92.65, 20.05,
                78.96, 74.11, 5.86, 15.13, 95.10,
                43.97, 88.34, 85.42, 45.84, 9696, 2.67, 59.62};
        L2T2 l = new L2T2(propertyPrice);
        System.out.println("Очікувана сума: " + l.sumOf(propertyPrice));
        l.launchRobbery();
    }
}