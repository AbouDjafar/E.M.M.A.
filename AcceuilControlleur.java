package sample;

import com.jfoenix.controls.JFXButton;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AcceuilControlleur {
    @FXML
    private AnchorPane racine;
    @FXML
    private StackPane pileMenu, iBoxStackPane, fadeScreen;
    @FXML
    private Pane verre;
    @FXML
    private ScrollPane corps;
    @FXML
    private Label Menu, cloneMenu, Ajouter, Modifier, Rechercher, Inscrire, AjoutPersonne, AjoutUE, AjoutNote, ModifInfos, ModifNote, InfosPersonne, InfosUE, iBoxTitle, iBoxMsg;
    @FXML
    private JFXButton iBoxOkBtn, iBoxCancelBtn;
    @FXML
    private VBox iBox;
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    private int[] niveaux; // 1 == menu activé | 0 == menu desactivé
    private int[][] mManager;
    private EMMAnim anima;
    private Label[][] menuCentral, menuAjouter, menuModifier, menuModInfos, menuInscrire, menuRechercher;
    private Label[] menuItems;
    private ArrayList<Object> colis;

    @FXML
    private void initialize() {
        //Rideau de scène TODO
        fadeScreen.setStyle("-fx-background-image: url('"+getClass().getResource("IMG/fade.png")+"'); -fx-background-size: cover");

        TranslateTransition ft = new TranslateTransition();
        ft.setNode(fadeScreen);
        ft.setToY(-800);
        ft.setDuration(Duration.seconds(5));
        ScaleTransition st = new ScaleTransition(Duration.seconds(5), new Label(""));
        st.setByX(-0.05);
        SequentialTransition seqT = new SequentialTransition();
        seqT.getChildren().addAll(st, ft);
        seqT.setOnFinished(event -> {
            racine.getChildren().removeAll(fadeScreen);
        });
        seqT.play();

        try {
            corps.contentProperty().set(FXMLLoader.load(getClass().getResource("InfosAcceuil.fxml")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        * Représentation des menu à partie d'une structure en arbre (parcourue en largeur) en une matrice MxN
        * M = nombre des menus au total (noeuds de l'arbre)
        * N = la dimension du tablea T[] contenant les informations [etat, nombreEnfants] (etat = 1 si menu actif et 0 inversement)
        */
        mManager = new int[][]{{0, 3}, {0, 2}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 2}, {0, 0}, {0, 0}, {0, 0}};
        // les sommets où commencent les niveaux de l'arbre des menus
        niveaux = new int[]{0, 4, 9};
        // Les noeus de l'arbre des menus (ou tout simplements les menus eux memes xD)
        menuItems = new Label[]{Ajouter, Modifier, Inscrire, Rechercher, AjoutPersonne, AjoutUE, AjoutNote, ModifInfos, ModifNote, InfosPersonne, InfosUE};

        anima = new EMMAnim();
        racine.getChildren().removeAll(verre, pileMenu);

        Rechercher.setStyle("-fx-background-image: url('"+getClass().getResource("IMG/v4/RechercherOFF.png")+"')");
        Menu.setStyle("-fx-background-image: url('"+getClass().getResource("IMG/v4/MenuOFF.png")+"')");
        racine.setStyle("-fx-background-image: url('"+getClass().getResource("IMG/img1.png")+"')");
        cloneMenu.setStyle("-fx-background-image: url('"+getClass().getResource("IMG/v4/bouttonCOFF.png")+"')");

        Ajouter.setStyle("-fx-background-image: url('"+getClass().getResource("IMG/v4/AjouterOFF.png")+"')");
        ModifInfos.setStyle("-fx-background-image: url('"+getClass().getResource("IMG/v4/InfosOFF.png")+"')");
        Inscrire.setStyle("-fx-background-image: url('"+getClass().getResource("IMG/v4/InscrireOFF.png")+"')");
        Modifier.setStyle("-fx-background-image: url('"+getClass().getResource("IMG/v4/ModifierOFF.png")+"')");
        AjoutNote.setStyle("-fx-background-image: url('"+getClass().getResource("IMG/v4/NoteOFF.png")+"')");
        ModifNote.setStyle("-fx-background-image: url('"+getClass().getResource("IMG/v4/NoteOFF.png")+"')");
        AjoutPersonne.setStyle("-fx-background-image: url('"+getClass().getResource("IMG/v4/PersonneOFF.png")+"')");
        InfosPersonne.setStyle("-fx-background-image: url('"+getClass().getResource("IMG/v4/PersonneOFF.png")+"')");
        AjoutUE.setStyle("-fx-background-image: url('"+getClass().getResource("IMG/v4/UEOFF.png")+"')");
        InfosUE.setStyle("-fx-background-image: url('"+getClass().getResource("IMG/v4/UEOFF.png")+"')");

        double diagonale = Math.sqrt(Math.pow(Menu.getMinWidth(), 2) + Math.pow(Menu.getMinHeight(), 2));

        menuCentral = new Label[][]{{Ajouter, Modifier}, {Rechercher, Inscrire}};
        menuAjouter = new Label[][]{{AjoutPersonne, AjoutUE}, {AjoutNote, null}};
        menuModifier = new Label[][]{{null, ModifInfos}, {null, ModifNote}};
        menuModInfos = new Label[][]{{InfosPersonne, InfosUE}, {null, null}};
        menuInscrire = new Label[][]{{null, null}, {null, null}};
        menuRechercher = new Label[][]{{null, null}, {null, null}};

        anima.decroissance(iBox, 2);
        // On glisse notre infoBox dans la poche secrète d'un node facilement accessible peu importe la page chargée
        colis = new ArrayList<>();
        colis.add(iBoxStackPane);
        colis.add(verre);
        colis.add(corps);
    }
//--------------------------------------------------------------------------------------
    private ParallelTransition menuCloseOrd_Rec(int pos, int niv){
        SequentialTransition st = new SequentialTransition();
        ParallelTransition pt = new ParallelTransition();
        if (mManager[pos][1] > 0){
            int k = 0;
            if (pos > 0) {
                for (int j = pos-1; j >= niveaux[niv]; j--)
                    k = k + mManager[j][1];
            }
            for (int i = 0; i < (mManager[pos][1]); i++) {
                int n = i + niveaux[niv + 1] + k;
                if (mManager[n][0] == 1)
                    st.getChildren().addAll(menuCloseOrd_Rec(n, niv + 1));
            }
            st.getChildren().add(setDeployer(menuItems[pos], 0));
            st.setOnFinished(finish -> {
                mManager[pos][0] = 0;
            });
        }
        pt.getChildren().addAll(st);

        return pt;
    }

    private ParallelTransition setDeployer(Label menu, int etat){
        ParallelTransition pt = new ParallelTransition();
        if (menu.equals(cloneMenu))
            pt = anima.deployerMenu(menu, menuCentral, etat);
        if (menu.equals(Ajouter))
            pt = anima.deployerMenu(menu, menuAjouter, etat);
        if (menu.equals(Modifier))
            pt = anima.deployerMenu(menu, menuModifier, etat);
        if (menu.equals(ModifInfos))
            pt = anima.deployerMenu(menu, menuModInfos, etat);

        return pt;
    }
//--------------------------------------------------------------------------------------------
    @FXML
    private void actionMenu(){
        ScaleTransition petitPetit, grandGrand;
        // On reduit jusqu'à la disparition totale, le premier boutton (ON)
        petitPetit = anima.croissance(Menu, -1, 3, 300);
        // On agrandit le boutton OFF
        grandGrand = anima.croissance(cloneMenu, 1, 4, 400);
        // apprêter le plan de verre
        anima.parutionVerre(corps, verre, 1);
        // Création et ordonnancement des animations
        SequentialTransition sAnim = new SequentialTransition();
        sAnim.getChildren().add(petitPetit);;
        sAnim.play();
        sAnim.setOnFinished(finish -> {
            if (racine.getChildren().contains(pileMenu))
                racine.getChildren().remove(pileMenu);
            racine.getChildren().add(pileMenu);
            grandGrand.play();
            grandGrand.setOnFinished(fini -> {
                ParallelTransition pAnim = anima.deployerMenu(cloneMenu, menuCentral, 1);
                pAnim.setOnFinished(fin -> {
                    // On envoit le signal ON à tous les menus à présent déployés
                    for (int i = 0; i < niveaux[0]; i++)
                        mManager[i][0] = 1;
                });
                pAnim.play();
            });
        });
    }
//------------------------------------------------------------------------------------------------
    @FXML
    private void actionCloneMenu(){
        SequentialTransition st = transitionsCloneMenu();
        st.play();
    }

    private SequentialTransition transitionsCloneMenu(){
        ScaleTransition petitPetit, grandGrand;
        petitPetit = anima.croissance(cloneMenu, -1, 3, 300);
        grandGrand = anima.croissance(Menu, 1, 3, 300);
        SequentialTransition sAnim = new SequentialTransition();
        sAnim.getChildren().addAll(petitPetit, grandGrand);
        sAnim.setOnFinished(finish -> {
            anima.parutionVerre(corps, verre, 0);
            racine.getChildren().remove(pileMenu);
        });
        ParallelTransition pt = new ParallelTransition();
        SequentialTransition st = new SequentialTransition();
        // S'assurer de fermer les sous menus restés ouverts
        if (mManager[0][0] == 1){
            pt.getChildren().addAll(menuCloseOrd_Rec(0, 0));
        }
        if (mManager[1][0] == 1){
            pt.getChildren().addAll(menuCloseOrd_Rec(1, 0));
        }
        pt.setOnFinished(finish -> {
            mManager[0][0] = 0;
            mManager[1][0] = 0;
        });
        st.getChildren().addAll(pt, anima.deployerMenu(cloneMenu, menuCentral, 0));
        st.setOnFinished(fini -> {
            sAnim.play();
        });

        return st;
    }
//--------------------------------------------------------------------------------------
    @FXML
    private void actionAjout(){
        // Fermer les autres menus rivaux
        ParallelTransition pAnim = new ParallelTransition();
        if (mManager[1][0] == 1) {
            pAnim.getChildren().add(menuCloseOrd_Rec(1, 0));
        }
        // Préparation de l'animation de deploiement
        ParallelTransition pt = (mManager[0][0] == 0) ? anima.deployerMenu(Ajouter, menuAjouter, 1) : menuCloseOrd_Rec(0, 0);
        pt.setOnFinished(finish -> {
            if (mManager[0][0] == 0)
                mManager[0][0] = 1;
        });
        pAnim.setOnFinished(finish -> {
            mManager[1][0] = 0;
            pt.play();
        });
        pAnim.play();
    }
    @FXML
    private void actionModif(){
        // Fermer les autres menus rivaux
        ParallelTransition pAnim = new ParallelTransition();
        if (mManager[0][0] == 1) {
            pAnim.getChildren().add(menuCloseOrd_Rec(0, 0));
        }
        // Préparation de l'animation de deploiement
        int signalFin = ((mManager[1][0] == 0)) ? 1 : 0;
        ParallelTransition pt = (mManager[1][0] == 0) ? anima.deployerMenu(Modifier, menuModifier, 1) : menuCloseOrd_Rec(1, 0);
        pt.setOnFinished(finish -> {
            if (mManager[1][0] == 0) {
                mManager[1][0] = signalFin;
            }
        });
        pAnim.setOnFinished(finish -> {
            mManager[0][0] = 0;
            pt.play();
        });
        pAnim.play();
    }
    @FXML
    private void actionRechercher(){
        ParallelTransition pAnim = new ParallelTransition();
        if (mManager[0][0] == 1) {
            pAnim.getChildren().add(menuCloseOrd_Rec(0, 0));
        }
        if (mManager[1][0] == 1) {
            pAnim.getChildren().add(menuCloseOrd_Rec(1, 0));
        }
        pAnim.setOnFinished(finish -> {
            mManager[0][0] = 0;
            mManager[1][0] = 0;
        });
        if (pAnim.getChildren().size() > 0)
            pAnim.play();
        chargerCorps("Recherche.fxml");
    }
    @FXML
    private void actionInscrire(){
        ParallelTransition pAnim = new ParallelTransition();
        if (mManager[0][0] == 1) {
            pAnim.getChildren().add(menuCloseOrd_Rec(0, 0));
        }
        if (mManager[1][0] == 1) {
            pAnim.getChildren().add(menuCloseOrd_Rec(1, 0));
        }
        pAnim.setOnFinished(finish -> {
            mManager[0][0] = 0;
            mManager[1][0] = 0;
        });
        if (pAnim.getChildren().size() > 0)
            pAnim.play();
        chargerCorps("Inscription.fxml");
    }
    @FXML
    private void actionModifInfos(){
        int signalFin = ((mManager[7][0] == 0)) ? 1 : 0;
        ParallelTransition pAnim = (mManager[7][0] == 0) ? anima.deployerMenu(ModifInfos, menuModInfos, 1) : menuCloseOrd_Rec(7, 1);
        pAnim.setOnFinished(finish -> {
            if (mManager[7][0] == 0)
                mManager[7][0] = signalFin;
        });
        pAnim.play();
    }

    @FXML
    private void actionModInfosPers(){
        chargerCorps("ModInfosPersonne.fxml");
    }
    @FXML
    private void actionModUE() { chargerCorps("ModUE.fxml");}
    @FXML
    private void actionBtnInfoBox(){
        anima.infoBoxHide(colis);
    }
//-----------------------------------------------------------------------------------------------------------
    private void chargerCorps(String urlVue){
        SequentialTransition pt = new SequentialTransition();
        pt.getChildren().add(transitionsCloneMenu());
        pt.setOnFinished(finish -> {
            try {
                Parent bigDady = FXMLLoader.load(getClass().getResource(urlVue));
                bigDady.setUserData(colis);
                if (colis.size() > 3)
                    colis.remove(3);
                corps.contentProperty().set(bigDady);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        pt.play();
    }
//---------------------------------------------------------------------------------------------------------------
    @FXML
    private void actionAjoutPersonne(){
        chargerCorps("AjoutPersonne.fxml");
    }
    @FXML
    private void actionAjoutUE(){
        chargerCorps("AjoutUE.fxml");
    }
    @FXML
    private void actionAjoutNote(){
        chargerCorps("AjoutNote.fxml");
    }
    @FXML
    private void actionModNote() { chargerCorps("ModNote.fxml");}

//-------- ( Affichage des infos bulles dénominatifs ) ------------------------------------------
    @FXML
    private void libelleAjout(){
        anima.afficherInfoBulle(racine, Ajouter, "Ajouter", 0, "green");
    }
    @FXML
    private void libelleModif(){
        anima.afficherInfoBulle(racine, Modifier, "Modifier", 1, "green");
    }
    @FXML
    private void libelleRechercher(){
        anima.afficherInfoBulle(racine, Rechercher, "Rechercher", 6, "green");
    }
    @FXML
    private void libelleInscrire(){
        anima.afficherInfoBulle(racine, Inscrire, "Inscrire", 5, "green");
    }
    @FXML
    private void libelleAjPersonne(){
        anima.afficherInfoBulle(racine, AjoutPersonne, "Nouvelle Personne", 0, "green");
    }
    @FXML
    private void libelleAjUE(){
        anima.afficherInfoBulle(racine, AjoutUE, "Nouvelle Unité d'enseignement", 1, "green");
    }
    @FXML
    private void libelleAjNote(){
        anima.afficherInfoBulle(racine, AjoutNote, "Nouvelle Note", 6, "green");
    }
    @FXML
    private void libelleModInfos(){
        anima.afficherInfoBulle(racine, ModifInfos, "Modifier des données", 2, "green");
    }
    @FXML
    private void libelleModNote(){
        anima.afficherInfoBulle(racine, ModifNote, "Modifier une note", 4, "green");
    }
    @FXML
    private void libelleInfosPers(){
        anima.afficherInfoBulle(racine, InfosPersonne, "Modifier des données personnelles", 0, "green");
    }
    @FXML
    private void libelleInfosUE(){
        anima.afficherInfoBulle(racine, InfosUE, "Modifier les informations d'une UE", 2, "green");
    }
    @FXML
    private void libelleOFF(){
        racine.getChildren().remove(racine.getChildren().size()-1);
    }
//-------------------------------------------------------------------------------
}
