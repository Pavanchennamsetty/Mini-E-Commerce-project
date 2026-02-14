import java.util.*;
import java.io.*;

/*
 * Mini E-commerce Console App
 * - Menu: browse products, add to cart, view cart, remove item, checkout, view orders, exit
 * - Products are initialized in memory
 * - Checkout writes a simple order record to "orders.txt"
 *
 * Save this file as Main.java and run:
 * javac Main.java
 * java Main
 */

class Product {
    private final int id;
    private final String name;
    private final String desc;
    private double price;
    private int stock;

    public Product(int id, String name, String desc, double price, int stock) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.price = price;
        this.stock = stock;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDesc() { return desc; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }

    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }

    public boolean reduceStock(int qty) {
        if (qty <= stock) {
            stock -= qty;
            return true;
        }
        return false;
    }

    public void increaseStock(int qty) {
        stock += qty;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s - ₹%.2f (stock: %d) - %s", id, name, price, stock, desc);
    }
}

class CartItem {
    private final Product product;
    private int qty;

    public CartItem(Product p, int qty) {
        this.product = p;
        this.qty = qty;
    }

    public Product getProduct() { return product; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public double getItemTotal() { return product.getPrice() * qty; }

    @Override
    public String toString() {
        return String.format("%s x %d = ₹%.2f", product.getName(), qty, getItemTotal());
    }
}

class Cart {
    private final Map<Integer, CartItem> items = new LinkedHashMap<>();

    public void add(Product p, int qty) {
        CartItem ci = items.get(p.getId());
        if (ci == null) items.put(p.getId(), new CartItem(p, qty));
        else ci.setQty(ci.getQty() + qty);
    }

    public void remove(int productId) {
        items.remove(productId);
    }

    public Collection<CartItem> getItems() {
        return items.values();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }

    public double total() {
        double t = 0;
        for (CartItem ci : items.values()) t += ci.getItemTotal();
        return t;
    }
}

class Order {
    private final String id;
    private final List<CartItem> items;
    private final double total;
    private final Date orderedAt;

    public Order(List<CartItem> items, double total) {
        this.id = "ORD" + System.currentTimeMillis();
        this.items = new ArrayList<>(items);
        this.total = total;
        this.orderedAt = new Date();
    }

    public String getId() { return id; }
    public List<CartItem> getItems() { return items; }
    public double getTotal() { return total; }
    public Date getOrderedAt() { return orderedAt; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OrderId: ").append(id).append("\n");
        sb.append("Date: ").append(orderedAt).append("\n");
        for (CartItem ci : items) {
            sb.append("  ").append(ci.toString()).append("\n");
        }
        sb.append(String.format("Total: ₹%.2f\n", total));
        return sb.toString();
    }
}

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final List<Product> products = new ArrayList<>();
    private static final Cart cart = new Cart();
    private static final String ORDERS_FILE = "orders.txt";

    public static void main(String[] args) {
        seedProducts();
        System.out.println("=== Welcome to Mini E-Commerce ===");

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readIntSafe("Choose option: ");
            switch (choice) {
                case 1: browseProducts(); break;
                case 2: addToCart(); break;
                case 3: viewCart(); break;
                case 4: removeFromCart(); break;
                case 5: checkout(); break;
                case 6: viewOrdersFromFile(); break;
                case 0:
                    running = false;
                    System.out.println("Thank you for visiting. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
        sc.close();
    }

    private static void seedProducts() {
        products.add(new Product(1, "Wireless Mouse", "Ergonomic mouse", 499.0, 10));
        products.add(new Product(2, "USB-C Cable", "1m fast charging cable", 199.0, 25));
        products.add(new Product(3, "Bluetooth Headset", "Noise-cancelling", 1599.0, 8));
        products.add(new Product(4, "Notebook", "200 pages ruled", 99.0, 50));
        products.add(new Product(5, "Water Bottle", "500 ml stainless", 349.0, 20));
    }

    private static void printMainMenu() {
        System.out.println("\nMain Menu:");
        System.out.println("1. Browse products");
        System.out.println("2. Add product to cart");
        System.out.println("3. View cart");
        System.out.println("4. Remove item from cart");
        System.out.println("5. Checkout");
        System.out.println("6. View past orders (orders.txt)");
        System.out.println("0. Exit");
    }

    private static void browseProducts() {
        System.out.println("\nAvailable Products:");
        for (Product p : products) {
            System.out.println(p);
        }
    }

    private static void addToCart() {
        browseProducts();
        int pid = readIntSafe("Enter product id to add: ");
        Product p = findProductById(pid);
        if (p == null) {
            System.out.println("Product not found.");
            return;
        }
        System.out.println("Selected: " + p.getName() + " (stock: " + p.getStock() + ")");
        int qty = readIntSafe("Enter quantity: ");
        if (qty <= 0) {
            System.out.println("Quantity must be >= 1");
            return;
        }
        if (qty > p.getStock()) {
            System.out.println("Not enough stock. Available: " + p.getStock());
            return;
        }
        cart.add(p, qty);
        System.out.println(qty + " x " + p.getName() + " added to cart.");
    }

    private static void viewCart() {
        System.out.println("\nYour Cart:");
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }
        int idx = 1;
        for (CartItem ci : cart.getItems()) {
            System.out.println(idx++ + ". " + ci);
        }
        System.out.println(String.format("Cart Total: ₹%.2f", cart.total()));
    }

    private static void removeFromCart() {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }
        viewCart();
        int index = readIntSafe("Enter item number to remove (1..): ");
        if (index <= 0 || index > cart.getItems().size()) {
            System.out.println("Invalid item number.");
            return;
        }
        // get key by index since Cart stores by product id
        List<CartItem> list = new ArrayList<>(cart.getItems());
        CartItem ci = list.get(index - 1);
        cart.remove(ci.getProduct().getId());
        System.out.println("Removed: " + ci.getProduct().getName());
    }

    private static void checkout() {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty. Nothing to checkout.");
            return;
        }
        System.out.println("\nCheckout Summary:");
        viewCart();
        String confirm = readLineSafe("Confirm checkout? (yes/no): ");
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Checkout cancelled.");
            return;
        }

        // Check stock again and reduce stock
        for (CartItem ci : cart.getItems()) {
            Product p = findProductById(ci.getProduct().getId());
            if (p == null || ci.getQty() > p.getStock()) {
                System.out.println("Stock changed. Cannot complete order for " + ci.getProduct().getName());
                return;
            }
        }

        // Reduce stock and create order
        for (CartItem ci : cart.getItems()) {
            Product p = findProductById(ci.getProduct().getId());
            p.reduceStock(ci.getQty());
        }

        Order order = new Order(new ArrayList<>(cart.getItems()), cart.total());
        persistOrder(order);
        System.out.println("Order placed successfully! Order ID: " + order.getId());
        cart.clear();
    }

    private static void persistOrder(Order order) {
        try (FileWriter fw = new FileWriter(ORDERS_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println("----");
            out.println(order.toString());
        } catch (IOException e) {
            System.out.println("Failed to save order: " + e.getMessage());
        }
    }

    private static void viewOrdersFromFile() {
        System.out.println("\nPast Orders (from " + ORDERS_FILE + "):");
        File f = new File(ORDERS_FILE);
        if (!f.exists()) {
            System.out.println("No orders yet.");
            return;
        }
        try (Scanner fileScanner = new Scanner(f)) {
            while (fileScanner.hasNextLine()) {
                System.out.println(fileScanner.nextLine());
            }
        } catch (IOException e) {
            System.out.println("Unable to read orders file: " + e.getMessage());
        }
    }

    private static Product findProductById(int id) {
        for (Product p : products) if (p.getId() == id) return p;
        return null;
    }

    private static int readIntSafe(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static String readLineSafe(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
}
