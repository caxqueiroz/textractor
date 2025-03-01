package one.cax.textractor.ocr;

import com.abbyy.FREngine.Engine;
import com.abbyy.FREngine.IEngine;
import one.cax.textractor.config.OcrConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class AbbyyEnginePool {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private  BlockingQueue<IEngine> enginePool;
    private ExecutorService executorService;
    private BlockingQueue<Runnable> taskQueue;
    private final OcrConfig config;
    /**
     *
     * @param config
     */
    public AbbyyEnginePool(@Autowired OcrConfig config) {
        this.config = config;
    }

    public void initialize() {
        int coreCount = Runtime.getRuntime().availableProcessors();
        this.enginePool = new ArrayBlockingQueue<>(coreCount);
        this.taskQueue = new LinkedBlockingQueue<>();
        this.executorService = Executors.newFixedThreadPool(coreCount);

        // Initialize the pool with Engine instances
        for (int i = 0; i < coreCount; i++) {
            try {
                IEngine engine = Engine.InitializeEngine(
                        config.getLibFolder(),
                        config.getCustomerProjectId(),
                        config.getLicensePath(),
                        config.getLicensePassword(),
                        "",
                        "",
                        false
                );
                enginePool.offer(engine);
                logger.info("Engine {} initialized", i);

            } catch (Exception e) {
                // Handle initialization error
                throw new RuntimeException("Failed to initialize engine", e);
            }
        }

        // Start the task processor
        startTaskProcessor();
    }

    public void submitTask(Runnable task) {
        taskQueue.offer(task);
    }

    private void startTaskProcessor() {
        new Thread(() -> {
            while (true) {
                try {
                    Runnable task = taskQueue.take(); // This will block if the queue is empty
                    processTask(task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    private void processTask(Runnable task) {
        executorService.submit(() -> {
            IEngine engine = null;
            try {
                engine = enginePool.take(); // This will block if no engine is available
                // Use the engine to process the task
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                if (engine != null) {
                    enginePool.offer(engine); // Return the engine to the pool
                }
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
        try {

            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            // deinitialize engines
            for (IEngine engine : enginePool) {
                Engine.DeinitializeEngine();
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }
}
