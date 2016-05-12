/*
 * Copyright (c) 2014. William Mora
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gamestudio24.martianrun.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.gamestudio24.martianrun.actors.*;
import com.gamestudio24.martianrun.actors.menu.*;
import com.gamestudio24.martianrun.enums.Difficulty;
import com.gamestudio24.martianrun.enums.GameState;
import com.gamestudio24.martianrun.utils.*;

public class GameStage extends Stage implements ContactListener {

    private static final int VIEWPORT_WIDTH = Constants.APP_WIDTH;
    private static final int VIEWPORT_HEIGHT = Constants.APP_HEIGHT;

    private World world;
    private Ground ground;
    private Runner runner;


    private final float TIME_STEP = 1 / 300f;
    private float accumulator = 0f;

    private OrthographicCamera camera;

    private Rectangle screenLeftSide;
    private Rectangle screenRightSide;

    private SoundButton soundButton;
    private MusicButton musicButton;
    private PauseButton pauseButton;
    private StartButton startButton;
    private LeaderboardButton leaderboardButton;
    private AboutButton aboutButton;
    private ShareButton shareButton;
    private AchievementsButton achievementsButton;

    private Score score;
    private float totalTimePassed;
    private boolean tutorialShown;

    private Vector3 touchPoint;


    /**
     * @return void
     * Constructeur du Stage, appel la méthode mère dans la classe Stage de Gdx
     * On setup : le stage de base, le main menu, les deux zones de contrôle du jeu (gauche et droite), et la caméra
     * Avec [ Gdx.input.setInputProcessor(this); ] on récupère l'accès aux contrôle : clavier, souris, etc...
     * Avec onGameOver(), on reset le jeu pour faire apparaître le menu
     */
    public GameStage() {
        super(new ScalingViewport(Scaling.stretch, VIEWPORT_WIDTH, VIEWPORT_HEIGHT, new OrthographicCamera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT)));
        setUpCamera();
        setUpStageBase();
        setUpGameLabel();
        setUpMainMenu();
        setUpTouchControlAreas();
        Gdx.input.setInputProcessor(this);
        AudioUtils.getInstance().init();
        onGameOver();
    }

    /**
     * @return void
     * On setup le "World"
     * On setup le menu "fixe" (son, musique, et score)
     */

    private void setUpStageBase() {
        setUpWorld();
        setUpFixedMenu();
    }

    /**
     * @return void
     * On place le nom du jeu dans le menu principal
     * Et on l'ajoute en tant qu'acteur du stage
     */

    private void setUpGameLabel() {
        Rectangle gameLabelBounds = new Rectangle(0, getCamera().viewportHeight * 7 / 8, getCamera().viewportWidth, getCamera().viewportHeight / 4);
        addActor(new GameLabel(gameLabelBounds));
    }

    /**
     * @return void
     * On place la zone de texte pour les crédits avec la classe Rectangle(
     * On l'ajoute en tant qu'acteur du stage
     */

    private void setUpAboutText() {
        Rectangle gameLabelBounds = new Rectangle(0, getCamera().viewportHeight * 5 / 8,getCamera().viewportWidth, getCamera().viewportHeight / 4);
        addActor(new AboutLabel(gameLabelBounds));
    }


    /**
     * @return void
     * Ces boutons sont toujours affichées
     * On les actives grâce à cette fonction :
     *  - le bouton de réglage du son
     *  - le bouton de réglage de la musique
     *  - le bouton de réglage
     */
    private void setUpFixedMenu() {
        setUpSound();
        setUpMusic();
        setUpScore();
    }

    /**
     * @return void
     * Placement du Contrôle du Son (Bruit dû aux intéractions dans le jeu) : génération d'un rectangle
     * Création d'un élément SoundButton à partir du rectangle
     * Et on l'ajoute aux acteurs
     */

    private void setUpSound() {
        Rectangle soundButtonBounds = new Rectangle(getCamera().viewportWidth / 64, getCamera().viewportHeight * 13 / 20, getCamera().viewportHeight / 10, getCamera().viewportHeight / 10);
        soundButton = new SoundButton(soundButtonBounds);
        addActor(soundButton);
    }

    /**
     * @return void
     * Placement du Contrôle du Musique (Musique énervante de fond) : génération d'un rectangle
     * Création d'un élément SoundButton à partir du rectangle
     * Et on l'ajoute aux acteurs
     */

    private void setUpMusic() {
        Rectangle musicButtonBounds = new Rectangle(getCamera().viewportWidth / 64,
                getCamera().viewportHeight * 4 / 5, getCamera().viewportHeight / 10,
                getCamera().viewportHeight / 10);
        musicButton = new MusicButton(musicButtonBounds);
        addActor(musicButton);
    }

    /**
     * @return void
     * Placement du Contrôle du Son (Bruit dû aux intéractions dans le jeu) : génération d'un rectangle
     * Création d'un élément SoundButton à partir du rectangle
     * Et on l'ajoute aux acteurs
     */

    private void setUpScore() {
        Rectangle scoreBounds = new Rectangle(getCamera().viewportWidth * 47 / 64, getCamera().viewportHeight * 57 / 64, getCamera().viewportWidth / 4, getCamera().viewportHeight / 8);
        score = new Score(scoreBounds);
        addActor(score);
    }

    /**
     * @return void
     * Placement du Contrôle du Son (Bruit dû aux intéractions dans le jeu) : génération d'un rectangle
     * Création d'un élément SoundButton à partir du rectangle
     * Et on l'ajoute aux acteurs
     */

    private void setUpPause() {
        Rectangle pauseButtonBounds = new Rectangle(getCamera().viewportWidth / 64,
                getCamera().viewportHeight * 1 / 2, getCamera().viewportHeight / 10,
                getCamera().viewportHeight / 10);
        pauseButton = new PauseButton(pauseButtonBounds, new GamePauseButtonListener());
        addActor(pauseButton);
    }

    /**
     * @return void
     * Lance les fonctions de setup des éléments qui s'affiche uniquement hors-jeu
     * - Le bouton start
     * - Le bouton du tableau des scores
     * - le bouton pour afficher les crédits
     * - le bouton pour partager le jeu
     * - le bouton pour afficher les succès
     */
    private void setUpMainMenu() {
        setUpStart();
        setUpLeaderboard();
        setUpAbout();
    }

    /**
     * @return void
     * Création de la zone ou sera affiché le bouton (via la classe rectangle)
     * Un StartButton est généré à partir de cette zone
     * ajout à la liste des acteurs de la scène
     */
    private void setUpStart() {
        Rectangle startButtonBounds = new Rectangle(getCamera().viewportWidth * 3 / 16,
                getCamera().viewportHeight / 4, getCamera().viewportWidth / 4,
                getCamera().viewportWidth / 4);
        startButton = new StartButton(startButtonBounds, new GameStartButtonListener());
        addActor(startButton);
    }

    /**
     * @return void
     * Création de la zone ou sera affiché le bouton (via la classe rectangle)
     * Un LeaderboardButton est généré à partir de cette zone
     * ajout à la liste des acteurs de la scène
     */

    private void setUpLeaderboard() {
        Rectangle leaderboardButtonBounds = new Rectangle(getCamera().viewportWidth * 9 / 16,
                getCamera().viewportHeight / 4, getCamera().viewportWidth / 4,
                getCamera().viewportWidth / 4);
        leaderboardButton = new LeaderboardButton(leaderboardButtonBounds,
                new GameLeaderboardButtonListener());
        addActor(leaderboardButton);
    }

    /**
     * @return void
     * Création de la zone ou sera affiché le bouton (via la classe rectangle
     * Un AboutButton est généré à partir de cette zone
     * ajout à la liste des acteurs de la scène
     */

    private void setUpAbout() {
        Rectangle aboutButtonBounds = new Rectangle(getCamera().viewportWidth * 23 / 25,
                getCamera().viewportHeight * 13 / 20, getCamera().viewportHeight / 10,
                getCamera().viewportHeight / 10);
        aboutButton = new AboutButton(aboutButtonBounds, new GameAboutButtonListener());
        addActor(aboutButton);
    }


    /**
     * @return void
     *
     * Instanciation de l'objet World
     * On lui ajoute la liste des objets à suivre au niveau des événements
     * On setup l'image de fond
     * On setup le sol
     */

    private void setUpWorld() {
        world = WorldUtils.createWorld();
        world.setContactListener(this);
        setUpBackground();
        setUpGround();
    }

    /**
     * @return void
     *
     * instanciation et ajout du background
     */

    private void setUpBackground() {
        addActor(new Background());
    }

    /**
     * @return void
     *
     * instanciation et ajout du sol
     */

    private void setUpGround() {
        ground = new Ground(WorldUtils.createGround(world));
        addActor(ground);
    }

    /**
     * @return void
     * Lance les fonctions de setup des éléments visible uniquement en jeu :
     * - Le héros
     * - Les ennemies
     * - le bouton pause
     */

    private void setUpCharacters() {
        setUpRunner();
        setUpPauseLabel();
        createEnemy();
    }

    private void setUpChoice() {
        if (runner != null) {
            runner.remove();
        }
        for (int i = 2 ; i<=6 ;i+=2){
            runner = new Runner(WorldUtils.createChoice(world,i));
            addActor(runner);

        }
    }

    /**
     * @return void
     *
     * S'il y a un runner qui existe déjà on l'enlève
     * on crée un nouveau runner et on l'ajoute à la liste des acteurs
     */

    private void setUpRunner() {
        if (runner != null) {
            runner.remove();
        }
        runner = new Runner(WorldUtils.createRunner(world));
        addActor(runner);
    }

    /**
     * @return void
     *
     * On crée une caméra othornormée avec les dimensions de la fenêtre
     * On place la position de la caméra au milieu de notre fenêtre
     * On update les coordonnées de la caméra.
     */

    private void setUpCamera() {
        camera = new OrthographicCamera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0f);
        camera.update();
    }

    /**
     * @return void
     *
     * On crée un touchPoint qui servira a enregistrer la position
     * On crée les deux zones qui délimitent si le héro se baisse ou saute
     */

    private void setUpTouchControlAreas() {
        touchPoint = new Vector3();
        screenLeftSide = new Rectangle(0, 0, getCamera().viewportWidth / 2,
                getCamera().viewportHeight);
        screenRightSide = new Rectangle(getCamera().viewportWidth / 2, 0,
                getCamera().viewportWidth / 2, getCamera().viewportHeight);
    }

    /**
     * @return void
     *
     * Création de la zone ou sera affiché le bouton (via la classe rectangle)
     * Un PausedLabel est généré à partir de cette zone
     * ajout à la liste des acteurs de la scène
     */

    private void setUpPauseLabel() {
        Rectangle pauseLabelBounds = new Rectangle(0, getCamera().viewportHeight * 7 / 8, getCamera().viewportWidth, getCamera().viewportHeight / 4);
        addActor(new PausedLabel(pauseLabelBounds));
    }

    /**
     * @return void
     *
     * On setup le tutoriel
     * si on ne l'a jamais montré, on le montre, sinon on setup la partie gauche et la partie droite
     */

    private void setUpTutorial() {
        if (tutorialShown) {
            return;
        }
        setUpLeftTutorial();
        setUpRightTutorial();
        tutorialShown = true;
    }

    /**
     * @return void
     *
     * On définit la largeur et la hauteur à un quart d'écran
     * On définit la position à 1/8 de l'écran en x
     * On crée notre rectangle de position à partir des deux données définies au dessus
     * On ajoute le côté gauche du tutoriel à notre stage.
     */

    private void setUpLeftTutorial() {
        float width = getCamera().viewportHeight / 4;
        float x = getCamera().viewportWidth / 4 - width / 2;
        Rectangle leftTutorialBounds = new Rectangle(x, getCamera().viewportHeight * 9 / 20, width, width);
        addActor(new Tutorial(leftTutorialBounds, Constants.TUTORIAL_LEFT_REGION_NAME, Constants.TUTORIAL_LEFT_TEXT));
    }

    /**
     * @return void
     *
     * On définit la largeur et la hauteur à un quart d'écran
     * On définit la position à 5/8 de l'écran en x
     * On crée notre rectangle de position à partir des deux données définies au dessus
     * On ajoute le côté droit du tutoriel à notre stage.
     */

    private void setUpRightTutorial() {
        float width = getCamera().viewportHeight / 4;
        float x = getCamera().viewportWidth * 3 / 4 - width / 2;
        Rectangle rightTutorialBounds = new Rectangle(x, getCamera().viewportHeight * 9 / 20, width,
                width);
        addActor(new Tutorial(rightTutorialBounds, Constants.TUTORIAL_RIGHT_REGION_NAME,
                Constants.TUTORIAL_RIGHT_TEXT));
    }

    /**
     * La fonction act est la fonction principale du jeu,
     * se lance si le jeu est à l'état "Running"
     * Gère le temps, l'appel au setup de la difficultée
     * Update les acteurs présent dans le stage
     * Gère l'écart entre deux frames
     *
     * @param delta , timestep entre deux frames
     */

    @Override
    public void act(float delta) {
        super.act(delta);
        //Si le jeu est en pause, on arrête
        if (GameManager.getInstance().getGameState() == GameState.PAUSED) return;
        //Sinon on incrémente les temps écoulés
        if (GameManager.getInstance().getGameState() == GameState.RUNNING) {
            totalTimePassed += delta;
            updateDifficulty(); //On va mettre à jour la difficultée du jeu
        }
        //On récupère les éléments du jeu
        Array<Body> bodies = new Array<Body>(world.getBodyCount()); //On instancie un array qui contient des body
        world.getBodies(bodies);
        //On parcours notre tableau d'éléments pour les mettre à jours
        for (Body body : bodies) {
            update(body);
        }
        // On met à jour le temps écoulé
        accumulator += delta;
        // tant que l'on a pas atteint la durée nécessaire pour une frame on attend.
        while (accumulator >= delta) {
            world.step(TIME_STEP, 6, 2);
            accumulator -= TIME_STEP;
        }
    }

    /**
     * Récupère un élément du jeu en entrée
     * Ne gère que la création d'ennemi si l'élément en cours est l'ennemi et si le héro n'est pas touché
     * Ensuite on détruit l'élément en question (on en a plus besoin pour cette frame)
     *
     * @param body
     */

    private void update(Body body) {
        if (!BodyUtils.bodyInBounds(body)) {
            if (BodyUtils.bodyIsEnemy(body) && !runner.isHit()) {
                createEnemy();
            }
            world.destroyBody(body);
        }
    }

    /**
     * On crée un ennemi en le plaçant dans le repère monde et en lui donnant une vélocité à partir
     * de la difficultée en cours
     */

    private void createEnemy() {
        Enemy enemy = new Enemy(WorldUtils.createEnemy(world));
        enemy.getUserData().setLinearVelocity(GameManager.getInstance().getDifficulty().getEnemyLinearVelocity());
        addActor(enemy);
    }

    /**
     * On fait correspondre les coordonnées de l'écran vers les coordonnées monde
     * On process le clic
     *
     * @param x //Coordonnée en X du clic
     * @param y //Coordonnée en Y du clic
     * @param pointer // Le pointeur utilisé (s'il s'agit d'un pointeur)
     * @param button // le button appuyé (s'il s'agit d'un bouton)
     * @return On retourne la méthode mère de touchDown
     */

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {

        // Translation des coordonnées écrans aux coordonnées monde
        translateScreenToWorldCoordinates(x, y);

        // Si on touche un élément du menu, on arrête là
        if (menuControlTouched(touchPoint.x, touchPoint.y)) {
            return super.touchDown(x, y, pointer, button);
        }

        //Si on touche à un élément du jeu lorsque le jeu est en pause on arrête là
        if (GameManager.getInstance().getGameState() != GameState.RUNNING) {
            return super.touchDown(x, y, pointer, button);
        }

        //Si on touche à droite, le héro saute
        if (rightSideTouched(touchPoint.x, touchPoint.y)) {
            runner.jump();
            //Si on touche à gauche, le héro s'accroupi
        } else if (leftSideTouched(touchPoint.x, touchPoint.y)) {
            runner.dodge();
        }

        //Au cas où ça passe : on laisse passer l'évent
        return super.touchDown(x, y, pointer, button);
    }

    /**
     * On process la fin du clic
     * Utilisé uniquement pour mettre fin au héro qui se baisse
     *
     * @param screenX //Coordonnée en X du clic
     * @param screenY //Coordonnée en Y du clic
     * @param pointer // Le pointeur utilisé (s'il s'agit d'un pointeur)
     * @param button // le button appuyé (s'il s'agit d'un bouton)
     * @return on retourne la méthode mère de touchUp
     */

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        //Si le jeu n'est pas en cours, on arrête
        if (GameManager.getInstance().getGameState() != GameState.RUNNING) {
            return super.touchUp(screenX, screenY, pointer, button);
        }
        //Si le héro est abaissé, on le remet droit
        if (runner.isDodging()) {
            runner.stopDodge();
        }
        //On process la méthode mère
        return super.touchUp(screenX, screenY, pointer, button);
    }

    /**
     * On va regarder en fonction de l'état du jeu (OVER | RUNNING | PAUSED)
     * Si un élément du menu a été touché
     *
     * @param x //Coordonnée du clic
     * @param y //Coordonnée du clic
     * @return boolean | Si un élément du menu a été touché ou pas
     */
    private boolean menuControlTouched(float x, float y) {
        boolean touched = false;

        switch (GameManager.getInstance().getGameState()) {
            case OVER:
                touched = startButton.getBounds().contains(x, y)
                        || leaderboardButton.getBounds().contains(x, y)
                        || aboutButton.getBounds().contains(x, y);
                break;
            case RUNNING:
            case PAUSED:
                touched = pauseButton.getBounds().contains(x, y);
                break;
        }

        return touched || soundButton.getBounds().contains(x, y)
                || musicButton.getBounds().contains(x, y);
    }

    /**
     * Permet de savoir si le côté droit a été cliqué
     *
     * @param x //Coordonnée en X
     * @param y //Coordonnée en Y
     * @return boolean, si la partie de l'écran est touché
     */
    private boolean rightSideTouched(float x, float y) {
        return screenRightSide.contains(x, y);
    }

    /**
     * Permet de savoir si le côté gauche a été cliqué
     *
     * @param x //Coordonnée en X
     * @param y //Coordonnée en Y
     * @return booelan, si la partie de l'écran est touché
     */

    private boolean leftSideTouched(float x, float y) {
        return screenLeftSide.contains(x, y);
    }

    /**
     * Permet de transformer les coordonnées écran en coordonnées monde
     *
     * @param x //ScreenX
     * @param y //ScreenY
     */
    private void translateScreenToWorldCoordinates(int x, int y) {
        getCamera().unproject(touchPoint.set(x, y, 0));
    }

    /**
     * TODO : TROUVER D'OU VIENS LE CONTACT
     * @param contact
     */

    @Override
    public void beginContact(Contact contact) {

        Body a = contact.getFixtureA().getBody();
        Body b = contact.getFixtureB().getBody();

        if ((BodyUtils.bodyIsRunner(a) && BodyUtils.bodyIsEnemy(b)) || (BodyUtils.bodyIsEnemy(a) && BodyUtils.bodyIsRunner(b))) {
            if (runner.isHit()) {
                return;
            }
            runner.hit();
            GameManager.getInstance().submitScore(score.getScore());
            onGameOver();
            GameManager.getInstance().addGamePlayed();
            GameManager.getInstance().addJumpCount(runner.getJumpCount());
        } else if ((BodyUtils.bodyIsRunner(a) && BodyUtils.bodyIsGround(b)) ||
                (BodyUtils.bodyIsGround(a) && BodyUtils.bodyIsRunner(b))) {
            runner.landed();
        }

    }

    /**
     * Permet de calculer si on doit augmenter la diffculté ou pas à partir des constantes et du temps passé
     */

    private void updateDifficulty() {
        //Si on est au niveau max on arrête là
        if (GameManager.getInstance().isMaxDifficulty()) {
            return;
        }
        //On récupère la difficulté courante
        Difficulty currentDifficulty = GameManager.getInstance().getDifficulty();
        //Si le temps passé est supérieur à 5 fois le niveau on augmente la difficulté
        if (totalTimePassed > currentDifficulty.getLevel() * 5) {
            //On calcule l'integer de niveau
            int nextDifficulty = currentDifficulty.getLevel() + 1;
            //On crée la chaine de caractère pour l'enum
            String difficultyName = "DIFFICULTY_" + nextDifficulty;
            //On set la difficulté
            GameManager.getInstance().setDifficulty(Difficulty.valueOf(difficultyName));
            //On update le runner et le score en fonction de la difficulté
            runner.onDifficultyChange(GameManager.getInstance().getDifficulty());
            score.setMultiplier(GameManager.getInstance().getDifficulty().getScoreMultiplier());
        }

    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    /**
     * Classe Interne ::
     * On gère le bouton pause à l'aide de l'interface défini dans actors.menu.PauseButton
     */
    private class GamePauseButtonListener implements PauseButton.PauseButtonListener {

        @Override
        public void onPause() {
            onGamePaused();
        }

        @Override
        public void onResume() {
            onGameResumed();
        }

    }

    /**
     * Classe Interne ::
     * On gère le bouton start à l'aide de l'interface défini dans actors.menu.StartButton
     */

    private class GameStartButtonListener implements StartButton.StartButtonListener {

        @Override
        public void onStart() {
            clear(); //Nettoie la zone
            setUpStageBase(); //Relance le stage de base
            setUpCharacters(); //Remet les personnages
            setUpPause(); //Remet le bouton pause
            setUpTutorial(); //Remet le tutoriel
            onGameResumed(); //Active le jeu en pause
        }

    }


    /**
     * Classe Interne ::
     * On gère le bouton Classement à l'aide de l'interface défini dans actors.menu.LeaderboardButton
     */

    private class GameLeaderboardButtonListener implements LeaderboardButton.LeaderboardButtonListener {
                @Override
        public void onLeaderboard() {
             if(GameManager.getInstance().getGameState() == GameState.CHOICE){
                onGameLeaderboard();
            }

            clear();

            onGameLeaderboard();

        }



    }

    /**
     * Classe Interne ::
     * On gère le bouton à propos à l'aide de l'interface défini dans actors.menu.AboutButton
     */

    private class GameAboutButtonListener implements AboutButton.AboutButtonListener {

        @Override
        public void onAbout() {
            if (GameManager.getInstance().getGameState() == GameState.OVER) {
                onGameAbout();
            } else {
                clear();
                setUpStageBase();
                setUpGameLabel();
                onGameOver();
            }
        }

    }

    private class GameShareButtonListener implements ShareButton.ShareButtonListener {

        @Override
        public void onShare() {
            GameManager.getInstance().share();
        }

    }

    private class GameAchievementsButtonListener
            implements AchievementsButton.AchievementsButtonListener {

        @Override
        public void onAchievements() {


        }

    }

    /**
     * Set l'état du jeu à PAUSED
     */
    private void onGamePaused() {
        GameManager.getInstance().setGameState(GameState.PAUSED);
    }

    /**
     * Set l'état du jeu à RUNNING
     */
    private void onGameResumed() {
        GameManager.getInstance().setGameState(GameState.RUNNING);
    }

    /**
     * Set l'état du jeu à OVER
     * Reset la difficulté, le temps passé et affiche le menu principal
     */
    private void onGameOver() {
        GameManager.getInstance().setGameState(GameState.OVER);
        GameManager.getInstance().resetDifficulty();
        totalTimePassed = 0;
        setUpMainMenu();
    }

    /**
     * Set l'état du jeu à ABOUT
     * Clear le stage
     * Remet le stage de base
     * Remet le titre du jeu
     * Met le Texte à Propos
     */

    private void onGameAbout() {
        GameManager.getInstance().setGameState(GameState.ABOUT);
        clear();
        setUpStageBase();
        setUpGameLabel();
        setUpAboutText();
        setUpAbout();
    }


    public void onGameLeaderboard() {
        GameManager.getInstance().setGameState(GameState.CHOICE);
        clear();
        setUpStageBase();
        setUpGameLabel();
        setUpChoice();
        setUpAbout();

    }

}