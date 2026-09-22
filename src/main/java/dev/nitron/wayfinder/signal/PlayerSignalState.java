package dev.nitron.wayfinder.signal;
public record PlayerSignalState(float factor0,float factor1,float factor2){public float factor(int type){return switch(type){case 1->factor1;case 2->factor2;default->factor0;};}}
