package common;

/**
 * Created by evgeniyh on 6/25/18.
 */

public class Incident {
    private final long time;
    private final String service;
    private final String message;

    public Incident(long time, String service, String message) {
        this.time = time;
        this.service = service;
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public String getService() {
        return service;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Incident {" +
                "time=" + time +
                ", service='" + service + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
