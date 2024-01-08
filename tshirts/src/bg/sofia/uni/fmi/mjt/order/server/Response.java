package bg.sofia.uni.fmi.mjt.order.server;

import bg.sofia.uni.fmi.mjt.order.server.order.Order;
import java.util.Collection;

public record Response(Status status, String additionalInfo, Collection<Order> orders) {

    private enum Status {
        OK, CREATED, DECLINED, NOT_FOUND
    }

    public static Response create(int id) {
        return new Response(Status.CREATED, "ORDER_ID=%d".formatted(id), null);
    }

    public static Response decline(String additionalInfo) {
        return new Response(Status.DECLINED, additionalInfo, null);
    }

    public static Response ok(Collection<Order> collection) {
        return new Response(Status.OK, null, collection);
    }

    public static Response notFound(int id) {
        return new Response(Status.NOT_FOUND, String.format("Order with id = %d does not exist.", id), null);
    }

    @Override
    public String toString() {
        return "{\"status\":\"" + status.name() + "\"" +
                ((additionalInfo != null) ? ", \"additionalInfo\":\"" + additionalInfo + "\"" : "") +
                ((orders != null) ? ", \"orders\":[" +
                        orders.parallelStream().collect(StringBuilder::new, StringBuilder::append,
                                            (a, b) -> a.append(", ").append(b)) + "]" : "") + "}";
    }
}
