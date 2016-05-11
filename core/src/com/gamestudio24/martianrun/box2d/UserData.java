package com.gamestudio24.martianrun.box2d;

import com.gamestudio24.martianrun.enums.UserDataType;

public abstract class UserData {

    protected UserDataType userDataType;

    public UserData() {

    }

    public UserDataType getUserDataType() {
        return userDataType;
    }

}