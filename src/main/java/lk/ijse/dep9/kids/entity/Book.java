package lk.ijse.dep9.kids.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Book implements Serializable {
    private String id;
    private String title;
    private String author;
    private String genre;
    private BigDecimal price;
    private LocalDate publishedDate;
    private String description;

    public Book() {
    }

    public Book(String id, String title, String author, String genre, BigDecimal price, LocalDate publishedDate, String description) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.price = price;
        this.publishedDate = publishedDate;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", genre='" + genre + '\'' +
                ", price=" + price +
                ", publishedDate=" + publishedDate +
                ", description='" + description + '\'' +
                '}';
    }
}
