package com.numbers.projectaura.auras.applicator;

/**
 * Represents an instance of elemental damage (can be non-damaging)
 * @param applicationType The type of applicator that caused the application; it is possible for one source to have multiple types (see {@link ApplicationType}).
 * @param applicationStrength
 * @param damage
 * @param applicationSource
 */
public record ApplicationInstance(ApplicationType applicationType, double applicationStrength, float damage, ApplicationSource applicationSource) { }
