package sample;

import com.jfoenix.controls.JFXButton;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.ArrayList;

public class EMMAnim {

    protected ScaleTransition croissance(Node Noeud, double valeur, int sens, long duree){
        // sens == 0 : horizontal , 1 : vertical, autre : les deux
        ScaleTransition st;
        st = new ScaleTransition(Duration.millis(duree), Noeud);
        switch (sens){
            case 0:
                st.setByX(valeur);
                break;
            case 1:
                st.setByY(valeur);
                break;
            default:
                st.setByX(valeur);
                st.setByY(valeur);
                break;
        }
        return st;
    }

    protected TranslateTransition deplacement(Node Noeud, double distanceX, double distanceY, long duree){
        TranslateTransition tt = new TranslateTransition(Duration.millis(duree), Noeud);
        tt.setToX(distanceX);
        tt.setToY(distanceY);
        return tt;
    }

    protected void parutionVerre(Node Noeud, Node verre, int etat){
        // etat = 0 -> disparition et etat = 1 -> apparition
        if (Noeud != null && Noeud.getParent().getClass().equals(AnchorPane.class)){
            ((AnchorPane)Noeud.getParent()).getChildren().remove(verre); // Au cas où
            if (etat != 1)
                etat = 0;
            if (etat == 1) {
                int idx = ((AnchorPane)Noeud.getParent()).getChildren().lastIndexOf(Noeud);
                ((AnchorPane)Noeud.getParent()).getChildren().add(idx + 1, verre);
                verre.setOpacity(0.75);
            }else {
                ((AnchorPane)Noeud.getParent()).getChildren().remove(verre);
            }
        }else
            System.out.println("Impossible de faire paraitre le verre car le noeud est null ou que le parent du noeud n'est pas un AnchorPane");
    }

    protected void infoBoxShow(ArrayList<Object> colis, String titre, String msg){
        // On crée l'infoBox packé dans un stackPane et glissé jadis dans la poche du noeud à l'id corps
        StackPane SP = (StackPane) colis.get(0);
        ScrollPane noeudDuCorps = (ScrollPane) colis.get(2);
        VBox iBox = (VBox) SP.getChildren().get(0);
        // Apparition
        // On fait d'abord apparitre le verre
        parutionVerre(noeudDuCorps, (Node) colis.get(1), 1);
        ((AnchorPane)noeudDuCorps.getParent()).getChildren().remove(SP);
        ((AnchorPane)noeudDuCorps.getParent()).getChildren().add(((AnchorPane)noeudDuCorps.getParent()).getChildren().size(), SP);
        ((Label)iBox.getChildren().get(0)).setText(titre);
        ((Label)iBox.getChildren().get(1)).setText(msg);
        SequentialTransition sAnim = new SequentialTransition();
        sAnim.getChildren().addAll(croissance(iBox, 1.4, 2, 300), croissance(iBox, -0.4, 2, 300));
        sAnim.play();
    }
    protected void infoBoxHide(ArrayList<Object> colis){
        StackPane SP = (StackPane) colis.get(0);
        ScrollPane noeudDuCorps = (ScrollPane) colis.get(2);
        VBox iBox = (VBox) SP.getChildren().get(0);
        if (((AnchorPane)noeudDuCorps.getParent()).getChildren().contains(SP) && SP.getScaleX() > 0) {
            SequentialTransition sAnim = new SequentialTransition();
            sAnim.getChildren().addAll(croissance(iBox, -1, 2, 300));
            sAnim.play();
            sAnim.setOnFinished(finish -> {
                ((AnchorPane)noeudDuCorps.getParent()).getChildren().remove(SP);
                parutionVerre(noeudDuCorps, (Node) colis.get(1), 0);
            });
        }
    }

    protected ParallelTransition deployerMenu(Node Noeud, Label[][] sousMenus, int etat){
        /*
         *  Position des sousMenus selon l'organisation matricielle ([00] == top left, [01] == top right, [10] == bottom left, etc
         *  On met un null dans une colonne de la matrice si l'on ne veut pas qu'un sousMenu soit créé
         * */
        ParallelTransition anim = new ParallelTransition();
        if (etat != 1) // Mesure de sécurité
            etat = 0;
        if (Noeud != null){
            int nbNull = 0;
            // Dénombrement des sous-menus vide (à ne pas créer)
            for (int i = 0; i < sousMenus.length; i++){
                for (int j = 0; j < sousMenus[i].length; j++){
                    if (sousMenus[i][j] == null)
                        ++nbNull;
                }
            }
            if (etat == 1){ // Sortir les menus
                // Déplacer les sous-menus jusqu'à la position du noeud déclencheur (position initiale ou au repos)
                for (int i = 0; i < sousMenus.length; i++) {
                    for (int j = 0; j < sousMenus[i].length; j++) {
                        if (sousMenus[i][j] != null) {
                            sousMenus[i][j].setTranslateX(Noeud.getTranslateX());
                            sousMenus[i][j].setTranslateY(Noeud.getTranslateY());
                            sousMenus[i][j].setVisible(true);
                        }
                    }
                }
                TranslateTransition[] tt = new TranslateTransition[(sousMenus.length * sousMenus[0].length) - nbNull];
                // Animation de retraction du noeud déclencheur
                ScaleTransition st = croissance(Noeud, (0.7 - Noeud.getScaleX()), 2, 300);
                int multX = 1, multY = 1, k = 0;
                // Mise en place des animation de déploiement des sous-menus (des translations sur les diagonales)
                for (int i = 0; i < sousMenus.length; i++) {
                    multY *= -1;
                    for (int j = 0; j < sousMenus[i].length; j++) {
                        multX *= -1;
                        if (sousMenus[i][j] != null) {
                            tt[k] = deplacement(sousMenus[i][j], Noeud.getTranslateX() + ((Noeud.getBoundsInParent().getWidth()*0.55) * multX), Noeud.getTranslateY() + ((Noeud.getBoundsInParent().getHeight()*0.55) * multY), 500);
                            ++k;
                        }
                    }
                }
                // empaquetage des animations prêtes à l'emploi
                anim.getChildren().add(st);
                anim.getChildren().addAll(tt);
            }else {  // Rentrer les menus
                TranslateTransition[] tt = new TranslateTransition[(sousMenus.length * sousMenus[0].length) - nbNull];
                // Le noeud déclencheur précédement retracté se décontracte
                ScaleTransition st = croissance(Noeud, (1 - Noeud.getScaleX()), 3, 300);
                int k = 0;
                // Ramener tous les sous-menus à létat initial, au repos
                for (int i = 0; i < sousMenus.length; i++){
                    for (int j = 0; j < sousMenus[i].length; j++) {
                        if (sousMenus[i][j] != null) {
                            tt[k] = deplacement(sousMenus[i][j], Noeud.getTranslateX(), Noeud.getTranslateY(), 500);
                            int finalJ = j;
                            int finalI = i;
                            tt[k].setOnFinished(fini -> {
                                sousMenus[finalI][finalJ].setVisible(false);
                            });
                            ++k;
                        }
                    }
                }
                // Ordonnancer les animations
                anim.getChildren().add(st);
                anim.getChildren().addAll(tt);
            }
        }else
            System.out.println("Veuillez renseigner un Node correct");
        return anim;
    }
    protected void afficherInfoBulle(AnchorPane racine, Node Noeud, String msg, int sens, String couleur){
        /*
        * sens:
        * 0 == top-left
        * 1 == top-center
        * 2 == top-right
        * 3 == center-right
        * 4 == bottom-right
        * 5 == bottom-center
        * 6 == bottom-left
        * 7 == center-left
        * */
        Label pop = new Label(msg);
        sens = sens % 8;
        switch (sens){
            case 0:
                pop.setLayoutX(Noeud.getBoundsInParent().getMinX());
                pop.setLayoutY(Noeud.getBoundsInParent().getMinY());
                break;
            case 1:
                pop.setLayoutX(Noeud.getBoundsInParent().getMinX() + (Noeud.getBoundsInParent().getWidth() * 0.20));
                pop.setLayoutY(Noeud.getBoundsInParent().getMinY());
                break;
            case 2:
                pop.setLayoutX(Noeud.getBoundsInParent().getMaxX());
                pop.setLayoutY(Noeud.getBoundsInParent().getMinY());
                break;
            case 3:
                pop.setLayoutX(Noeud.getBoundsInParent().getMaxX());
                pop.setLayoutY(Noeud.getBoundsInParent().getMinY() + (Noeud.getBoundsInParent().getHeight() * 0.25));
                break;
            case 4:
                pop.setLayoutX(Noeud.getBoundsInParent().getMaxX());
                pop.setLayoutY(Noeud.getBoundsInParent().getMaxY());
                break;
            case 5:
                pop.setLayoutX(Noeud.getBoundsInParent().getMinX() + (Noeud.getBoundsInParent().getWidth() * 0.20));
                pop.setLayoutY(Noeud.getBoundsInParent().getMaxY());
                break;
            case 6:
                pop.setLayoutX(Noeud.getBoundsInParent().getMinX());
                pop.setLayoutY(Noeud.getBoundsInParent().getMaxY());
                break;
            case 7:
                pop.setLayoutX(Noeud.getBoundsInParent().getMaxX());
                pop.setLayoutY(Noeud.getBoundsInParent().getMaxY() - (Noeud.getBoundsInParent().getHeight() * 0.25));
                break;
        }
        pop.setScaleX(0);
        pop.setScaleY(0);
        pop.setStyle("-fx-background-color: white; -fx-border-color:"+couleur);
        racine.getChildren().add(pop);
        ScaleTransition st = new ScaleTransition();
        st.setNode(pop);
        st.setByX(1);
        st.setByY(1);
        st.setDuration(Duration.millis(300));
        st.play();
    }
    protected SequentialTransition titleShowTime(Pane zoneDeTitre, String titre, boolean aleatoire){
        /*
        *  aleatoire == true signifie que les blocs de couleurs s'afficherons... ben de facon aléatoire xD
        *  et avec aleatoire == false on aura un affichage standard
        * */
        // Nettoyage de la zone
        zoneDeTitre.getChildren().clear();
        //Definition des vaiables
        StackPane SP = new StackPane();
        SP.setPrefSize(zoneDeTitre.getBoundsInParent().getWidth(), zoneDeTitre.getBoundsInParent().getHeight());
        SequentialTransition st = new SequentialTransition();
        String[] couleurs = {"#FFE6CC", "#FFF2CC", "#D5E8D4", "#60A917", "#008A00"};
        Label[] blocs = new Label[10];
        // Definition et structuration des futurs blocs colorés
        for (int i = 0; i < 10; i++){
            blocs[i] = new Label("");
            blocs[i].setPrefSize(zoneDeTitre.getBoundsInParent().getHeight()*0.3, zoneDeTitre.getBoundsInParent().getHeight()*0.3);
            blocs[i].setRotate(45);
            blocs[i].setVisible(false);
            blocs[i].setStyle("-fx-background-color: "+couleurs[(int) (Math.random()*4)]+"; -fx-background-radius: 5; -fx-border-radius: 5");
        }
        // Ajout des blocs à la zone
        SP.getChildren().addAll(blocs);
        // Designation des blocs leader
        // -- bloc leader gauche
        blocs[0].setVisible(true);
        blocs[0].setScaleX(0);
        blocs[0].setScaleY(0);
        // -- bloc leader droit
        blocs[5].setVisible(true);
        blocs[5].setScaleX(0);
        blocs[5].setScaleY(0);
        // preéparation des animations
        TranslateTransition dep1 = deplacement(blocs[0], -250, 0, 500), dep2 = deplacement(blocs[5], 200, 0, 500);
        dep1.setOnFinished(finish -> {
            Label[][] LG = setSubMenuMatrix(blocs, 0, aleatoire);
            deployerMenu(blocs[0], LG, 1).play();
        });
        dep2.setOnFinished(finish -> {
            Label[][] LD = setSubMenuMatrix(blocs, 1, aleatoire);
            deployerMenu(blocs[5], LD, 1).play();
        });
        st.getChildren().add(new ParallelTransition(croissance(blocs[0], 1, 2, 500), croissance(blocs[5], 1, 5, 500)));
        st.getChildren().add(new ParallelTransition(dep1, dep2));
        // Déclaration du titre et ajout
        Label leTitre = new Label(titre);
        leTitre.setStyle("-fx-font-color: green");
        SP.getChildren().add(leTitre);
        // Structuration du titre
        leTitre.setScaleX(0);
        leTitre.setScaleY(0);
        // Ajout de la pile dans la zone
        zoneDeTitre.getChildren().add(SP);
        // Ajout de l'animation du titre
        st.getChildren().add(croissance(leTitre, 1, 2, 200));

        return st;
    }

    private Label[][] setSubMenuMatrix(Label[] blocs, int cote, boolean aleatoire){
        // cote == 0 : Gauche et 1 : Droit
        Label[][] LG = new Label[2][2], LD = new Label[2][2];
        if (aleatoire){
            int rng, k = 1;
            for (int i = 0; i < 2; i++){
                for (int j = 0; j < 2; j++){
                    rng = (int)(Math.random() * 4) % 2;
                    if (rng == 0)
                        LG[i][j] = null;
                    else {
                        LG[i][j] = blocs[k];
                        ++k;
                    }
                }
            }
            k = 1;
            for (int i = 0; i < 2; i++){
                for (int j = 0; j < 2; j++){
                    rng = (int)(Math.random() * 4) % 2;
                    if (rng == 0)
                        LD[i][j] = null;
                    else {
                        LD[i][j] = blocs[5 + k];
                        ++k;
                    }
                }
            }
        }else {
            LD = new Label[][]{{blocs[6], blocs[7]}, {blocs[8], blocs[9]}};
            LG = new Label[][]{{blocs[1], blocs[2]}, {blocs[3], blocs[4]}};
        }
        return (cote == 0) ? LG : LD;
    }

    protected SequentialTransition formShowTime(Pane titre, String titreForm, GridPane formulaire, HBox boutons, long duree, boolean aleatoire){
        SequentialTransition st = new SequentialTransition();
        st.getChildren().addAll(croissance(titre, (titre.getScaleX() < 1)? 1 : 0, 0, duree));
        if (formulaire != null)
            st.getChildren().add(croissance(formulaire, (formulaire.getScaleX() < 1)? 1 : 0, 2, duree));
        if (boutons != null)
            st.getChildren().add(croissance(boutons, (boutons.getScaleX() + 1) % 2, 2, duree));
        st.setOnFinished(finish -> {
            SequentialTransition sst = titleShowTime(titre, titreForm, aleatoire);
            sst.play();
        });
        return st;
    }

    protected void decroissance(Node Noeud, int option){
        switch (option){
            case 0 :
                Noeud.setScaleX(0);
                break;
            case 1:
                Noeud.setScaleY(0);
            default:
                Noeud.setScaleX(0);
                Noeud.setScaleY(0);
                break;
        }
    }
}
