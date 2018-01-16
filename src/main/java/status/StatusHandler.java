package status;

import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by evgeniyh on 1/15/18.
 */

public class StatusHandler {
    private final static Logger logger = Logger.getLogger(StatusHandler.class);

    private final Object listSync = new Object();
    private final List<ServiceStatus> statusList;

    public StatusHandler(int frequencyInSec) {
        statusList = loadStatusFromConfigFile();

        startChecksThread(frequencyInSec * 60 * 1000);
        logger.info("The check thread has been started");
    }

    private void startChecksThread(int sleepDuration) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        synchronized (listSync) {
                            logger.info("Running check");
                            statusList.forEach(ServiceStatus::runCheck);
                        }
                        logger.info("Sleeping for - " + sleepDuration);
                        Thread.sleep(sleepDuration);
                    }
                } catch (Exception ex) {
                    logger.error("Failure during execution of the health checks thread", ex);
                    run();
                }
            }
        }).start();
    }

    public List<String> getStatusesHtmls() {
        synchronized (listSync) {
            return statusList.stream().map(ServiceStatus::generateHtml).collect(Collectors.toList());
        }
    }

    private List<ServiceStatus> loadStatusFromConfigFile() {
        List<ServiceStatus> list = new LinkedList<>();
        list.add(new ServiceStatus("Messaging-Service", new StatusCheck("http://9.148.10.164:998/health-check", 200, "OK")));
        list.add(new ServiceStatus("Dummy-Fails-Service", new StatusCheck("http://no_url_fail", 200, "OK")));
        return list;
    }
//
//    private class HealhChecker implements Runnable{
//        private int sleepDuration;
//
//        public HealhChecker(Object listSync, int sleepDuration) {
//            this.sleepDuration = sleepDuration;
//        }
//
//        @Override
//        public void run() {
//            while
//        }
//    }
}

