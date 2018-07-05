import com.scarlatti.SlowService;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * ______    __                         __           ____             __     __  __  _
 * ___/ _ | / /__ ___ ___ ___ ____  ___/ /______    / __/______ _____/ /__ _/ /_/ /_(_)
 * __/ __ |/ / -_|_-<(_-</ _ `/ _ \/ _  / __/ _ \  _\ \/ __/ _ `/ __/ / _ `/ __/ __/ /
 * /_/ |_/_/\__/___/___/\_,_/_//_/\_,_/_/  \___/ /___/\__/\_,_/_/ /_/\_,_/\__/\__/_/
 * Wednesday, 7/4/2018
 */
public class Demo {

    private SlowService slowService;

    @Before
    public void setup() {
        slowService = new SlowService();
    }

    @Test
    public void vanillaLoop() {
        timed(this::doVanillaLoop);
    }

    // approx. 4 seconds
    private void doVanillaLoop() {
        List<String> responses = new ArrayList<>();
        for (String val : data()) {
            String response = slowService.getSomethingSlowly(val);
            responses.add(response);
        }

        System.out.println("responses: " + responses);
    }

    @Test
    public void useForkJoinPool() {
        timed(this::doUseForkJoinPool);
    }

    @Test
    public void useForkJoinPoolWithError() {
        timed(this::doUseForkJoinPoolWithError);
    }

    @Test
    public void useForkJoinPoolWithErrorHandling() {
        timed(this::doUseForkJoinPoolWithErrorHandling);
    }

    @Test
    public void useForkJoinPoolWithErrorHandlingSuccess() {
        timed(this::doUseForkJoinPoolWithErrorHandlingSuccess);
    }

    @Test
    public void useCompletableFutures() {
        timed(this::doUseCompletableFutures);
    }

    @Test
    public void useCompletableFuturesWithErrorHandling() {
        timed(this::doUseCompletableFuturesWithErrorHandling);
    }

    @Test
    public void useVanillaParallelStream() {
        timed(this::doUseVanillaParallelStream);
    }


    private void doUseForkJoinPool() {

        ForkJoinPool forkJoinPool = new ForkJoinPool(8);

        ForkJoinTask<List<String>> task = forkJoinPool.submit(() -> {
            return data()
                .stream()
                .parallel()
                .map(s -> slowService.getSomethingSlowly(s))
                .collect(Collectors.toList());
        });

        List<String> responses = task.join();

        System.out.println("responses: " + responses);
    }

    private void doUseForkJoinPoolWithError() {

        ForkJoinPool forkJoinPool = new ForkJoinPool(8);

        ForkJoinTask<List<String>> task = forkJoinPool.submit(() -> {
            return data()
                .stream()
                .parallel()
                .map(s -> slowService.getSomethingSlowlyWithError(s))
                .collect(Collectors.toList());
        });

        List<String> responses = task.join();

        System.out.println("responses: " + responses);
    }

    private void doUseForkJoinPoolWithErrorHandling() {

        ForkJoinPool forkJoinPool = new ForkJoinPool(8);

        ForkJoinTask<List<String>> task = forkJoinPool.submit(() -> {
            List<String> result = new ArrayList<>();
            data()
                .stream()
                .parallel()
                .forEach(s -> {
                    try {
                        result.add(slowService.getSomethingSlowlyWithError(s));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            return result;
        });

        List<String> responses = task.join();

        System.out.println("responses: " + responses);
    }

    private void doUseForkJoinPoolWithErrorHandlingSuccess() {

        ForkJoinPool forkJoinPool = new ForkJoinPool(8);

        ForkJoinTask<List<String>> task = forkJoinPool.submit(() -> {
            List<String> result = new ArrayList<>();
            data().stream().parallel().forEach(s -> {
                try {
                    result.add(slowService.getSomethingSlowly(s));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            return result;
        });

        List<String> responses = task.join();

        System.out.println("responses: " + responses);
    }

    private void doUseCompletableFutures() {
        Executor executor = Executors.newFixedThreadPool(8);

        List<CompletableFuture<String>> futures = data().stream().map(s ->
            CompletableFuture.supplyAsync(() -> slowService.getSomethingSlowly(s), executor))
            .collect(Collectors.toList());

        List<String> responses = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        System.out.println("responses: " + responses);
    }

    private void doUseCompletableFuturesWithErrorHandling() {
        Executor executor = Executors.newFixedThreadPool(8);

        List<CompletableFuture<String>> futures = data()
            .stream()
            .map(s ->
                CompletableFuture.supplyAsync(() -> slowService.getSomethingSlowlyWithError(s), executor))
            .collect(Collectors.toList());

        List<String> responses = Collections.synchronizedList(new ArrayList<>());

        for (CompletableFuture<String> stringCompletableFuture : futures) {
            System.out.println("joining...");

            try {
                responses.add(stringCompletableFuture.join());
            } catch (Exception e) {
                new RuntimeException("Error processing string", e).printStackTrace();
            }
        }

        System.out.println("responses: " + responses);
    }

    private void doUseVanillaParallelStream() {

        List<String> responses = data()
            .stream()
            .parallel()
            .map(s -> slowService.getSomethingSlowly(s))
            .collect(Collectors.toList());

        System.out.println("responses: " + responses);
    }

    private class UseSlowServiceTask extends RecursiveTask<String> {

        private String val;
        private List<String> vals;

        private UseSlowServiceTask(String val) {
            this.val = val;
        }

        public UseSlowServiceTask(List<String> vals) {
            this.vals = vals;
        }

        @Override
        protected String compute() {
//            if (val != null) {
//                return Collections.singletonList(slowService.getSomethingSlowly(val));
//            } else {
//                return invokeAll(vals
//                    .stream()
//                    .map(UseSlowServiceTask::new)
//                    .collect(Collectors.toList())
//                ).stream()
//                    .map(ForkJoinTask::join)
//                    .collect(Collectors.toList());

            return null;
        }
    }

    private static void timed(Runnable codeUnderTest) {
        Instant start = Instant.now();
        codeUnderTest.run();
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        System.out.println("duration: " + duration);
    }

    public static List<String> data() {
        return Arrays.asList(
            "thing1",
            "thing2",
            "thing3",
            "thing4",
            "thing5",
            "thing6",
            "thing11",
            "thing12",
            "thing13",
            "thing14",
            "thing15",
            "thing16"
        );
    }
}
