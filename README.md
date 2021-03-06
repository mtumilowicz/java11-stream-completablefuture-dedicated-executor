[![Build Status](https://travis-ci.com/mtumilowicz/java11-stream-completablefuture-dedicated-executor.svg?branch=master)](https://travis-ci.com/mtumilowicz/java11-stream-completablefuture-dedicated-executor)

# java11-stream-completablefuture-dedicated-executor
_Reference_: https://www.amazon.com/Modern-Java-Action-functional-programming/dp/1617293563

# project description
1. We could ask shop for a price of a product 
(by its id)
    ```
    class Shop {
        Price getPrice(int id) {
            Delay.delay();
            return Price.of(id);
        }
    }
    ```
1. `Shop` answers with some delay (`200` ms)
    ```
    class Delay {
        static void delay() {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // not used
            }
        }
    }
    ```
1. we want to ask shop for many ids (for example 
we have a stream of ids)

# solution
* **naive approach** - scales badly
    * one product
        ```
        var priceFutures = IntStream.range(1, 2)
                .parallel()
                .mapToObj(id -> CompletableFuture.supplyAsync(
                        () -> shop.getPrice(id)))
                .collect(toList());
        
        var prices = priceFutures.stream()
                .parallel()
                .map(CompletableFuture::join)
                .collect(toList());
        ```
        **time: 203 ms**
    * four products
        ```
        var priceFutures = IntStream.range(1, 4)
                .parallel()
                .mapToObj(id -> CompletableFuture.supplyAsync(
                        () -> shop.getPrice(id)))
                .collect(toList());
        
        var prices = priceFutures.stream()
                .parallel()
                .map(CompletableFuture::join)
                .collect(toList());
        ```
        **time: 203 ms**
    * many products
        ```
        var priceFutures = IntStream.range(1, 30)
                .parallel()
                .mapToObj(id -> CompletableFuture.supplyAsync(
                        () -> shop.getPrice(id)))
                .collect(toList());
        
        var prices = priceFutures.stream()
                .parallel()
                .map(CompletableFuture::join)
                .collect(toList());
        ```
        **time: 2 s**
* **dedicated executor** - scales perfectly
    ```
    var size = 300;
        
    var executor =
            Executors.newFixedThreadPool(Math.min(size, 100),
                    r -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        return t;
                    });

    var priceFutures = IntStream.range(1, size)
            .parallel()
            .mapToObj(id -> CompletableFuture.supplyAsync(
                    () -> shop.getPrice(id), executor))
            .collect(toList());

    var prices = priceFutures.parallelStream()
            .map(CompletableFuture::join)
            .collect(toList());
    ```
    **time: 668 ms**
    
# explanation
`CompletableFuture` and `parallel` streams **internally 
use the same common pool** that by default has a fixed 
number of threads equal to the one returned by 
`Runtime.getRuntime().availableProcessors()`.

So we decide to prepare dedicated executor for 
`CompletableFuture` tasks. How we estimated
the number of threads in a fixed pool?
From the given formula:

* `Nthreads = NCPU * UCPU * (1 + W/C)`
    * NCPU is the number of cores, available through 
    `Runtime.getRuntime().availableProcessors()`
    * UCPU is the target CPU utilization (between 0 and 1), and
    * W/C is the ratio of wait time to compute time