/**
 * Created by mtumilowicz on 2018-12-15.
 */
class Shop {
    Price getPrice(int id) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            // not used
        }
        return Price.of(id);
    }
}