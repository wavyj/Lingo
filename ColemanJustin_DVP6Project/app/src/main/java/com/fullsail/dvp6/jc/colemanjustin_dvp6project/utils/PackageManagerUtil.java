package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import com.google.common.io.BaseEncoding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PackageManagerUtil {

    public static String getSignature(PackageManager pm, String packageName){
        try{
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            if (packageInfo == null || packageInfo.signatures == null || packageInfo.signatures.
                    length == 0 || packageInfo.signatures[0] == null){
                return null;
            }

            return getSignature(packageInfo.signatures[0]);
        } catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }

    private static String getSignature(Signature sig){
        byte[] signature = sig.toByteArray();

        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            byte[] digest = messageDigest.digest(signature);
            return BaseEncoding.base16().lowerCase().encode(digest);
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            return null;
        }
    }
}
