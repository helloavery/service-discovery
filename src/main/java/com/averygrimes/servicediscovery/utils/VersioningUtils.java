package com.averygrimes.servicediscovery.utils;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * @author Avery Grimes-Farrow
 * Created on: 10/13/19
 * https://github.com/helloavery
 */

public class VersioningUtils {

    public static String deriveVersion(String version){
        String[] versionSplit = version.split("\\.");
        // No minor or patch version specified
        if(versionSplit.length == 1){
            return versionSplit[0];
        }
        // Check if patch version is specified
        if(NumberUtils.toInt(versionSplit[versionSplit.length - 1]) != 0){
            return version;
        }
        // Check if versions after major version are all zeros
        int zeroCount = 0;
        int nextDigit = 0;
        StringBuilder builder = new StringBuilder(versionSplit[0]);
        for(int i = 1; i < versionSplit.length; i++){
            // Trim trailing zeros; save previous number so if 0 it isn't lost if next number after isn't
            if(i < versionSplit.length - 1){
                nextDigit = NumberUtils.toInt(versionSplit[i+1]);
            }
            if(NumberUtils.toInt(versionSplit[i]) != 0 || (nextDigit != 0 && NumberUtils.toInt(versionSplit[i]) == 0)){
                builder.append(".");
                builder.append(versionSplit[i]);
            }
            zeroCount += NumberUtils.toInt(versionSplit[i]);
        }
        // If versions after major are all zeros, trim them and return only major
        if(zeroCount == 0){
            return versionSplit[0];
        }
        return builder.toString();
    }
}