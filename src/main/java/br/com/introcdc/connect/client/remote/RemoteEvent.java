package br.com.introcdc.connect.client.remote;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:04
 */

import java.io.Serializable;

public class RemoteEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {
        MOUSE_MOVE,
        MOUSE_PRESS,
        MOUSE_RELEASE,
        MOUSE_WHEEL,
        KEY_PRESS,
        KEY_RELEASE
    }

    private Type type;
    private int x;
    private int y;
    private int button;
    private int keyCode;
    private int wheelAmount;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getButton() {
        return button;
    }

    public void setButton(int button) {
        this.button = button;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public int getWheelAmount() {
        return wheelAmount;
    }

    public void setWheelAmount(int wheelAmount) {
        this.wheelAmount = wheelAmount;
    }

}
