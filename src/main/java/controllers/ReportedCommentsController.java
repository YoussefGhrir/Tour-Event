package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Comment;
import service.CommentService;

import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.List;

public class ReportedCommentsController {

    @FXML
    private TableView<Comment> reportedCommentsTable;

    @FXML
    private TableColumn<Comment, Integer> idColumn;

    @FXML
    private TableColumn<Comment, String> contentColumn;

    @FXML
    private TableColumn<Comment, String> reasonColumn;

    @FXML
    private TableColumn<Comment, Void> actionColumn;

    private final CommentService commentService = new CommentService();

    @FXML
    public void initialize() {
        // Liaison des colonnes
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        contentColumn.setCellValueFactory(new PropertyValueFactory<>("content"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reportReason"));

        // Définir les actions pour approuver ou bannir
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button approveButton = new Button("Approuver");
            private final Button banButton = new Button("Bannir");

            {
                approveButton.setOnAction(e -> handleApprove(getTableRow().getItem()));
                banButton.setOnAction(e -> handleBan(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(approveButton, banButton);
                    buttons.setSpacing(10);
                    setGraphic(buttons);
                }
            }
        });

        loadReportedComments();
    }

    private void loadReportedComments() {
        try {
            List<Comment> reportedComments = commentService.getReportedComments();
            reportedCommentsTable.getItems().setAll(reportedComments);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleApprove(Comment comment) {
        try {
            commentService.approveComment(comment.getId());
            loadReportedComments();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleBan(Comment comment) {
        try {
            commentService.banComment(comment.getId());
            loadReportedComments();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshTable() {
        loadReportedComments();
    }
}