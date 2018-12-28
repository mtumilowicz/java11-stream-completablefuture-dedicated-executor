import org.junit.Test;

import java.util.concurrent.CompletableFuture;
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
        long start = System.currentTimeMillis();

        var priceFutures = IntStream.range(1, 2)
                .parallel()
                .mapToObj(id -> CompletableFuture.supplyAsync(
                        () -> shop.getPrice(id)))
                .collect(toList());

        var prices = priceFutures.stream()
                .parallel()
                .map(CompletableFuture::join)
                .collect(toList());

        System.out.println(System.currentTimeMillis() - start);
    }
    
    @Test
    public void naive_4() {
        long start = System.currentTimeMillis();

        var priceFutures = IntStream.range(1, 4)
                .parallel()
                .mapToObj(id -> CompletableFuture.supplyAsync(
                        () -> shop.getPrice(id)))
                .collect(toList());

        var prices = priceFutures.stream()
                .parallel()
                .map(CompletableFuture::join)
                .collect(toList());

        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void naive_scalesBadly() {
        long start = System.currentTimeMillis();
        
        var priceFutures = IntStream.range(1, 30)
                .parallel()
                .mapToObj(id -> CompletableFuture.supplyAsync(
                        () -> shop.getPrice(id)))
                .collect(toList());

        var prices = priceFutures.stream()
                .parallel()
                .map(CompletableFuture::join)
                .collect(toList());

        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void dedicated_pool() {
        long start = System.currentTimeMillis();
        
        var executor =
                Executors.newFixedThreadPool(Math.min(300, 100),
                        r -> {
                            Thread t = new Thread(r);
                            t.setDaemon(true);
                            return t;
                        });

        var priceFutures = IntStream.range(1, 300)
                .parallel()
                .mapToObj(id -> CompletableFuture.supplyAsync(
                        () -> shop.getPrice(id), executor))
                .collect(toList());

        var prices = priceFutures.parallelStream()
                .map(CompletableFuture::join)
                .collect(toList());

        System.out.println(System.currentTimeMillis() - start);
    }
}
