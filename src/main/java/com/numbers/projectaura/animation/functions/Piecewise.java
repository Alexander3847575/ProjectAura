package com.numbers.projectaura.animation.functions;

import java.util.ArrayList;

public final class Piecewise implements IAnimationFunction {

    @Override
    public float getAt(long deltaTime) {
        return 0;
    }

    private ArrayList<IAnimationFunction> functionComponents;

}
