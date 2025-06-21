package controller.user.pages;

import controller.UserSessionController;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import model.Datasource;


public class UserHomeController {

    public Label productsCount;
    public Label ordersCount;


    public void getDashboardProdCount() {
        Task<Integer> getDashProdCount = new Task<Integer>() {
            @Override
            protected Integer call() {
                return Datasource.getInstance().countAllProducts();
            }
        };

        getDashProdCount.setOnSucceeded(e -> {
            productsCount.setText(String.valueOf(getDashProdCount.valueProperty().getValue()));
        });

        new Thread(getDashProdCount).start();
    }


    public void getDashboardOrdersCount() {
        Task<Integer> getDashOrderCount = new Task<Integer>() {
            @Override
            protected Integer call() {
                return Datasource.getInstance().countUserOrders(UserSessionController.getUserId());
            }
        };

        getDashOrderCount.setOnSucceeded(e -> {
            ordersCount.setText(String.valueOf(getDashOrderCount.valueProperty().getValue()));
        });

        new Thread(getDashOrderCount).start();
    }

}
