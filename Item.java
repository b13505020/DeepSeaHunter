import java.io.Serializable;

public class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private double weight;
    private int price;
    private String imagePath;
    private int rarityStars;

    public Item(String name, double weight, int price, String imagePath, int rarityStars) {
        this.name = name;
        this.weight = weight;
        this.price = price;
        this.imagePath = imagePath;
        this.rarityStars = rarityStars;
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    public int getPrice() {
        return price;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getRarityStars() {
        return rarityStars;
    }
}