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

package com.gamestudio24.martianrun.actors.menu;

import com.badlogic.gdx.math.Rectangle;
import com.gamestudio24.martianrun.enums.GameState;
import com.gamestudio24.martianrun.utils.Constants;
import com.gamestudio24.martianrun.utils.GameManager;

public class AboutButton extends GameButton {

    public interface AboutButtonListener {
        public void onAbout();
    }

    private AboutButtonListener listener;

    public AboutButton(Rectangle bounds, AboutButtonListener listener) {
        super(bounds);
        this.listener = listener;
    }

    @Override
    protected String getRegionName() {
        return (GameManager.getInstance().getGameState() == GameState.CHOICE ||GameManager.getInstance().getGameState() == GameState.ABOUT) ? Constants.CLOSE_REGION_NAME :
                Constants.ABOUT_REGION_NAME;
    }

    @Override
    public void touched() {
        listener.onAbout();
    }

}
