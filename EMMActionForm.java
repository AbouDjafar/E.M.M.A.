package sample;

import com.jfoenix.controls.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EMMActionForm {

    protected boolean verification(Node N, String regexPattern){
        Pattern schemaControl;
        Matcher verificateur = null;
        schemaControl = Pattern.compile(regexPattern);
        if (N.getClass().equals(JFXTextField.class)){
            verificateur = schemaControl.matcher(((JFXTextField)N).getText());
        }
        if (N.getClass().equals(JFXDatePicker.class)){
            verificateur = schemaControl.matcher(((JFXDatePicker)N).getValue().toString());
        }
        return verificateur.matches();
    }

    protected void msgBelow(VBox parentObject, String msg, String txtColor){
        Label L = new Label(msg);
        L.setStyle("-fx-font-style: italic; -fx-text-fill:"+txtColor);
        int n = parentObject.getChildren().size();
        if (n > 1)
            parentObject.getChildren().remove(parentObject.getChildren().get(n - 1));
        parentObject.getChildren().add(L);
    }

    protected boolean verificationOnEvent(Node N, String expReg, String msgErreur, String msgSucces){
        /*
         * Verifier la donnée d'un input du formulaire à l'aide d'expression régulière
         *  et afficher un message d'erreur/d'affirmation en dessous de l'input
         *   et incrémenter la valeur du vérificateur global de 1 selon le critère de l'index (son rang vertical dans le formulaire) de l'input
         * */
        boolean verif = verification(N, expReg);
        if (!verif)
            msgBelow((VBox) N.getParent(), msgErreur, "red");
        else
            msgBelow((VBox) N.getParent(), msgSucces, "green");
        return verif;
    }

    protected void verificationGeneraleComboBoxes(VBox VB, ArrayList<Node> ALN){
        // Methode permettant de générer les événements sur les ComboBox et dont la fonction
        // est de dire si un de ces derniers a été utilisé (activé/choisi)
        for (int j = 0; j < VB.getChildren().size(); j++) {
            if (VB.getChildren().get(j).getClass().equals(GridPane.class)) {
                GridPane GP = ((GridPane)VB.getChildren().get(j));
                int n = GP.getRowConstraints().size();
                for (Node N : GP.getChildren()) {
                    if (N.getClass().equals(JFXComboBox.class)) {
                        ((JFXComboBox) N).setOnAction(e -> {
                            if (ALN.contains(N)) {
                                int idx = ALN.indexOf(N);
                                ALN.set(idx, null);
                            }
                        });
                    }
                    if (N.getClass().equals(VBox.class)) {
                        if (((VBox) N).getChildren().get(0).getClass().equals(JFXComboBox.class)) {
                            ((JFXComboBox) ((VBox) N).getChildren().get(0)).setOnAction(ev -> {
                                if (ALN.contains(((VBox) N).getChildren().get(0))) {
                                    int idx = ALN.indexOf(((VBox) N).getChildren().get(0));
                                    ALN.set(idx, null);
                                    if (((VBox) N).getChildren().size() > 1) {
                                        ((VBox) N).getChildren().remove(1);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    protected boolean verificationNivelle(int idxDebut, int nbElem, ArrayList<Node> ALN){
        // Verification par niveau
        // ALN est l'arrayList contenant tous les noeuds (inputs) non vérifiés
        // idxDebut == index (dans ALN) de l'élément premiers du groupe de nodes à verifier
        // bien evidemment nbElem est le nombre de ces éléments
        if (idxDebut >= ALN.size())
            return false;
        else {
            if (nbElem <= 1)
                return ALN.get(idxDebut) == null;
            else
                return (ALN.get(idxDebut) == null) && verificationNivelle(idxDebut+1, nbElem-1, ALN);
        }
    }

    protected void removeMsgBelow(VBox parentNode){
        int idx = -1;
        for (Node N : parentNode.getChildren()){
            if (N.getClass().equals(Label.class))
                idx = parentNode.getChildren().indexOf(N);
        }
        if (idx >= 0)
            parentNode.getChildren().remove(idx);
    }

    protected void btnSkinSwitch(Label L, String btnURL){
        L.setStyle("-fx-background-image: url("+getClass().getResource(btnURL)+")");
    }

    protected void cancelling(VBox VBFormulaires){
        for (Node GP : VBFormulaires.getChildren()){
            if (GP.getClass().equals(GridPane.class)){
                for (Node N : ((GridPane) GP).getChildren()){
                    Node petitN = (N.getClass().equals(VBox.class)) ? ((VBox)N).getChildren().get(0) : N;
                    if (petitN.getClass().equals(JFXComboBox.class))
                        ((JFXComboBox)petitN).setValue(null);
                    if (petitN.getClass().equals(JFXTextField.class))
                        ((JFXTextField)petitN).setText("");
                    if (petitN.getClass().equals(JFXDatePicker.class))
                        ((JFXDatePicker)petitN).setValue(null);
                    if (petitN.getClass().equals(JFXSlider.class))
                        ((JFXSlider)petitN).setValue(0);
                    if (petitN.getClass().equals(Spinner.class)) {
                        if (((Spinner) petitN).valueProperty().getValue().getClass().equals(Integer.class))
                            ((Spinner) petitN).getValueFactory().setValue(0);
                        else
                            ((Spinner) petitN).getValueFactory().setValue(0.0);
                    }
                }
            }
            if (GP.getClass().equals(TableView.class)){
                ((TableView)GP).getColumns().clear();
            }
        }
    }

    protected void showAutoCompletion(Node N, AnchorPane noeudDappui, ArrayList<String> suggestions){
        if (noeudDappui.getChildren().size() >= 2){
            for (Node No : noeudDappui.getChildren()){
                if (No.getClass().equals(JFXListView.class)){
                    ((JFXListView)No).getItems().clear();
                    ((JFXListView)No).getItems().addAll(suggestions);
                }
            }
        }else {
            // création
            JFXListView<String> autoComp = new JFXListView<>();
            autoComp.getItems().addAll(suggestions);
            // Determination de la position relative
            double x, y;
            if (N.getParent().getClass().equals(VBox.class)){
                Node M = (N.getParent());
                //x = M.getBoundsInParent().getMinX() + N.getBoundsInParent().getWidth();
                x = M.parentToLocal(M.getParent().boundsInParentProperty().getValue()).getMinX() + N.getBoundsInParent().getWidth();
                y = M.parentToLocal(M.getParent().boundsInParentProperty().getValue()).getMaxY();
            }else {
                x = N.parentToLocal(N.getParent().boundsInParentProperty().getValue()).getMinX();
                y = N.parentToLocal(N.getParent().boundsInParentProperty().getValue()).getMaxY();
            }
            autoComp.setLayoutX(x);
            autoComp.setLayoutY(y);
            autoComp.setMinSize(300, 20);
            autoComp.setMaxSize(600, 300);
            autoComp.setOnMouseClicked(e -> {
                ((JFXTextField)N).setText(autoComp.getSelectionModel().getSelectedItem());
                hideAutoCompletion(noeudDappui);
            });
            noeudDappui.getChildren().add(autoComp);
        }
    }

    protected void hideAutoCompletion(AnchorPane noeudDappui){
        for (Node No : noeudDappui.getChildren()){
            if (No.getClass().equals(JFXListView.class)) {
                noeudDappui.getChildren().remove(No);
                break;
            }
        }
    }

    protected ArrayList<Node> loadingBadNodes(VBox noeudParent){
        ArrayList<Node> badNodes = new ArrayList<>();
        for (Node N : noeudParent.getChildren()){
            if (N.getClass().equals(TableView.class))
                badNodes.add(N);
            if (N.getClass().equals(GridPane.class)){
                for (Node No : ((GridPane)N).getChildren()){
                    if (!No.getClass().equals(Label.class) && !No.getClass().equals(VBox.class))
                        badNodes.add(No);
                    else if (No.getClass().equals(VBox.class))
                        badNodes.add(((VBox)No).getChildren().get(0));
                }
            }
        }
        return badNodes;
    }

    protected ArrayList<Node> outOfBadNodes(Node N, ArrayList<Node> badNodes){
        if (badNodes.contains(N)){
            int idx = badNodes.indexOf(N);
            badNodes.set(idx, null);
        }
        return badNodes;
    }

    protected void loadingContentsInTableView(TableView<Integer> tableViewNode, TableColumn<Integer, String>[] columnNodes, ArrayList<String>[] columnValues, ArrayList<String> dataFromDB, int sens){
        // Sens == 0 si les données retournées par la BD sont de la sorte {[id1, données1], [id2, données2], ...}
        // Sens == 1 ou autre si les données issues de la BD sont stockées comme {données1, données2, ...}
        // On reset les données pour éviter des la surcharge
        for (ArrayList<String> AL : columnValues)
            AL.clear();
        tableViewNode.getItems().clear();
        // On rempli les conteneurs des données des colonnes avec les résultats d'une requete BD
        if (sens == 0) {
            for (int i = 0; i < (dataFromDB.size() - 1) / columnValues.length; i++) {
                for (int j = 0; j < columnValues.length; j++)
                    columnValues[j].add(dataFromDB.get((j + 1) + columnValues.length * i));
                tableViewNode.getItems().add(i); // On initialise les numeros des lignes du tableau
            }
        }else {
            for (int i = 0; i < dataFromDB.size()/columnValues.length; i++) {
                for (int j = 0; j < columnValues.length; j++)
                    columnValues[j].add(dataFromDB.get((columnValues.length * i) + j));
                tableViewNode.getItems().add(i); // On initialise les numeros des lignes du tableau
            }
        }
        // On prépare les colonnes (initialisation et attribution des données)
        for (int i = 0; i < columnNodes.length; i++){
            final int final_i = i;
            columnNodes[i].setCellValueFactory(CellData -> {
                int idx = CellData.getValue();
                return (new ReadOnlyStringWrapper(columnValues[final_i].get(idx)));
            });
        }
        // On uni les colonnes au tableau jusquà ce qu'un nouvel appel de fonction les sépare
        tableViewNode.getColumns().clear();
        for (TableColumn<Integer, String> Col: columnNodes)
            tableViewNode.getColumns().add(Col);
    }

    protected void loadModForm(String url, ArrayList<String> colis, VBox etagere){
        try {
            AnchorPane bigDady = FXMLLoader.load(getClass().getResource(url));
            if (etagere.getParent().getUserData() != null) {
                ArrayList<Object> tmp = (ArrayList<Object>) (etagere.getParent()).getUserData();
                tmp.add(colis);
                bigDady.setUserData(tmp);
            }else
                bigDady.setUserData(colis);
            etagere.getChildren().add(bigDady);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
