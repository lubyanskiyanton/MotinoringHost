package motinoringhost;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Класс проверяющийдоступность хостов.
 *
 * @author antoxa
 */
public class AvailableTask implements Runnable {

    private final Monitoring monitoring;

    private final String address;

    private final int port;

    private final int timeOutMillis;

    private boolean alert;
    
    private String alertTime;

    public AvailableTask(String address, int port, int timeOutMillis, Monitoring monitoring) {
        this.address = address;
        this.port = port;
        this.timeOutMillis = timeOutMillis;
        this.monitoring = monitoring;
        alert = false;
    }

    /**
     * Метод для проверки доступности порта на адресе.
     *
     * @param addr - адрес.
     * @param port - номер порта.
     * @param timeOutMillis - таймаут в милисекундах.
     * @return
     */
    private void isReachable() {
        // Any Open port on other machine
        // port = 7 - icmp, 22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.
        if (7 == port) {
            try {
                InetAddress addr = InetAddress.getByName(address);
                boolean reacheble = addr.isReachable(timeOutMillis);
                if (!reacheble) {
                    alertIsTrue();
                } else {
                    alertIsFalse();
                }
            } catch (IOException ex) {
                alertIsTrue();
            }
        } else {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(address, port), timeOutMillis);
                alertIsFalse();
            } catch (IOException ex) {
                alertIsTrue();
            }
        }
    }

    public String getAddress() {
        return address;
    }
    
    public boolean getAlert() {
        return alert;
    }
    
    public String getAlertTime() {
        return alertTime;
    }
    
    private void alertIsTrue() {
        alert = true;
        Monitoring.setAlert(true);   
        alertTime = getDateTime();
    }

    private void alertIsFalse() {
        alert = false;
        alertTime = getDateTime();
    }

    private String getDateTime() {
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        Calendar calendar = new GregorianCalendar();        
        return format.format(calendar.getTime());
    }

    @Override
    public void run() {
        System.out.println("Starting monitoring host - " + address);
        isReachable();
        System.out.println(address + " is " + alert);
    }

}
