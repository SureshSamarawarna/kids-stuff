package lk.ijse.dep9.kids.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import lk.ijse.dep9.kids.entity.Book;
import lk.ijse.dep9.kids.entity.Book;
import lk.ijse.dep9.kids.exception.BlankFieldException;
import lk.ijse.dep9.kids.exception.InvalidBookException;
import lk.ijse.dep9.kids.exception.InvalidFieldException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class MainFormController {
    public TextField txtId;
    public TextField txtTitle;
    public TextField txtAuthor;
    public TextField txtGenre;
    public TextField txtPrice;
    public DatePicker txtDate;
    public TextField txtDescription;
    public TableView<Book> tblBooks;
    public Label lblSelectStatus;
    public Label lblStatus;
    public ProgressBar pgb;
    public Button btnNew;
    public Button btnSave;
    public Button btnDelete;
    public Button btnExport;
    public Button btnImport;
    public Button btnPrint;
    public AnchorPane root;
    private HashMap<String, Node> textFieldMap;
    private List<Book> selectedBooks;

    public void initialize() {
        btnImport.setDisable(false);
        textFieldMap = new HashMap<>();
        textFieldMap.put("id", txtId);
        textFieldMap.put("title", txtTitle);
        textFieldMap.put("author", txtAuthor);
        textFieldMap.put("price", txtPrice);
        textFieldMap.put("genre", txtGenre);
        textFieldMap.put("description", txtDescription);
        textFieldMap.put("date", txtDate);
        selectedBooks = new ArrayList<>();

        tblBooks.setEditable(true);
        TableColumn<Book, Boolean> firstCol = (TableColumn<Book, Boolean>) tblBooks.getColumns().get(0);
        firstCol.setCellFactory(bookBookTableColumn -> new CheckBoxTableCell<>());
        firstCol.setCellValueFactory(param -> {
            SimpleBooleanProperty chk = new SimpleBooleanProperty();
            chk.addListener((observableValue, oldValue, newValue) -> {
                if (newValue){
                    selectedBooks.add(param.getValue());
                }else{
                    selectedBooks.remove(param.getValue());
                }
                updateSelectedStatus();
            });
            return chk;
        });
        tblBooks.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblBooks.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("title"));
        tblBooks.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("author"));
        tblBooks.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("price"));

        loadAllBooksFromDB();

        tblBooks.getSelectionModel().selectedItemProperty().
                addListener((observableValue, previousSelectedRow, currentSelectedRow) -> {
                    if (currentSelectedRow == null)return;

                    txtId.setText(currentSelectedRow.getId());
                    txtTitle.setText(currentSelectedRow.getTitle());
                    txtAuthor.setText(currentSelectedRow.getAuthor());
                    txtGenre.setText(currentSelectedRow.getGenre());
                    txtPrice.setText(currentSelectedRow.getPrice().toString());
                    txtDate.setValue(currentSelectedRow.getPublishedDate());
                    txtDescription.setText(currentSelectedRow.getDescription());

                    root.getChildren().forEach(node -> {
                        if (node instanceof TextField || node instanceof DatePicker) node.setDisable(true);
                    });
                    btnSave.setDisable(true);
                });
    }

    private void updateSelectedStatus(){
        lblSelectStatus.setText(String.format("%d/%d SELECTED", selectedBooks.size(), tblBooks.getItems().size()));
        btnDelete.setDisable(selectedBooks.isEmpty());
        btnExport.setDisable(selectedBooks.isEmpty());
    }

    private void loadAllBooksFromDB() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dep9_kids", "root", "MORA@spsa8")) {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM Book");

            while (rst.next()) {
                String id = rst.getString("id");
                String title = rst.getString("title");
                String author = rst.getString("author");
                String genre = rst.getString("genre");
                BigDecimal price = rst.getBigDecimal("price");
                LocalDate publishedDate = rst.getDate("published_date").toLocalDate();
                String description = rst.getString("description");
                tblBooks.getItems().add(new Book(id, title, author, genre, price, publishedDate, description));
            }
            updateSelectedStatus();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load books");
            e.printStackTrace();
        }
    }

    public void btnNewOnAction(ActionEvent actionEvent) {
        root.getChildren().forEach(node -> {
            if (node instanceof TextField || node instanceof DatePicker) node.setDisable(false);
            if (node instanceof TextField) ((TextField) node).clear();
        });
        btnSave.setDisable(false);
        txtDate.setValue(LocalDate.now());
        txtId.requestFocus();
        tblBooks.getSelectionModel().clearSelection();
    }

    public void btnSaveOnAction(ActionEvent actionEvent) {
        try {
            insertBook(txtId.getText(), txtTitle.getText(), txtAuthor.getText(), txtGenre.getText(),
                    txtPrice.getText(), txtDate.getValue(), txtDescription.getText());
        } catch (InvalidBookException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            textFieldMap.get(e.getField()).requestFocus();
            if (textFieldMap.get(e.getField()) instanceof TextField)
                ((TextField) textFieldMap.get(e.getField())).selectAll();
        }
    }

    private void insertBook(String id,
                            String title,
                            String author,
                            String genre,
                            String strPrice,
                            LocalDate publishedDate,
                            String description) throws BlankFieldException, InvalidFieldException {

        /* Data Validation Logic */
        if (id == null || id.isBlank()) {
            throw new BlankFieldException("Book's id can't be empty", "id");
        } else if (!id.toUpperCase().matches("BK\\d{3}")) {
            throw new InvalidFieldException("Invalid book id", "id");
        } else if (title == null || title.isBlank()) {
            throw new BlankFieldException("Book's title can't be empty", "title");
        } else if (author == null || author.isBlank()) {
            throw new BlankFieldException("Book's author can't be empty", "author");
        } else if (!author.matches("[A-Za-z ][A-Za-z ,']+")) {
            throw new InvalidFieldException("Invalid author", "author");
        } else if (genre == null || genre.isBlank()) {
            throw new BlankFieldException("Book's genre can't be empty", "genre");
        } else if (strPrice == null || strPrice.isBlank()) {
            throw new BlankFieldException("Book's price can't be empty", "price");
        } else if (!strPrice.matches("\\d+([.]\\d{1,2})?")) {
            throw new InvalidFieldException("Invalid book price", "price");
        } else if (new BigDecimal(strPrice).compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFieldException("Book price can't be a negative value or zero", "price");
        } else if (publishedDate == null) {
            throw new BlankFieldException("Book's published date can't be empty", "date");
        } else if (publishedDate.isAfter(LocalDate.now())) {
            throw new InvalidFieldException("Book's published date can't be a future date", "date");
        }

        try (Connection connection =
                     DriverManager.getConnection("jdbc:mysql://localhost:3306/dep9_kids",
                             "root", "mysql")) {

            /* Business Validation */
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM Book WHERE id=?");
            stm.setString(1, id);
            ResultSet rst = stm.executeQuery();
            if (rst.next()) throw new InvalidFieldException("Book id already exists", "id");

            PreparedStatement stm2 = connection.
                    prepareStatement("INSERT INTO Book VALUES (?, ?, ?, ?, ?, ?, ?)");
            stm2.setString(1, id);
            stm2.setString(2, title);
            stm2.setString(3, author);
            stm2.setString(4, genre);
            stm2.setBigDecimal(5, new BigDecimal(strPrice));
            stm2.setDate(6, Date.valueOf(publishedDate));
            stm2.setString(7, description);

            stm2.executeUpdate();

            Book newBook = new Book(id, title, author, genre, new BigDecimal(strPrice), publishedDate, description);
            tblBooks.getItems().add(newBook);
            updateSelectedStatus();

            btnNew.fire();

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to save the book").show();
            e.printStackTrace();
        }
    }


    public void btnDeleteOnAction(ActionEvent actionEvent) {
        Optional<ButtonType> response = new Alert(Alert.AlertType.CONFIRMATION,
                String.format("There are %d records selected. Are you sure to delete?", selectedBooks.size()),
                ButtonType.YES, ButtonType.NO).showAndWait();
        if (response.get() == ButtonType.NO) return;

        try (Connection connection =
                     DriverManager.getConnection("jdbc:mysql://localhost:3306/dep9_kids",
                             "root", "MORA@spsa8")) {
            PreparedStatement stm = connection.prepareStatement("DELETE FROM Book WHERE id=?");
            for (Book book : selectedBooks) {
                stm.setString(1, book.getId());
                stm.executeUpdate();
            }
            tblBooks.getItems().removeAll(selectedBooks);
            selectedBooks.clear();
            updateSelectedStatus();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to delete records").show();
            e.printStackTrace();
        }
    }

    public void btnExportOnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Books");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName(String.format("Books-%1$tF-%1$tT.xml", Calendar.getInstance().getTime()));
        File exportedFile = fileChooser.showSaveDialog(btnExport.getScene().getWindow());
        if (exportedFile == null) return;

        try {
            if (!exportedFile.exists()) exportedFile.createNewFile();
            try(BufferedWriter bw = new BufferedWriter(new FileWriter(exportedFile))){

                Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element elmCatalog = xmlDoc.createElement("catalog");
                for (Book book : selectedBooks) {
                    Element elmBook = xmlDoc.createElement("book");
                    elmBook.setAttribute("id", book.getId());

                    Element elmTitle = xmlDoc.createElement("title");
                    Element elmAuthor = xmlDoc.createElement("author");
                    Element elmGenre = xmlDoc.createElement("genre");
                    Element elmPrice = xmlDoc.createElement("price");
                    Element elmPublishedDate = xmlDoc.createElement("publish_date");
                    Element elmDescription = xmlDoc.createElement("description");

                    elmTitle.setTextContent(book.getTitle());
                    elmAuthor.setTextContent(book.getAuthor());
                    elmGenre.setTextContent(book.getGenre());
                    elmPrice.setTextContent(book.getPrice().setScale(2).toString());
                    elmPublishedDate.setTextContent(book.getPublishedDate().toString());
                    elmDescription.setTextContent(book.getDescription());

                    elmBook.appendChild(elmTitle);
                    elmBook.appendChild(elmAuthor);
                    elmBook.appendChild(elmGenre);
                    elmBook.appendChild(elmPrice);
                    elmBook.appendChild(elmPublishedDate);
                    elmBook.appendChild(elmDescription);

                    elmCatalog.appendChild(elmBook);
                }
                xmlDoc.appendChild(elmCatalog);
                TransformerFactory.newInstance().newTransformer().transform(new DOMSource(xmlDoc),
                        new StreamResult(bw));
                new Alert(Alert.AlertType.INFORMATION,
                        String.format("%d Book(s) have been exported successfully", selectedBooks.size())).show();
            }
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to export books").show();
            e.printStackTrace();
        }
    }

    public void btnImportOnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Books");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File importedFile = fileChooser.showOpenDialog(btnImport.getScene().getWindow());
        if (importedFile == null) return;

        try {
            Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(importedFile);
            Element rootElm = xmlDoc.getDocumentElement();
            if (!rootElm.getTagName().equals("catalog")) {
                new Alert(Alert.AlertType.ERROR, "Invalid book database").show();
                return;
            }

            NodeList books = rootElm.getElementsByTagName("book");
            if (books.getLength() == 0){
                new Alert(Alert.AlertType.INFORMATION, "No books to import").show();
                return;
            }

            NodeList authorList = rootElm.getElementsByTagName("author");
            NodeList titleList = rootElm.getElementsByTagName("title");
            NodeList priceList = rootElm.getElementsByTagName("price");
            NodeList genreList = rootElm.getElementsByTagName("genre");
            NodeList descriptionList = rootElm.getElementsByTagName("description");
            NodeList dateList = rootElm.getElementsByTagName("publish_date");

            if (!(books.getLength() == authorList.getLength() &&
                    books.getLength() == titleList.getLength() &&
                    books.getLength() == priceList.getLength() &&
                    books.getLength() == genreList.getLength() &&
                    books.getLength() == descriptionList.getLength() &&
                    books.getLength() == dateList.getLength())) {
                new Alert(Alert.AlertType.ERROR, "Failed to validate the XML schema").show();
                return;
            }

            int importedBooks = 0;
            for (int i = 0; i < books.getLength(); i++) {
                String id = ((Element) books.item(i)).getAttribute("id");
                try {
                    insertBook(id,
                            titleList.item(i).getTextContent(),
                            authorList.item(i).getTextContent(),
                            genreList.item(i).getTextContent(),
                            priceList.item(i).getTextContent(),
                            LocalDate.parse(dateList.item(i).getTextContent()),
                            descriptionList.item(i).getTextContent());
                    importedBooks++;
                } catch (InvalidBookException e) {
                    /* Do nothing */
                }
            }
            new Alert(Alert.AlertType.INFORMATION,
                    String.format("Imported %d from %d successfully",importedBooks, books.getLength() )).show();
            updateSelectedStatus();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to import books").show();
            e.printStackTrace();
        }
    }

    public void btnPrintOnAction(ActionEvent actionEvent) {
    }
}
