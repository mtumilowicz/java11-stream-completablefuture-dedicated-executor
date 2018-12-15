import com.google.common.base.Preconditions;

/**
 * Created by mtumilowicz on 2018-12-15.
 */
class Price {
    private int price;

    private Price(int price) {
        this.price = price;
    }
    
    static Price of(int price) {
        Preconditions.checkArgument(price > 0);
        
        return new Price(price);
    }
}
