package com.edgedevstudio.generate_imei;

/**
 * Created by OPEYEMI OLORUNLEKE on 12/2/2017.
 */

public class LanguageModel {
    private String languageName;
    private String langShortCode;
    private String langCountryCode = "";

    public LanguageModel(String languageName, String langShortCode, String langCountryCode) {
        this.languageName = languageName;
        this.langShortCode = langShortCode;
        this.langCountryCode = langCountryCode;
    }

    public String getLanguageName() {
        return languageName;
    }

    public String getLangShortCode() {
        return langShortCode;
    }

    public String getLangCountryCode() {
        return langCountryCode;
    }

    public void setLangCountryCode(String langCountryCode) {
        this.langCountryCode = langCountryCode;
    }

    @Override
    public String toString() {
        return "LanguageModel{" +
                "languageName='" + languageName + '\'' +
                ", langShortCode='" + langShortCode + '\'' +
                '}';
    }
}
