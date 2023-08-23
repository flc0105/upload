package flc.upload.config;

import flc.upload.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import static flc.upload.manager.impl.LogManagerImpl.LOGS;
import static flc.upload.manager.impl.LogManagerImpl.FILENAME;


@Component
public class ShutdownListener implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        SerializationUtil.saveToFile(LOGS, FILENAME);
        Logger logger = LoggerFactory.getLogger(ShutdownListener.class);
        logger.info("日志保存成功");
    }
}