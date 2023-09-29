package org.example;

public class L2T2 {
    private final double[] propertyPrice;
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
            buffer = d;
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
            System.out.println("Іванов почав брати майно зі складу...");

            while(i < propertyPrice.length){
                g.put(propertyPrice[i]);
                System.out.println("Іванов взяв об'єкт №" + (i+1) + " зі складу");
                i++;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ProducerConsumer implements Runnable {
        GoodsAccess g1;
        GoodsAccess g2;

        ProducerConsumer (GoodsAccess g1, GoodsAccess g2){
            this.g1 = g1;
            this.g2 = g2;
        }
        public void run(){
            int gotAndPut = 0;
            System.out.println("Петров почав завантаження майна у вантажівку...");
            while(gotAndPut < propertyPrice.length){
                double d = g1.get();
                System.out.println("Петров взяв об'єкт №" + (gotAndPut+1) + " у Іванова");

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                g2.put(d);
                System.out.println("Петров загрузив об'єкт №" + (gotAndPut+1) + " у вантажівку");
                gotAndPut++;

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Consumer implements Runnable {
        GoodsAccess g;

        Consumer (GoodsAccess g){
            this.g = g;
        }
        public void run(){
            int got = 0;
            double[] receivedProperty = new double[propertyPrice.length];
            System.out.println("Нечипорчук почав рахувати вартість накраденого майна...");

            while(got < propertyPrice.length){
                receivedProperty[got] = g.get();
                System.out.println("Нечипорчук додав об'єкт №" + (got+1) + " у список накраденого");
                got++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            double sum = sumOf(receivedProperty);
            System.out.println("Загальна вартість накраденого майна складає " +
                    (int)Math.floor(sum) + " гривень " + (int)((sum - Math.floor(sum)) * 100) + " копійок.");
        }
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
        GoodsAccess g1 = new GoodsAccess();
        GoodsAccess g2 = new GoodsAccess();
        Thread ivanov = new Thread(new Producer(g1), "Warehouse");
        Thread petrov = new Thread(new ProducerConsumer(g1, g2), "TruckLoader");
        Thread nechyporchuk = new Thread(new Consumer(g2), "Counter");
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
                43.97, 88.34, 85.42, 45.84, 96.96, 2.67, 59.62};
        L2T2 l = new L2T2(propertyPrice);
        System.out.println("Очікувана сума: " + l.sumOf(propertyPrice));
        l.launchRobbery();
    }
}