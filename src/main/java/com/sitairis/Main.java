package com.sitairis;

import com.sitairis.config.ApplicationCompositionRoot;

public class Main {
    public static void main(String[] args) {
        new ApplicationCompositionRoot().compose().start();
    }
}
