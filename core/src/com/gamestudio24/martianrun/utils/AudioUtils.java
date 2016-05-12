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

package com.gamestudio24.martianrun.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioUtils {

    private static AudioUtils ourInstance = new AudioUtils();
    private static Music music;
    private static Sound jumpSound;
    private static Sound hitSound;

    private static final String MUSIC_ON_PREFERENCE = "music_on";
    private static final String SOUND_ON_PREFERENCE = "sound_on";

    /**
     * On déclare le constructeur en privé pour qu'il ne soit pas utilisé en dehors l'utilisation static
     */
    private AudioUtils() {
    }

    /**
     * Renvoie l'instance du Singleton
     * @return l'instance
     */

    public static AudioUtils getInstance() {
        return ourInstance;
    }

    /**
     * Getter de music
     * @return music
     */

    public Music getMusic() {
        return music;
    }

    /**
     * Getter de Preferences
     * @return Preferences(GameManager.PREFERENCES_NAME)
     */

    private Preferences getPreferences() {
        return Gdx.app.getPreferences(GameManager.PREFERENCES_NAME);
    }

    /**
     * Initialisation du singleton
     * On crée
     */

    public void init() {
        music = Gdx.audio.newMusic(Gdx.files.internal(Constants.GAME_MUSIC));
        music.setLooping(true);
        playMusic();
        jumpSound = createSound(Constants.RUNNER_JUMPING_SOUND);
        hitSound = createSound(Constants.RUNNER_HIT_SOUND);
    }

    public Sound createSound(String soundFileName) {
        return Gdx.audio.newSound(Gdx.files.internal(soundFileName));
    }

    public void playMusic() {
        boolean musicOn = getPreferences().getBoolean(MUSIC_ON_PREFERENCE, true);
        if (musicOn) {
            music.play();
        }
    }

    public void playSound(Sound sound) {
        boolean soundOn = getPreferences().getBoolean(SOUND_ON_PREFERENCE, true);
        if (soundOn) {
            sound.play();
        }
    }

    public void toggleMusic() {
        saveBoolean(MUSIC_ON_PREFERENCE, !getPreferences().getBoolean(MUSIC_ON_PREFERENCE, true));
    }

    public void toggleSound() {
        saveBoolean(SOUND_ON_PREFERENCE, !getPreferences().getBoolean(SOUND_ON_PREFERENCE, true));
    }

    private void saveBoolean(String key, boolean value) {
        Preferences preferences = getPreferences();
        preferences.putBoolean(key, value);
        preferences.flush();
    }

    public static void dispose() {
        music.dispose();
        jumpSound.dispose();
        hitSound.dispose();
    }

    public void pauseMusic() {
        music.pause();
    }

    public String getSoundRegionName() {
        boolean soundOn = getPreferences().getBoolean(SOUND_ON_PREFERENCE, true);
        return soundOn ? Constants.SOUND_ON_REGION_NAME : Constants.SOUND_OFF_REGION_NAME;
    }

    public String getMusicRegionName() {
        boolean musicOn = getPreferences().getBoolean(MUSIC_ON_PREFERENCE, true);
        return musicOn ? Constants.MUSIC_ON_REGION_NAME : Constants.MUSIC_OFF_REGION_NAME;
    }

    public Sound getJumpSound() {
        return jumpSound;
    }

    public Sound getHitSound() {
        return hitSound;
    }
}
