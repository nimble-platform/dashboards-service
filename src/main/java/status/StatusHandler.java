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
    }

    private void startChecksThread(int sleepDuration) {
        Runnable r = new Runnable() {
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
        };
        r.run();
    }

    public List<String> getStatusesHtmls() {
        synchronized (listSync) {
            return statusList.stream().map(ServiceStatus::generateHtml).collect(Collectors.toList());
        }
    }

    private List<ServiceStatus> loadStatusFromConfigFile() {
        return new LinkedList<>();
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

