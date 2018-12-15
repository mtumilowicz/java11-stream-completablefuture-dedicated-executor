import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Created by mtumilowicz on 2018-12-15.
 */
public class ShopTest {

    private Shop shop = new Shop();

    @Test
    public void naive() {
        List<CompletableFuture<Price>> priceFutures = IntStream.range(1, 300)
                .parallel()
                .mapToObj(id -> CompletableFuture.supplyAsync(
                        () -> shop.getPrice(id)))
                .collect(toList());

        List<Price> prices = priceFutures.stream()
                .parallel()
                .map(CompletableFuture::join)
                .collect(toList());
    }

    @Test
    public void dedicated_pool() {
        Executor executor =
                Executors.newFixedThreadPool(Math.min(300, 100),
                        r -> {
                            Thread t = new Thread(r);
                            t.setDaemon(true);
                            return t;
                        });

        List<CompletableFuture<Price>> priceFutures = IntStream.range(1, 300)
                .parallel()
                .mapToObj(id -> CompletableFuture.supplyAsync(
                        () -> shop.getPrice(id), executor))
                .collect(toList());

        List<Price> prices = priceFutures.parallelStream()
                .map(CompletableFuture::join)
                .collect(toList());
    }
}
