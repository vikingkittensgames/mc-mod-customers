package com.vikingkittens.mc.customers.client.customer;

import java.util.ArrayList;
import java.util.List;

import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshot.Customer;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshot.Customer.Type;

/**
 * Arranges customer item groups into centered rows for the customer boss bar.
 */
public final class CustomerBossBarLayout {
    private static final int ICON_SIZE = 12;
    private static final int PADDING = 2;
    private static final int GAP = 2;
    private static final int GROUP_HEIGHT = ICON_SIZE + PADDING * 2;

    private CustomerBossBarLayout() {
    }

    /**
     * Creates a centered layout without splitting an individual customer's items
     * across rows.
     *
     * @param customers customers whose requested items should be arranged
     * @param centerX horizontal center of the available area
     * @param topY top of the first row
     * @param maxWidth maximum preferred width of each row
     * @return the positioned customer groups and total layout height
     */
    public static Layout create(List<Customer> customers, int centerX, int topY, int maxWidth) {
        List<List<Customer>> rows = createRows(customers, maxWidth);
        List<Group> groups = new ArrayList<>();
        int y = topY;

        for (List<Customer> row : rows) {
            int rowWidth = rowWidth(row);
            int x = centerX - rowWidth / 2;
            for (Customer customer : row) {
                int width = groupWidth(customer);
                groups.add(new Group(customer, new Bounds(x, y, width, GROUP_HEIGHT)));
                x += width + GAP;
            }
            y += GROUP_HEIGHT + GAP;
        }

        int height = rows.isEmpty() ? 0 : rows.size() * GROUP_HEIGHT + (rows.size() - 1) * GAP;
        return new Layout(groups, height);
    }

    /**
     * Returns the translucent background color associated with a customer type.
     *
     * @param type customer type
     * @return ARGB background color
     */
    public static int backgroundColor(Type type) {
        return switch (type) {
            case NORMAL -> 0x80FFD54F;
            case IMPATIENT -> 0x80EF5350;
            case CASUAL -> 0x8066BB6A;
        };
    }

    private static List<List<Customer>> createRows(List<Customer> customers, int maxWidth) {
        List<List<Customer>> rows = new ArrayList<>();
        List<Customer> row = new ArrayList<>();
        int width = 0;

        for (Customer customer : customers) {
            if (customer.offerCostItems().isEmpty()) {
                continue;
            }
            int customerWidth = groupWidth(customer);
            int candidateWidth = row.isEmpty() ? customerWidth : width + GAP + customerWidth;
            if (!row.isEmpty() && candidateWidth > maxWidth) {
                rows.add(List.copyOf(row));
                row.clear();
                width = 0;
            }
            width = row.isEmpty() ? customerWidth : width + GAP + customerWidth;
            row.add(customer);
        }

        if (!row.isEmpty()) {
            rows.add(List.copyOf(row));
        }
        return rows;
    }

    private static int rowWidth(List<Customer> row) {
        return row.stream().mapToInt(CustomerBossBarLayout::groupWidth).sum()
                + Math.max(0, row.size() - 1) * GAP;
    }

    private static int groupWidth(Customer customer) {
        return PADDING * 2 + customer.offerCostItems().size() * ICON_SIZE;
    }

    /**
     * Describes the complete item-group layout.
     *
     * @param groups positioned customer groups
     * @param height total layout height
     */
    public record Layout(List<Group> groups, int height) {
        public Layout {
            groups = List.copyOf(groups);
        }
    }

    /**
     * Associates a customer with its positioned item-group bounds.
     *
     * @param customer customer represented by the group
     * @param bounds group bounds
     */
    public record Group(Customer customer, Bounds bounds) {
    }

    /**
     * Describes the position and size of an item group.
     *
     * @param x left coordinate
     * @param y top coordinate
     * @param width group width
     * @param height group height
     */
    public record Bounds(int x, int y, int width, int height) {
    }
}
