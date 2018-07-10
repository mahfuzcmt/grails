package com.webcommander.conversion

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Utility class containing class operations for converting
 * between SI (or SI derived standard units) and other known
 * units of Mass.
 * SI base units for mass is kilogram.
 */

public enum MassType {
    POUNDS,
    OUNCES,
    GRAMS,
    TONNES,
    KILOGRAMS
}

public class MassConversions {
    //'POUNDS','OUNCES','KILOGRAMS','GRAMS','TONNES'


    /**
     * Constant conversion factor
     * 1 shortton = 907.18474 kilogram
     */
    public static final BigDecimal SI_SHORTTON = new BigDecimal("907.18474");
    /**
     * Constant conversion factor
     * 1 gram = 0.001 kilogram
     */
    public static final BigDecimal SI_GM = new BigDecimal("0.001");
    /**
     * Constant conversion factor
     * 1 pound = 0.45359237 kilogram
     */
    public static final BigDecimal SI_LBM = new BigDecimal("0.45359237");
    /**
     * Constant conversion factor
     * 1 metric tonne = 1000 kilogram
     */
    public static final BigDecimal SI_METRICTON = new BigDecimal("1000");
    /**
     * Constant conversion factor
     * 1 long tonne = 1016.0469088 kilogram
     */
    public static final BigDecimal SI_LONGTON = new BigDecimal("1016.0469088");
    /**
     * Constant conversion factor
     * 1 OUNCES = 0.02835 kilogram
     */
    public static final BigDecimal SI_OUNCE = new BigDecimal("0.02835");

    /**
     * Utility method converting a BigDecimal value
     * from defined type to the SI unit.
     *
     * @param type  BigDecimal value to be convertion type.
     * @param value BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */

    public static BigDecimal convertMassToSI(String type, Double value) {
        BigDecimal val = new BigDecimal("0.0");
        MassType massType = MassType.valueOf(type.toUpperCase());
        try {
            switch (massType) {
                case MassType.POUNDS:
                    val = MassConversions.lbm2SI(value);
                    break;
                case MassType.OUNCES:
                    val = MassConversions.ounce2SI(value);
                    break;
                case MassType.GRAMS:
                    val = MassConversions.gm2SI(value);
                    break;
                case MassType.TONNES:
                    val = MassConversions.metricton2SI(value);
                    break;
                case MassType.KILOGRAMS:
                    val = new BigDecimal(value);
                    break;
                default:
                    val = new BigDecimal(0);
            }
        } catch (ArithmeticException e) {
        } catch (Exception e) {
        }

        return val;
    }

    /**
     * Utility method converting a BigDecimal value
     * from SI Unit to defined type.
     *
     * @param type  BigDecimal value to be convertion type.
     * @param value BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */

    public static BigDecimal convertSIToMass(String type, Double value) {
        BigDecimal val = new BigDecimal("0.0");
        MassType massType = MassType.valueOf(type.toUpperCase());
        try {
            switch (massType) {
                case MassType.POUNDS:
                    val = MassConversions.SI2lbm(value);
                    break;
                case MassType.OUNCES:
                    val = MassConversions.SI2ounce(value);
                    break;
                case MassType.GRAMS:
                    val = MassConversions.SI2gm(value);
                    break;
                case MassType.TONNES:
                    val = MassConversions.SI2metricton(value);
                    break;
                case MassType.KILOGRAMS:
                    val = new BigDecimal(value);
                    break;
                default:
                    val = new BigDecimal(0.0);
            }
        } catch (ArithmeticException e) {
            return new BigDecimal(0.0);
        } catch (Exception e) {
        }
        return val;
    }

    /**
     * Utility method converting a BigDecimal value
     * from shortton to the SI or SI derived unit.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal shortton2SI(Double d) {
        return new BigDecimal(d).multiply(SI_SHORTTON, new MathContext(16, RoundingMode.FLOOR));
    }

    /**
     * Utility method converting a BigDecimal value
     * from SI or SI derived to shortton.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal SI2shortton(Double d) {
        return new BigDecimal(d).divide(SI_SHORTTON, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from gm to the SI or SI derived unit.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal gm2SI(Double d) {
        return new BigDecimal(d).multiply(SI_GM, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from SI or SI derived to gm.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal SI2gm(Double d) {
        return new BigDecimal(d).divide(SI_GM, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from lbm to the SI or SI derived unit.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal lbm2SI(Double d) {
        return new BigDecimal(d).multiply(SI_LBM, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from SI or SI derived to lbm.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal SI2lbm(Double d) {
        return new BigDecimal(d).divide(SI_LBM, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from metricton to the SI or SI derived unit.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal metricton2SI(Double d) {
        return new BigDecimal(d).multiply(SI_METRICTON, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from SI or SI derived to metricton.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal SI2metricton(Double d) {
        return new BigDecimal(d).divide(SI_METRICTON, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from longton to the SI or SI derived unit.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal longton2SI(Double d) {
        return new BigDecimal(d).multiply(SI_LONGTON, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from SI or SI derived to longton.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal SI2longton(Double d) {
        return new BigDecimal(d).divide(SI_LONGTON, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from ounce to the SI or SI derived unit.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal ounce2SI(Double d) {
        return new BigDecimal(d).multiply(SI_OUNCE, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from SI or SI derived to ounce.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal SI2ounce(Double d) {
        return new BigDecimal(d).divide(SI_OUNCE, new MathContext(16, RoundingMode.FLOOR));
    }
}
