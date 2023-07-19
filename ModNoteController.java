package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ModNoteController {
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
    private ArrayList<String> infosModif, infosUE;
    private double[] Notes;

    @FXML
    private void initialize() {
        anima = new EMMAnim();
        modelo = new EMModel();
        formaxio = new EMMActionForm();
        infosModif = new ArrayList<>();
        Notes = new double[5];

        filtre.getItems().addAll(modelo.getSuggestions("UE", "Code", null));
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
                infosUE = modelo.getInformations("UE", "Code", filtre.getValue());
                actionBtnRechercher();
            }
        });

        resultsTable.setOnMouseClicked((e) -> {
            int idx = resultsTable.getSelectionModel().getFocusedIndex();
            infosModif.clear();
            for (Object TC : resultsTable.getColumns()){
                infosModif.add(((TableColumn)TC).getCellData(idx).toString());
            }
        });

    }

    @FXML
    private void actionBtnRechercher(){
        if (filtre.getValue() != null)
            peuplementTableView(filtre.getValue());
        else
            System.out.println("aucune valeur dans le filtre");
    }

    private TableColumn<Integer, String>[] generateTableColumns(String[] tableColumnsID){
        TableColumn<Integer, String>[] TC = new TableColumn[tableColumnsID.length];
        for (int i = 0; i < TC.length; i++){
            TC[i] = new TableColumn<>(tableColumnsID[i]);
            TC[i].setId(tableColumnsID[i]);
            if (i > 2 && i != (TC.length - 1)) { // Le matricule, nom, prénom et la moyenne ne seront pas modifiables
                TC[i].setCellFactory(TextFieldTableCell.forTableColumn());
                final int frozen_i = i;
                TC[i].setOnEditCommit(event -> { //TODO
                    if (event.getNewValue() != null && event.getNewValue().matches("^[0-9]+")) {
                        int idx = TC[frozen_i].getTableView().getSelectionModel().getFocusedIndex(), iNote = TC[frozen_i].getTableView().getColumns().indexOf(TC[frozen_i]);
                        Notes = gatherMarks(idx, iNote, Double.parseDouble(event.getNewValue()));
                        double moyenne = AVG_Calculation(infosUE, Notes);
                        modelo.updateNote(filtre.getValue(), infosModif.get(0), Notes[0], Notes[1], Notes[2], Notes[3], Notes[4], moyenne);
                        anima.infoBoxShow((ArrayList<Object>) etagere.getParent().getUserData(), "Information", "Notes Modifiées");
                        actionBtnRechercher();
                    }else
                        anima.infoBoxShow((ArrayList<Object>) etagere.getParent().getUserData(), "Erreur", "Les valeurs saisies doivent être des chiffres uniquement");
                });

            }
        }
        return TC;
    }
    private void peuplementTableView(String codeUE) {
        // Colonnes == colonnes de la table
        // table == une des tables de la base de donnée
        // deviant si c'est une table liée à la table personne (Etudiant ou Enseignant)
        String[] colonnesTable = new String[]{"Matricule", "Nom", "Prenom", "CC", "TP", "TPE", "Session1_EE", "Session2_EE", "Moyenne"};
        TableColumn<Integer, String>[] colonnes = generateTableColumns(colonnesTable);
        ArrayList<String>[] valeurColonnes = new ArrayList[colonnes.length];
        for (int i = 0; i < valeurColonnes.length; i++)
            valeurColonnes[i] = new ArrayList<>();
        if (colonnesTable.length > 0 && codeUE != null && !codeUE.isEmpty()) {
            ArrayList<String> sortieBD = modelo.procesVerbalUE(codeUE, null);
            formaxio.loadingContentsInTableView(resultsTable, colonnes, valeurColonnes, sortieBD, 1);
        }else {
            System.out.println("fonction mal paramétré, vérifier les valeurs de params");
        }
    }

    private double AVG_Calculation(ArrayList<String> infosUE, double[] notes){
        double moyenne;
        moyenne = (notes[0] / Integer.parseInt(infosUE.get(3))) * Double.parseDouble(infosUE.get(7))
                + (notes[1] / Integer.parseInt(infosUE.get(4))) * Double.parseDouble(infosUE.get(8))
                + (notes[2] / Integer.parseInt(infosUE.get(5))) * Double.parseDouble(infosUE.get(9)) ;
        moyenne += (notes[4] > 0) ? (notes[4] / Integer.parseInt(infosUE.get(6))) * Double.parseDouble(infosUE.get(10)) : (notes[3] / Integer.parseInt(infosUE.get(6))) * Double.parseDouble(infosUE.get(10));
        moyenne *= 0.2;

        return moyenne;
    }

    private double[] gatherMarks(int rowIndex, int markIndex, double markValue){
        double[] Marks = new double[5];
        String fetchedData;
        for (int i = 3; i < resultsTable.getColumns().size() - 1; i++) {
            fetchedData = (String) resultsTable.getColumns().get(i).getCellData(rowIndex);
            Marks [i-3] = (i == markIndex) ? markValue : Double.parseDouble(fetchedData);
        }

        return Marks;
    }
}
