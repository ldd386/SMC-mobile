package it.unimi.ssri.smc.network.converter;

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BigIntegerListConverter extends StringBasedTypeConverter<List<BigInteger>> {
    @Override
    public List<BigInteger> getFromString(String s) {
        List<BigInteger> bigIntegers = new ArrayList<>();
        for(String bigIntegerString : s.split(",")){
            bigIntegers.add(new BigInteger(bigIntegerString));
        }
        return bigIntegers;
    }

    public String convertToString(List<BigInteger> bi) {
        StringBuilder stringBuilder = new StringBuilder();
        for(BigInteger bigInteger : bi){
            stringBuilder.append(bigInteger.toString() + ",");
        }
        String convertedBigIntList = stringBuilder.toString();
        if(convertedBigIntList.length() > 0) {
            return convertedBigIntList.substring(0, convertedBigIntList.length() - 1);
        } else {
            return null;
        }
    }

}