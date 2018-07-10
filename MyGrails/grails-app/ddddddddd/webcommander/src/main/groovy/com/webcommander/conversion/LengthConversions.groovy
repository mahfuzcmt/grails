package com.webcommander.conversion

/**
 * Created by IntelliJ IDEA.
 * User: sohel
 * Date: 28/01/12
 * Time: 10:02 AM
 * To change this template use File | Settings | File Templates.
 */

import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Utility class containing class operations for converting
 * between SI (or SI derived standard units) and other known
 * units of Length.
 * SI base units for length is meter.
 */

public enum LengthType {
    INCHES,
    CENTIMETERS,
    KILOMETERS,
    MILES,
    MILLIMETERS,
    FOOTS,
    METERS
}

public class LengthConversions {
    //'Pounds','Ounces','Kilograms','Grams','Tonnes'


    /**
     * Constant conversion factor
     * 1 cm =  0.01 meter
     */
    public static final BigDecimal SI_CM = new BigDecimal("0.01");
    /**
     * Constant conversion factor
     * 1 inch =  0.0254 meter
     */
    public static final BigDecimal SI_IN = new BigDecimal("0.0254");
    /**
     * Constant conversion factor
     * 1 kilometer = 1000 meter
     */
    public static final BigDecimal SI_KM = new BigDecimal("1000");
    /**
     * Constant conversion factor
     * 1 mile = 1609.164 meter
     */
    public static final BigDecimal SI_MILE = new BigDecimal("1609.164");
    /**
     * Constant conversion factor
     * 1 millimeter  = 0.001 meter
     */
    public static final BigDecimal SI_MM = new BigDecimal("0.001");
    /**
     * Constant conversion factor
     * 1 feet  = 0.3048 meter
     */
    public static final BigDecimal SI_FT = new BigDecimal("0.3048");

    /**
     * Utility method converting a BigDecimal value
     * from defined type to the SI unit.
     *
     * @param type  BigDecimal value to be convertion type.
     * @param value BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */

    public static BigDecimal convertLengthToSI(String type, Double value) {
        BigDecimal val = new BigDecimal("0.0");
        LengthType lengthType = LengthType.valueOf(type.toUpperCase());
        try {
            switch (lengthType) {
                case LengthType.INCHES:
                    val = LengthConversions.in2SI(value);
                    break;
                case LengthType.CENTIMETERS:
                    val = LengthConversions.cm2SI(value);
                    break;
                case LengthType.KILOMETERS:
                    val = LengthConversions.km2SI(value);
                    break;
                case LengthType.MILES:
                    val = LengthConversions.mile2SI(value);
                    break;
                case LengthType.MILLIMETERS:
                    val = LengthConversions.mm2SI(value);
                    break;
                case LengthType.FOOTS:
                    val = LengthConversions.ft2SI(value);
                    break;
                case LengthType.METERS:
                    val = new BigDecimal(value);
                    break;
                default:
                    val = new BigDecimal(0.0);
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

    public static BigDecimal convertSIToLength(String type, Double value) {
        BigDecimal val = new BigDecimal("0.0");
        LengthType lengthType = LengthType.valueOf(type.toUpperCase());
        try {
            switch (lengthType) {
                case LengthType.INCHES:
                    val = LengthConversions.SI2in(value);
                    break;
                case LengthType.CENTIMETERS:
                    val = LengthConversions.SI2cm(value);
                    break;
                case LengthType.KILOMETERS:
                    val = LengthConversions.SI2km(value);
                    break;
                case LengthType.MILES:
                    val = LengthConversions.SI2mile(value);
                    break;
                case LengthType.MILLIMETERS:
                    val = LengthConversions.SI2mm(value);
                    break;
                case LengthType.FOOTS:
                    val = LengthConversions.SI2ft(value);
                    break;
                case LengthType.METERS:
                    val = new BigDecimal(value);
                    break;
                default:
                    value = 0.0;
            }
        } catch (ArithmeticException e) {
            return val = new BigDecimal(0.0);
        } catch (Exception e) {
        }
        return val;
    }


    /**
     * Utility method converting a BigDecimal value
     * from cm to the SI or SI derived unit.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal cm2SI(Double d) {

        return new BigDecimal(d).multiply(SI_CM, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from SI or SI derived to cm.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal SI2cm(Double d) {
        return new BigDecimal(d).divide(SI_CM, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from in to the SI or SI derived unit.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal in2SI(Double d) {
        return new BigDecimal(d).multiply(SI_IN, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from SI or SI derived to in.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal SI2in(Double d) {
        return new BigDecimal(d).divide(SI_IN, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from km to the SI or SI derived unit.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal km2SI(Double d) {
        return new BigDecimal(d).multiply(SI_KM, new MathContext(16, RoundingMode.FLOOR));
    }

    /**
     * Utility method converting a BigDecimal value
     * from SI or SI derived to km.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal SI2km(Double d) {
        return new BigDecimal(d).divide(SI_KM, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from mile to the SI or SI derived unit.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal mile2SI(Double d) {
        return new BigDecimal(d).multiply(SI_MILE, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from SI or SI derived to mile.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal SI2mile(Double d) {
        return new BigDecimal(d).divide(SI_MILE, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from mm to the SI or SI derived unit.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal mm2SI(Double d) {
        return new BigDecimal(d).multiply(SI_MM, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from SI or SI derived to mm.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal SI2mm(Double d) {
        return new BigDecimal(d).divide(SI_MM, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from ft to the SI or SI derived unit.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal ft2SI(Double d) {
        return new BigDecimal(d).multiply(SI_FT, new MathContext(16, RoundingMode.FLOOR));
    }


    /**
     * Utility method converting a BigDecimal value
     * from SI or SI derived to ft.
     *
     * @param d BigDecimal value to be convertenew BigDecimal(d).
     * @return BigDecimal containing the converted value.
     */
    public static final BigDecimal SI2ft(Double d) {
        return new BigDecimal(d).divide(SI_FT, new MathContext(16, RoundingMode.FLOOR));
    }

}


