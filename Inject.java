package com.my.injectos;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Inject {
    private static String pkgName = null;
    private static String soname = null;
    private static String hook_js_path = null;

    private final String targetDir = "/data/local/tmp/";

    private static final String TAG = "Inject_dynamic_lib";

    private static Inject instance;

    private Inject() {
    }

    public static Inject getInstance() {
        if (instance == null) {
            instance = new Inject();
            instance.loadConfig();
        }
        return instance;
    }

    public void loadConfig() {
        if (pkgName == null) {
            try {
                String configFile = targetDir + "config.json";
                Log.d(TAG, configFile);
                File f = new File(configFile);
                if (!f.exists()) {
                    Log.d(TAG, configFile + "\t is not exists");
                    return;
                }
                Log.d(TAG, configFile + "\t is exists");
                FileInputStream fileInputStream = new FileInputStream(f);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                bufferedReader.close();
                inputStreamReader.close();
                Log.d(TAG, stringBuilder.toString());

                //新建一个json对象，用它对数据进行操作
                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                pkgName = jsonObject.getString("pkgName");
                soname = jsonObject.getString("soname");
                hook_js_path = jsonObject.getString("hook_js_path");

            } catch (IOException | JSONException e) {
                Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean saveFile(String filePath, String textMsg) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            fileOutputStream.write(textMsg.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private boolean copyFile(File srcFile, File dstFile) {
        try {
            FileInputStream fileInputStream = new FileInputStream(srcFile);
            FileOutputStream fileOutputStream = new FileOutputStream(dstFile);
            byte[] data = new byte[16 * 1024];
            int len = -1;
            while ((len = fileInputStream.read(data)) != -1) {
                fileOutputStream.write(data, 0, len);
                fileOutputStream.flush();
            }
            fileInputStream.close();
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // 拷贝源so文件到app私有目录
    private void copySoFile(Context context) {
        // 判断源so文件是否存在
        File srcSoFile = new File(targetDir + soname);
        if (!srcSoFile.exists()) {
            Log.d(TAG, "srcSoFile not exists");
            return;
        }
        // 拷贝源so文件到app私有目录
        File dstSoFile = new File(context.getFilesDir(), soname);
        boolean isCopyFileOk = copyFile(srcSoFile, dstSoFile);
        if (!isCopyFileOk) {
            Log.d(TAG, "copySoFile fail: " + srcSoFile + " -> " + dstSoFile);
        }
    }

    // 生成Gadget配置文件
    private void genGadgetConfig(Context context) {
        JSONObject jsonObject = new JSONObject();
        JSONObject childObj = new JSONObject();
        try {
            childObj.put("type", "script");
            childObj.put("path", hook_js_path);
            jsonObject.put("interaction", childObj);
        } catch (JSONException e) {
            Log.d(TAG, "json error:" + e.getMessage());
            e.printStackTrace();
        }
        String[] arr = soname.split("\\.");
        String so_config_name = arr[0] + ".config." + arr[1];
        String configFilePath = context.getFilesDir() + File.separator + so_config_name;
        boolean isSaveOk = saveFile(configFilePath, jsonObject.toString());
        if (!isSaveOk) {
            Log.d(TAG, "saveFile fail: " + configFilePath);
        } else {
            Log.d(TAG, "Gadget saveFile success: " + configFilePath);
        }
    }

    public void start(String currentPkgname, Context context) {
        if (pkgName != null && pkgName.equals(currentPkgname)) {
            Log.d(TAG, "pkgName is match : " + pkgName);
            copySoFile(context);
            genGadgetConfig(context);
            File dstSoFile = new File(context.getFilesDir(), soname);
            System.load(dstSoFile.toString());
        }
    }
}
