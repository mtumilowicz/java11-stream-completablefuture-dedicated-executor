/**
 * Created by mtumilowicz on 2018-12-15.
 */
class Shop {
    Price getPrice(int id) {
        Delay.delay();
        return Price.of(id);
    }
}