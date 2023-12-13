#include <windows.h>
#include <iostream>
#include <string>
#include <map>
#include "jni.h"

using namespace std;

std::wstring stringToWString(const std::string &s) {
    int len;
    int slength = (int) s.length() + 1;
    len = MultiByteToWideChar(CP_ACP, 0, s.c_str(), slength, 0, 0);
    wchar_t *buf = new wchar_t[len];
    MultiByteToWideChar(CP_ACP, 0, s.c_str(), slength, buf, len);
    std::wstring r(buf);
    delete[] buf;
    return r;
}


extern "C"
JNIEXPORT jobject JNICALL
Java_com_wuhongru_jini_WRegistry_getAccountInfoFromReg(JNIEnv *env, jobject thisObj) {
    std::map<std::string, std::string> accountInfo;
    HKEY hKey;
    LONG lRes = RegOpenKeyExW(HKEY_CURRENT_USER, L"Software\\miHoYo\\原神", 0, KEY_READ, &hKey);
    if (lRes == ERROR_SUCCESS) {

        LPCSTR key1 = "GENERAL_DATA_h2389025596";
        LPCSTR key2 = "MIHOYOSDK_ADL_PROD_CN_h3123967166";
        std::wstring wkey1 = stringToWString(key1);
        std::wstring wkey2 = stringToWString(key2);

        DWORD i = 0;
        WCHAR valueName[1683]; // Maximum length of a value name is 16383 characters
        DWORD valueNameSize = sizeof(valueName);
        BYTE valueData[104876]; // Assuming the value data is no larger than 8192 bytes
        DWORD valueDataSize = sizeof(valueData);
        DWORD valueType;

        while (RegEnumValueW(hKey, i, valueName, &valueNameSize, NULL, &valueType, valueData, &valueDataSize) ==
               ERROR_SUCCESS) {
            if (valueType == REG_BINARY && std::wstring(valueName) == wkey1 ) {
                std::string value(reinterpret_cast<char*>(valueData), valueDataSize);
                size_t end = value.find_first_of('\0');
                if (end != std::string::npos) {
                    value = value.substr(0, end);
                }
                accountInfo[key1] = value; // accountInfo["GENERAL_DATA_h2389025596"] = value;
//                std::cout << "Value name: " << key1 << "\n";
//                std::cout << "Binary data: " << value << "\n";
            } else if (valueType == REG_BINARY && std::wstring(valueName) == wkey2){
                std::string value(reinterpret_cast<char*>(valueData), valueDataSize);
                size_t end = value.find_first_of('\0');
                if (end != std::string::npos) {
                    value = value.substr(0, end);
                }
                accountInfo[key2] = value; // accountInfo["MIHOYOSDK_ADL_PROD_CN_h3123967166"] = value;
//                std::cout << "Value name: " << key2 << "\n";
//                std::cout << "Binary data: " << value << "\n";
            }

            i++;
            valueNameSize = sizeof(valueName);
            valueDataSize = sizeof(valueData);
        }

        RegCloseKey(hKey);
    }

    // Convert std::map to Java HashMap
    jclass mapClass = env->FindClass("java/util/HashMap");
    jmethodID mapConstructor = env->GetMethodID(mapClass, "<init>", "()V");
    jobject hashMap = env->NewObject(mapClass, mapConstructor);
    jmethodID putMethod = env->GetMethodID(mapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    for (const auto &pair : accountInfo) {
        jstring key = env->NewStringUTF(pair.first.c_str());
        jstring value = env->NewStringUTF(pair.second.c_str());
        env->CallObjectMethod(hashMap, putMethod, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);
    }

    return hashMap;
}



extern "C" JNIEXPORT jlong JNICALL
Java_com_wuhongru_jini_WRegistry_setRegistryValue(JNIEnv* env, jobject /* this */, jstring jKey, jstring jValue) {
    const char* keyCStr = env->GetStringUTFChars(jKey, 0);
    const char* valueCStr = env->GetStringUTFChars(jValue, 0);
    std::string key(keyCStr);
    std::string value(valueCStr);
    env->ReleaseStringUTFChars(jKey, keyCStr);
    env->ReleaseStringUTFChars(jValue, valueCStr);

    HKEY hKey;
    LONG result = RegCreateKeyExW(
            HKEY_CURRENT_USER, // hKey
            L"Software\\miHoYo\\原神", // lpSubKey
            0,                 // Reserved
            NULL,              // lpClass
            0,                 // dwOptions
            KEY_WRITE,         // samDesired
            NULL,              // lpSecurityAttributes
            &hKey,             // phkResult
            NULL               // lpdwDisposition
    );

    if (result == ERROR_SUCCESS) {
        result = RegSetValueEx(
                hKey,           // hKey
                key.c_str(),    // lpValueName
                0,              // Reserved
                REG_SZ,         // dwType
                (const BYTE*)value.c_str(), // lpData
                value.size() + 1   // cbData
        );

        RegCloseKey(hKey);
    }

    return result;
}


char* printAllValues(HKEY hKey) {
    DWORD dwIndex = 0;
    char valueName[256];
    DWORD valueNameLength = 256;
    static char valueData[2048];  // Make this static so it can be returned
    DWORD valueDataLength = 2048;
    DWORD dwType = 0;

    while (RegEnumValue(hKey, dwIndex++, valueName, &valueNameLength, NULL, &dwType,
                        reinterpret_cast<LPBYTE>(valueData), &valueDataLength) == ERROR_SUCCESS) {
        if (strcmp(valueName, "MatchedExeFullPath") == 0 && strstr((char*)valueData, "YuanShen.exe") != NULL) {
            return (char*)valueData;
        }
        valueNameLength = 256;
        valueDataLength = 2048;
    }
    return NULL;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_wuhongru_jini_WRegistry_searchYuanShenPath(JNIEnv* env, jobject thisObj) {
    HKEY hKey;
    DWORD dwIndex = 0;
    FILETIME ftLastWriteTime;
    char szSubKey[256];
    DWORD dwSubKeyLength = 256;
    LPCSTR path = "System\\GameConfigStore\\Children";

    if (RegOpenKeyEx(HKEY_CURRENT_USER, path, 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        while (RegEnumKeyEx(hKey, dwIndex++, szSubKey, &dwSubKeyLength, NULL, NULL, NULL, &ftLastWriteTime) == ERROR_SUCCESS) {
            HKEY hSubKey;
            char subKeyPath[1024];
            sprintf(subKeyPath, "%s\\%s", path, szSubKey);
            if (RegOpenKeyEx(HKEY_CURRENT_USER, subKeyPath, 0, KEY_READ, &hSubKey) == ERROR_SUCCESS) {
                char* matchedExeFullPath = printAllValues(hSubKey);
                if (matchedExeFullPath != NULL) {
                    RegCloseKey(hSubKey);
                    RegCloseKey(hKey);
                    return env->NewStringUTF(matchedExeFullPath);
                }
                RegCloseKey(hSubKey);
            }
            dwSubKeyLength = 256;
        }
        RegCloseKey(hKey);
    }
    return NULL;
}
