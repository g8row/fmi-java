package bg.sofia.uni.fmi.mjt.order.server.repository;

import bg.sofia.uni.fmi.mjt.order.server.Response;
import bg.sofia.uni.fmi.mjt.order.server.destination.Destination;
import bg.sofia.uni.fmi.mjt.order.server.order.Order;
import bg.sofia.uni.fmi.mjt.order.server.tshirt.Color;
import bg.sofia.uni.fmi.mjt.order.server.tshirt.Size;
import bg.sofia.uni.fmi.mjt.order.server.tshirt.TShirt;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MJTOrderRepository implements OrderRepository {
    ConcurrentLinkedDeque<Order> orders;
    int successfulOrders;
    public MJTOrderRepository() {
        orders = new ConcurrentLinkedDeque<>();
        successfulOrders = 0;
    }

    /**
     * Creates a new T-Shirt order
     *
     * @param size        - size of the requested T-Shirt
     * @param color       - color of the requested T-Shirt
     * @param destination - destination of the requested T-Shirt
     * @return response which contains status and additional info (orderId or invalid parameters if there are such)
     */

    @Override
    public Response request(String size, String color, String destination) {
        if (size == null || color == null || destination == null) {
            throw new IllegalArgumentException();
        }
        Size sizeE = Size.of(size);
        Color colorE = Color.of(color);
        Destination destinationE = Destination.of(destination);
        StringBuilder additionalInfo = new StringBuilder("invalid=");
        TShirt shirt = new TShirt(Size.of(size), Color.of(color));
        Order order;
        Response response;
        if (sizeE == Size.UNKNOWN ) {
            additionalInfo.append("size,");
        }
        if (colorE == Color.UNKNOWN ) {
            additionalInfo.append("color,");
        }
        if (destinationE == Destination.UNKNOWN ) {
            additionalInfo.append("destination,");
        }
        if (sizeE == Size.UNKNOWN || colorE == Color.UNKNOWN || destinationE == Destination.UNKNOWN) {
            order = new Order(-1, shirt, destinationE);
            response = Response.decline(additionalInfo.deleteCharAt(additionalInfo.length() - 1).toString());
        } else {
            order = new Order(++successfulOrders, shirt, destinationE);
            response = Response.create(successfulOrders);
        }
        orders.add(order);
        return response;
    }

    /**
     * Retrieves a T-Shirt order with the given id
     *
     * @param id order id
     * @return response which contains status and an order with the given id
     */
    @Override
    public Response getOrderById(int id) {
        for (Order order : orders) {
            if (order.id() == id) {
                return Response.ok(List.of(order));
            }
        }
        return Response.notFound(id);
    }

    /**
     * Retrieves all T-Shirt orders
     *
     * @return response which contains status and  all T-Shirt orders from the repository, in undefined order.
     * If there are no orders in the repository, returns an empty collection.
     */
    @Override
    public Response getAllOrders() {
        return Response.ok(orders);
    }

    /**
     * Retrieves all successful orders for T-Shirts
     *
     * @return response which contains status and all successful orders for T-Shirts from the repository,
     * in undefined order.
     * If there are no such orders in the repository, returns an empty collection.
     */
    @Override
    public Response getAllSuccessfulOrders() {
        return Response.ok(orders.stream().filter(x -> x.id() != -1).toList());
    }
}
