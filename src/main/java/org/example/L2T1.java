package org.example;
import java.util.Random;
import java.util.concurrent.RecursiveAction;
public class L2T1 {
    private int winnieLocation; //Місцезнаходження Вінні-Пуха

    private static int getChunkSize(final int nChunks, final int nElements) {
        // Integer ceil
        return (nElements + nChunks - 1) / nChunks;
    }

    private static int getChunkStartInclusive(final int chunk,
                                              final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }

    private static int getChunkEndExclusive(final int chunk, final int nChunks,
                                            final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }
    private class SeekTask extends RecursiveAction{
        private final int startArea; //Номер ділянки, з якої зграя починає свій пошук
        private final int finishArea; //Номер ділянки, по досягненню якої зграя завершує свій пошук
        private final int seekerNumber; //Номер зграї
        public SeekTask(final int startArea, final int finishArea,
                        final int seekerNumber){
            this.startArea = startArea;
            this.finishArea = finishArea;
            this.seekerNumber = seekerNumber;
        }
        @Override
        protected void compute() {
            System.out.println("Зграя №" + seekerNumber + " розпочала пошук.");
            for (int i = startArea; i < finishArea; i++) {
                if(i == winnieLocation) {
                    System.out.println("Зграя №" + seekerNumber + " знайшла Вінні-Пуха! Час на покарання і святкового обіду для зграї!");
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Зграя №" + seekerNumber + " завершила пошук, не знайшовши злочинця.");
        }
    }

    /**
     *
     * @param numTasks - кількість зграй, що шукають Вінні-Пуха
     * @param mapSize - Карта складається з mapSize атомарних ділянок
     */
    public void FindThePooh(int numTasks, int mapSize){
        //На одній із mapSize ділянок знаходиться Вінні-Пух
        winnieLocation = new Random().nextInt(mapSize);

        SeekTask[] st = new SeekTask[numTasks];

        System.out.println("Вінні знаходиться на ділянці " + winnieLocation + "!");

        for (int i = 0; i < numTasks; i++) {
            st[i] = new SeekTask(getChunkStartInclusive(i, numTasks, mapSize),
                    getChunkEndExclusive(i, numTasks, mapSize),
                    i+1);
        }
        for (int i = 1; i < numTasks; i++) {
            st[i].fork();
        }
        st[0].compute();
        for (int i = 1; i < numTasks; i++) {
            st[i].join();
        }
    }
    public static void main(String[] args) {
        L2T1 l = new L2T1();
        l.FindThePooh(6, 72);
    }
}
