import java.util.*;
import java.util.stream.Collectors;

// TODO: add locks to handle multi-threading
// TODO: add test cases
public class OrderBook {
    private final TreeSet<OrderBookLevel> bids;
    private final TreeSet<OrderBookLevel> asks;
    private final HashMap<Long, OrderBookLevel> OrderBookLevelCache;
    private final Comparator<OrderBookLevel> sortByTime = Comparator.comparing(OrderBookLevel::getTimeInNanos);
    private final Comparator<OrderBookLevel> sortByPrice = Comparator.comparing(OrderBookLevel::getPrice);
    private final char BID = 'B';
    private final char OFFER = 'O';

    public OrderBook() {
        bids = new TreeSet<>(sortByPrice.reversed().thenComparing(sortByTime));
        asks = new TreeSet<>(sortByPrice.thenComparing(sortByTime));
        OrderBookLevelCache = new HashMap<>();
    }

    public OrderBookLevel addOrder(Order order){
        if(order == null) {
            System.out.println(String.format("Order is null, failed to add"));
            return null;
        }

        try {
            OrderBookLevel level = new OrderBookLevel( System.nanoTime(),order);
            if (order.getSide() == BID) {
                bids.add(level);
                OrderBookLevelCache.put(order.getId(), level);
            } else if (order.getSide() == OFFER) {
                asks.add(level);
                OrderBookLevelCache.put(order.getId(), level);
            }
            else{
                throw new Exception(String.format("Unsupported side %s", order.getSide()));
            }
            return level;
        }catch (Exception e){
            System.out.println(String.format("Failed to add order %s due to exception %s", order.getId(), e.getMessage()));
        }
        return null;
    }

    public OrderBookLevel removeOrder(String orderId){
        if(!OrderBookLevelCache.containsKey(orderId)) {
            System.out.println(String.format("Order %s is not found, failed to remove", orderId));
            return null;
        }
        OrderBookLevel oldLevel = OrderBookLevelCache.get(orderId);
        try {
            if (oldLevel.getSide() == BID) {
                bids.remove(oldLevel);
            } else if (oldLevel.getSide() == OFFER) {
                asks.remove(oldLevel);
            } else {
                throw new Exception(String.format("Unsupported side %s", oldLevel.getSide()));
            }
            OrderBookLevelCache.remove(oldLevel);
            return oldLevel;
        }catch (Exception e){
            System.out.println(String.format("Failed to remove order %s due to exception %s", oldLevel.getId(), e.getMessage()));
        }
        return null;
    }

    public OrderBookLevel replaceOrderSize(long orderId, long size){
        if(!OrderBookLevelCache.containsKey(orderId)) {
            System.out.println(String.format("Order %s is not found, failed to replace size", orderId));
            return null;
        }

        OrderBookLevel level = OrderBookLevelCache.get(orderId);
        level.setSize(size);
        return level;
    }

    public double getLevelPrice(char side, int level){
        TreeSet<OrderBookLevel> levels = side == BID? bids: asks;
        Iterator<OrderBookLevel> it = levels.iterator();
        int i = 0;
        OrderBookLevel current = null;
        while(it.hasNext() && i < level) {
            current = it.next();
            i++;
        }
        return current != null ? current.getPrice():Double.NaN;
    }

    public long getTotalLevelSize(char side, int level){
        double price = getLevelPrice(side, level);
        if(Double.isNaN(price))
            return 0;
        TreeSet<OrderBookLevel> levels = side == BID? bids: asks;
        return levels.stream().filter(l->l.getPrice() == price).mapToLong(l -> l.getSize()).sum();
    }

    public List<Order> getOrders(char side){
        TreeSet<OrderBookLevel> levels = side == BID? bids: asks;
        return levels.stream().map(l -> l.getOrder()).collect(Collectors.toList());
    }

}
