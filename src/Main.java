import java.util.concurrent.Semaphore;

class BarberShop {
    private static final int NUM_CHAIRS = 5; // Количество стульев в парикмахерской
    private static final Semaphore customers = new Semaphore(0); // Семафор для клиентов
    private static final Semaphore barber = new Semaphore(0); // Семафор для парикмахера
    private static final Semaphore accessSeats = new Semaphore(1); // Семафор для доступа к стульям
    private static int numCustomers = 0; // Количество ожидающих клиентов

    static class Customer implements Runnable {
        private final int id;

        public Customer(int id) {
            this.id = id;
        }

        public void run() {
            try {
                accessSeats.acquire(); // Попытка занять стул в парикмахерской

                if (numCustomers == NUM_CHAIRS) {
                    System.out.println("Customer " + id + " is leaving the barber shop because it's full.");
                    accessSeats.release();
                    return;
                }

                numCustomers++;
                System.out.println("Customer " + id + " is waiting in the barber shop. Total customers: " + numCustomers);

                accessSeats.release();
                customers.release(); // Будим парикмахера
                barber.acquire(); // Ожидание парикмахера

                // Парикмахер стрижет волосы клиенту
                System.out.println("Barber is cutting hair for customer " + id);
                Thread.sleep(2000);

                System.out.println("Barber finished cutting hair for customer " + id);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Barber implements Runnable {
        public void run() {
            while (true) {
                try {
                    customers.acquire(); // Ожидание клиента
                    accessSeats.acquire(); // Попытка получить доступ к стульям
                    numCustomers--;
                    System.out.println("Barber is cutting hair. Remaining customers: " + numCustomers);
                    barber.release(); // Разрешаем клиенту сесть в кресло
                    accessSeats.release(); // Освобождаем стулья

                    // Парикмахер стрижет волосы клиента
                    Thread.sleep(3000);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) {
        Thread barberThread = new Thread(new Barber());
        barberThread.start();

        for (int i = 1; i <= 10; i++) {
            Thread customerThread = new Thread(new Customer(i));
            customerThread.start();
            try {
                Thread.sleep(1000); // Задержка между приходом клиентов
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}