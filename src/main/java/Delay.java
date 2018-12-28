/**
 * Created by mtumilowicz on 2018-12-28.
 */
class Delay {
    static void delay() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            // not used
        }
    }
}
