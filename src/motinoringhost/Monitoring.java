package motinoringhost;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 *
 * @author antoxa
 */
public class Monitoring extends com.victorlaerte.asynctask.AsyncTask {
    
    enum color {
        GREEN("#00FF00"),
        RED("#FF0000"),
        GRAY("#808080");

        private String code;

        private color(String code) {
            this.code = code;
        }

        public String getColor() {
            return code;
        }
    }

    /**
     * Флаг на запуск потока.
     */
    private boolean isRun;

    /**
     * ФЛаг на событие.
     */
    static private boolean alert;

    /**
     * Список с проверяемыми адресами.
     */
    private Map<String, Integer> addressList;

    /**
     * Контроллер отображения.
     */
    private FXMLDocumentController controller;

    /**
     * Переменная влияющая на продолжение выполнения задания.
     */
    private boolean interrupt;

    public Monitoring(FXMLDocumentController controller) {
        interrupt = false;
        alert = false;
        isRun = false;
        addressList = getHostList();
        this.controller = controller;
    }

    public static void setAlert(boolean alert) {
        Monitoring.alert = alert;
    }

    public boolean getInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }

    public boolean isRun() {
        return isRun;
    }

    /**
     * Определяем контроллер отображения.
     *
     * @param controller
     */
    public void setViewController(FXMLDocumentController controller) {
        this.controller = controller;
    }
    
    /**
     * Метод для формирования списка проверяемых хостов из файла.
     * @return 
     */
    private Map<String, Integer> getHostList() {
        Map<String, Integer> map = new HashMap<>();
        // считываем список хостов и портов из файла
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("hosts.cfg")))) {
            String line;
            while ( (line = reader.readLine()) != null ) {
                String[] arr = line.split(" ");
                if (arr[0].contains("#")) {
                    continue;
                }
                map.put(arr[0], Integer.parseInt(arr[1]));
            }
        } catch (IOException ex) {
            System.out.println("Config file reader error!");
            ex.printStackTrace();
        }
        return map;
    }

    private void task() {
        // список всех проверяемых хостов
        List<AvailableTask> hostList = new ArrayList<>();
        // наполняем список объектами
        addressList.forEach((k, v) -> {
            hostList.add(new AvailableTask(k, v, 5000, this));
        });
        // Список проблемных хостов
        List<AvailableTask> hostListAlert = new ArrayList<>();
        // выполняем цикл, пока не нажата кнопка СТОП
        while (!interrupt) {
            // обнуляем счетчик предупреждений перед каждым циклом
            alert = false;
            // запускаем каждый хост на проверку
            // и ждем пока все хосты закончат проверку
            ExecutorService es = Executors.newCachedThreadPool();
            hostList.forEach((AvailableTask host) -> {
                es.execute(host);
            });
            System.out.println("Запустили все живые хосты");
            hostListAlert.forEach((host) -> es.execute(host));
            System.out.println("Запустили все проблемные хосты");
            es.shutdown();
            try {
                boolean finished = es.awaitTermination(6000, TimeUnit.MILLISECONDS);
                if (finished) {
                    System.out.println("Все процессы закончились удачно");
                } else {
                    System.out.println("Не дохдались завершения!");
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Monitoring.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Дождались пока все проверились");
            // проблемные хосты переносим в отдельный список
            hostList.stream().filter((host) -> (host.getAlert())).forEachOrdered((host) -> {
                hostListAlert.add(host);
                String msg = host.getAlertTime() + ": " + host.getAddress() + " - is problem!";
                progressCallback(msg);
            });
            hostList.removeAll(hostListAlert);
            // возвращаем "выздоровевший" хост в общий список
            hostListAlert.forEach((host) -> {
                if (!host.getAlert()) {
                    hostList.add(host);
                    String msg = host.getAlertTime() + ": " + host.getAddress() + " - is OK!";
                    progressCallback(msg);
                }
            });
            hostListAlert.removeAll(hostList);
            // если все ОК, то цвет зеленый
            if (alert) {
                progressCallback(color.RED);
            } else {
                progressCallback(color.GREEN);
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("LAP");
        }
    }

    public void stop() {
        interrupt = true;
        progressCallback(color.GRAY);
    }

    @Override
    public void onPreExecute() {
        String msg;
        msg = "Контролируемые хосты:\n";
        for (Map.Entry<String, Integer> entry : addressList.entrySet()) {
            msg += entry.getKey() + ", порт: " + entry.getValue() + "\n";
        }
        controller.addMessage(msg);
    }

    @Override
    public void doInBackground() {
        interrupt = false;
        isRun = true;
        task();
    }

    @Override
    public void onPostExecute() {

    }

    @Override
    public void progressCallback(Object... params) {
        if (params[0].getClass().equals(color.class)) {
            controller.setBackgroudColor(params[0].toString());
        } else if (params[0].getClass().equals(String.class)) {
            controller.addMessage(params[0].toString());
        }
        
    }

}
