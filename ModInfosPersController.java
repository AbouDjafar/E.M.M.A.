package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ModInfosPersController {
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private VBox etagere;
    @FXML
    private Pane titre, titre1, titre3;
    @FXML
    private GridPane grille1, grille2, grille3;
    @FXML
    private JFXTextField motRecherche;
    @FXML
    private JFXButton btnRechercher;
    @FXML
    private TableView<Integer> resultsTable;
    @FXML
    private JFXComboBox<String> filtre;

    private EMMAnim anima;
    private EMModel modelo;
    private EMMActionForm formaxio;
    private ArrayList<String> infosModif;

    @FXML
    private void initialize() {
        anima = new EMMAnim();
        modelo = new EMModel();
        formaxio = new EMMActionForm();
        infosModif = new ArrayList<>();

        filtre.getItems().addAll("Etudiant", "Enseignant");
        for (Node N : etagere.getChildren()) {
            if (N.getClass().equals(Pane.class))
                anima.decroissance(N, 0);
            else
                anima.decroissance(N, 2);
        }
        titre.setScaleX(1);
        SequentialTransition sAnim = new SequentialTransition();
        Animation st1, st2, st3;
        st1 = anima.formShowTime(titre1, "Spécifications", grille2, null, 500, true);
        st2 = anima.croissance(grille1, 1, 2, 500);
        st3 = anima.formShowTime(titre3, "Données Modifiables", grille3, null, 500, true);
        sAnim.getChildren().addAll(st1, st2, st3);
        sAnim.play();

        filtre.valueProperty().addListener((obs, oldVal, newVal) -> {
            resultsTable.getColumns().clear();
            if (newVal != null) {
                motRecherche.setDisable(false);
                btnRechercher.setDisable(false);
                if (newVal.equals("Etudiant")) {
                    //TODO
                }
                actionBtnRechercher();
            }
        });

        resultsTable.setOnMouseClicked((e) -> {
            int idx = resultsTable.getSelectionModel().getFocusedIndex();
            infosModif.clear();
            for (Object TC : resultsTable.getColumns()){
                infosModif.add(((TableColumn)TC).getCellData(idx).toString());
            }
            if (etagere.getChildren().get(etagere.getChildren().size() - 1).getClass().equals(AnchorPane.class))
                etagere.getChildren().remove(etagere.getChildren().size() - 1);
            formaxio.loadModForm("AjoutPersonne.fxml", infosModif, etagere);
        });

    }

    @FXML
    private void actionBtnRechercher(){
        if (filtre.getValue().equals("Etudiant")){
            peuplementTableView(new String[]{"Identite", "Matricule", "Nom", "Prenom", "Sexe", "DateNaissance", "LieuNaissance", "Departement", "Filiere", "Cycle", "Niveau", "Credits"}, "Etudiant", true);
            resultsTable.getColumns().get(0).setVisible(false);
        }

        if (filtre.getValue().equals("Enseignant")){
            peuplementTableView(new String[]{"Identite", "Nom", "Prenom", "Sexe", "DateNaissance", "LieuNaissance", "Departement", "Grade"}, "Enseignant", true);
            resultsTable.getColumns().get(0).setVisible(false);
        }
    }

    private TableColumn<Integer, String>[] generateTableColumns(String[] tableColumnsID){
        TableColumn<Integer, String>[] TC = new TableColumn[tableColumnsID.length];
        for (int i = 0; i < TC.length; i++){
            TC[i] = new TableColumn<>(tableColumnsID[i]);
            TC[i].setId(tableColumnsID[i]);
            //TC[i].setCellFactory(TextFieldTableCell.forTableColumn());
        }
        return TC;
    }
    private void peuplementTableView(String[] colonnesTable, String table, boolean deviant) {
        // Colonnes == colonnes de la table
        // table == une des tables de la base de donnée
        // deviant si c'est une table liée à la table personne (Etudiant ou Enseignant)
        String filtrage = null;
        if (motRecherche.getText() != null) {
            if (motRecherche.getText().contains(" - "))
                filtrage = motRecherche.getText().split(" - ")[0];
            else
                filtrage = motRecherche.getText();
        }
        TableColumn<Integer, String>[] colonnes = generateTableColumns(colonnesTable);
        ArrayList<String>[] valeurColonnes = new ArrayList[colonnes.length];
        for (int i = 0; i < valeurColonnes.length; i++)
            valeurColonnes[i] = new ArrayList<>();
        if (colonnesTable.length > 0 && table != null && !table.isEmpty()) {
            ArrayList<String> sortieBD;
            if (table.matches("^[A-Z]{3}\\d{3}$")){
                sortieBD = modelo.procesVerbalUE(table, filtrage);
            }else {
                sortieBD = modelo.appelGeneral(table, colonnesTable, filtrage, deviant);
            }
            formaxio.loadingContentsInTableView(resultsTable, colonnes, valeurColonnes, sortieBD, 1);
        }else {
            System.out.println("fonction mal paramétré, vérifier les valeurs de params");
        }
    }

}
