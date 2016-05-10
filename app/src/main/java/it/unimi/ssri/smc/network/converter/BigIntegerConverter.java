package it.unimi.ssri.smc.network.converter;

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

import java.math.BigInteger;

public class BigIntegerConverter extends StringBasedTypeConverter<BigInteger> {
    @Override
    public BigInteger getFromString(String s) {
        return new BigInteger(s);
    }

    public String convertToString(BigInteger bi) {
        return bi.toString();
    }

}