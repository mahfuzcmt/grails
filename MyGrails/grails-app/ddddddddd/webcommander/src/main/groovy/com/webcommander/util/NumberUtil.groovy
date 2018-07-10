package com.webcommander.util

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat

/**
 * Created by sharif ul islam on 18/01/2018.
 */
class NumberUtil {

    static Double getFormatedDouble(Double input) {
        getFormatedDouble(input, "HALF_EVEN", 2, true)
    }

    static Double getFormatedDouble(Double input, String roundingType, Integer decimalPoints) {
        getFormatedDouble(input, roundingType, decimalPoints, true)
    }

    static Double getFormatedDouble(Double input, String roundingType, Integer decimalPoints, boolean withoutCurrencySymbol) {

        RoundingMode roundingMode = RoundingMode.HALF_EVEN
        switch (roundingType) {
            case "up":
                roundingMode = RoundingMode.UP
                break
            case "down":
                roundingMode = RoundingMode.DOWN
                break
        }

        NumberFormat numberFormat = NumberFormat.getCurrencyInstance()
        numberFormat.setRoundingMode(roundingMode)
        numberFormat.setMaximumFractionDigits(decimalPoints)
        numberFormat.setMinimumFractionDigits(decimalPoints)
        numberFormat.setGroupingUsed(false);

        if (withoutCurrencySymbol) {
            DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) numberFormat).getDecimalFormatSymbols();
            decimalFormatSymbols.setCurrencySymbol("");
            ((DecimalFormat) numberFormat).setDecimalFormatSymbols(decimalFormatSymbols);
        }

        Double interval = getInterval(decimalPoints)
        BigDecimal number = input.toBigDecimal()

        NumberFormat numberFormat1 = new DecimalFormat("00.###############")
        //numberFormat1.setCurrency(java.util.Currency.getInstance(currency.code))
        numberFormat1.setRoundingMode(RoundingMode.HALF_EVEN)
        BigDecimal returnValue = numberFormat1.format(interval).toBigDecimal()
        number = number.divide(returnValue, 10, roundingMode).setScale(0, roundingMode).multiply(returnValue)

        return new Double(numberFormat.format(number.toDouble()).trim())
    }

    static Double getInterval(Integer decimalPoints) {
        Double interval = getGeneralInterval(decimalPoints)
        return interval
    }

    static Double getGeneralInterval(Integer decimalPoints) {
        Double interval = 0.01D
        switch (decimalPoints) {
            case 1:
                interval = 0.1D
                break
            case 2:
                interval = 0.01D
                break
            case 3:
                interval = 0.001D
                break
            case 4:
                interval = 0.0001D
                break
            case 5:
                interval = 0.00001D
                break
            case 6:
                interval = 0.000001D
                break
            case 7:
                interval = 0.0000001D
                break
            case 8:
                interval = 0.00000001D
                break
            case 9:
                interval = 0.000000001D
                break
            case 10:
                interval = 0.0000000001D
                break
            default:
                interval = 0.01D
        }
        return interval
    }

    static Double getAustralianInterval(Integer decimalPoints) {
        Double interval = 0.05D
        switch (decimalPoints) {
            case 1:
                interval = 0.5D
                break
            case 2:
                interval = 0.05D
                break
            case 3:
                interval = 0.005D
                break
            case 4:
                interval = 0.0005D
                break
            case 5:
                interval = 0.00005D
                break
            case 6:
                interval = 0.000005D
                break
            case 7:
                interval = 0.0000005D
                break
            case 8:
                interval = 0.00000005D
                break
            case 9:
                interval = 0.000000005D
                break
            case 10:
                interval = 0.0000000005D
                break
            default:
                interval = 0.05D
        }
        return interval
    }

}
