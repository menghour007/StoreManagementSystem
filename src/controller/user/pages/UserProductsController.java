package controller.user.pages;

import app.utils.HelperMethods;
import controller.UserSessionController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import model.Datasource;
import model.Product;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;


public class UserProductsController {


    @FXML
    public TextField fieldProductsSearch;

    @FXML
    private TableView<Product> tableProductsPage;


    @FXML
    public void listProducts() {

        Task<ObservableList<Product>> getAllProductsTask = new Task<ObservableList<Product>>() {
            @Override
            protected ObservableList<Product> call() {
                return FXCollections.observableArrayList(Datasource.getInstance().getAllProducts(Datasource.ORDER_BY_NONE));
            }
        };

        tableProductsPage.itemsProperty().bind(getAllProductsTask.valueProperty());
        addActionButtonsToTable();
        new Thread(getAllProductsTask).start();

    }


    @FXML
    private void addActionButtonsToTable() {
        //  Check if the "Actions" column already exists
        for (TableColumn<?, ?> column : tableProductsPage.getColumns()) {
            if ("Actions".equals(column.getText())) {
                return; // Skip adding if it's already there
            }
        }

        TableColumn<Product, Void> colBtnBuy = new TableColumn<>("Actions");

        Callback<TableColumn<Product, Void>, TableCell<Product, Void>> cellFactory =new Callback<TableColumn<Product, Void>, TableCell<Product, Void>>() {
            @Override
            public TableCell<Product, Void> call(final TableColumn<Product, Void> param) {
                return new TableCell<Product, Void>() {

                    private final Button buyButton = new Button("Buy");

                    {
                        buyButton.getStyleClass().add("button");
                        buyButton.getStyleClass().add("xs");
                        buyButton.getStyleClass().add("success");

                        buyButton.setOnAction((ActionEvent event) -> {
                            Product productData = (Product) getTableView().getItems().get(getIndex());
                            if (productData.getQuantity() <= 0) {
                                HelperMethods.alertBox("You can't buy this product because there is no stock!", "", "No Stock");
                            } else {
                                btnBuyProduct(productData.getId(), productData.getName());
                                System.out.println("Buy Product");
                                System.out.println("product id: " + productData.getId());
                                System.out.println("product name: " + productData.getName());
                            }
                        });
                    }

                    private final HBox buttonsPane = new HBox();

                    {
                        buttonsPane.setSpacing(10);
                        buttonsPane.getChildren().add(buyButton);
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : buttonsPane);
                    }
                };
            }
        };

        colBtnBuy.setCellFactory(cellFactory);
        tableProductsPage.getColumns().add(colBtnBuy);
    }

    @FXML
    private void btnProductsSearchOnAction() {
        Task<ObservableList<Product>> searchProductsTask = new Task<ObservableList<Product>>() {
            @Override
            protected ObservableList<Product> call() {
                return FXCollections.observableArrayList(
                        Datasource.getInstance().searchProducts(fieldProductsSearch.getText().toLowerCase(), Datasource.ORDER_BY_NONE));
            }
        };
        tableProductsPage.itemsProperty().bind(searchProductsTask.valueProperty());

        new Thread(searchProductsTask).start();
    }



    @FXML
    private void btnBuyProduct(int product_id, String product_name) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("You are about to buy \"" + product_name + "\"");
        alert.setTitle("Buy Product");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int user_id = UserSessionController.getUserId();
            Timestamp order_date = new Timestamp(System.currentTimeMillis()); // ✅ Proper timestamp
            String order_status = "Received";

            Task<Boolean> orderTask = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    boolean success = Datasource.getInstance().insertNewOrder(product_id, user_id, order_date, order_status);

                    if (success) {
                        return Datasource.getInstance().decreaseStock(product_id);
                    }

                    return false;
                }
            };

            orderTask.setOnSucceeded(e -> {
                if (orderTask.getValue()) {
                    HelperMethods.alertBox(" Order placed successfully!", "", "Success");
                    listProducts(); // Refresh table
                } else {
                    HelperMethods.alertBox(" Failed to place order. Please try again.", "", "Order Failed");
                }
            });

            orderTask.setOnFailed(e -> {
                HelperMethods.alertBox(" An unexpected error occurred!", "", "Error");
                orderTask.getException().printStackTrace();
            });

            new Thread(orderTask).start();
        }
    }




}